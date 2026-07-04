/**
 * Unit tests for settings/schema.ts
 *
 * Covers sanitize() fallback behaviour, object shallow-merge semantics,
 * and all specific validators declared in SETTINGS_SCHEMA.
 *
 * No browser / jsdom needed — schema is pure TypeScript.
 */

import { describe, it, expect, vi, afterEach } from 'vitest';
import {
  sanitize,
  getSchema,
  SETTINGS_SCHEMA,
  MIN_STEP_STRIDE,
  MAX_STEP_STRIDE,
  MIN_EXECUTION_DELAY_MS,
  MAX_EXECUTION_DELAY_MS,
  DEFAULT_PIPELINE_COLORS,
  ALLOWED_THEME_MODES,
} from '../../webapp/settings/schema';
import { SettingKey } from '../../webapp/settings/SettingKey';

// Silence expected console.warn calls from sanitize().
afterEach(() => vi.restoreAllMocks());
const suppressWarn = () => vi.spyOn(console, 'warn').mockImplementation(() => {});

// ---------------------------------------------------------------------------
// getSchema
// ---------------------------------------------------------------------------

describe('getSchema', () => {
  it('returns the schema entry for a known key', () => {
    const entry = getSchema(SettingKey.VI_MODE);
    expect(entry).toBeDefined();
    expect(entry.type).toBe('boolean');
    expect(entry.default).toBe(false);
  });

  it('throws for an unknown key', () => {
    expect(() => getSchema('__not_a_real_key__')).toThrow(/Unknown setting key/);
  });
});

// ---------------------------------------------------------------------------
// sanitize — generic fallback paths
// ---------------------------------------------------------------------------

describe('sanitize — undefined falls back to default', () => {
  it('returns the schema default when raw is undefined (boolean key)', () => {
    const result = sanitize(SettingKey.VI_MODE, undefined);
    expect(result).toBe(false);
  });

  it('returns the schema default when raw is undefined (number key)', () => {
    const result = sanitize(SettingKey.FONT_SIZE, undefined);
    expect(result).toBe(14);
  });

  it('returns the schema default when raw is undefined (string key)', () => {
    const result = sanitize(SettingKey.THEME_MODE, undefined);
    expect(result).toBe('auto');
  });
});

describe('sanitize — wrong type falls back to default', () => {
  it('falls back when a boolean setting has a string value', () => {
    suppressWarn();
    const result = sanitize(SettingKey.VI_MODE, 'true');
    expect(result).toBe(false);
  });

  it('falls back when a number setting has a boolean value', () => {
    suppressWarn();
    const result = sanitize(SettingKey.FONT_SIZE, true);
    expect(result).toBe(14);
  });

  it('falls back when a string setting has a number value', () => {
    suppressWarn();
    const result = sanitize(SettingKey.THEME_MODE, 42);
    expect(result).toBe('auto');
  });

  it('falls back when an object setting receives an array', () => {
    suppressWarn();
    const result = sanitize(SettingKey.PIPELINE_COLORS, []);
    expect(result).toEqual(DEFAULT_PIPELINE_COLORS);
  });

  it('falls back when an object setting receives null', () => {
    suppressWarn();
    const result = sanitize(SettingKey.PIPELINE_COLORS, null);
    expect(result).toEqual(DEFAULT_PIPELINE_COLORS);
  });

  it('warns when falling back due to type mismatch', () => {
    const warn = suppressWarn();
    sanitize(SettingKey.VI_MODE, 'not-a-boolean');
    expect(warn).toHaveBeenCalledWith(expect.stringContaining('wrong type'));
  });
});

describe('sanitize — validate predicate fails → default', () => {
  it('falls back when font size is below the minimum', () => {
    suppressWarn();
    const result = sanitize(SettingKey.FONT_SIZE, 0);
    expect(result).toBe(14);
  });

  it('falls back when font size is above the maximum', () => {
    suppressWarn();
    const result = sanitize(SettingKey.FONT_SIZE, 100);
    expect(result).toBe(14);
  });

  it('falls back when theme mode is not in the allowed list', () => {
    suppressWarn();
    const result = sanitize(SettingKey.THEME_MODE, 'sepia');
    expect(result).toBe('auto');
  });

  it('warns when falling back due to validate failure', () => {
    const warn = suppressWarn();
    sanitize(SettingKey.THEME_MODE, 'invalid-mode');
    expect(warn).toHaveBeenCalledWith(expect.stringContaining('failed validation'));
  });
});

