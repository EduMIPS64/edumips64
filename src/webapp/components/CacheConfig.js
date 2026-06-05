import React from 'react';
import Typography from '@mui/material/Typography';
import Box from '@mui/material/Box';
import TextField from '@mui/material/TextField';
import { useSetting } from '../settings/useSetting';
import { SettingKey } from '../settings/SettingKey';

const parsePositiveInteger = (value) => {
  const parsed = Number(value);
  return Number.isInteger(parsed) && parsed > 0 ? parsed : null;
};

const CacheNumberField = ({ label, field, value, setCache, isDisabled }) => {
  const [draftValue, setDraftValue] = React.useState(String(value));

  const handleChange = (e) => {
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

const CacheRow = ({ label, cache, setCache, isDisabled }) => (
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

const CacheConfig = ({ onChange, status }) => {
  const [l1d, setL1D] = useSetting(SettingKey.CACHE_L1D);
  const [l1i, setL1I] = useSetting(SettingKey.CACHE_L1I);

  const isDisabled = status === 'RUNNING';

  React.useEffect(() => {
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

export default CacheConfig;
