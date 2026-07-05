import React from 'react';
import Box from '@mui/material/Box';
import Typography from '@mui/material/Typography';
import Switch from '@mui/material/Switch';
import Button from '@mui/material/Button';
import ButtonGroup from '@mui/material/ButtonGroup';
import ToggleButton from '@mui/material/ToggleButton';
import ToggleButtonGroup from '@mui/material/ToggleButtonGroup';
import FormControlLabel from '@mui/material/FormControlLabel';
import {
  DEFAULT_PIPELINE_COLORS,
  ALLOWED_THEME_MODES,
  type PipelineColors,
  type ThemeMode,
} from '../../settings/schema';

// Human-readable labels for each pipeline color knob, in display order.
const PIPELINE_COLOR_FIELDS: Array<{
  key: keyof PipelineColors;
  label: string;
}> = [
  { key: 'IF', label: 'IF' },
  { key: 'ID', label: 'ID' },
  { key: 'EX', label: 'EX' },
  { key: 'MEM', label: 'MEM' },
  { key: 'WB', label: 'WB' },
  { key: 'FPAdder', label: 'FP Adder' },
  { key: 'FPMultiplier', label: 'FP Multiplier' },
  { key: 'FPDivider', label: 'FP Divider' },
  { key: 'Stall', label: 'Stall' },
];

export interface UiSettingsPanelProps {
  viMode: boolean;
  setViMode: (v: boolean) => void;
  fontSize: number;
  setFontSize: (v: number) => void;
  accordionAlerts: boolean;
  setAccordionAlerts: (v: boolean) => void;
  pipelineColors: PipelineColors | undefined;
  setPipelineColors: (v: PipelineColors) => void;
  themeMode: ThemeMode;
  setThemeMode: (v: ThemeMode) => void;
}

/**
 * Editor and appearance settings: things that change how the UI looks and
 * feels, but not how the simulated CPU behaves.
 */
const UiSettingsPanel = ({
  viMode,
  setViMode,
  fontSize,
  setFontSize,
  accordionAlerts,
  setAccordionAlerts,
  pipelineColors,
  setPipelineColors,
  themeMode,
  setThemeMode,
}: UiSettingsPanelProps) => {
  const handleColorChange =
    (key: keyof PipelineColors) => (e: React.ChangeEvent<HTMLInputElement>) => {
      // `pipelineColors` may be undefined when the parent doesn't wire the
      // setting (e.g. older callers); guard so we always start from a complete
      // object before merging the user's edit.
      const base = { ...DEFAULT_PIPELINE_COLORS, ...(pipelineColors || {}) };
      setPipelineColors({ ...base, [key]: e.target.value });
    };

  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
      <Box
        sx={{
          display: 'grid',
          gridTemplateColumns: 'repeat(auto-fit, minmax(180px, 1fr))',
          gap: 2,
          alignItems: 'center',
        }}
      >
        <Box>
          <FormControlLabel
            control={
              <Switch
                checked={viMode}
                onChange={(e) => setViMode(e.target.checked)}
                color="primary"
                size="small"
              />
            }
            label={
              <Typography sx={{ fontSize: '0.85rem' }}>
                Editor Vi Mode
              </Typography>
            }
          />
        </Box>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
          <Typography sx={{ fontSize: '0.85rem' }}>Font Size</Typography>
          <ButtonGroup size="small" variant="contained">
            <Button
              onClick={() => setFontSize(Math.max(fontSize - 1, 1))}
              aria-label="Decrease font size"
            >
              -
            </Button>
            <Button
              disabled
              sx={{ minWidth: '40px' }}
              aria-label={`Current font size: ${fontSize}`}
            >
              {fontSize}
            </Button>
            <Button
              onClick={() => setFontSize(fontSize + 1)}
              aria-label="Increase font size"
            >
              +
            </Button>
          </ButtonGroup>
        </Box>
        <Box>
          <FormControlLabel
            control={
              <Switch
                checked={accordionAlerts}
                onChange={(e) => setAccordionAlerts(e.target.checked)}
                color="primary"
                size="small"
              />
            }
            label={
              <Typography sx={{ fontSize: '0.85rem' }}>
                Accordion Change Alerts
              </Typography>
            }
          />
        </Box>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
          <Typography sx={{ fontSize: '0.85rem' }}>Theme</Typography>
          <ToggleButtonGroup
            size="small"
            exclusive
            value={themeMode || 'auto'}
            onChange={(_, value: ThemeMode | null) => {
              // ToggleButtonGroup emits `null` when the user clicks the
              // currently-selected button; treat that as "no change" so the
              // theme mode never gets cleared from settings.
              if (value && ALLOWED_THEME_MODES.includes(value)) {
                setThemeMode(value);
              }
            }}
            aria-label="Theme mode"
            data-testid="theme-mode-toggle"
          >
            {ALLOWED_THEME_MODES.map((mode) => (
              <ToggleButton
                key={mode}
                value={mode}
                aria-label={`Theme mode: ${mode}`}
                data-testid={`theme-mode-${mode}`}
                sx={{ fontSize: '0.75rem', textTransform: 'capitalize' }}
              >
                {mode}
              </ToggleButton>
            ))}
          </ToggleButtonGroup>
        </Box>
      </Box>
      <Box>
        <Typography sx={{ fontSize: '0.85rem', fontWeight: 'bold', mb: 1 }}>
          Pipeline Colors
        </Typography>
        <Box
          sx={{
            display: 'grid',
            gridTemplateColumns: 'repeat(auto-fit, minmax(140px, 1fr))',
            gap: 1.5,
          }}
        >
          {PIPELINE_COLOR_FIELDS.map(({ key, label }) => {
            const value =
              (pipelineColors && pipelineColors[key]) ||
              DEFAULT_PIPELINE_COLORS[key];
            const inputId = `pipeline-color-${key}`;
            return (
              <Box
                key={key}
                sx={{ display: 'flex', alignItems: 'center', gap: 1 }}
              >
                <Box
                  component="input"
                  type="color"
                  id={inputId}
                  data-testid={`pipeline-color-${key}`}
                  value={value}
                  onChange={handleColorChange(key)}
                  sx={{
                    width: 36,
                    height: 28,
                    border: '1px solid #ccc',
                    borderRadius: 1,
                    padding: 0,
                    background: 'none',
                    cursor: 'pointer',
                  }}
                />
                <Typography
                  component="label"
                  htmlFor={inputId}
                  sx={{ fontSize: '0.8rem' }}
                >
                  {label}
                </Typography>
              </Box>
            );
          })}
        </Box>
      </Box>
    </Box>
  );
};

export default UiSettingsPanel;
