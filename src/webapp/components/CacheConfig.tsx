import React from 'react';
import Typography from '@mui/material/Typography';
import Box from '@mui/material/Box';
import TextField from '@mui/material/TextField';
import { useSetting } from '../settings/useSetting';
import { SettingKey } from '../settings/SettingKey';
import type { CacheConfig } from '../settings/schema';
import type { CpuStatus } from '../simulator/protocol';

const parsePositiveInteger = (value: string): number | null => {
  const parsed = Number(value);
  return Number.isInteger(parsed) && parsed > 0 ? parsed : null;
};

interface CacheNumberFieldProps {
  label: string;
  field: keyof CacheConfig;
  value: number;
  setCache: (updater: (prev: CacheConfig) => CacheConfig) => void;
  isDisabled: boolean;
}

const CacheNumberField = ({
  label,
  field,
  value,
  setCache,
  isDisabled,
}: CacheNumberFieldProps) => {
  const [draftValue, setDraftValue] = React.useState(String(value));

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const nextValue = e.target.value;
    setDraftValue(nextValue);

    if (nextValue === '') {
      return;
    }

    const parsed = parsePositiveInteger(nextValue);
    if (parsed !== null) {
      setCache((cache) => ({ ...cache, [field]: parsed }));
    }
  };

  const handleBlur = () => {
    if (parsePositiveInteger(draftValue) === null) {
      setDraftValue(String(value));
    }
  };

  return (
    <TextField
      label={label}
      type="number"
      size="small"
      value={draftValue}
      onChange={handleChange}
      onBlur={handleBlur}
      disabled={isDisabled}
      slotProps={{ htmlInput: { min: 1 } }}
    />
  );
};

interface CacheRowProps {
  label: string;
  cache: CacheConfig;
  setCache: (updater: (prev: CacheConfig) => CacheConfig) => void;
  isDisabled: boolean;
}

const CacheRow = ({ label, cache, setCache, isDisabled }: CacheRowProps) => (
  <Box sx={{ mb: 2 }}>
    <Typography
      sx={{
        fontSize: '0.85rem',
        fontWeight: 'bold',
        color: isDisabled ? 'text.disabled' : 'text.primary',
        mb: 1,
      }}
    >
      {label}
    </Typography>
    <Box
      sx={{
        display: 'grid',
        gridTemplateColumns: 'repeat(auto-fit, minmax(100px, 1fr))',
        gap: 1.5,
      }}
    >
      <CacheNumberField
        label="Size"
        field="size"
        value={cache.size}
        setCache={setCache}
        isDisabled={isDisabled}
      />
      <CacheNumberField
        label="Block Size"
        field="blockSize"
        value={cache.blockSize}
        setCache={setCache}
        isDisabled={isDisabled}
      />
      <CacheNumberField
        label="Associativity"
        field="associativity"
        value={cache.associativity}
        setCache={setCache}
        isDisabled={isDisabled}
      />
    </Box>
  </Box>
);

interface CacheConfigProps {
  onChange?: (config: { l1d: CacheConfig; l1i: CacheConfig }) => void;
  status: CpuStatus;
}

// `onChange` ultimately calls the worker's `setCacheConfig`, which resets the
// CPU (see Simulator.setCacheConfig in the Java client). This component used
// to be the only occupant of its own "Cache" tab, so a naive "push on every
// mount" effect only ran when a user deliberately opened that tab. Now that
// Cache shares a tab with CPU/Execution, simply opening Settings to flip a
// CPU switch also mounts this component — a naive effect would silently
// reset a running simulation just from that.
//
// The persisted cache config only needs to reach the worker once per page
// load (the worker has no other way of learning a user's non-default
// preference), and only once it's actually safe to do so (never while
// RUNNING). Two effects split those concerns:
//   - the first fires at most once for the whole page session, deferring
//     until `status` is not RUNNING;
//   - the second fires on genuine edits to l1d/l1i after this *instance* has
//     already painted once, so real edits (only possible while not running)
//     and settings-wide resets still propagate immediately.
let hasSyncedCacheConfigThisSession = false;

const CacheConfigPanel = ({ onChange, status }: CacheConfigProps) => {
  const [l1d, setL1D] = useSetting(SettingKey.CACHE_L1D);
  const [l1i, setL1I] = useSetting(SettingKey.CACHE_L1I);

  const isDisabled = status === 'RUNNING';

  // Deliberately keyed on `isDisabled` alone: this effect's only job is to
  // retry the one-time initial sync once it becomes safe. Value changes are
  // handled by the edit-propagation effect below.
  // biome-ignore lint/correctness/useExhaustiveDependencies: see comment above.
  React.useEffect(() => {
    if (hasSyncedCacheConfigThisSession || isDisabled) {
      return;
    }
    hasSyncedCacheConfigThisSession = true;
    if (onChange) {
      onChange({ l1d, l1i });
    }
  }, [isDisabled]);

  const isFirstRenderRef = React.useRef(true);
  React.useEffect(() => {
    if (isFirstRenderRef.current) {
      isFirstRenderRef.current = false;
      return;
    }
    if (onChange) {
      onChange({ l1d, l1i });
    }
  }, [l1d, l1i, onChange]);

  return (
    <Box>
      <CacheRow
        label="L1 Instruction Cache"
        cache={l1i}
        setCache={setL1I}
        isDisabled={isDisabled}
      />
      <CacheRow
        label="L1 Data Cache"
        cache={l1d}
        setCache={setL1D}
        isDisabled={isDisabled}
      />
    </Box>
  );
};

export default CacheConfigPanel;
