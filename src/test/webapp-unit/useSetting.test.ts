// @vitest-environment jsdom
/**
 * Unit tests for settings/useSetting.ts
 *
 * Tests cover:
 *   - returns the schema default when nothing is persisted
 *   - direct setValue works
 *   - functional-updater form of setValue receives the sanitized previous value
 *   - reset() restores the default
 *   - multi-instance sync (same key, two hook instances)
 */

import { describe, it, expect, beforeEach, afterEach } from 'vitest';
import { renderHook, act } from '@testing-library/react';
import {
  useSetting,
  resetUiSettings,
  resetSimulationSettings,
} from '../../webapp/settings/useSetting';
import { SettingKey } from '../../webapp/settings/SettingKey';
import {
  getSchema,
  DEFAULT_PIPELINE_COLORS,
} from '../../webapp/settings/schema';

beforeEach(() => window.localStorage.clear());
afterEach(() => window.localStorage.clear());

// ---------------------------------------------------------------------------
// Returns the schema default
// ---------------------------------------------------------------------------

describe('useSetting — initial value', () => {
  it('returns the schema default for VI_MODE (boolean false)', () => {
    const { result } = renderHook(() => useSetting(SettingKey.VI_MODE));
    const [value] = result.current;
    expect(value).toBe(false);
  });

  it('returns the schema default for FONT_SIZE (number 14)', () => {
    const { result } = renderHook(() => useSetting(SettingKey.FONT_SIZE));
    const [value] = result.current;
    expect(value).toBe(14);
  });

  it('returns the schema default for THEME_MODE (string "auto")', () => {
    const { result } = renderHook(() => useSetting(SettingKey.THEME_MODE));
    const [value] = result.current;
    expect(value).toBe('auto');
  });

  it('returns the schema default for STEP_STRIDE (number 500)', () => {
    const { result } = renderHook(() => useSetting(SettingKey.STEP_STRIDE));
    const [value] = result.current;
    expect(value).toBe(getSchema(SettingKey.STEP_STRIDE).default);
  });

  it('returns the schema default for PIPELINE_COLORS (full default object)', () => {
    const { result } = renderHook(() => useSetting(SettingKey.PIPELINE_COLORS));
    const [value] = result.current;
    expect(value).toEqual(DEFAULT_PIPELINE_COLORS);
  });
});

// ---------------------------------------------------------------------------
// setValue — direct value form
// ---------------------------------------------------------------------------

describe('useSetting — setValue (direct)', () => {
  it('updates the value when a new value is set', () => {
    const { result } = renderHook(() => useSetting(SettingKey.VI_MODE));
    act(() => {
      const [, setValue] = result.current;
      setValue(true);
    });
    const [value] = result.current;
    expect(value).toBe(true);
  });

  it('persists the value to localStorage under the correct key', () => {
    const { result } = renderHook(() => useSetting(SettingKey.FONT_SIZE));
    act(() => {
      const [, setValue] = result.current;
      setValue(20);
    });
    const stored = window.localStorage.getItem(
      `edumips64:v1:${SettingKey.FONT_SIZE}`,
    );
    expect(stored).toBe('20');
  });

  it('applies the shallow merge when setting an object value', () => {
    const { result } = renderHook(() => useSetting(SettingKey.PIPELINE_COLORS));
    // Set a partial object (just one key override).
    const partialColors = { ...DEFAULT_PIPELINE_COLORS, IF: '#112233' };
    act(() => {
      const [, setValue] = result.current;
      setValue(partialColors);
    });
    const [value] = result.current;
    expect((value as typeof DEFAULT_PIPELINE_COLORS).IF).toBe('#112233');
    // Other keys come from either the stored value or the default.
    expect((value as typeof DEFAULT_PIPELINE_COLORS).ID).toBe(
      DEFAULT_PIPELINE_COLORS.ID,
    );
  });
});

// ---------------------------------------------------------------------------
// setValue — functional updater form
// ---------------------------------------------------------------------------

