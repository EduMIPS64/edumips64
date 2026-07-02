/**
 * Unit tests for simulatorState.js — deriveLogicalState helper.
 *
 * deriveLogicalState(status, executing, inputRequest) maps the three
 * observable simulator values onto the five UI states used by the toolbar
 * and keyboard-shortcut gating logic.
 */

import { describe, it, expect } from 'vitest';
import { deriveLogicalState } from '../../webapp/simulatorState.js';

describe('deriveLogicalState', () => {
  describe('WAITING_FOR_INPUT', () => {
    it('returns WAITING_FOR_INPUT when inputRequest is non-null, regardless of status', () => {
      const req = { some: 'request' };
      expect(deriveLogicalState('READY',   false, req)).toBe('WAITING_FOR_INPUT');
      expect(deriveLogicalState('RUNNING', false, req)).toBe('WAITING_FOR_INPUT');
      expect(deriveLogicalState('STOPPED', false, req)).toBe('WAITING_FOR_INPUT');
      expect(deriveLogicalState('RUNNING', true,  req)).toBe('WAITING_FOR_INPUT');
    });
  });

  describe('EMPTY', () => {
    it('returns EMPTY when status=READY and no inputRequest', () => {
      expect(deriveLogicalState('READY', false, null)).toBe('EMPTY');
      expect(deriveLogicalState('READY', true,  null)).toBe('EMPTY');
    });
  });

  describe('ENDED', () => {
    it('returns ENDED when status=STOPPED and no inputRequest', () => {
      expect(deriveLogicalState('STOPPED', false, null)).toBe('ENDED');
      expect(deriveLogicalState('STOPPED', true,  null)).toBe('ENDED');
    });
  });

  describe('EXECUTING', () => {
    it('returns EXECUTING when status=RUNNING, executing=true, no inputRequest', () => {
      expect(deriveLogicalState('RUNNING', true, null)).toBe('EXECUTING');
    });
  });

  describe('READY', () => {
    it('returns READY when status=RUNNING, executing=false, no inputRequest', () => {
      // This is the "program loaded, paused between steps" state.
      expect(deriveLogicalState('RUNNING', false, null)).toBe('READY');
    });
  });

  describe('integration with executionReducer.executing', () => {
    it('reflects the reducer executing flag: false → READY, true → EXECUTING', () => {
      // Ensure deriveLogicalState works with the boolean values the reducer emits.
      expect(deriveLogicalState('RUNNING', false, null)).toBe('READY');
      expect(deriveLogicalState('RUNNING', true,  null)).toBe('EXECUTING');
    });
  });
});
