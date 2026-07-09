import { useReducer, useRef, useEffect, useCallback } from 'react';
import {
  executionReducer,
  initialExecState,
  type ExecState,
  type ExecAction,
} from '../executionReducer';
import type { SimulatorResult, SimulatorWorker } from '../simulator/protocol';

// The number of steps dispatched to the worker in each internal batch.
// Kept here (not in Simulator.js) so the scheduling logic in updateState
// can reference it without prop-drilling.
const INTERNAL_STEPS_STRIDE = 50;

// ---------------------------------------------------------------------------
// Param and return types
// ---------------------------------------------------------------------------

/** Parameters accepted by useExecutionController. */
export interface ExecutionControllerParams {
  /** The augmented worker proxy created in index.js (stable across renders). */
  worker: SimulatorWorker;
  /** Bulk state updater from useSimulatorData. */
  applyResultState: (result: SimulatorResult) => void;
  /** Checksyntax-only updater from useSimulatorData. */
  applyChecksyntaxResult: (result: SimulatorResult) => void;
  /** Input-request setter from useSimulatorData. */
  setInputRequest: (v: SimulatorResult | null) => void;
  /** Milliseconds to insert between step batches (0 = no delay). */
  executionDelayMs: number;
  /**
   * Called with a human-readable message when a runtime error result arrives
   * (e.g. synchronous exception, unsupported syscall). The caller renders the
   * RuntimeErrorDialog; execution is stopped here regardless, immediately.
   */
  onRuntimeError: (message: string) => void;
}

/** The public API returned by useExecutionController to Simulator.js. */
export interface ExecutionControllerAPI {
  /** Full execution state for consumers that need more than `executing`. */
  execState: ExecState;
  /** Convenience destructure of execState.executing. */
  executing: boolean;
  /** Dispatch for PAUSE_REQUESTED / INPUT_SUBMITTED. */
  dispatch: React.Dispatch<ExecAction>;
  /** Start run-all mode (worker runs batches until STOPPED). */
  runCode: () => void;
  /** Run exactly n steps (batched internally by INTERNAL_STEPS_STRIDE). */
  stepCode: (n: number) => void;
  /** Stop execution and reset the worker to READY. */
  stopCode: () => void;
  /** Called by clearCode() / restoreDefaultSample() in Simulator.js. */
  notifyReset: () => void;
}

/**
 * useExecutionController — owns all execution-related state and side-effects.
 *
 * Responsibilities:
 *   - The execution reducer (execState / dispatch) that replaces the old
 *     stepsToRun / mustPause / runAll / executing useState quartet.
 *   - Batch scheduling: nextBatchTimeoutRef ref, cancelPendingBatch,
 *     scheduleNextBatch, and executionDelayRef (a ref mirror of the
 *     executionDelayMs setting so live delay changes take effect mid-run).
 *   - Worker 'message' subscription (workerHandlerRef pattern):
 *     a single stable listener is registered once; the real handler body is
 *     reassigned on every render so it always reads the latest closures.
 *   - updateState decision logic: after each result it decides whether to
 *     schedule another batch, halt, or surface a runtime error.
 */
