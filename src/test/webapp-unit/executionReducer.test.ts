/**
 * Unit tests for executionReducer.ts
 *
 * These tests exercise every action in isolation and several multi-action
 * sequences that mirror real run-control flows.  No browser or JSDOM is
 * required: the reducer is a pure function.
 */

import { describe, it, expect } from 'vitest';
import {
  executionReducer,
  initialExecState,
  type ExecAction,
  type ExecState,
} from '../../webapp/executionReducer';

// Convenience: apply a sequence of actions to the initial state.
function apply(...actions: ExecAction[]): ExecState {
  return actions.reduce(executionReducer, initialExecState);
}

// ---------------------------------------------------------------------------
// Initial state
// ---------------------------------------------------------------------------

describe('initialExecState', () => {
  it('starts with all flags cleared', () => {
    expect(initialExecState).toEqual({
      stepsToRun: 0,
      mustPause: false,
      runAll: false,
      executing: false,
    });
  });
});

// ---------------------------------------------------------------------------
// STEP_REQUESTED
// ---------------------------------------------------------------------------

describe('STEP_REQUESTED', () => {
  it('sets executing=true and records remaining steps', () => {
    const state = executionReducer(initialExecState, {
      type: 'STEP_REQUESTED',
      stepsRemaining: 0,
    });
    expect(state.executing).toBe(true);
    expect(state.stepsToRun).toBe(0);
    expect(state.runAll).toBe(false);
  });

  it('stores non-zero stepsRemaining for multi-step batching', () => {
    // Simulates stepCode(120): first batch dispatches 50, leaves 70 remaining.
    const state = executionReducer(initialExecState, {
      type: 'STEP_REQUESTED',
      stepsRemaining: 70,
    });
    expect(state.stepsToRun).toBe(70);
    expect(state.executing).toBe(true);
  });

  it('does not set runAll', () => {
    const state = executionReducer(initialExecState, {
      type: 'STEP_REQUESTED',
      stepsRemaining: 0,
    });
    expect(state.runAll).toBe(false);
  });
});

// ---------------------------------------------------------------------------
// RUN_ALL_REQUESTED
// ---------------------------------------------------------------------------

describe('RUN_ALL_REQUESTED', () => {
  it('sets runAll=true, executing=true, stepsToRun=0', () => {
    const state = executionReducer(initialExecState, { type: 'RUN_ALL_REQUESTED' });
    expect(state).toEqual({
      stepsToRun: 0,
      mustPause: false,
      runAll: true,
      executing: true,
    });
  });

  it('preserves any pre-existing mustPause flag (edge case: pause before run lands)', () => {
    // This edge case should not arise in normal usage, but the reducer must be
    // safe: mustPause is spread from the previous state.
    const stateWithPause: ExecState = { ...initialExecState, mustPause: true };
    const state = executionReducer(stateWithPause, { type: 'RUN_ALL_REQUESTED' });
    expect(state.mustPause).toBe(true);
  });
});

// ---------------------------------------------------------------------------
// RESULT_RECEIVED — single step
// ---------------------------------------------------------------------------

describe('RESULT_RECEIVED (single step)', () => {
  it('clears executing when single step completes (stepsToRun=0, runAll=false)', () => {
    const running = apply(
      { type: 'STEP_REQUESTED', stepsRemaining: 0 },
    );
    const done = executionReducer(running, {
      type: 'RESULT_RECEIVED',
      status: 'RUNNING',
      encounteredBreak: false,
    });
    expect(done.executing).toBe(false);
    expect(done.stepsToRun).toBe(0);
    expect(done.runAll).toBe(false);
    expect(done.mustPause).toBe(false);
  });

  it('clears all flags when program ends (status=STOPPED)', () => {
    const running = apply({ type: 'STEP_REQUESTED', stepsRemaining: 0 });
    const done = executionReducer(running, {
      type: 'RESULT_RECEIVED',
      status: 'STOPPED',
      encounteredBreak: false,
    });
    expect(done).toEqual(initialExecState);
  });

  it('clears all flags when status=READY (e.g. after reset)', () => {
    const running = apply({ type: 'STEP_REQUESTED', stepsRemaining: 0 });
    const done = executionReducer(running, {
      type: 'RESULT_RECEIVED',
      status: 'READY',
      encounteredBreak: false,
    });
    expect(done).toEqual(initialExecState);
  });
});

// ---------------------------------------------------------------------------
// RESULT_RECEIVED — multi-step batching
// ---------------------------------------------------------------------------

