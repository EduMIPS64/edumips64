/**
 * executionReducer.ts — pure state machine for the EduMIPS64 execution flags.
 *
 * Why a reducer?
 * --------------
 * The original Simulator.js managed four related `useState` variables
 * (`stepsToRun`, `mustPause`, `runAll`, `executing`) that must always
 * move together. Because they were individual setters, every transition
 * required multiple `set*` calls whose values were not visible to each
 * other within the same event-handler invocation (React batching).
 * Defensive workarounds (reads via refs, stale-closure guards) were
 * scattered across the file.
 *
 * A single `useReducer` makes every legal transition explicit and atomic:
 * the reducer receives the complete current state and produces a complete
 * new state, so no individual setter can race another.
 *
 * No React imports — this module is pure TypeScript and can be unit-tested
 * without a browser or JSDOM.
 */

// ---------------------------------------------------------------------------
// State shape
// ---------------------------------------------------------------------------

/**
 * Execution flags owned by the reducer.
 *
 * @property stepsToRun  – steps still queued after the current batch is
 *   dispatched to the worker; 0 = none pending.
 * @property mustPause   – pause flag: if true, the next RESULT_RECEIVED
 *   transition clears all execution flags rather than scheduling another batch.
 * @property runAll      – true while in "run until program ends" mode; drives
 *   the open-ended scheduling loop in the component.
 * @property executing   – true while the worker is actively computing steps
 *   *or* while a scheduled inter-batch delay is in flight.  Kept true across
 *   inter-batch delays so the toolbar buttons do not flash between strides
 *   (user sees the same "running" UI the whole time).
 */
export interface ExecState {
  stepsToRun: number;
  mustPause: boolean;
  runAll: boolean;
  executing: boolean;
}

// ---------------------------------------------------------------------------
// Actions — discriminated union
// ---------------------------------------------------------------------------

/**
 * Supported action types.  The component dispatches these; the reducer is the
 * only place that decides what the new ExecState should be.
 *
 * STEP_REQUESTED      — user pressed Step (or a multi-step batch was scheduled).
 *   payload: { stepsRemaining }  number of steps still queued after the
 *   current batch is sent to the worker (i.e. n - stride, where stride =
 *   Math.min(n, INTERNAL_STEPS_STRIDE)).  The actual worker.step(stride)
 *   call is a side-effect kept in the component.
 *
 * RUN_ALL_REQUESTED   — user pressed Run All.
 *   No payload.  Equivalent to the original setRunAll(true) + stepCode(...).
 *   The component calls worker.step(INTERNAL_STEPS_STRIDE) as a side-effect.
 *
 * RESULT_RECEIVED     — a step result arrived from the worker.
 *   payload: { status, encounteredBreak }
 *   Based on the current state (mustPause, stepsToRun, runAll) and the
 *   result the reducer decides whether to stay in "executing" (more batches
 *   are expected) or to stop.  The component reads the *pre-dispatch* state
 *   to decide which batch to schedule next (see Simulator.js).
 *
 * PAUSE_REQUESTED     — user pressed Pause (or keyboard F8 during run).
 *   No payload.  Sets mustPause=true; the next RESULT_RECEIVED will stop.
 *
 * STOP                — user pressed Stop, or a runtime error was detected.
 *   payload: { hadPendingBatch } boolean — true when cancelPendingBatch()
 *   found a live timeout.  In that case no worker result is in flight, so
 *   we must clear executing immediately.  When the worker is mid-step,
 *   executing stays true until the response arrives and sees mustPause=true.
 *
 * RESET               — clearCode() / restoreDefaultSample() called.
 *   payload: { hadPendingBatch }  Same semantics as STOP for executing.
 *   Does NOT set mustPause; the worker.reset() response will arrive with
 *   status='READY' and the RESULT_RECEIVED first-branch will clear all flags.
 *
 * INPUT_REQUESTED     — worker signalled it needs stdin input.
 *   No payload.  Pauses execution until the user submits.
 *
 * INPUT_SUBMITTED     — user submitted input via the InputDialog.
 *   No payload.  Resumes execution (worker.provideInput is the side-effect).
 */

