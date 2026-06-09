/**
 * Derives the logical UI state from the three observable simulator values.
 *
 * @param {string} status      - 'READY' | 'RUNNING' | 'STOPPED'
 * @param {boolean} executing  - worker is actively processing steps
 * @param {object|null} inputRequest - non-null while InputDialog is open
 * @returns {'EMPTY'|'READY'|'EXECUTING'|'ENDED'|'WAITING_FOR_INPUT'}
 */
export function deriveLogicalState(status, executing, inputRequest) {
  if (inputRequest !== null) return 'WAITING_FOR_INPUT';
  if (status === 'READY') return 'EMPTY';
  if (status === 'STOPPED') return 'ENDED';
  // status === 'RUNNING'
  if (executing) return 'EXECUTING';
  return 'READY';
}
