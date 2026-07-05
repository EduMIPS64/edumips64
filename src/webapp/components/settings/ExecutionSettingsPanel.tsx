import React from 'react';
import Box from '@mui/material/Box';
import TextField from '@mui/material/TextField';
import {
  MIN_STEP_STRIDE,
  MAX_STEP_STRIDE,
  MIN_EXECUTION_DELAY_MS,
  MAX_EXECUTION_DELAY_MS,
} from '../../settings/schema';

export interface ExecutionSettingsPanelProps {
  stepStride: number;
  setStepStride: (v: number) => void;
  executionDelayMs: number;
  setExecutionDelayMs: (v: number) => void;
}

/**
 * Parameters that control how a simulation run is paced: how many steps a
 * "Multi Step" click runs, and how much artificial delay is inserted between
 * batches so intermediate states remain visible.
 */
const ExecutionSettingsPanel = ({
  stepStride,
  setStepStride,
  executionDelayMs,
  setExecutionDelayMs,
}: ExecutionSettingsPanelProps) => (
  <Box
    sx={{
      display: 'grid',
      gridTemplateColumns: 'repeat(auto-fit, minmax(180px, 1fr))',
      gap: 2,
      alignItems: 'center',
    }}
  >
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

export default ExecutionSettingsPanel;
