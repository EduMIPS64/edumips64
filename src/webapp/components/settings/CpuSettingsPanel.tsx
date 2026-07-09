import type React from 'react';
import Box from '@mui/material/Box';
import Typography from '@mui/material/Typography';
import Switch from '@mui/material/Switch';
import FormControlLabel from '@mui/material/FormControlLabel';
import type { CpuStatus } from '../../simulator/protocol';

export interface CpuSettingsPanelProps {
  forwarding: boolean;
  setForwarding: (v: boolean) => void;
  delaySlot: boolean;
  setDelaySlot: (v: boolean) => void;
  status: CpuStatus;
}

/**
 * CPU behavior settings. These change the simulated pipeline's semantics and
 * reset the CPU when toggled, so they are disabled while a program is
 * running.
 */
const CpuSettingsPanel = ({
  forwarding,
  setForwarding,
  delaySlot,
  setDelaySlot,
  status,
}: CpuSettingsPanelProps) => {
  const disabled = status === 'RUNNING';

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
              checked={forwarding}
              onChange={(e) => setForwarding(e.target.checked)}
              color="primary"
              size="small"
              disabled={disabled}
              // TODO(ts): MUI's SwitchInputSlotPropsOverrides doesn't include
              // data-* HTML attributes; cast to pass through to the input DOM node.
              slotProps={{
                input: {
                  'data-testid': 'forwarding-switch',
                } as React.ComponentProps<'input'>,
              }}
            />
          }
          label={
            <Typography
              sx={{
                fontSize: '0.85rem',
                color: disabled ? 'text.disabled' : 'text.primary',
              }}
            >
              CPU Forwarding
            </Typography>
          }
        />
      </Box>
      <Box>
        <FormControlLabel
          control={
            <Switch
              checked={!!delaySlot}
              onChange={(e) => setDelaySlot(e.target.checked)}
              color="primary"
              size="small"
              disabled={disabled}
              // TODO(ts): same data-* attribute cast as above.
              slotProps={{
                input: {
                  'data-testid': 'delay-slot-switch',
                } as React.ComponentProps<'input'>,
              }}
            />
          }
          label={
            <Typography
              sx={{
                fontSize: '0.85rem',
                color: disabled ? 'text.disabled' : 'text.primary',
              }}
            >
              Branch Delay Slot
            </Typography>
          }
        />
      </Box>
    </Box>
  );
};

export default CpuSettingsPanel;
