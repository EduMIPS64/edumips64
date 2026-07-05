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

function resetSettings(keys: SettingKeyType[]): void {
  for (const key of keys) {
    resetStoredValue(key, getSchema(key).default);
  }
}

/**
 * Settings shown on the Settings dialog's UI tab: editor/appearance and
 * execution-pacing knobs. None of these affect the CPU or worker state, so
 * resetting them is always safe, even while a program is running.
 */
const UI_SETTINGS_KEYS: SettingKeyType[] = [
  SettingKey.VI_MODE,
  SettingKey.FONT_SIZE,
  SettingKey.ACCORDION_ALERTS,
  SettingKey.THEME_MODE,
  SettingKey.PIPELINE_COLORS,
  SettingKey.STEP_STRIDE,
  SettingKey.EXECUTION_DELAY_MS,
];

/**
 * Settings shown on the Settings dialog's Simulation tab: CPU behavior and
 * cache configuration. Applying these defaults resets the CPU (forwarding /
 * delay slot changes reset the pipeline; cache changes reset the CPU via
 * the worker's `setCacheConfig`), so callers should only invoke this while
 * not RUNNING — the Simulation tab itself is disabled in that state.
 */
const SIMULATION_SETTINGS_KEYS: SettingKeyType[] = [
  SettingKey.FORWARDING,
  SettingKey.DELAY_SLOT,
  SettingKey.CACHE_L1D,
  SettingKey.CACHE_L1I,
];

/**
 * Reset the UI-tab settings to their schema defaults. Mirrors (a scoped
 * subset of) the Swing UI's "Reset to defaults" button (`GUIConfig.java`),
 * including resetting settings whose `useSetting` hook lives in a different
 * component — see `resetStoredValue`.
 */
export function resetUiSettings(): void {
  resetSettings(UI_SETTINGS_KEYS);
}

/**
 * Reset the Simulation-tab settings (CPU + cache) to their schema defaults.
 * Callers must not invoke this while a program is running (see above).
 */
export function resetSimulationSettings(): void {
  resetSettings(SIMULATION_SETTINGS_KEYS);
}
