/**
 * Edge-case unit tests for executionReducer.ts
 *
 * These tests complement executionReducer.test.ts (which covers the happy path
 * for each action in isolation).  Here we focus on multi-action sequences that
 * mirror real concurrency patterns: interleaved PAUSE+RESULT, RESET during a
 * pending batch, INPUT round-trips inside run-all, and encounteredBreak during
 * a multi-step run with steps remaining.
 */

import { describe, it, expect } from 'vitest';
import {
  executionReducer,
  initialExecState,
  type ExecAction,
  type ExecState,
} from '../../webapp/executionReducer';

function apply(startState: ExecState, ...actions: ExecAction[]): ExecState {
  return actions.reduce(executionReducer, startState);
}

function applyFromInitial(...actions: ExecAction[]): ExecState {
  return apply(initialExecState, ...actions);
}

// ---------------------------------------------------------------------------
// Interleaved PAUSE + RESULT sequences
// ---------------------------------------------------------------------------

describe('PAUSE interleaved with multi-step batching', () => {
  it('stops after the batch that was in-flight when PAUSE arrived', () => {
    // Sequence: RUN_ALL → PAUSE → RESULT(RUNNING) → all flags cleared.
    const s0 = applyFromInitial({ type: 'RUN_ALL_REQUESTED' });
    expect(s0.runAll).toBe(true);

    const s1 = executionReducer(s0, { type: 'PAUSE_REQUESTED' });
    expect(s1.mustPause).toBe(true);
    expect(s1.executing).toBe(true); // still running until result arrives

    const s2 = executionReducer(s1, {
      type: 'RESULT_RECEIVED',
      status: 'RUNNING',
      encounteredBreak: false,
    });
    // mustPause=true → first branch → clear all flags.
    expect(s2).toEqual(initialExecState);
  });

  it('PAUSE after a multi-step STEP_REQUESTED stops on the next result', () => {
    // Simulates: stepCode(200) → pause mid-execution.
    const s0 = applyFromInitial({ type: 'STEP_REQUESTED', stepsRemaining: 150 });
    const s1 = executionReducer(s0, { type: 'PAUSE_REQUESTED' });
    expect(s1.mustPause).toBe(true);
    expect(s1.stepsToRun).toBe(150); // unchanged

    // Worker finishes its current batch.
    const s2 = executionReducer(s1, {
      type: 'RESULT_RECEIVED',
      status: 'RUNNING',
      encounteredBreak: false,
    });
    // mustPause was true → stopped, regardless of pending steps.
    expect(s2).toEqual(initialExecState);
  });

  it('two consecutive PAUSE_REQUESTED actions are idempotent', () => {
    const s0 = applyFromInitial({ type: 'RUN_ALL_REQUESTED' });
    const s1 = executionReducer(s0, { type: 'PAUSE_REQUESTED' });
    const s2 = executionReducer(s1, { type: 'PAUSE_REQUESTED' });
    // A second pause doesn't change the state meaningfully.
    expect(s2.mustPause).toBe(true);
    expect(s2.runAll).toBe(true);
  });
});

// ---------------------------------------------------------------------------
// RESET during a pending batch
// ---------------------------------------------------------------------------

describe('RESET during a pending batch', () => {
  it('clears executing immediately when hadPendingBatch=true', () => {
    // Batch was sleeping in setTimeout: no worker response will arrive.
    const running = applyFromInitial({ type: 'STEP_REQUESTED', stepsRemaining: 100 });
    const reset = executionReducer(running, { type: 'RESET', hadPendingBatch: true });
    expect(reset.executing).toBe(false);
    expect(reset.stepsToRun).toBe(0);
    expect(reset.runAll).toBe(false);
    expect(reset.mustPause).toBe(false);
  });

  it('keeps executing=true when hadPendingBatch=false (worker mid-step)', () => {
    // Worker is computing steps: its reset response will arrive later.
    const running = applyFromInitial({ type: 'RUN_ALL_REQUESTED' });
    const reset = executionReducer(running, { type: 'RESET', hadPendingBatch: false });
    expect(reset.executing).toBe(true); // worker response clears this
    expect(reset.mustPause).toBe(false); // RESET does not set mustPause
    expect(reset.runAll).toBe(false);
  });

  it('does NOT set mustPause (distinguishing it from STOP)', () => {
    const running = applyFromInitial({ type: 'RUN_ALL_REQUESTED' });
    const reset = executionReducer(running, { type: 'RESET', hadPendingBatch: false });
    const stop = executionReducer(running, { type: 'STOP', hadPendingBatch: false });
    expect(reset.mustPause).toBe(false);
    expect(stop.mustPause).toBe(true);
  });

  it('after RESET (no pending batch), a READY result clears all flags', () => {
    // worker.reset() → response arrives with status='READY'.
    const s0 = applyFromInitial(
      { type: 'RUN_ALL_REQUESTED' },
      { type: 'RESET', hadPendingBatch: false },
    );
    expect(s0.executing).toBe(true);

    const s1 = executionReducer(s0, {
      type: 'RESULT_RECEIVED',
      status: 'READY',
      encounteredBreak: false,
    });
    // status !== 'RUNNING' → first branch → clear all.
    expect(s1).toEqual(initialExecState);
  });

  it('RESET clears stepsToRun from a multi-step sequence', () => {
    const s0 = applyFromInitial({ type: 'STEP_REQUESTED', stepsRemaining: 75 });
    expect(s0.stepsToRun).toBe(75);

    const reset = executionReducer(s0, { type: 'RESET', hadPendingBatch: true });
    expect(reset.stepsToRun).toBe(0);
  });
});

