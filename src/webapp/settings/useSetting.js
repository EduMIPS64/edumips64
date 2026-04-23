import { useCallback, useMemo } from 'react';
import { useLocalStorage } from '../hooks/useLocalStorage';
import { getSchema, sanitize } from './schema';

/**
 * Higher-level, typed hook for a single persisted setting.
 *
 * Usage:
 *   const [viMode, setViMode, resetViMode] = useSetting(SettingKey.VI_MODE);
 *
 * Compared to the raw `useLocalStorage` primitive, this hook:
 *   - looks up the setting's declared type and default from `SETTINGS_SCHEMA`,
 *     so call sites don't repeat the default value;
 *   - validates the persisted value against the declared type (and optional
 *     `validate` predicate) on every read, falling back to the default if it
 *     doesn't match — the simulator stays usable even if localStorage is
 *     corrupted or was written by a different schema version;
 *   - for `object` settings, shallow-merges the stored value on top of the
 *     default so newly-added default keys appear for existing users.
 *
 * This mirrors the Java `ConfigStore` interface (typed getters, documented
 * defaults in a single place, graceful fallback).
 *
 * @param {string} key  One of `SettingKey.*`.
 * @returns {[any, (v:any)=>void, ()=>void]}  `[value, setValue, reset]`.
 */
export function useSetting(key) {
  // Validate the key up front so typos fail fast instead of silently writing
  // random localStorage entries.
  const schema = getSchema(key);

  const [raw, setRaw, resetRaw] = useLocalStorage(key, schema.default);

  const value = useMemo(() => sanitize(key, raw), [key, raw]);

  const setValue = useCallback(
    (next) => {
      if (typeof next === 'function') {
        setRaw((prev) => next(sanitize(key, prev)));
      } else {
        setRaw(next);
      }
    },
    [key, setRaw],
  );

  return [value, setValue, resetRaw];
}
