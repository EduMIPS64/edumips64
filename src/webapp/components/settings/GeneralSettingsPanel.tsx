import React from 'react';
import Box from '@mui/material/Box';
import Typography from '@mui/material/Typography';
import Switch from '@mui/material/Switch';
import ToggleButton from '@mui/material/ToggleButton';
import ToggleButtonGroup from '@mui/material/ToggleButtonGroup';
import FormControlLabel from '@mui/material/FormControlLabel';
import TextField from '@mui/material/TextField';
import {
  ALLOWED_THEME_MODES,
  MIN_STEP_STRIDE,
  MAX_STEP_STRIDE,
  MIN_EXECUTION_DELAY_MS,
  MAX_EXECUTION_DELAY_MS,
  type ThemeMode,
} from '../../settings/schema';

export interface GeneralSettingsPanelProps {
  accordionAlerts: boolean;
  setAccordionAlerts: (v: boolean) => void;
  themeMode: ThemeMode;
  setThemeMode: (v: ThemeMode) => void;
  stepStride: number;
  setStepStride: (v: number) => void;
  executionDelayMs: number;
  setExecutionDelayMs: (v: number) => void;
}

/**
 * General app behavior: theme, right-rail change alerts, and how a
 * simulation run is paced (multi-step size, delay between batches). None of
 * these touch the CPU or worker state.
 */
const GeneralSettingsPanel = ({
  accordionAlerts,
  setAccordionAlerts,
  themeMode,
  setThemeMode,
  stepStride,
  setStepStride,
  executionDelayMs,
  setExecutionDelayMs,
}: GeneralSettingsPanelProps) => (
  <Box
    sx={{
      display: 'grid',
      gridTemplateColumns: 'repeat(auto-fit, minmax(180px, 1fr))',
      gap: 2,
      alignItems: 'center',
    }}
  >
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
      <TextField
        label="Multi Step Size"
        type="number"
        size="small"
        value={stepStride}
        onChange={(e) => {
          const value = parseInt(e.target.value, 10);
          if (
            Number.isInteger(value) &&
            value >= MIN_STEP_STRIDE &&
            value <= MAX_STEP_STRIDE
          ) {
            setStepStride(value);
          }
        }}
        slotProps={{
          htmlInput: { min: MIN_STEP_STRIDE, max: MAX_STEP_STRIDE },
        }}
        sx={{ width: '140px' }}
      />
    </Box>
    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
      <TextField
        label="Execution Delay (ms)"
        type="number"
        size="small"
        value={executionDelayMs}
        onChange={(e) => {
          const value = parseInt(e.target.value, 10);
          if (
            Number.isInteger(value) &&
            value >= MIN_EXECUTION_DELAY_MS &&
            value <= MAX_EXECUTION_DELAY_MS
          ) {
            setExecutionDelayMs(value);
          }
        }}
        slotProps={{
          htmlInput: {
            min: MIN_EXECUTION_DELAY_MS,
            max: MAX_EXECUTION_DELAY_MS,
            step: 10,
          },
        }}
        sx={{ width: '160px' }}
      />
    </Box>
  </Box>
);

export default GeneralSettingsPanel;
