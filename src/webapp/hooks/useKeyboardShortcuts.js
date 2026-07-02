import React from 'react';
import { deriveLogicalState } from '../simulatorState';

/**
 * useKeyboardShortcuts — registers a window-level keydown handler for the
 * run-control keyboard shortcuts (F2, F8, F9, F10, Escape).
 *
 * A ref holds the callback so the window listener itself is registered just
 * once, while the body is reassigned on every render and therefore always
 * reads the latest closures (status, executing, inputRequest, …).  This is
 * the same workerHandlerRef / keyboardHandlerRef pattern used elsewhere in
 * the codebase.
 *
 * @param {object} params
 * @param {string}        params.status        - 'READY' | 'RUNNING' | 'STOPPED'
 * @param {boolean}       params.executing     - true while worker is computing
 * @param {object|null}   params.inputRequest  - non-null while InputDialog is open
 * @param {function}      params.isValidProgram - () => boolean
 * @param {function}      params.loadCode      - () => void
 * @param {function}      params.runCode       - () => void
 * @param {function}      params.stepCode      - (n: number) => void
 * @param {function}      params.stopCode      - () => void
 * @param {function}      params.pauseCode     - () => void (dispatch PAUSE_REQUESTED)
 * @param {number}        params.stepStride    - multi-step count from settings
 * @param {object}        params.appInsights   - telemetry client
 */
export function useKeyboardShortcuts({
  status,
  executing,
  inputRequest,
  isValidProgram,
  loadCode,
  runCode,
  stepCode,
  stopCode,
  pauseCode,
  stepStride,
  appInsights,
}) {
  const keyboardHandlerRef = React.useRef(null);
  keyboardHandlerRef.current = (e) => {
    // Don't steal keys while a modal dialog is open (Help, Settings, Input).
    if (document.querySelector('[role="dialog"]')) return;
    const logicalState = deriveLogicalState(status, executing, inputRequest);
    if (logicalState === 'WAITING_FOR_INPUT') return;

    switch (e.key) {
      case 'F2':
        e.preventDefault();
        if (isValidProgram()) {
          appInsights.trackEvent({
            name: 'click',
            properties: { action: 'load', source: 'keyboard' },
          });
          loadCode();
        }
        break;
      case 'F8':
        e.preventDefault();
        if (logicalState === 'READY') {
          appInsights.trackEvent({
            name: 'click',
            properties: { action: 'run', source: 'keyboard' },
          });
          runCode();
        } else if (logicalState === 'EXECUTING') {
          appInsights.trackEvent({ name: 'pause', source: 'keyboard' });
          pauseCode();
        }
        break;
      case 'F9':
        e.preventDefault();
        if (logicalState === 'READY') {
          appInsights.trackEvent({
            name: 'click',
            properties: { action: 'step', source: 'keyboard' },
          });
          stepCode(1);
        }
        break;
      case 'F10':
        e.preventDefault();
        if (logicalState === 'READY') {
          appInsights.trackEvent({
            name: 'click',
            properties: { action: 'step', source: 'keyboard' },
          });
          stepCode(stepStride);
        }
        break;
      case 'Escape':
        e.preventDefault();
        if (logicalState === 'READY') {
          appInsights.trackEvent({
            name: 'click',
            properties: { action: 'stop', source: 'keyboard' },
          });
          stopCode();
        }
        break;
      default:
        break;
    }
  };

  React.useEffect(() => {
    const handleKeyDown = (e) => keyboardHandlerRef.current(e);
    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, []);
}