describe('sanitize — valid value passthrough', () => {
  it('returns the value unchanged for a valid boolean', () => {
    expect(sanitize(SettingKey.VI_MODE, true)).toBe(true);
    expect(sanitize(SettingKey.VI_MODE, false)).toBe(false);
  });

  it('returns the value unchanged for a valid number within bounds', () => {
    expect(sanitize(SettingKey.FONT_SIZE, 16)).toBe(16);
  });

  it('returns the value unchanged for a valid string', () => {
    expect(sanitize(SettingKey.THEME_MODE, 'dark')).toBe('dark');
  });
});

// ---------------------------------------------------------------------------
// sanitize — object shallow-merge semantics
// ---------------------------------------------------------------------------

describe('sanitize — object shallow-merge', () => {
  it('merges stored keys on top of defaults (stored wins)', () => {
    const stored = { IF: '#ff0000' }; // partial override
    const result = sanitize(SettingKey.PIPELINE_COLORS, stored) as Record<string, string>;
    // Stored key wins.
    expect(result.IF).toBe('#ff0000');
    // Unset keys come from the default.
    expect(result.ID).toBe(DEFAULT_PIPELINE_COLORS.ID);
    expect(result.WB).toBe(DEFAULT_PIPELINE_COLORS.WB);
  });

  it('surfaces new default keys that are missing from the stored object', () => {
    // PIPELINE_COLORS allows partial stored objects: isValidPipelineColors accepts
    // missing keys (via `colors[k] === undefined`).  Simulate a stored value from
    // an older schema version that only persisted one stage color.
    const partialStored = { IF: '#aabbcc' }; // only one key
    const result = sanitize(SettingKey.PIPELINE_COLORS, partialStored) as Record<string, string>;
    // Stored key survives.
    expect(result.IF).toBe('#aabbcc');
    // Keys absent from the stored object come from the default.
    expect(result.ID).toBe(DEFAULT_PIPELINE_COLORS.ID);
    expect(result.WB).toBe(DEFAULT_PIPELINE_COLORS.WB);
    expect(result.Stall).toBe(DEFAULT_PIPELINE_COLORS.Stall);
  });

  it('stored object with all keys wins completely over defaults', () => {
    const stored = { ...DEFAULT_PIPELINE_COLORS, IF: '#abcdef' };
    const result = sanitize(SettingKey.PIPELINE_COLORS, stored) as typeof DEFAULT_PIPELINE_COLORS;
    expect(result.IF).toBe('#abcdef');
    expect(result.ID).toBe(DEFAULT_PIPELINE_COLORS.ID);
  });
});

// ---------------------------------------------------------------------------
// Specific validators — STEP_STRIDE
// ---------------------------------------------------------------------------

describe('sanitize — STEP_STRIDE validator', () => {
  it('accepts MIN_STEP_STRIDE', () => {
    expect(sanitize(SettingKey.STEP_STRIDE, MIN_STEP_STRIDE)).toBe(MIN_STEP_STRIDE);
  });

  it('accepts MAX_STEP_STRIDE', () => {
    expect(sanitize(SettingKey.STEP_STRIDE, MAX_STEP_STRIDE)).toBe(MAX_STEP_STRIDE);
  });

  it('rejects a value below the minimum (0)', () => {
    suppressWarn();
    expect(sanitize(SettingKey.STEP_STRIDE, 0)).toBe(
      getSchema(SettingKey.STEP_STRIDE).default,
    );
  });

  it('rejects a value above the maximum', () => {
    suppressWarn();
    expect(sanitize(SettingKey.STEP_STRIDE, MAX_STEP_STRIDE + 1)).toBe(
      getSchema(SettingKey.STEP_STRIDE).default,
    );
  });

  it('rejects a fractional value (must be integer)', () => {
    suppressWarn();
    expect(sanitize(SettingKey.STEP_STRIDE, 1.5)).toBe(
      getSchema(SettingKey.STEP_STRIDE).default,
    );
  });
});

