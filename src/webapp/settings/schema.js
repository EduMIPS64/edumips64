import { SettingKey } from './SettingKey';

/**
 * Supported setting types.
 *
 * - `boolean`, `number`, `string`: match the corresponding JS `typeof`.
 * - `object`: a plain JSON object (non-null, non-array). For objects, the
 *   persisted value is *shallow-merged* on top of the default on read, so
 *   that fields added to the default in a later release are transparently
 *   populated for existing users without losing their customizations.
 */
export const SettingType = Object.freeze({
  BOOLEAN: 'boolean',
  NUMBER: 'number',
  STRING: 'string',
  OBJECT: 'object',
});

const ALLOWED_HELP_LANGUAGES = ['en', 'it', 'zh'];

// Font size bounds for the code editor, in pt.
const MIN_FONT_SIZE = 1;
const MAX_FONT_SIZE = 72;

// Multi-step stride bounds (number of simulation steps the "Multi Step"
// button runs in one click). Mirrors the `<input min/max>` that used to live
// in the Header, but enforced centrally here so the persisted value is
// always in range.
const MIN_STEP_STRIDE = 1;
const MAX_STEP_STRIDE = 100000;

// Execution delay bounds, in milliseconds. `0` means "run as fast as
// possible" (the previous behavior). The upper bound is generous enough to
// demonstrate pipeline effects without being able to accidentally lock the
// UI into a multi-minute sleep between tiny batches.
const MIN_EXECUTION_DELAY_MS = 0;
const MAX_EXECUTION_DELAY_MS = 5000;

// Shared validator for the L1D / L1I cache configuration objects.
const isValidCacheConfig = (v) =>
  Number.isFinite(v.size) &&
  Number.isFinite(v.blockSize) &&
  Number.isFinite(v.associativity);

/**
 * The single, canonical registry of all locally-persisted settings.
 *
 * Each entry describes:
 *   - `type`:     one of `SettingType.*`. Used to validate the stored value.
 *   - `default`:  the value returned when nothing is persisted, or when the
 *                 persisted value is missing / has the wrong type / fails
 *                 `validate`. This is the *documented* default.
 *   - `validate`: (optional) extra predicate called after the type check. If
 *                 it returns `false`, the stored value is discarded and the
 *                 default is used instead (a warning is logged).
 *
 * This mirrors the `defaults` map and getter signatures in the Java
 * `ConfigStore`: everything a user can tweak has exactly one row here, with
 * its type and its default value.
 */
export const SETTINGS_SCHEMA = Object.freeze({
  [SettingKey.VI_MODE]: {
    type: SettingType.BOOLEAN,
    default: false,
  },
  [SettingKey.FONT_SIZE]: {
    type: SettingType.NUMBER,
    default: 14,
    validate: (v) =>
      Number.isFinite(v) && v >= MIN_FONT_SIZE && v <= MAX_FONT_SIZE,
  },
  [SettingKey.ACCORDION_ALERTS]: {
    type: SettingType.BOOLEAN,
    default: true,
  },
  [SettingKey.EXPANDED_ACCORDIONS]: {
    type: SettingType.OBJECT,
    default: {
      stats: true,
      pipeline: false,
      registers: false,
      memory: false,
      stdout: false,
      cache: true,
      settings: true,
    },
  },
  [SettingKey.CACHE_L1D]: {
    type: SettingType.OBJECT,
    default: {
      size: 1024,
      blockSize: 16,
      associativity: 1,
    },
    validate: isValidCacheConfig,
  },
  [SettingKey.CACHE_L1I]: {
    type: SettingType.OBJECT,
    default: {
      size: 1024,
      blockSize: 16,
      associativity: 1,
    },
    validate: isValidCacheConfig,
  },
  [SettingKey.HELP_LANGUAGE]: {
    type: SettingType.STRING,
    default: 'en',
    validate: (v) => ALLOWED_HELP_LANGUAGES.includes(v),
  },
  [SettingKey.FORWARDING]: {
    type: SettingType.BOOLEAN,
    // Matches the Java `ConfigStore` default for `ConfigKey.FORWARDING`.
    default: false,
  },
  [SettingKey.STEP_STRIDE]: {
    type: SettingType.NUMBER,
    // Matches the legacy `multiStepCount` default that used to live in
    // `Header.js` local state.
    default: 500,
    validate: (v) =>
      Number.isInteger(v) && v >= MIN_STEP_STRIDE && v <= MAX_STEP_STRIDE,
  },
  [SettingKey.EXECUTION_DELAY_MS]: {
    type: SettingType.NUMBER,
    // 0 means "no artificial delay between batches", which is the pre-existing
    // behavior.
    default: 0,
    validate: (v) =>
      Number.isFinite(v) &&
      v >= MIN_EXECUTION_DELAY_MS &&
      v <= MAX_EXECUTION_DELAY_MS,
  },
});

/**
 * Return true if `value` matches the declared `type`.
 */
function matchesType(value, type) {
  switch (type) {
    case SettingType.BOOLEAN:
      return typeof value === 'boolean';
    case SettingType.NUMBER:
      return typeof value === 'number' && Number.isFinite(value);
    case SettingType.STRING:
      return typeof value === 'string';
    case SettingType.OBJECT:
      return (
        typeof value === 'object' && value !== null && !Array.isArray(value)
      );
    default:
      return false;
  }
}

/**
 * Look up the schema entry for `key`, or throw if `key` is not registered.
 * Using an unknown key is always a programmer error, never user data.
 */
export function getSchema(key) {
  const entry = SETTINGS_SCHEMA[key];
  if (!entry) {
    throw new Error(`Unknown setting key: "${key}"`);
  }
  return entry;
}

/**
 * Validate a raw value against the schema entry for `key`.
 *
 * Returns the sanitized value to use:
 *  - the default if `raw` is `undefined` or fails the type/validate checks;
 *  - for `object` settings, a shallow merge of the default with the raw
 *    object, so keys added to the default in later versions are populated;
 *  - otherwise `raw` itself.
 *
 * Never throws: invalid data is logged and silently replaced with the default,
 * so persistence can't break the simulator.
 */
export function sanitize(key, raw) {
  const { type, default: defaultValue, validate } = getSchema(key);

  if (raw === undefined) {
    return defaultValue;
  }

  if (!matchesType(raw, type)) {
    console.warn(
      `settings: stored value for "${key}" has wrong type (expected ${type}), ` +
        'falling back to default.',
    );
    return defaultValue;
  }

  if (validate && !validate(raw)) {
    console.warn(
      `settings: stored value for "${key}" failed validation, ` +
        'falling back to default.',
    );
    return defaultValue;
  }

  if (type === SettingType.OBJECT) {
    // Shallow-merge default under stored value so new default keys surface.
    return { ...defaultValue, ...raw };
  }

  return raw;
}
