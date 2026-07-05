import React from 'react';
import Box from '@mui/material/Box';
import Typography from '@mui/material/Typography';
import {
  DEFAULT_PIPELINE_COLORS,
  type PipelineColors,
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

export interface PipelineColorsPanelProps {
  pipelineColors: PipelineColors | undefined;
  setPipelineColors: (v: PipelineColors) => void;
}

/** Per-stage colors used by the Pipeline widget and Monaco's stage highlights. */
const PipelineColorsPanel = ({
  pipelineColors,
  setPipelineColors,
}: PipelineColorsPanelProps) => {
  const handleColorChange =
    (key: keyof PipelineColors) => (e: React.ChangeEvent<HTMLInputElement>) => {
      // `pipelineColors` may be undefined when the parent doesn't wire the
      // setting (e.g. older callers); guard so we always start from a complete
      // object before merging the user's edit.
      const base = { ...DEFAULT_PIPELINE_COLORS, ...(pipelineColors || {}) };
      setPipelineColors({ ...base, [key]: e.target.value });
    };

  return (
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
          <Box key={key} sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
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
  );
};

export default PipelineColorsPanel;
