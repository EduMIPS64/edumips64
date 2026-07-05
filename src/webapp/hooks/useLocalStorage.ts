import { useCallback, useSyncExternalStore } from 'react';

const NAMESPACE = 'edumips64';
const SCHEMA_VERSION = 1;

const storageKey = (name: string) => `${NAMESPACE}:v${SCHEMA_VERSION}:${name}`;

// ---------------------------------------------------------------------------
// Module-level same-tab notification channel
//
// The native 'storage' event fires in OTHER tabs only. We need same-tab
// notifications so that two hook instances for the same key stay in sync
// within a single page. A simple set of per-key listener sets achieves this
// without a heavy EventEmitter dependency.
// ---------------------------------------------------------------------------

type Listener = () => void;
const listeners = new Map<string, Set<Listener>>();

function getListeners(key: string): Set<Listener> {
  let set = listeners.get(key);
  if (!set) {
    set = new Set();
    listeners.set(key, set);
  }
  return set;
}

/** Notify all same-tab subscribers for a given localStorage key. */
function notifyLocal(key: string): void {
  getListeners(key).forEach((fn) => fn());
}

// ---------------------------------------------------------------------------
// Per-key parsed-value cache
//
// useSyncExternalStore requires getSnapshot to return a STABLE reference
// across calls when the underlying store hasn't changed. We cache the parsed
// value keyed by the raw localStorage string so repeated snapshot reads
// return the same object reference and don't trigger infinite re-renders.
// ---------------------------------------------------------------------------

interface ParseCache<T> {
  raw: string | null;
  parsed: T;
}

// We use a WeakMap-like pattern via a plain Map keyed by storageKey.
// The cache entries are per-hook-instance (stored in closure).

// ---------------------------------------------------------------------------
// Hook
// ---------------------------------------------------------------------------

type Dispatch<T> = (value: T | ((prev: T) => T)) => void;

/**
 * Persist a value to localStorage and keep it in sync with React state.
 * Uses useSyncExternalStore for correct cross-instance and cross-tab sync.
 *
 * @param name         localStorage key suffix (namespaced internally).
 * @param defaultValue Value to use when nothing is persisted yet.
 * @returns [value, setValue, reset] — same contract as useState plus a reset
 *          helper that restores the default.
 */
export function useLocalStorage<T>(
  name: string,
  defaultValue: T,
): [T, Dispatch<T>, () => void] {
  const key = storageKey(name);

  // Cache lives in module scope keyed by storage key so all hook instances
  // for the same key share the same parsed-value cache.
  // We keep it as a module-level Map of WeakRef-like plain objects.
  // Simpler: one cache object per key, reset when the raw string changes.

  // Subscribe function: registers both the same-tab local listener and the
  // cross-tab 'storage' event listener.
  const subscribe = useCallback(
    (onStoreChange: Listener) => {
      // Same-tab listener
      getListeners(key).add(onStoreChange);

      // Cross-tab listener (storage event fires only in OTHER tabs)
      const onStorageEvent = (e: StorageEvent) => {
        if (e.key === key) {
          onStoreChange();
        }
      };
      window.addEventListener('storage', onStorageEvent);

      return () => {
        getListeners(key).delete(onStoreChange);
        window.removeEventListener('storage', onStorageEvent);
      };
    },
    [key],
  );

  // We cache parsed values per key to maintain stable references.
  // The cache is stored in a module-level Map.
  const value = useSyncExternalStore(
    subscribe,
    () => getSnapshot(key, defaultValue),
    () => defaultValue, // server snapshot
  );

  const setValue = useCallback(
    (next: T | ((prev: T) => T)) => {
      const current = getSnapshot(key, defaultValue);
      const newValue = typeof next === 'function' ? (next as (prev: T) => T)(current) : next;
      writeStoredValue(key, name, newValue);
    },
    [key, name, defaultValue],
  );

  const reset = useCallback(() => setValue(defaultValue), [setValue, defaultValue]);

  return [value, setValue, reset];
}

/**
 * Write a value directly to a (namespaced) localStorage key and notify every
 * same-tab `useLocalStorage`/`useSetting` instance subscribed to it, without
 * needing a mounted hook instance for that key. Used by `resetStoredValue`
 * below, so a single "reset to defaults" action can reset settings whose
 * hooks live deep inside other components (e.g. cache configuration) without
 * threading callbacks through every layer.
 */
function writeStoredValue(key: string, name: string, value: unknown): void {
  try {
    window.localStorage.setItem(key, JSON.stringify(value));
  } catch (e) {
    console.warn(`useLocalStorage: failed to write "${name}"`, e);
  }
  // Invalidate the parse cache for this key
  parseCacheMap.delete(key);
  // Notify all same-tab subscribers (storage event doesn't fire in the writing tab)
  notifyLocal(key);
}

/**
 * Reset a persisted setting to `defaultValue` by its (unnamespaced) key name,
 * without needing a mounted `useLocalStorage`/`useSetting` hook instance.
 * Any component currently subscribed to that key (via either hook) picks up
 * the change immediately, the same way it would if that component's own
 * `reset()` had been called.
 */
export function resetStoredValue(name: string, defaultValue: unknown): void {
  writeStoredValue(storageKey(name), name, defaultValue);
}

// ---------------------------------------------------------------------------
// Parse cache (module-level, shared across all hook instances for a key)
// ---------------------------------------------------------------------------

const parseCacheMap = new Map<string, ParseCache<unknown>>();

function getSnapshot<T>(key: string, defaultValue: T): T {
  let raw: string | null;
  try {
    raw = window.localStorage.getItem(key);
  } catch (e) {
    console.warn(`useLocalStorage: failed to read key "${key}"`, e);
    return defaultValue;
  }

  const existing = parseCacheMap.get(key) as ParseCache<T> | undefined;
  if (existing && existing.raw === raw) {
    return existing.parsed;
  }

  let parsed: T;
  if (raw === null) {
    parsed = defaultValue;
  } else {
    try {
      parsed = JSON.parse(raw) as T;
    } catch (e) {
      console.warn(`useLocalStorage: failed to parse value for key "${key}"`, e);
      parsed = defaultValue;
    }
  }

  parseCacheMap.set(key, { raw, parsed });
  return parsed;
}
