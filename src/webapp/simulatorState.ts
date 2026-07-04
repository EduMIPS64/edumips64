import type { CpuStatus } from './simulator/protocol';

/**
 * The five logical UI states derived from the three observable simulator values.
 *
 * EMPTY            – CPU is initialised but no program is loaded (status=READY).
 * READY            – Program is loaded, paused between steps (status=RUNNING, not executing).
 * EXECUTING        – Worker is actively running steps (status=RUNNING, executing=true).
 * ENDED            – Program has halted (status=STOPPED).
 * WAITING_FOR_INPUT – Worker blocked on stdin (inputRequest non-null).
 */
export type LogicalState =
  | 'EMPTY'
  | 'READY'
  | 'EXECUTING'
  | 'ENDED'
  | 'WAITING_FOR_INPUT';

/**
 * Derives the logical UI state from the three observable simulator values.
 *
 * @param status       - CPU status from the worker result.
 * @param executing    - true while the worker is actively processing steps.
 * @param inputRequest - non-null while the InputDialog is open.
 * @returns The current logical UI state.
 */
export function deriveLogicalState(
  status: CpuStatus,
  executing: boolean,
  inputRequest: object | null,
): LogicalState {
  if (inputRequest !== null) return 'WAITING_FOR_INPUT';
  if (status === 'READY') return 'EMPTY';
  if (status === 'STOPPED') return 'ENDED';
  // status === 'RUNNING'
  if (executing) return 'EXECUTING';
  return 'READY';
}