describe('RESULT_RECEIVED (multi-step batching)', () => {
  it('keeps executing=true between batches when stepsToRun > 0', () => {
    // Simulate: stepCode(120) → first batch of 50 dispatched, 70 remaining.
    const afterFirstBatch = apply({ type: 'STEP_REQUESTED', stepsRemaining: 70 });

    // Worker returns from first batch, program still running.
    const afterFirstResult = executionReducer(afterFirstBatch, {
      type: 'RESULT_RECEIVED',
      status: 'RUNNING',
      encounteredBreak: false,
    });

    // State must keep executing=true so toolbar doesn't flash between batches.
    expect(afterFirstResult.executing).toBe(true);
    // The remaining steps are still in state for the component to use.
    expect(afterFirstResult.stepsToRun).toBe(70);
  });

  it('clears executing after the final batch when stepsToRun reaches 0', () => {
    // Three-batch sequence: 120 steps with stride=50 → batches of 50, 50, 20.
    // Simulate via reducer: just track state transitions.
    const batch1 = apply({ type: 'STEP_REQUESTED', stepsRemaining: 70 });
    const result1 = executionReducer(batch1, {
      type: 'RESULT_RECEIVED', status: 'RUNNING', encounteredBreak: false,
    });
    // Component reads stepsToRun=70, schedules stepCode(70).
    const batch2 = executionReducer(result1, {
      type: 'STEP_REQUESTED', stepsRemaining: 20,
    });
    const result2 = executionReducer(batch2, {
      type: 'RESULT_RECEIVED', status: 'RUNNING', encounteredBreak: false,
    });
    // Component reads stepsToRun=20, schedules stepCode(20).
    const batch3 = executionReducer(result2, {
      type: 'STEP_REQUESTED', stepsRemaining: 0,
    });
    const result3 = executionReducer(batch3, {
      type: 'RESULT_RECEIVED', status: 'RUNNING', encounteredBreak: false,
    });
    // Final batch: stepsToRun=0, runAll=false → executing cleared.
    expect(result3.executing).toBe(false);
    expect(result3.stepsToRun).toBe(0);
  });
});

// ---------------------------------------------------------------------------
// RUN_ALL — multi-result loop
// ---------------------------------------------------------------------------

describe('RESULT_RECEIVED (run-all mode)', () => {
  it('keeps executing=true while program is running in run-all mode', () => {
    const running = apply({ type: 'RUN_ALL_REQUESTED' });
    const afterResult = executionReducer(running, {
      type: 'RESULT_RECEIVED', status: 'RUNNING', encounteredBreak: false,
    });
    // runAll=true → keep executing for next batch.
    expect(afterResult.executing).toBe(true);
    expect(afterResult.runAll).toBe(true);
  });

  it('clears all flags when program ends during run-all', () => {
    const running = apply({ type: 'RUN_ALL_REQUESTED' });
    const ended = executionReducer(running, {
      type: 'RESULT_RECEIVED', status: 'STOPPED', encounteredBreak: false,
    });
    expect(ended).toEqual(initialExecState);
  });
});

// ---------------------------------------------------------------------------
// PAUSE_REQUESTED
// ---------------------------------------------------------------------------

describe('PAUSE_REQUESTED', () => {
  it('sets mustPause=true without changing other flags', () => {
    const running = apply({ type: 'RUN_ALL_REQUESTED' });
    const paused = executionReducer(running, { type: 'PAUSE_REQUESTED' });
    expect(paused.mustPause).toBe(true);
    expect(paused.runAll).toBe(true);
    expect(paused.executing).toBe(true);
  });

  it('stops execution on next RESULT_RECEIVED when mustPause is set', () => {
    const state = apply(
      { type: 'RUN_ALL_REQUESTED' },
      { type: 'PAUSE_REQUESTED' },
    );
    const stopped = executionReducer(state, {
      type: 'RESULT_RECEIVED', status: 'RUNNING', encounteredBreak: false,
    });
    // mustPause=true → first branch of RESULT_RECEIVED → clear everything.
    expect(stopped).toEqual(initialExecState);
  });
});

// ---------------------------------------------------------------------------
// STOP
// ---------------------------------------------------------------------------

describe('STOP', () => {
  it('clears stepsToRun, runAll; sets mustPause=true; keeps executing when no pending batch', () => {
    // Worker is mid-step (hadPendingBatch=false) — executing must stay true
    // until the worker's in-flight response arrives and sees mustPause=true.
    const running = apply({ type: 'RUN_ALL_REQUESTED' });
    const stopped = executionReducer(running, {
      type: 'STOP', hadPendingBatch: false,
    });
    expect(stopped.stepsToRun).toBe(0);
    expect(stopped.runAll).toBe(false);
    expect(stopped.mustPause).toBe(true);
    expect(stopped.executing).toBe(true); // worker response will clear this
  });

  it('immediately clears executing when a batch was sleeping (hadPendingBatch=true)', () => {
    // No worker response is coming — clear executing right now.
    const running = apply({ type: 'RUN_ALL_REQUESTED' });
    const stopped = executionReducer(running, {
      type: 'STOP', hadPendingBatch: true,
    });
    expect(stopped.executing).toBe(false);
    expect(stopped.mustPause).toBe(true);
  });

  it('after STOP (mid-step), the incoming RESULT_RECEIVED clears executing', () => {
    const afterStop = apply(
      { type: 'RUN_ALL_REQUESTED' },
      { type: 'STOP', hadPendingBatch: false },
    );
    // worker.reset() response arrives; status='READY' ≠ 'RUNNING' → first branch.
    const final = executionReducer(afterStop, {
      type: 'RESULT_RECEIVED', status: 'READY', encounteredBreak: false,
    });
    expect(final).toEqual(initialExecState);
  });
});

