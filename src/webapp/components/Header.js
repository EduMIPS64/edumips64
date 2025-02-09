import React from 'react';

import AppBar from '@mui/material/AppBar';
import ToolBar from '@mui/material/Toolbar';
import Button from '@mui/material/Button';
import IconButton from '@mui/material/IconButton';
import Typography from '@mui/material/Typography';
import TextField from '@mui/material/TextField';

import HelpDialog from './HelpDialog';
import CpuStatusDisplay from './CpuStatusDisplay';
import logo from '../static/logo.png';

import HelpIcon from '@mui/icons-material/Help';
import PlayArrowIcon from '@mui/icons-material/PlayArrow';
import FastForwardIcon from '@mui/icons-material/FastForward';
import SyncIcon from '@mui/icons-material/Sync';
import PlayCircleIcon from '@mui/icons-material/PlayCircle';
import StopCircleIcon from '@mui/icons-material/StopCircle';
import DeleteForeverIcon from '@mui/icons-material/DeleteForever';

export default function Header(props) {
  const [dialogOpen, setDialogOpen] = React.useState(false);
  const [multiStepCount, setMultiStepCount] = React.useState(500);

  return (
    <AppBar position="static">
      <ToolBar>
        <img id="logo" alt="EduMIPS64" src={logo} className="logo" />
        <Typography
          variant="h6"
          noWrap
          component="div"
          sx={{ flexGrow: 1, display: { xs: 'none', lg: 'block' } }}
        >
          Web Version
        </Typography>
        <CpuStatusDisplay status={props.status} />
        <Button

          color="inherit"
          className="load-button"
          id="load-button"
          onClick={() => {
            props.onLoadClick(1);
          }}
          startIcon={<SyncIcon />}
          disabled={!props.loadEnabled}
        >
          Load / Reset
        </Button>
        <Button
          color="inherit"
          className="step-button"
          id="step-button"
          onClick={() => {
            props.onStepClick(1);
          }}
          startIcon={<PlayArrowIcon />}
          disabled={!props.stepEnabled}
        >
          Single Step
        </Button>
        <TextField
          type="number"
          size="small"
          value={multiStepCount}
          onChange={(e) => {
            const value = parseInt(e.target.value, 10);
            if (value > 0 && value <= 10000) {
              setMultiStepCount(value);
            }
          }}
          disabled={props.stopEnabled}
          sx={{
            width: '100px',
            mx: 1,
            input: {
              color: 'inherit'
            },
            '& .MuiOutlinedInput-root': {
              '& fieldset': {
                borderColor: 'rgba(255, 255, 255, 0.23)',
              },
            },
          }}
          inputProps={{
            min: 1,
            max: 100000,
            'aria-label': 'Steps count'
          }}
        />
        <Button
          color="inherit"
          className="multi-step-button"
          id="multi-step-button"
          onClick={() => {
            props.onStepClick(multiStepCount);
          }}
          startIcon={<FastForwardIcon />}
          disabled={!props.stepEnabled}
        >
          Multi Step
        </Button>
        <Button
          color="inherit"
          className="run-button"
          id="run-button"
          onClick={() => {
            props.onRunClick();
          }}
          startIcon={<PlayCircleIcon />}
          disabled={!props.runEnabled}
        >
          Run All
        </Button>
        <Button
          color="inherit"
          className="stop-button"
          id="stop-button"
          onClick={() => {
            props.onStopClick();
          }}
          startIcon={<StopCircleIcon />}
          disabled={!props.stopEnabled}
        >
          Stop
        </Button>
        <Button
          color="inherit"
          className="clear-code-button"
          id="clear-code-button"
          startIcon={<DeleteForeverIcon />}
          onClick={() => {
            props.onClearClick();
          }}
          disabled={props.status === 'RUNNING'}
        > Clear Code
        </Button>
        <IconButton
          color="inherit"
          className="help-button"
          onClick={() => {
            setDialogOpen(true);
          }}
        >
          <HelpIcon />
        </IconButton>
        <HelpDialog
          open={dialogOpen}
          handleClose={() => setDialogOpen(false)}
          ver={props.version}
        />
      </ToolBar>
    </AppBar>
  );
}
