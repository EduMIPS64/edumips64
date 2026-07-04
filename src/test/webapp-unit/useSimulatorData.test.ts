// @vitest-environment jsdom
/**
 * Unit tests for hooks/useSimulatorData.ts
 *
 * Focuses on the reference-preservation contract of applyResultState:
 * when the incoming data is deep-equal to the current state, the hook must
 * return the *same* object reference so React.memo skip-renders are effective.
 *
 * Also covers applyChecksyntaxResult and the hasRealErrors helper.
 */

import { describe, it, expect } from 'vitest';
import { renderHook, act } from '@testing-library/react';
import { useSimulatorData, hasRealErrors } from '../../webapp/hooks/useSimulatorData';
import type { SimulatorResult, Registers, Memory, Pipeline } from '../../webapp/simulator/protocol';

// ---------------------------------------------------------------------------
// Minimal fixture factories
// ---------------------------------------------------------------------------

function makeRegisters(seed = 0): Registers {
  return {
    gpr: [{ name: `R${seed}`, hexString: '0000000000000000', value: '0' }],
    fpu: [],
    special: [],
  };
}

function makeMemory(seed = 0): Memory {
  return { cells: [{ address_hex: '0x0', address: seed, value: '0', value_hex: '0x0', label: '', code: '', comment: '' }] };
}

function makeStats() {
  return {
    cycles: 0, instructions: 0, rawStalls: 0, wawStalls: 0, dividerStalls: 0,
    memoryStalls: 0, exStalls: 0, funcUnitStalls: 0,
    L1I_reads: 0, L1I_misses: 0, L1D_reads: 0, L1D_reads_misses: 0,
    L1D_writes: 0, L1D_writes_misses: 0, codeSizeBytes: 0, fcsr: '',
  };
}

function makePipeline(): Pipeline {
  return {
    IF: null, ID: null, EX: null, MEM: null, WB: null,
    FPAdder1: null, FPAdder2: null, FPAdder3: null, FPAdder4: null,
    FPMultiplier1: null, FPMultiplier2: null, FPMultiplier3: null,
    FPMultiplier4: null, FPMultiplier5: null, FPMultiplier6: null,
    FPMultiplier7: null, FPDivider: null,
  };
}

function makeResult(overrides: Partial<SimulatorResult> = {}): SimulatorResult {
  return {
    success: true,
    errorMessage: '',
    status: 'READY',
    registers: makeRegisters(),
    memory: makeMemory(),
    statistics: makeStats(),
    pipeline: makePipeline(),
    encounteredBreak: false,
    parsingErrors: null,
    parsedInstructions: null,
    stdout: '',
    method: 'reset',
    inputRequested: false,
    inputMaxLength: 0,
    inputResumeSteps: 0,
    inputDialogTitle: '',
    inputPromptMessage: '',
    inputTooLongMessage: '',
    errorCode: '',
    errorInstruction: '',
    errorStage: '',
    ...overrides,
  };
}

// ---------------------------------------------------------------------------
// hasRealErrors
// ---------------------------------------------------------------------------

describe('hasRealErrors', () => {
  it('returns false for null', () => {
    expect(hasRealErrors(null)).toBe(false);
  });

  it('returns false for an empty array', () => {
    expect(hasRealErrors([])).toBe(false);
  });

  it('returns false when all errors are warnings', () => {
    const warnings = [
      { row: 1, column: 1, isWarning: true, description: 'w' },
      { row: 2, column: 1, isWarning: true, description: 'w2' },
    ];
    expect(hasRealErrors(warnings)).toBe(false);
  });

  it('returns true when at least one error is not a warning', () => {
    const errors = [
      { row: 1, column: 1, isWarning: true, description: 'w' },
      { row: 2, column: 1, isWarning: false, description: 'err' },
    ];
    expect(hasRealErrors(errors)).toBe(true);
  });
});

// ---------------------------------------------------------------------------
// useSimulatorData — initial state
// ---------------------------------------------------------------------------

describe('useSimulatorData — initial state', () => {
  it('exposes the initialState values on mount', () => {
    const initial = makeResult();
    const { result } = renderHook(() => useSimulatorData(initial));
    expect(result.current.registers).toEqual(initial.registers);
    expect(result.current.memory).toEqual(initial.memory);
    expect(result.current.status).toBe('READY');
    expect(result.current.stdout).toBe('');
    expect(result.current.inputRequest).toBeNull();
  });
});

// ---------------------------------------------------------------------------
// useSimulatorData — reference preservation
// ---------------------------------------------------------------------------

