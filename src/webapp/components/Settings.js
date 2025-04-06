import React from 'react';
import Grid2 from '@mui/material/Grid2';
import Typography from '@mui/material/Typography';
import Switch from '@mui/material/Switch';
import Button from '@mui/material/Button';

const Settings = ({ viMode, setViMode, fontSize, setFontSize }) => {
    return (
        <Grid2 container spacing={2} alignItems="center">
            <Grid2 item>
                <Typography sx={{ fontSize: '0.75rem' }}>Vi Mode</Typography>
            </Grid2>
            <Grid2 item>
                <Switch
                    checked={viMode}
                    onChange={(e) => setViMode(e.target.checked)}
                    color="primary"
                />
            </Grid2>
            <Grid2 item>
                <Typography sx={{ fontSize: '0.75rem' }}>Font Size</Typography>
            </Grid2>
            <Grid2 item>
                <Button variant="contained" onClick={() => setFontSize(fontSize + 1)}>+</Button>
            </Grid2>
            <Grid2 item>
                <Button variant="contained" onClick={() => setFontSize(Math.max(fontSize - 1, 1))}>-</Button>
            </Grid2>
        </Grid2>
    );
};

export default Settings;