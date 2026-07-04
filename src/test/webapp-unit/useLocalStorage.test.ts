// @vitest-environment jsdom
/**
 * Unit tests for useLocalStorage hook.
 *
 * Tests cover: initial read, write+read, functional update, reset,
 * cross-instance sync within the same tab, and a simulated storage event
 * from another tab.
 */

import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest';
import { renderHook, act } from '@testing-library/react';
import { useLocalStorage } from '../../webapp/hooks/useLocalStorage';

// Helper: fire a synthetic 'storage' event as if another tab wrote to localStorage.
function fireStorageEvent(key: string, newValue: string | null): void {
  const event = new StorageEvent('storage', { key, newValue });
  window.dispatchEvent(event);
}

beforeEach(() => {
  window.localStorage.clear();
});

afterEach(() => {
  window.localStorage.clear();
});

describe('useLocalStorage', () => {
  it('returns defaultValue when nothing is stored', () => {
    const { result } = renderHook(() => useLocalStorage('test-key', 42));
    const [value] = result.current;
    expect(value).toBe(42);
  });

  it('reads an existing value from localStorage', () => {
    // Pre-populate localStorage with the namespaced key.
    window.localStorage.setItem('edumips64:v1:test-key', JSON.stringify(99));
    const { result } = renderHook(() => useLocalStorage('test-key', 42));
    const [value] = result.current;
    expect(value).toBe(99);
  });

  it('writes and reads back a new value', () => {
    const { result } = renderHook(() => useLocalStorage('test-key', 0));
    act(() => {
      const [, setValue] = result.current;
      setValue(7);
    });
    const [value] = result.current;
    expect(value).toBe(7);
    expect(window.localStorage.getItem('edumips64:v1:test-key')).toBe('7');
  });

  it('supports functional updater form', () => {
    const { result } = renderHook(() => useLocalStorage('test-key', 10));
    act(() => {
      const [, setValue] = result.current;
      setValue((prev) => prev + 5);
    });
    const [value] = result.current;
    expect(value).toBe(15);
  });

  it('reset restores the default value', () => {
    const { result } = renderHook(() => useLocalStorage('test-key', 'default'));
    act(() => {
      const [, setValue] = result.current;
      setValue('changed');
    });
    expect(result.current[0]).toBe('changed');
    act(() => {
      const [, , reset] = result.current;
      reset();
    });
    expect(result.current[0]).toBe('default');
  });

  it('syncs across two hook instances for the same key (same-tab notification)', () => {
    const hook1 = renderHook(() => useLocalStorage('shared-key', 0));
    const hook2 = renderHook(() => useLocalStorage('shared-key', 0));

    act(() => {
      const [, setValue] = hook1.result.current;
      setValue(42);
    });

    // hook2 should pick up the change without any explicit interaction.
    expect(hook2.result.current[0]).toBe(42);
  });

  it('updates when a storage event fires from another tab', () => {
    const { result } = renderHook(() => useLocalStorage('cross-tab-key', 'initial'));
    expect(result.current[0]).toBe('initial');

    // Simulate another tab writing to localStorage.
    act(() => {
      window.localStorage.setItem('edumips64:v1:cross-tab-key', JSON.stringify('from-other-tab'));
      fireStorageEvent('edumips64:v1:cross-tab-key', JSON.stringify('from-other-tab'));
    });

    expect(result.current[0]).toBe('from-other-tab');
  });

  it('ignores storage events for other keys', () => {
    const { result } = renderHook(() => useLocalStorage('my-key', 'original'));
    act(() => {
      fireStorageEvent('edumips64:v1:other-key', JSON.stringify('noise'));
    });
    expect(result.current[0]).toBe('original');
  });

  it('works with object values', () => {
    const defaultVal = { a: 1, b: 'hello' };
    const { result } = renderHook(() => useLocalStorage('obj-key', defaultVal));
    act(() => {
      const [, setValue] = result.current;
      setValue({ a: 2, b: 'world' });
    });
    expect(result.current[0]).toEqual({ a: 2, b: 'world' });
  });

  it('falls back to default on parse error and warns', () => {
    window.localStorage.setItem('edumips64:v1:bad-key', 'not-valid-json{{{');
    const warnSpy = vi.spyOn(console, 'warn').mockImplementation(() => {});
    const { result } = renderHook(() => useLocalStorage('bad-key', 'fallback'));
    expect(result.current[0]).toBe('fallback');
    expect(warnSpy).toHaveBeenCalled();
    warnSpy.mockRestore();
  });
});