describe('useSimulatorData — applyResultState reference preservation', () => {
  it('keeps the same registers reference when deep-equal data arrives', () => {
    const initial = makeResult();
    const { result } = renderHook(() => useSimulatorData(initial));

    const refBefore = result.current.registers;

    // Apply a result whose registers are deep-equal to the current ones.
    const sameDataResult = makeResult({ registers: makeRegisters(0) });
    act(() => {
      result.current.applyResultState(sameDataResult);
    });

    // The hook must return the same object reference (isEqual short-circuit).
    expect(result.current.registers).toBe(refBefore);
  });

  it('produces a new registers reference when the data changes', () => {
    const initial = makeResult();
    const { result } = renderHook(() => useSimulatorData(initial));

    const refBefore = result.current.registers;

    // Apply a result with different registers (seed=1).
    const changedResult = makeResult({ registers: makeRegisters(1) });
    act(() => {
      result.current.applyResultState(changedResult);
    });

    // Data changed → new reference.
    expect(result.current.registers).not.toBe(refBefore);
    expect(result.current.registers).toEqual(makeRegisters(1));
  });

  it('keeps the same memory reference when deep-equal data arrives', () => {
    const initial = makeResult();
    const { result } = renderHook(() => useSimulatorData(initial));

    const refBefore = result.current.memory;

    act(() => {
      result.current.applyResultState(makeResult({ memory: makeMemory(0) }));
    });

    expect(result.current.memory).toBe(refBefore);
  });

  it('produces a new memory reference when the data changes', () => {
    const initial = makeResult();
    const { result } = renderHook(() => useSimulatorData(initial));

    const refBefore = result.current.memory;

    act(() => {
      result.current.applyResultState(makeResult({ memory: makeMemory(99) }));
    });

    expect(result.current.memory).not.toBe(refBefore);
  });

  it('keeps the same pipeline reference when deep-equal data arrives', () => {
    const initial = makeResult();
    const { result } = renderHook(() => useSimulatorData(initial));

    const refBefore = result.current.pipeline;

    act(() => {
      result.current.applyResultState(makeResult({ pipeline: makePipeline() }));
    });

    expect(result.current.pipeline).toBe(refBefore);
  });

  it('accumulates stdout when a result carries stdout text', () => {
    const initial = makeResult();
    const { result } = renderHook(() => useSimulatorData(initial));

    act(() => {
      result.current.applyResultState(makeResult({ stdout: 'Hello\n' }));
    });

    expect(result.current.stdout).toBe('Hello\n');
  });

  it('does not update stdout when the result has no stdout field', () => {
    const initial = makeResult({ stdout: 'existing\n' });
    // Patch stdout into initial state via setStdout (initial state picks it up via applyResultState).
    const { result } = renderHook(() => useSimulatorData(initial));

    act(() => {
      result.current.applyResultState(makeResult({ stdout: 'existing\n' }));
    });

    const stdoutBefore = result.current.stdout;

    // Apply a result with empty stdout.
    act(() => {
      result.current.applyResultState(makeResult({ stdout: '' }));
    });

    // Empty stdout → condition `if (result.stdout)` is falsy → setStdout not called.
    expect(result.current.stdout).toBe(stdoutBefore);
  });
});

// ---------------------------------------------------------------------------
// useSimulatorData — applyResultState parsingErrors + parsedInstructions
// ---------------------------------------------------------------------------

describe('useSimulatorData — parsing errors', () => {
  it('sets parsedInstructions to null when parsingErrors contains a real error', () => {
    const initial = makeResult();
    const { result } = renderHook(() => useSimulatorData(initial));

    act(() => {
      result.current.applyResultState(
        makeResult({
          parsingErrors: [{ row: 1, column: 1, isWarning: false, description: 'err' }],
          parsedInstructions: [
            { Name: 'DADD', Code: 'DADD R1,R2,R3', Comment: '', SerialNumber: 0,
              Address: 0, Line: 1, BinaryRepresentation: '0'.repeat(32), OpCode: '000000',
              Stage: null, DivCount: -1 },
          ],
        }),
      );
    });

    expect(result.current.parsedInstructions).toBeNull();
  });

  it('preserves parsedInstructions when only warnings are present', () => {
    const instr = {
      Name: 'DADD', Code: 'DADD R1,R2,R3', Comment: '', SerialNumber: 0,
      Address: 0, Line: 1, BinaryRepresentation: '0'.repeat(32), OpCode: '000000',
      Stage: null, DivCount: -1,
    };
    const initial = makeResult();
    const { result } = renderHook(() => useSimulatorData(initial));

    act(() => {
      result.current.applyResultState(
        makeResult({
          parsingErrors: [{ row: 1, column: 1, isWarning: true, description: 'warn' }],
          parsedInstructions: [instr],
        }),
      );
    });

    expect(result.current.parsedInstructions).not.toBeNull();
    expect(result.current.parsedInstructions).toHaveLength(1);
  });
});

// ---------------------------------------------------------------------------
// useSimulatorData — applyChecksyntaxResult
// ---------------------------------------------------------------------------

describe('useSimulatorData — applyChecksyntaxResult', () => {
  it('updates only parsingErrors and parsedInstructions', () => {
    const initial = makeResult();
    const { result } = renderHook(() => useSimulatorData(initial));

    const registersBefore = result.current.registers;

    act(() => {
      result.current.applyChecksyntaxResult(
        makeResult({
          parsingErrors: [{ row: 2, column: 1, isWarning: true, description: 'w' }],
          parsedInstructions: null,
        }),
      );
    });

    // Registers must not be touched.
    expect(result.current.registers).toBe(registersBefore);
    // Parsing errors updated.
    expect(result.current.parsingErrors).toHaveLength(1);
  });
});

// ---------------------------------------------------------------------------
// useSimulatorData — fine-grained setters
// ---------------------------------------------------------------------------

describe('useSimulatorData — setStdout / setParsingErrors / setInputRequest', () => {
  it('setStdout updates stdout', () => {
    const { result } = renderHook(() => useSimulatorData(makeResult()));
    act(() => {
      result.current.setStdout('output line\n');
    });
    expect(result.current.stdout).toBe('output line\n');
  });

  it('setParsingErrors updates parsingErrors', () => {
    const { result } = renderHook(() => useSimulatorData(makeResult()));
    const errors = [{ row: 1, column: 1, isWarning: false, description: 'bad' }];
    act(() => {
      result.current.setParsingErrors(errors);
    });
    expect(result.current.parsingErrors).toEqual(errors);
  });

  it('setInputRequest updates inputRequest', () => {
    const initial = makeResult();
    const { result } = renderHook(() => useSimulatorData(initial));
    const req = makeResult({ inputRequested: true });
    act(() => {
      result.current.setInputRequest(req);
    });
    expect(result.current.inputRequest).toBe(req);
    act(() => {
      result.current.setInputRequest(null);
    });
    expect(result.current.inputRequest).toBeNull();
  });
});
