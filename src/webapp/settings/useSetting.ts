import { useCallback, useMemo } from 'react';
import { useLocalStorage } from '../hooks/useLocalStorage';
import { getSchema, sanitize, type SettingValueMap } from './schema';
import type { SettingKeyType } from './SettingKey';

/**
 * Higher-level hook for a single persisted setting.
 *
 * Usage:
 *   const [viMode, setViMode, resetViMode] = useSetting(SettingKey.VI_MODE);
 *   // viMode is inferred as `boolean`
 *   // setViMode accepts `boolean | ((prev: boolean) => boolean)`
 *
 * Compared to the raw `useLocalStorage` primitive, this hook:
 *   - looks up the setting's declared type and default from `SETTINGS_SCHEMA`,
 *     so call sites don't repeat the default value;
 *   - validates the persisted value against the declared type (and optional
 *     `validate` predicate) on every read, falling back to the default if it
 *     doesn't match — the simulator stays usable even if localStorage is
 *     corrupted or was written by a different schema version;
 *   - for `object` settings, shallow-merges the stored value on top of the
 *     default so newly-added default keys appear for existing users;
 *   - for TypeScript call sites, returns a precisely-typed tuple via the
 *     `SettingValueMap` index, giving real type inference without any casts.
 *
 * This mirrors the Java `ConfigStore` interface (typed getters, documented
 * defaults in a single place, graceful fallback).
 *
 * @param key  One of `SettingKey.*` (the string value, e.g. `'viMode'`).
 * @returns [value, setValue, reset]
 */
export function useSetting<K extends SettingKeyType>(
  key: K,
): [
  SettingValueMap[K],
  (
    v: SettingValueMap[K] | ((prev: SettingValueMap[K]) => SettingValueMap[K]),
  ) => void,
  () => void,
] {
  // Validate the key up front so typos fail fast instead of silently writing
  // random localStorage entries.
  const schema = getSchema(key);

  // useLocalStorage is generic; thread `unknown` here since the schema
  // default can be any supported type. The public return type is narrowed by K
  // via SettingValueMap, so callers always get a concrete type.
  const [raw, setRaw, resetRaw] = useLocalStorage<unknown>(key, schema.default);

  const value = useMemo(
    () => sanitize(key, raw) as SettingValueMap[K],
    [key, raw],
  );

  const setValue = useCallback(
    (
      next:
        | SettingValueMap[K]
        | ((prev: SettingValueMap[K]) => SettingValueMap[K]),
    ) => {
      if (typeof next === 'function') {
        // Updater-function form: pass the sanitized current value so the
        // caller always receives the validated (not raw) previous state.
        setRaw((prev: unknown) =>
          (next as (prev: SettingValueMap[K]) => SettingValueMap[K])(
            sanitize(key, prev) as SettingValueMap[K],
          ),
        );
      } else {
        setRaw(next);
      }
    },
    [key, setRaw],
  );

  return [value, setValue, resetRaw];
}
