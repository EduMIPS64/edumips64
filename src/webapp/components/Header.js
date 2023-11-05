import React from 'react';

import AppBar from '@mui/material/AppBar';
import ToolBar from '@mui/material/Toolbar';
import Button from '@mui/material/Button';
import IconButton from '@mui/material/IconButton';
import Typography from '@mui/material/Typography';

import HelpDialog from './HelpDialog';
import CpuStatusDisplay from './CpuStatusDisplay';
import logo from '../static/logo.png';

import HelpIcon from '@mui/icons-material/Help';
import PlayArrowIcon from '@mui/icons-material/PlayArrow';
import FastForwardIcon from '@mui/icons-material/FastForward';
import SyncIcon from '@mui/icons-material/Sync';
import PlayCircleIcon from '@mui/icons-material/PlayCircle';
import StopCircleIcon from '@mui/icons-material/StopCircle';

export default function Header(props) {
  const [dialogOpen, setDialogOpen] = React.useState(false);

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
        <Button
          color="inherit"
          className="multi-step-button"
          id="multi-step-button"
          onClick={() => {
            // TODO: make this customizable
            props.onStepClick(500);
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
