import { useState, useCallback } from 'react';
import isEqual from 'lodash/isEqual';
import type {
  SimulatorResult,
  Registers,
  Memory,
  Statistics,
  CpuStatus,
  Pipeline,
  CyclesDiagram,
  ParsingError,
  PipelineInstruction,
} from '../simulator/protocol';

/**
 * Returns true if the given parsingErrors array contains at least one actual
 * error (not just warnings).  Exported so the execution controller can reuse
 * the same logic when processing checksyntax responses.
 */
export const hasRealErrors = (parsingErrors: ParsingError[] | null): boolean => {
  if (!parsingErrors) return false;
  return parsingErrors.some((e) => !e.isWarning);
};

/**
 * useSimulatorData — owns the simulator *data* state.
 *
 * Exposes:
 *   - The current values for all simulator data fields.
 *   - applyResultState(result) — applies a full step/load/reset worker result,
 *     using reference-preserving updates so React.memo shallow-compare on
 *     downstream panels skips re-renders when a worker result didn't change
 *     their data.
 *
 *     Pattern: setRegisters(prev => isEqual(prev, next) ? prev : next)
 *     (lodash isEqual already in the dep-graph; imported from 'lodash/isEqual').
 *
 *   - applyChecksyntaxResult(result) — applies a checksyntax worker response;
 *     updates only parsingErrors and parsedInstructions.
 *
 *   - Individual setters (setStdout, setParsingErrors, setInputRequest) for the
 *     few call sites in Simulator.js that need fine-grained control.
 *
 * @param initialState - the initial simulator state passed by AppLoader.
 */
export function useSimulatorData(initialState: SimulatorResult) {
  const [registers, setRegisters] = useState<Registers>(initialState.registers);
  const [memory, setMemory] = useState<Memory>(initialState.memory);
  const [stats, setStats] = useState<Statistics>(initialState.statistics);
  const [status, setStatus] = useState<CpuStatus>(initialState.status);
  const [pipeline, setPipeline] = useState<Pipeline>(initialState.pipeline);
  const [cycles, setCycles] = useState<CyclesDiagram>(initialState.cycles);
  const [parsingErrors, setParsingErrors] = useState<ParsingError[] | null>(
    initialState.parsingErrors,
  );
  const [parsedInstructions, setParsedInstructions] = useState<
    PipelineInstruction[] | null
  >(initialState.parsedInstructions);
  const [stdout, setStdout] = useState<string>('');
  // inputRequest carries the full SimulatorResult when the worker requests
  // stdin input (result.inputRequested === true). null means no pending input.
  const [inputRequest, setInputRequest] = useState<SimulatorResult | null>(null);

  /**
   * Apply a full worker result (step / load / reset) to the data state.
   *
   * Reference-preserving updates: when the incoming data is deep-equal to the
   * previous value we return the *same* object reference.  React.memo performs
   * a shallow-compare of props, so a stable reference means the memo'd panel
   * will not re-render even though we called the setter.
   */
  const applyResultState = useCallback((result: SimulatorResult) => {
    setRegisters((prev) =>
      isEqual(prev, result.registers) ? prev : result.registers,
    );
    setMemory((prev) => (isEqual(prev, result.memory) ? prev : result.memory));
    setStats((prev) =>
      isEqual(prev, result.statistics) ? prev : result.statistics,
    );
    // `status` is a plain string — strict equality is sufficient.
    setStatus(result.status);
    setPipeline((prev) =>
      isEqual(prev, result.pipeline) ? prev : result.pipeline,
    );
    setCycles((prev) => (isEqual(prev, result.cycles) ? prev : result.cycles));
    setParsingErrors((prev) =>
      isEqual(prev, result.parsingErrors) ? prev : result.parsingErrors,
    );

    if (hasRealErrors(result.parsingErrors)) {
      setParsedInstructions(null);
    } else {
      setParsedInstructions((prev) =>
        isEqual(prev, result.parsedInstructions)
          ? prev
          : result.parsedInstructions,
      );
    }

    if (result.stdout) {
      setStdout(result.stdout);
    }
  }, []);

  /**
   * Apply a checksyntax worker response (method === 'checksyntax').
   *
   * Only updates parsingErrors and parsedInstructions so that the rest of the
   * UI does not re-render unnecessarily on every keypress.
   */
  const applyChecksyntaxResult = useCallback((result: SimulatorResult) => {
    setParsingErrors((prev) =>
      isEqual(prev, result.parsingErrors) ? prev : result.parsingErrors,
    );
    if (hasRealErrors(result.parsingErrors)) {
      setParsedInstructions(null);
    } else {
      setParsedInstructions((prev) =>
        isEqual(prev, result.parsedInstructions)
          ? prev
          : result.parsedInstructions,
      );
    }
  }, []);

  return {
    // State values
    registers,
    memory,
    stats,
    status,
    pipeline,
    cycles,
    parsingErrors,
    parsedInstructions,
    stdout,
    inputRequest,
    // Setters (for Simulator.js call sites that need fine-grained control)
    setStdout,
    setParsingErrors,
    setInputRequest,
    // Bulk updaters
    applyResultState,
    applyChecksyntaxResult,
  };
}