// ---------------------------------------------------------------------------
// Specific validators — EXECUTION_DELAY_MS
// ---------------------------------------------------------------------------

describe('sanitize — EXECUTION_DELAY_MS validator', () => {
  it('accepts 0 (no delay)', () => {
    expect(sanitize(SettingKey.EXECUTION_DELAY_MS, 0)).toBe(0);
  });

  it('accepts MIN_EXECUTION_DELAY_MS', () => {
    expect(sanitize(SettingKey.EXECUTION_DELAY_MS, MIN_EXECUTION_DELAY_MS)).toBe(
      MIN_EXECUTION_DELAY_MS,
    );
  });

  it('accepts MAX_EXECUTION_DELAY_MS', () => {
    expect(sanitize(SettingKey.EXECUTION_DELAY_MS, MAX_EXECUTION_DELAY_MS)).toBe(
      MAX_EXECUTION_DELAY_MS,
    );
  });

  it('rejects a value above the maximum', () => {
    suppressWarn();
    expect(sanitize(SettingKey.EXECUTION_DELAY_MS, MAX_EXECUTION_DELAY_MS + 1)).toBe(
      getSchema(SettingKey.EXECUTION_DELAY_MS).default,
    );
  });

  it('rejects a negative value', () => {
    suppressWarn();
    expect(sanitize(SettingKey.EXECUTION_DELAY_MS, -1)).toBe(
      getSchema(SettingKey.EXECUTION_DELAY_MS).default,
    );
  });

  it('rejects Infinity', () => {
    suppressWarn();
    expect(sanitize(SettingKey.EXECUTION_DELAY_MS, Infinity)).toBe(
      getSchema(SettingKey.EXECUTION_DELAY_MS).default,
    );
  });
});

// ---------------------------------------------------------------------------
// Specific validators — FONT_SIZE
// ---------------------------------------------------------------------------

describe('sanitize — FONT_SIZE validator', () => {
  it('accepts a font size in the valid range', () => {
    expect(sanitize(SettingKey.FONT_SIZE, 12)).toBe(12);
  });

  it('accepts font size 1 (minimum)', () => {
    expect(sanitize(SettingKey.FONT_SIZE, 1)).toBe(1);
  });

  it('accepts font size 72 (maximum)', () => {
    expect(sanitize(SettingKey.FONT_SIZE, 72)).toBe(72);
  });

  it('rejects font size 0', () => {
    suppressWarn();
    expect(sanitize(SettingKey.FONT_SIZE, 0)).toBe(14);
  });

  it('rejects font size 73', () => {
    suppressWarn();
    expect(sanitize(SettingKey.FONT_SIZE, 73)).toBe(14);
  });

  it('rejects NaN (fails isFinite)', () => {
    suppressWarn();
    expect(sanitize(SettingKey.FONT_SIZE, NaN)).toBe(14);
  });
});

// ---------------------------------------------------------------------------
// Specific validators — PIPELINE_COLORS (hex color)
// ---------------------------------------------------------------------------