export function useExecutionController({
  worker,
  applyResultState,
  applyChecksyntaxResult,
  setInputRequest,
  executionDelayMs,
  onRuntimeError,
}: ExecutionControllerParams): ExecutionControllerAPI {
  // ---------------------------------------------------------------------------
  // Execution state machine (replaces the four individual useState variables)
  // ---------------------------------------------------------------------------
  const [execState, dispatch] = useReducer(executionReducer, initialExecState);
  const { stepsToRun, mustPause, runAll, executing } = execState;

  // `executionDelayMs` is read inside async callbacks captured when a step
  // batch started (potentially many batches ago).  Mirror the latest value in
  // a ref so the delay applied between batches always reflects the *current*
  // setting, not the one active when "Run All" was pressed.  This lets the
  // user tweak the delay live, mid-run.
  const executionDelayRef = useRef<number>(executionDelayMs);
  useEffect(() => {
    executionDelayRef.current = executionDelayMs;
  }, [executionDelayMs]);

  // ---------------------------------------------------------------------------
  // Batch scheduling
  // ---------------------------------------------------------------------------

  // Pending timeout id for a delayed follow-up batch. Stored in a ref so that
  // stopping the simulation cancels any batch sleeping between strides instead
  // of having it fire after `worker.reset()` and surface a spurious error.
  const nextBatchTimeoutRef = useRef<ReturnType<typeof setTimeout> | null>(
    null,
  );

  const cancelPendingBatch = useCallback(() => {
    if (nextBatchTimeoutRef.current !== null) {
      clearTimeout(nextBatchTimeoutRef.current);
      nextBatchTimeoutRef.current = null;
    }
  }, []);

  // Schedule the next internal step batch, inserting the user-configured
  // execution delay so long runs are visually paced.  A delay of 0 ms (the
  // default) runs batches back-to-back, matching the pre-existing behaviour.
  const scheduleNextBatch = useCallback(
    (fn: () => void) => {
      cancelPendingBatch();
      const delay = executionDelayRef.current;
      if (delay <= 0) {
        fn();
        return;
      }
      nextBatchTimeoutRef.current = setTimeout(() => {
        nextBatchTimeoutRef.current = null;
        fn();
      }, delay);
    },
    [cancelPendingBatch],
  );

  // ---------------------------------------------------------------------------
  // Public operations
  // ---------------------------------------------------------------------------

  const runCode = useCallback(() => {
    dispatch({ type: 'RUN_ALL_REQUESTED' });
    worker.step(INTERNAL_STEPS_STRIDE);
  }, [worker]);

  const stepCode = useCallback(
    (n: number) => {
      const toRun = Math.min(n, INTERNAL_STEPS_STRIDE);
      dispatch({ type: 'STEP_REQUESTED', stepsRemaining: n - toRun });
      worker.step(toRun);
    },
    [worker],
  );

  const stopCode = useCallback(() => {
    // If a batch was sleeping between strides, cancel it.  In that case no
    // worker result is in flight to flip `executing` back off via
    // `updateState`, so we pass hadPendingBatch=true and the reducer clears
    // executing immediately.  When the worker is mid-step (hadPendingBatch
    // false), the upcoming result will see mustPause=true and clear executing.
    const hadPendingBatch = nextBatchTimeoutRef.current !== null;
    cancelPendingBatch();
    dispatch({ type: 'STOP', hadPendingBatch });
    setInputRequest(null);
    worker.reset();
  }, [worker, cancelPendingBatch, setInputRequest]);

  // Called by clearCode() / restoreDefaultSample() in Simulator.js.
  // Handles the controller side: cancel any pending batch, dispatch RESET,
  // reset the worker, and clear the inputRequest dialog.
  const notifyReset = useCallback(() => {
    const hadPendingBatch = nextBatchTimeoutRef.current !== null;
    cancelPendingBatch();
    dispatch({ type: 'RESET', hadPendingBatch });
    setInputRequest(null);
    worker.reset();
  }, [worker, cancelPendingBatch, setInputRequest]);

  // ---------------------------------------------------------------------------
  // Worker message subscription
  // ---------------------------------------------------------------------------

  // `updateState` is a render-local function; it reads stepsToRun/runAll/mustPause
  // from the render closure (same semantics as the original Simulator.js code).
  // Defined before the workerHandlerRef assignment so there is no TDZ concern.
  const updateState = (result: SimulatorResult) => {
    applyResultState(result);

    // TODO: cleaner handling of error types. Checking the error message is a
    // pretty weak check.  Runtime errors should not cause multiple alert
    // prompting to avoid webui getting stuck.
    if (!result.success && result.errorMessage !== 'Parsing errors.') {
      // Synchronous exceptions carry structured info (errorCode, errorInstruction,
      // errorStage).  When present, compose a clearer, multi-line alert message.
      let message = result.errorMessage;
      if (result.errorCode) {
        message = `Synchronous exception: ${result.errorMessage}`;
        if (result.errorInstruction && result.errorStage) {
          message += `\n\nInstruction: ${result.errorInstruction}\nPipeline stage: ${result.errorStage}`;
        }
      }
      onRuntimeError(message);
      stopCode();
      // stopCode() dispatches STOP (mustPause=true, runAll=false, stepsToRun=0),
      // but since we are inside a worker-result handler (not a pending batch),
      // hadPendingBatch is false and STOP leaves executing unchanged (still
      // true).  Dispatch RESULT_RECEIVED with status='STOPPED' to atomically
      // clear executing and reset all flags, preventing any follow-up batch
      // from racing the worker.reset() already queued by stopCode().
      dispatch({
        type: 'RESULT_RECEIVED',
        status: 'STOPPED',
        encounteredBreak: false,
      });
      return;
    }

    // Dispatch the execution state transition atomically.  The reducer decides
    // whether to stay in "executing" (more batches pending) or clear the flag.
    dispatch({
      type: 'RESULT_RECEIVED',
      status: result.status,
      encounteredBreak: result.encounteredBreak,
    });

    // Scheduling is a side-effect and cannot live inside the reducer.  We read
    // the *pre-dispatch* execState here — which is correct, because the closure
    // captured by workerHandlerRef.current reflects the last render before this
    // message arrived (i.e., the state that drove the batch that just finished).
    //
    // Note: we intentionally keep `executing === true` across inter-batch
    // delays when more steps are queued.  Clearing `executing` between batches
    // would toggle the toolbar buttons (Run/Step/Stop becoming enabled, Pause
    // becoming disabled) every stride, which looks like a flash during long
    // runs with a non-zero execution delay.  The user should see the same
    // "running" controls whether the worker is busy stepping or we're simply
    // waiting out the inter-batch delay.
    if (result.status !== 'RUNNING' || mustPause || result.encounteredBreak) {
      // Execution halted; reducer already cleared all flags.  Nothing to schedule.
    } else if (stepsToRun > 0) {
      scheduleNextBatch(() => stepCode(stepsToRun));
    } else if (runAll) {
      scheduleNextBatch(() => stepCode(INTERNAL_STEPS_STRIDE));
    }
    // else: single step finished normally; reducer set executing=false — done.
  };

  // Worker message handler — stored in a ref so the addEventListener-based
  // subscription (below) can register a single stable wrapper once, while
  // the *real* handler body is reassigned on every render and therefore
  // always reads the latest closures (execState, scheduleNextBatch, …).
  // This is the same pattern as the existing keyboardHandlerRef.
  const workerHandlerRef = useRef<((e: MessageEvent) => void) | null>(null);
  workerHandlerRef.current = (e: MessageEvent) => {
    const result = worker.parseResult(e.data as Record<string, unknown>);

    // For syntax check responses, only update parsing errors to avoid
    // unnecessary re-renders on the rest of the UI.
    if (result.method === 'checksyntax') {
      applyChecksyntaxResult(result);
      return;
    }

    if (result.inputRequested) {
      applyResultState(result);
      dispatch({ type: 'INPUT_REQUESTED' });
      setInputRequest(result);
      return;
    }

    updateState(result);
  };

  // Register exactly one stable message listener for the lifetime of this
  // component instead of the previous render-body `worker.onmessage = …`
  // assignment, which was a side-effect during render — a React anti-pattern
  // that could silently drop messages during Strict-Mode double-invocations
  // and could not carry a cleanup.
  useEffect(() => {
    const handleMessage = (e: MessageEvent) => {
      if (workerHandlerRef.current) {
        workerHandlerRef.current(e);
      }
    };
    worker.addEventListener('message', handleMessage);
    return () => worker.removeEventListener('message', handleMessage);
    // worker is a stable prop reference; include it so the linter is satisfied
    // and so the subscription is re-created if a caller ever passes a new worker.
  }, [worker]);

  // ---------------------------------------------------------------------------
  // Return public API
  // ---------------------------------------------------------------------------

  return {
    execState,
    executing,
    dispatch,
    runCode,
    stepCode,
    stopCode,
    notifyReset,
  };
}
