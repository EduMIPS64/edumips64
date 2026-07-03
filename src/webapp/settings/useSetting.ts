import { useCallback, useMemo } from 'react';
import { useLocalStorage } from '../hooks/useLocalStorage';
import { getSchema, sanitize } from './schema';

/**
 * Higher-level hook for a single persisted setting.
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
 * @param key  One of `SettingKey.*`.
 * @returns [value, setValue, reset]
 *
 * TODO(ts): The return type is `unknown` here because the schema's `default`
 * field is `unknown` (each key has a different concrete type). A per-key
 * overload map would give call sites concrete types; deferred to a later
 * phase when the component layer is converted.
 */
export function useSetting(
  key: string,
): [unknown, (v: unknown) => void, () => void] {
  // Validate the key up front so typos fail fast instead of silently writing
  // random localStorage entries.
  const schema = getSchema(key);

  // useLocalStorage is generic; we thread `unknown` here since the schema
  // default can be any supported type. Call sites that need a concrete type
  // should cast the returned value.  TODO(ts): remove casts when components
  // are converted.
  const [raw, setRaw, resetRaw] = useLocalStorage<unknown>(key, schema.default);

  const value = useMemo(() => sanitize(key, raw), [key, raw]);

  const setValue = useCallback(
    (next: unknown) => {
      if (typeof next === 'function') {
        // Updater-function form: pass the sanitized current value so the
        // caller always receives the validated (not raw) previous state.
        setRaw((prev: unknown) => (next as (prev: unknown) => unknown)(sanitize(key, prev)));
      } else {
        setRaw(next);
      }
    },
    [key, setRaw],
  );

  return [value, setValue, resetRaw];
}
