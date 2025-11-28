import React from 'react';
import Box from '@mui/material/Box';
import Typography from '@mui/material/Typography';
import Switch from '@mui/material/Switch';
import Button from '@mui/material/Button';
import ButtonGroup from '@mui/material/ButtonGroup';
import FormControlLabel from '@mui/material/FormControlLabel';

const Settings = ({ viMode, setViMode, fontSize, setFontSize }) => {
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
    </Box>
  );
};

export default Settings;