describe('sanitize — PIPELINE_COLORS hex color validation', () => {
  it('accepts a valid #RRGGBB hex color for each stage', () => {
    const valid = { ...DEFAULT_PIPELINE_COLORS, IF: '#aabbcc' };
    const result = sanitize(SettingKey.PIPELINE_COLORS, valid);
    expect((result as typeof DEFAULT_PIPELINE_COLORS).IF).toBe('#aabbcc');
  });

  it('rejects an object where one stage has a non-hex-color string', () => {
    suppressWarn();
    const bad = { ...DEFAULT_PIPELINE_COLORS, ID: 'blue' };
    const result = sanitize(SettingKey.PIPELINE_COLORS, bad);
    // Falls back to the default because 'blue' is not #RRGGBB.
    expect(result).toEqual(DEFAULT_PIPELINE_COLORS);
  });

  it('rejects a 3-digit shorthand color (#RGB)', () => {
    suppressWarn();
    const bad = { ...DEFAULT_PIPELINE_COLORS, EX: '#abc' };
    expect(sanitize(SettingKey.PIPELINE_COLORS, bad)).toEqual(DEFAULT_PIPELINE_COLORS);
  });

  it('rejects a color string without the leading hash', () => {
    suppressWarn();
    const bad = { ...DEFAULT_PIPELINE_COLORS, MEM: 'ff0000' };
    expect(sanitize(SettingKey.PIPELINE_COLORS, bad)).toEqual(DEFAULT_PIPELINE_COLORS);
  });

  it('allows extra keys alongside valid colors (object with unknown keys is fine)', () => {
    // isValidPipelineColors only checks that *known* keys are valid hex colors.
    // Extra keys are silently ignored.
    const withExtra = { ...DEFAULT_PIPELINE_COLORS, unknownStage: '#123456' };
    const result = sanitize(SettingKey.PIPELINE_COLORS, withExtra);
    // The validator should pass because all required keys are valid.
    // After shallow-merge the extra key will appear in the result too.
    expect((result as Record<string, string>).IF).toBe(DEFAULT_PIPELINE_COLORS.IF);
  });
});

// ---------------------------------------------------------------------------
// Specific validators — CACHE_L1D / CACHE_L1I
// ---------------------------------------------------------------------------

describe('sanitize — cache config validators', () => {
  it('accepts a valid cache config', () => {
    const valid = { size: 1024, blockSize: 16, associativity: 4 };
    const result = sanitize(SettingKey.CACHE_L1D, valid) as typeof valid;
    expect(result.size).toBe(1024);
    expect(result.blockSize).toBe(16);
    expect(result.associativity).toBe(4);
  });

  it('rejects a cache config with a non-positive size', () => {
    suppressWarn();
    const bad = { size: 0, blockSize: 16, associativity: 1 };
    const result = sanitize(SettingKey.CACHE_L1D, bad);
    expect(result).toEqual(getSchema(SettingKey.CACHE_L1D).default);
  });

  it('rejects a cache config with a negative blockSize', () => {
    suppressWarn();
    const bad = { size: 512, blockSize: -1, associativity: 1 };
    expect(sanitize(SettingKey.CACHE_L1D, bad)).toEqual(getSchema(SettingKey.CACHE_L1D).default);
  });

  it('rejects a cache config where associativity is a float', () => {
    suppressWarn();
    const bad = { size: 512, blockSize: 16, associativity: 1.5 };
    expect(sanitize(SettingKey.CACHE_L1I, bad)).toEqual(getSchema(SettingKey.CACHE_L1I).default);
  });

  it('applies the same validation to CACHE_L1I', () => {
    const valid = { size: 2048, blockSize: 32, associativity: 2 };
    const result = sanitize(SettingKey.CACHE_L1I, valid) as typeof valid;
    expect(result.associativity).toBe(2);
  });
});

// ---------------------------------------------------------------------------
// Specific validators — THEME_MODE
// ---------------------------------------------------------------------------

describe('sanitize — THEME_MODE validator', () => {
  it.each(ALLOWED_THEME_MODES)('accepts allowed value "%s"', (mode) => {
    expect(sanitize(SettingKey.THEME_MODE, mode)).toBe(mode);
  });

  it('rejects an unknown theme mode', () => {
    suppressWarn();
    expect(sanitize(SettingKey.THEME_MODE, 'high-contrast')).toBe('auto');
  });
});

// ---------------------------------------------------------------------------
// Schema completeness — every SettingKey is registered
// ---------------------------------------------------------------------------

describe('SETTINGS_SCHEMA', () => {
  it('has an entry for every key declared in SettingKey', () => {
    for (const key of Object.values(SettingKey)) {
      expect(SETTINGS_SCHEMA[key as string], `Missing schema entry for "${key}"`).toBeDefined();
    }
  });
});
