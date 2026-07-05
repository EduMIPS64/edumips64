import { useCallback, useMemo } from 'react';
import { resetStoredValue, useLocalStorage } from '../hooks/useLocalStorage';
import { getSchema, sanitize, type SettingValueMap } from './schema';
import { SettingKey, type SettingKeyType } from './SettingKey';

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

/**
 * The settings surfaced in the Settings dialog (UI + Simulation tabs).
 * Deliberately excludes `EDITOR_CODE` (the user's in-progress program),
 * `EXPANDED_ACCORDIONS` (right-rail layout, not a "setting") and
 * `HELP_LANGUAGE` (belongs to the Help dialog, not this one).
 */
const SETTINGS_DIALOG_KEYS: SettingKeyType[] = [
  SettingKey.VI_MODE,
  SettingKey.FONT_SIZE,
  SettingKey.ACCORDION_ALERTS,
  SettingKey.THEME_MODE,
  SettingKey.PIPELINE_COLORS,
  SettingKey.FORWARDING,
  SettingKey.DELAY_SLOT,
  SettingKey.STEP_STRIDE,
  SettingKey.EXECUTION_DELAY_MS,
  SettingKey.CACHE_L1D,
  SettingKey.CACHE_L1I,
];

/**
 * Reset every setting shown in the Settings dialog to its schema default.
 * Mirrors the Swing UI's "Reset to defaults" button (`GUIConfig.java`),
 * including resetting settings whose `useSetting` hook lives in a different
 * component (e.g. cache configuration) — see `resetStoredValue`.
 */
export function resetAllDialogSettings(): void {
  for (const key of SETTINGS_DIALOG_KEYS) {
    resetStoredValue(key, getSchema(key).default);
  }
}
