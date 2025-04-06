import React from 'react';
import Grid2 from '@mui/material/Grid2';
import Typography from '@mui/material/Typography';
import Switch from '@mui/material/Switch';
import Button from '@mui/material/Button';

const Parameters = ({enableCache}) => {
    return (
        <Grid2 container spacing={2} alignItems="center">
            <Grid2 container spacing={2}>
                <Grid2 item xs={3}>
                    <Typography sx={{ fontSize: '0.75rem', display: 'block' }}>L1D Size</Typography>
                    <input type="number" size="4" />
                </Grid2>
                <Grid2 item xs={3}>
                    <Typography sx={{ fontSize: '0.75rem', display: 'block' }}>L1D Block Size</Typography>
                    <input type="number" size="4" />
                </Grid2>
                <Grid2 item xs={3}>
                    <Typography sx={{ fontSize: '0.75rem', display: 'block' }}>L1D Associativity</Typography>
                    <input type="number" size="4" />
                </Grid2>
                <Grid2 item xs={3}>
                    <Typography sx={{ fontSize: '0.75rem', display: 'block' }}>L1D Penalty</Typography>
                    <input type="number" size="4" />
                </Grid2>
            </Grid2>
            <Grid2 container spacing={2}>
                <Grid2 item xs={3}>
                    <Typography sx={{ fontSize: '0.75rem', display: 'block' }}>L1D Size</Typography>
                    <input type="number" size="4" />
                </Grid2>
                <Grid2 item xs={3}>
                    <Typography sx={{ fontSize: '0.75rem', display: 'block' }}>L1D Block Size</Typography>
                    <input type="number" size="4" />
                </Grid2>
                <Grid2 item xs={3}>
                    <Typography sx={{ fontSize: '0.75rem', display: 'block' }}>L1D Associativity</Typography>
                    <input type="number" size="4" />
                </Grid2>
                <Grid2 item xs={3}>
                    <Typography sx={{ fontSize: '0.75rem', display: 'block' }}>L1D Penalty</Typography>
                    <input type="number" size="4" />
                </Grid2>
            </Grid2>
        </Grid2>
    );
};

export default Parameters;