describe('useSetting — setValue (functional updater)', () => {
  it('receives the current value as the argument to the updater', () => {
    const { result } = renderHook(() => useSetting(SettingKey.FONT_SIZE));

    // Set initial persisted value.
    act(() => {
      const [, setValue] = result.current;
      setValue(18);
    });
    expect(result.current[0]).toBe(18);

    // Use functional updater.
    act(() => {
      const [, setValue] = result.current;
      setValue((prev) => (prev as number) + 2);
    });
    expect(result.current[0]).toBe(20);
  });

  it('toggles a boolean setting via functional updater', () => {
    const { result } = renderHook(() => useSetting(SettingKey.VI_MODE));
    act(() => {
      result.current[1]((prev) => !prev);
    });
    expect(result.current[0]).toBe(true);

    act(() => {
      result.current[1]((prev) => !prev);
    });
    expect(result.current[0]).toBe(false);
  });

  it('functional updater receives sanitized prev (not raw stored value)', () => {
    // Pre-populate localStorage with an invalid value for STEP_STRIDE.
    window.localStorage.setItem(
      `edumips64:v1:${SettingKey.STEP_STRIDE}`,
      JSON.stringify(-999), // invalid — below MIN_STEP_STRIDE
    );

    const { result } = renderHook(() => useSetting(SettingKey.STEP_STRIDE));

    let receivedPrev: number | undefined;
    act(() => {
      result.current[1]((prev) => {
        receivedPrev = prev as number;
        return prev as number;
      });
    });

    // sanitize() should have replaced -999 with the default (500).
    expect(receivedPrev).toBe(getSchema(SettingKey.STEP_STRIDE).default);
  });
});

// ---------------------------------------------------------------------------
// reset
// ---------------------------------------------------------------------------

describe('useSetting — reset', () => {
  it('restores the default after a value has been set', () => {
    const { result } = renderHook(() => useSetting(SettingKey.VI_MODE));

    act(() => {
      result.current[1](true);
    });
    expect(result.current[0]).toBe(true);

    act(() => {
      result.current[2](); // reset
    });
    expect(result.current[0]).toBe(false); // back to default
  });

  it('writes the schema default to localStorage on reset', () => {
    const { result } = renderHook(() => useSetting(SettingKey.THEME_MODE));

    act(() => {
      result.current[1]('dark');
    });
    expect(
      window.localStorage.getItem(`edumips64:v1:${SettingKey.THEME_MODE}`),
    ).toBe('"dark"');

    act(() => {
      result.current[2]();
    });
    // reset() calls setValue(default), writing the default back to localStorage.
    const storedAfterReset = window.localStorage.getItem(
      `edumips64:v1:${SettingKey.THEME_MODE}`,
    );
    expect(storedAfterReset).toBe(
      JSON.stringify(getSchema(SettingKey.THEME_MODE).default),
    );
  });

  it('resetting an already-default value is a no-op', () => {
    const { result } = renderHook(() => useSetting(SettingKey.FORWARDING));
    act(() => {
      result.current[2]();
    });
    expect(result.current[0]).toBe(false); // default
  });
});

// ---------------------------------------------------------------------------
// Multi-instance sync
// ---------------------------------------------------------------------------

describe('useSetting — multi-instance sync', () => {
  it('two instances for the same key stay in sync', () => {
    const hook1 = renderHook(() => useSetting(SettingKey.FONT_SIZE));
    const hook2 = renderHook(() => useSetting(SettingKey.FONT_SIZE));

    act(() => {
      hook1.result.current[1](22);
    });

    // hook2 must reflect the change without any direct interaction.
    expect(hook2.result.current[0]).toBe(22);
  });

  it('two instances for different keys are independent', () => {
    const hook1 = renderHook(() => useSetting(SettingKey.VI_MODE));
    const hook2 = renderHook(() => useSetting(SettingKey.FORWARDING));

    act(() => {
      hook1.result.current[1](true);
    });

    // FORWARDING should be unaffected.
    expect(hook2.result.current[0]).toBe(false);
  });
});

// ---------------------------------------------------------------------------
// resetUiSettings / resetSimulationSettings — the Settings dialog's two
// "Reset to defaults" buttons
// ---------------------------------------------------------------------------