interface StepRequestedAction {
  type: 'STEP_REQUESTED';
  stepsRemaining: number;
}
interface RunAllRequestedAction {
  type: 'RUN_ALL_REQUESTED';
}
interface ResultReceivedAction {
  type: 'RESULT_RECEIVED';
  status: string;
  encounteredBreak: boolean;
}
interface PauseRequestedAction {
  type: 'PAUSE_REQUESTED';
}
interface StopAction {
  type: 'STOP';
  hadPendingBatch: boolean;
}
interface ResetAction {
  type: 'RESET';
  hadPendingBatch: boolean;
}
interface InputRequestedAction {
  type: 'INPUT_REQUESTED';
}
interface InputSubmittedAction {
  type: 'INPUT_SUBMITTED';
}

export type ExecAction =
  | StepRequestedAction
  | RunAllRequestedAction
  | ResultReceivedAction
  | PauseRequestedAction
  | StopAction
  | ResetAction
  | InputRequestedAction
  | InputSubmittedAction;

// ---------------------------------------------------------------------------
// Initial state
// ---------------------------------------------------------------------------

export const initialExecState: ExecState = {
  stepsToRun: 0,
  mustPause: false,
  runAll: false,
  executing: false,
};

// ---------------------------------------------------------------------------
// Reducer
// ---------------------------------------------------------------------------

export function executionReducer(
  state: ExecState,
  action: ExecAction,
): ExecState {
  switch (action.type) {
    case 'STEP_REQUESTED':
      // The component has already called worker.step(stride).  Record how
      // many steps remain after this batch and mark execution as in-progress.
      return {
        ...state,
        stepsToRun: action.stepsRemaining,
        executing: true,
      };

    case 'RUN_ALL_REQUESTED':
      // Open-ended run: subsequent batches are driven by the runAll flag, not
      // by a decreasing stepsToRun counter.
      return {
        ...state,
        runAll: true,
        stepsToRun: 0,
        executing: true,
      };

    case 'RESULT_RECEIVED': {
      const { status, encounteredBreak } = action;

      // Invariant: stop execution when the program has ended, the user asked
      // to pause, or a breakpoint was hit.  Clear all flags atomically so no
      // subsequent RESULT_RECEIVED can re-schedule work.
      if (status !== 'RUNNING' || state.mustPause || encounteredBreak) {
        return {
          stepsToRun: 0,
          mustPause: false,
          runAll: false,
          executing: false,
        };
      }

      // More steps are queued (stepsToRun > 0) or we're in run-all mode:
      // keep executing=true so the toolbar stays in "running" state across
      // the inter-batch delay.  The component will call scheduleNextBatch
      // based on the pre-dispatch state it read from the workerHandlerRef
      // closure (see Simulator.js for the full scheduling comment).
      if (state.stepsToRun > 0 || state.runAll) {
        return state;
      }

      // A plain single-step (or the final batch of a multi-step) completed
      // normally.  No further batches are queued.
      return { ...state, executing: false };
    }

    case 'PAUSE_REQUESTED':
      // Record the pause intent; the next RESULT_RECEIVED will stop the run.
      return { ...state, mustPause: true };

    case 'STOP':
      // stopCode(): cancelled pending batch + will call worker.reset().
      // If hadPendingBatch is true, no worker response is in flight so we
      // clear executing right now.  Otherwise the worker is mid-step; its
      // response will arrive with mustPause=true visible and will clear
      // executing via the RESULT_RECEIVED first-branch above.
      return {
        stepsToRun: 0,
        mustPause: true,
        runAll: false,
        executing: action.hadPendingBatch ? false : state.executing,
      };

    case 'RESET':
      // clearCode() / restoreDefaultSample(): same hadPendingBatch logic as
      // STOP for executing.  mustPause is NOT set here because worker.reset()
      // will send back a response with status='READY'; the RESULT_RECEIVED
      // first-branch (status !== 'RUNNING') will then clear all remaining flags.
      return {
        stepsToRun: 0,
        mustPause: false,
        runAll: false,
        executing: action.hadPendingBatch ? false : state.executing,
      };

    case 'INPUT_REQUESTED':
      // Worker needs stdin: pause execution while the InputDialog is open.
      // The worker will resume when worker.provideInput() is called.
      return { ...state, executing: false };

    case 'INPUT_SUBMITTED':
      // User provided the required input; execution resumes.
      return { ...state, executing: true };

    default: {
      // TypeScript exhaustiveness check: this branch is unreachable if all
      // action types are handled above.
      const _exhaustive: never = action;
      void _exhaustive;
      return state;
    }
  }
}