// ---------------------------------------------------------------------------
// INPUT_REQUESTED during run-all then INPUT_SUBMITTED resumes
// ---------------------------------------------------------------------------

describe('INPUT_REQUESTED / INPUT_SUBMITTED during run-all', () => {
  it('pauses execution while preserving runAll and stepsToRun', () => {
    const running = applyFromInitial({ type: 'RUN_ALL_REQUESTED' });
    const waiting = executionReducer(running, { type: 'INPUT_REQUESTED' });
    expect(waiting.executing).toBe(false);
    expect(waiting.runAll).toBe(true); // must survive so the loop can resume
    expect(waiting.stepsToRun).toBe(0);
    expect(waiting.mustPause).toBe(false);
  });

  it('resumes execution after INPUT_SUBMITTED', () => {
    const s0 = applyFromInitial(
      { type: 'RUN_ALL_REQUESTED' },
      { type: 'INPUT_REQUESTED' },
    );
    const s1 = executionReducer(s0, { type: 'INPUT_SUBMITTED' });
    expect(s1.executing).toBe(true);
    expect(s1.runAll).toBe(true);
  });

  it('full run-all → input → submit → done sequence', () => {
    const s0 = applyFromInitial({ type: 'RUN_ALL_REQUESTED' });
    const s1 = executionReducer(s0, { type: 'INPUT_REQUESTED' });
    expect(s1.executing).toBe(false);

    const s2 = executionReducer(s1, { type: 'INPUT_SUBMITTED' });
    expect(s2.executing).toBe(true);

    // Worker returns; program finished.
    const s3 = executionReducer(s2, {
      type: 'RESULT_RECEIVED',
      status: 'STOPPED',
      encounteredBreak: false,
    });
    expect(s3).toEqual(initialExecState);
  });

  it('PAUSE during INPUT wait is honoured when execution resumes', () => {
    // User hits Pause while the InputDialog is open.
    const s0 = applyFromInitial(
      { type: 'RUN_ALL_REQUESTED' },
      { type: 'INPUT_REQUESTED' },
      { type: 'PAUSE_REQUESTED' },
    );
    expect(s0.mustPause).toBe(true);
    expect(s0.executing).toBe(false);

    // User then submits input; executing resumes.
    const s1 = executionReducer(s0, { type: 'INPUT_SUBMITTED' });
    expect(s1.executing).toBe(true);

    // Worker's result arrives; mustPause=true → stops.
    const s2 = executionReducer(s1, {
      type: 'RESULT_RECEIVED',
      status: 'RUNNING',
      encounteredBreak: false,
    });
    expect(s2).toEqual(initialExecState);
  });
});

// ---------------------------------------------------------------------------
// encounteredBreak during multi-step with steps remaining
// ---------------------------------------------------------------------------

describe('encounteredBreak during multi-step execution', () => {
  it('stops immediately even though stepsToRun > 0', () => {
    // stepCode(200): first batch dispatched, 150 still queued.
    const s0 = applyFromInitial({ type: 'STEP_REQUESTED', stepsRemaining: 150 });
    expect(s0.stepsToRun).toBe(150);

    // Worker hits a BREAK instruction partway through.
    const s1 = executionReducer(s0, {
      type: 'RESULT_RECEIVED',
      status: 'RUNNING',
      encounteredBreak: true,
    });
    // encounteredBreak=true → first branch → clear all, do not schedule more batches.
    expect(s1).toEqual(initialExecState);
  });

  it('stops in run-all mode on encounteredBreak', () => {
    const s0 = applyFromInitial({ type: 'RUN_ALL_REQUESTED' });
    const s1 = executionReducer(s0, {
      type: 'RESULT_RECEIVED',
      status: 'RUNNING',
      encounteredBreak: true,
    });
    expect(s1).toEqual(initialExecState);
  });

  it('encounteredBreak with status=STOPPED also clears all flags', () => {
    // Defensive: both conditions are independently satisfied.
    const s0 = applyFromInitial({ type: 'STEP_REQUESTED', stepsRemaining: 0 });
    const s1 = executionReducer(s0, {
      type: 'RESULT_RECEIVED',
      status: 'STOPPED',
      encounteredBreak: true,
    });
    expect(s1).toEqual(initialExecState);
  });
});