describe('resetUiSettings', () => {
  it('resets UI and execution settings: vi mode, pipeline colors, step stride', () => {
    const viMode = renderHook(() => useSetting(SettingKey.VI_MODE));
    const stepStride = renderHook(() => useSetting(SettingKey.STEP_STRIDE));
    const pipelineColors = renderHook(() =>
      useSetting(SettingKey.PIPELINE_COLORS),
    );

    act(() => {
      viMode.result.current[1](true);
      stepStride.result.current[1](999);
      pipelineColors.result.current[1]({
        ...DEFAULT_PIPELINE_COLORS,
        IF: '#000000',
      });
    });

    act(() => {
      resetUiSettings();
    });

    expect(viMode.result.current[0]).toBe(false);
    expect(stepStride.result.current[0]).toBe(
      getSchema(SettingKey.STEP_STRIDE).default,
    );
    expect(pipelineColors.result.current[0]).toEqual(DEFAULT_PIPELINE_COLORS);
  });

  it('does not touch Simulation-tab settings (forwarding, delay slot, cache)', () => {
    const forwarding = renderHook(() => useSetting(SettingKey.FORWARDING));
    const delaySlot = renderHook(() => useSetting(SettingKey.DELAY_SLOT));

    act(() => {
      forwarding.result.current[1](true);
      delaySlot.result.current[1](true);
    });

    act(() => {
      resetUiSettings();
    });

    expect(forwarding.result.current[0]).toBe(true);
    expect(delaySlot.result.current[0]).toBe(true);
  });

  it('does not touch settings outside the dialog (editor code, expanded accordions, help language)', () => {
    const editorCode = renderHook(() => useSetting(SettingKey.EDITOR_CODE));
    const helpLanguage = renderHook(() => useSetting(SettingKey.HELP_LANGUAGE));

    act(() => {
      editorCode.result.current[1]('.code\nSYSCALL 0\n');
      helpLanguage.result.current[1]('it');
    });

    act(() => {
      resetUiSettings();
    });

    // Untouched — a "reset settings" action must never discard the user's
    // in-progress program or their Help-dialog language preference.
    expect(editorCode.result.current[0]).toBe('.code\nSYSCALL 0\n');
    expect(helpLanguage.result.current[0]).toBe('it');
  });
});

describe('resetSimulationSettings', () => {
  it('resets a setting to its default even though no hook instance for it is mounted', () => {
    // Write a non-default value directly, without mounting a hook — this is
    // the situation for cache config, whose useSetting calls live inside
    // CacheConfig.tsx, not in the component that renders the reset button.
    window.localStorage.setItem(
      `edumips64:v1:${SettingKey.CACHE_L1D}`,
      JSON.stringify({ size: 4096, blockSize: 32, associativity: 2 }),
    );

    act(() => {
      resetSimulationSettings();
    });

    const { result } = renderHook(() => useSetting(SettingKey.CACHE_L1D));
    expect(result.current[0]).toEqual(getSchema(SettingKey.CACHE_L1D).default);
  });

  it('updates an already-mounted hook instance immediately (same-tab notification)', () => {
    const { result } = renderHook(() => useSetting(SettingKey.FORWARDING));
    act(() => {
      result.current[1](true);
    });
    expect(result.current[0]).toBe(true);

    act(() => {
      resetSimulationSettings();
    });

    expect(result.current[0]).toBe(false);
  });

  it('resets both CPU settings and both cache configs', () => {
    const forwarding = renderHook(() => useSetting(SettingKey.FORWARDING));
    const delaySlot = renderHook(() => useSetting(SettingKey.DELAY_SLOT));
    const l1d = renderHook(() => useSetting(SettingKey.CACHE_L1D));
    const l1i = renderHook(() => useSetting(SettingKey.CACHE_L1I));

    act(() => {
      forwarding.result.current[1](true);
      delaySlot.result.current[1](true);
      l1d.result.current[1]({ size: 4096, blockSize: 32, associativity: 2 });
      l1i.result.current[1]({ size: 4096, blockSize: 32, associativity: 2 });
    });

    act(() => {
      resetSimulationSettings();
    });

    expect(forwarding.result.current[0]).toBe(false);
    expect(delaySlot.result.current[0]).toBe(false);
    expect(l1d.result.current[0]).toEqual(
      getSchema(SettingKey.CACHE_L1D).default,
    );
    expect(l1i.result.current[0]).toEqual(
      getSchema(SettingKey.CACHE_L1I).default,
    );
  });

  it('does not touch UI-tab settings (vi mode, pipeline colors, step stride)', () => {
    const viMode = renderHook(() => useSetting(SettingKey.VI_MODE));
    const stepStride = renderHook(() => useSetting(SettingKey.STEP_STRIDE));

    act(() => {
      viMode.result.current[1](true);
      stepStride.result.current[1](999);
    });

    act(() => {
      resetSimulationSettings();
    });

    expect(viMode.result.current[0]).toBe(true);
    expect(stepStride.result.current[0]).toBe(999);
  });
});
