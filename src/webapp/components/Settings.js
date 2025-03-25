import React from 'react';
import Accordion from '@mui/material/Accordion';
import AccordionDetails from '@mui/material/AccordionDetails';
import Grid from '@mui/material/Grid';
import Typography from '@mui/material/Typography';
import Switch from '@mui/material/Switch';
import Button from '@mui/material/Button';

const Settings = ({ viMode, setViMode, fontSize, setFontSize }) => {
    return (
        <Accordion defaultExpanded disableGutters>
            <AccordionDetails>
                <Grid container spacing={2} alignItems="center">
                    <Grid item>
                        <Typography>Vi Mode</Typography>
                    </Grid>
                    <Grid item>
                        <Switch
                            checked={viMode}
                            onChange={(e) => setViMode(e.target.checked)}
                            color="primary"
                        />
                    </Grid>
                    <Grid item>
                        <Typography>Font Size</Typography>
                    </Grid>
                    <Grid item>
                        <Button variant="contained" onClick={() => setFontSize(fontSize + 1)}>+</Button>
                    </Grid>
                    <Grid item>
                        <Button variant="contained" onClick={() => setFontSize(Math.max(fontSize - 1, 1))}>-</Button>
                    </Grid>
                </Grid>
            </AccordionDetails>
        </Accordion>
    );
};

export default Settings;