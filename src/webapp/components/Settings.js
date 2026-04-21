import React from 'react';
import Box from '@mui/material/Box';
import Typography from '@mui/material/Typography';
import Switch from '@mui/material/Switch';
import Button from '@mui/material/Button';
import ButtonGroup from '@mui/material/ButtonGroup';
import FormControlLabel from '@mui/material/FormControlLabel';
import TextField from '@mui/material/TextField';
import {
  MIN_STEP_STRIDE,
  MAX_STEP_STRIDE,
  MIN_EXECUTION_DELAY_MS,
  MAX_EXECUTION_DELAY_MS,
} from '../settings/schema';

const Settings = ({
  viMode,
  setViMode,
  fontSize,
  setFontSize,
  accordionAlerts,
  setAccordionAlerts,
  forwarding,
  setForwarding,
  stepStride,
  setStepStride,
  executionDelayMs,
  setExecutionDelayMs,
  status,
}) => {
  // Forwarding affects the CPU pipeline behavior and resets the CPU when
  // changed, so we only allow toggling it when the simulator is not running
  // a program. This mirrors the way `CacheConfig` grays out its inputs.
  const forwardingDisabled = status === 'RUNNING';
  return (
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
            <Typography sx={{ fontSize: '0.85rem' }}>Editor Vi Mode</Typography>
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
      <Box>
        <FormControlLabel
          control={
            <Switch
              checked={forwarding}
              onChange={(e) => setForwarding(e.target.checked)}
              color="primary"
              size="small"
              disabled={forwardingDisabled}
              inputProps={{ 'data-testid': 'forwarding-switch' }}
            />
          }
          label={
            <Typography
              sx={{
                fontSize: '0.85rem',
                color: forwardingDisabled ? 'text.disabled' : 'text.primary',
              }}
            >
              CPU Forwarding
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
};

export default Settings;