// ---------------------------------------------------------------------------
// RESET
// ---------------------------------------------------------------------------

describe('RESET', () => {
  it('clears stepsToRun, runAll, mustPause; keeps executing when no pending batch', () => {
    // Unlike STOP, RESET does NOT set mustPause: the worker.reset() response
    // will arrive with status='READY' and the RESULT_RECEIVED branch will clean up.
    const running = apply({ type: 'RUN_ALL_REQUESTED' });
    const reset = executionReducer(running, {
      type: 'RESET', hadPendingBatch: false,
    });
    expect(reset.stepsToRun).toBe(0);
    expect(reset.runAll).toBe(false);
    expect(reset.mustPause).toBe(false);
    expect(reset.executing).toBe(true); // reset response will clear this
  });

  it('immediately clears executing when a batch was pending', () => {
    const running = apply({ type: 'RUN_ALL_REQUESTED' });
    const reset = executionReducer(running, {
      type: 'RESET', hadPendingBatch: true,
    });
    expect(reset.executing).toBe(false);
  });

  it('does not set mustPause (distinguishes RESET from STOP)', () => {
    const running = apply({ type: 'RUN_ALL_REQUESTED' });
    const reset  = executionReducer(running, { type: 'RESET',  hadPendingBatch: false });
    const stop   = executionReducer(running, { type: 'STOP',   hadPendingBatch: false });
    expect(reset.mustPause).toBe(false);
    expect(stop.mustPause).toBe(true);
  });
});

// ---------------------------------------------------------------------------
// INPUT_REQUESTED / INPUT_SUBMITTED
// ---------------------------------------------------------------------------

describe('INPUT_REQUESTED', () => {
  it('pauses execution without changing runAll or stepsToRun', () => {
    const running = apply({ type: 'RUN_ALL_REQUESTED' });
    const waitingForInput = executionReducer(running, { type: 'INPUT_REQUESTED' });
    expect(waitingForInput.executing).toBe(false);
    expect(waitingForInput.runAll).toBe(true); // preserved for later resumption
  });
});

describe('INPUT_SUBMITTED', () => {
  it('resumes execution', () => {
    const waitingForInput = apply(
      { type: 'RUN_ALL_REQUESTED' },
      { type: 'INPUT_REQUESTED' },
    );
    const resumed = executionReducer(waitingForInput, { type: 'INPUT_SUBMITTED' });
    expect(resumed.executing).toBe(true);
  });

  it('full input round-trip: run → input needed → submitted → result → done', () => {
    const s0 = apply({ type: 'RUN_ALL_REQUESTED' });         // running
    const s1 = executionReducer(s0, { type: 'INPUT_REQUESTED' });  // paused for input
    expect(s1.executing).toBe(false);
    const s2 = executionReducer(s1, { type: 'INPUT_SUBMITTED' });  // user submits
    expect(s2.executing).toBe(true);
    // Worker provides result; program finished after input.
    const s3 = executionReducer(s2, {
      type: 'RESULT_RECEIVED', status: 'STOPPED', encounteredBreak: false,
    });
    expect(s3).toEqual(initialExecState);
  });
});

// ---------------------------------------------------------------------------
// encounteredBreak
// ---------------------------------------------------------------------------

describe('encounteredBreak', () => {
  it('stops execution when a breakpoint is hit, even with runAll=true', () => {
    const running = apply({ type: 'RUN_ALL_REQUESTED' });
    const broke = executionReducer(running, {
      type: 'RESULT_RECEIVED', status: 'RUNNING', encounteredBreak: true,
    });
    // encounteredBreak=true → first branch → clear all flags.
    expect(broke).toEqual(initialExecState);
  });
});

// ---------------------------------------------------------------------------
// Unknown action (defensive)
// ---------------------------------------------------------------------------

describe('unknown action', () => {
  it('returns the same state reference for unrecognised action types', () => {
    const state: ExecState = { ...initialExecState };
    // Cast through unknown to simulate a runtime dispatch of an untyped action.
    const next = executionReducer(state, { type: '__UNKNOWN__' } as unknown as ExecAction);
    expect(next).toBe(state);
  });
});
