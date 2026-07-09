import { useRef, useEffect } from 'react';
import { deriveLogicalState } from '../simulatorState';
import type { CpuStatus, SimulatorResult } from '../simulator/protocol';
import type { ITelemetryClient } from '../telemetry';

// ---------------------------------------------------------------------------
// Params type
// ---------------------------------------------------------------------------

/** Parameters accepted by useKeyboardShortcuts. */
export interface KeyboardShortcutsParams {
  /** CPU status from the worker result. */
  status: CpuStatus;
  /** True while the worker is actively computing steps. */
  executing: boolean;
  /** Non-null while the InputDialog is open waiting for user input. */
  inputRequest: SimulatorResult | null;
  /** Returns true when the current editor content is a syntactically valid program. */
  isValidProgram: () => boolean;
  /** Load the current editor code into the simulator. */
  loadCode: () => void;
  /** Start run-all mode. */
  runCode: () => void;
  /** Run n steps. */
  stepCode: (n: number) => void;
  /** Stop execution and reset the simulator. */
  stopCode: () => void;
  /** Pause an in-progress run (dispatches PAUSE_REQUESTED). */
  pauseCode: () => void;
  /** Multi-step count from the STEP_STRIDE setting. */
  stepStride: number;
  /** Telemetry client for keyboard-shortcut action tracking. */
  appInsights: ITelemetryClient;
}

/**
 * useKeyboardShortcuts — registers a window-level keydown handler for the
 * run-control keyboard shortcuts (F2, F8, F9, F10, Escape).
 *
 * A ref holds the callback so the window listener itself is registered just
 * once, while the body is reassigned on every render and therefore always
 * reads the latest closures (status, executing, inputRequest, …).  This is
 * the same workerHandlerRef / keyboardHandlerRef pattern used elsewhere in
 * the codebase.
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
}: KeyboardShortcutsParams): void {
  const keyboardHandlerRef = useRef<((e: KeyboardEvent) => void) | null>(null);
  keyboardHandlerRef.current = (e: KeyboardEvent) => {
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
          appInsights.trackEvent({
            name: 'pause',
            properties: { source: 'keyboard' },
          });
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

  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      if (keyboardHandlerRef.current) {
        keyboardHandlerRef.current(e);
      }
    };
    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, []);
}
