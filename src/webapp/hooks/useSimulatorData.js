import React from 'react';
import isEqual from 'lodash/isEqual';

/**
 * Returns true if the given parsingErrors array contains at least one actual
 * error (not just warnings).  Exported so the execution controller can reuse
 * the same logic when processing checksyntax responses.
 */
export const hasRealErrors = (parsingErrors) => {
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
 * @param {object} initialState - the initial simulator state passed by AppLoader.
 */
export function useSimulatorData(initialState) {
  const [registers, setRegisters] = React.useState(initialState.registers);
  const [memory, setMemory] = React.useState(initialState.memory);
  const [stats, setStats] = React.useState(initialState.statistics);
  const [status, setStatus] = React.useState(initialState.status);
  const [pipeline, setPipeline] = React.useState(initialState.pipeline);
  const [parsingErrors, setParsingErrors] = React.useState(
    initialState.parsingErrors,
  );
  const [parsedInstructions, setParsedInstructions] = React.useState(
    initialState.parsedInstructions,
  );
  const [stdout, setStdout] = React.useState('');
  const [inputRequest, setInputRequest] = React.useState(null);

  /**
   * Apply a full worker result (step / load / reset) to the data state.
   *
   * Reference-preserving updates: when the incoming data is deep-equal to the
   * previous value we return the *same* object reference.  React.memo performs
   * a shallow-compare of props, so a stable reference means the memo'd panel
   * will not re-render even though we called the setter.
   */
  const applyResultState = React.useCallback((result) => {
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
  const applyChecksyntaxResult = React.useCallback((result) => {
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
