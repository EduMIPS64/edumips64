import { useState, useEffect, useCallback, Dispatch, SetStateAction } from 'react';

const NAMESPACE = 'edumips64';
const SCHEMA_VERSION = 1;

const storageKey = (name: string) => `${NAMESPACE}:v${SCHEMA_VERSION}:${name}`;

/**
 * Persist a value to localStorage and keep it in sync with React state.
 *
 * @param name         localStorage key suffix (namespaced internally).
 * @param defaultValue Value to use when nothing is persisted yet.
 * @returns [value, setValue, reset] — same contract as useState plus a reset
 *          helper that restores the default.
 */
export function useLocalStorage<T>(
  name: string,
  defaultValue: T,
): [T, Dispatch<SetStateAction<T>>, () => void] {
  const [value, setValue] = useState<T>(() => {
    try {
      const raw = window.localStorage.getItem(storageKey(name));
      return raw !== null ? (JSON.parse(raw) as T) : defaultValue;
    } catch (e) {
      console.warn(`useLocalStorage: failed to read "${name}"`, e);
      return defaultValue;
    }
  });

  useEffect(() => {
    try {
      window.localStorage.setItem(storageKey(name), JSON.stringify(value));
    } catch (e) {
      // Quota exceeded or storage disabled (private mode, etc.) — fail gracefully.
      console.warn(`useLocalStorage: failed to write "${name}"`, e);
    }
  }, [name, value]);

  const reset = useCallback(() => setValue(defaultValue), [defaultValue]);
  return [value, setValue, reset];
}
