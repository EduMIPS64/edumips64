import { useState, useEffect, useCallback } from 'react';

const NAMESPACE = 'edumips64';
const SCHEMA_VERSION = 1;

const storageKey = (name) => `${NAMESPACE}:v${SCHEMA_VERSION}:${name}`;

export function useLocalStorage(name, defaultValue) {
  const [value, setValue] = useState(() => {
    try {
      const raw = window.localStorage.getItem(storageKey(name));
      return raw !== null ? JSON.parse(raw) : defaultValue;
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
