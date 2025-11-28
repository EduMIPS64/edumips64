import React, { useState, useEffect } from 'react';
import Typography from '@mui/material/Typography';
import Box from '@mui/material/Box';
import TextField from '@mui/material/TextField';

const CacheConfig = ({ onChange, status }) => {
  const [l1d, setL1D] = useState({
    size: 1024,
    blockSize: 16,
    associativity: 1,
    penalty: 40,
  });
  const [l1i, setL1I] = useState({
    size: 1024,
    blockSize: 16,
    associativity: 1,
    penalty: 40,
  });

  const isDisabled = status === 'RUNNING';

  useEffect(() => {
    if (onChange) {
      onChange({ l1d, l1i });
    }
  }, [l1d, l1i]);

  const CacheRow = ({ label, cache, setCache }) => (
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
        <TextField
          label="Size"
          type="number"
          size="small"
          value={cache.size}
          onChange={(e) =>
            setCache({ ...cache, size: parseInt(e.target.value) || 0 })
          }
          disabled={isDisabled}
          slotProps={{ htmlInput: { min: 0 } }}
        />
        <TextField
          label="Block Size"
          type="number"
          size="small"
          value={cache.blockSize}
          onChange={(e) =>
            setCache({ ...cache, blockSize: parseInt(e.target.value) || 0 })
          }
          disabled={isDisabled}
          slotProps={{ htmlInput: { min: 0 } }}
        />
        <TextField
          label="Associativity"
          type="number"
          size="small"
          value={cache.associativity}
          onChange={(e) =>
            setCache({ ...cache, associativity: parseInt(e.target.value) || 0 })
          }
          disabled={isDisabled}
          slotProps={{ htmlInput: { min: 0 } }}
        />
        <TextField
          label="Penalty"
          type="number"
          size="small"
          value={cache.penalty}
          onChange={(e) =>
            setCache({ ...cache, penalty: parseInt(e.target.value) || 0 })
          }
          disabled={isDisabled}
          slotProps={{ htmlInput: { min: 0 } }}
        />
      </Box>
    </Box>
  );

  return (
    <Box>
      <CacheRow label="L1 Data Cache" cache={l1d} setCache={setL1D} />
      <CacheRow label="L1 Instruction Cache" cache={l1i} setCache={setL1I} />
    </Box>
  );
};

export default CacheConfig;
