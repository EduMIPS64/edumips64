import React from 'react';

import AppBar from '@mui/material/AppBar';
import ToolBar from '@mui/material/Toolbar';
import Button from '@mui/material/Button';
import Typography from '@mui/material/Typography';

import HelpDialog from './HelpDialog';
import logo from '../static/logo.png';
import ErrorDisplay from './ErrorDisplay';

import HelpIcon from '@mui/icons-material/Help';
import PlayArrowIcon from '@mui/icons-material/PlayArrow';
import FastForwardIcon from '@mui/icons-material/FastForward';
import SyncIcon from '@mui/icons-material/Sync';
import PlayCircleIcon from '@mui/icons-material/PlayCircle';
import StopCircleIcon from '@mui/icons-material/StopCircle';
import { Height } from '@mui/icons-material';

// Number of steps to run with the multi-step button.
const STEP_STRIDE = 500;

export default function Header(props) {
  const [dialogOpen, setDialogOpen] = React.useState(false);

  return (
    <AppBar position="static">
      <ToolBar>
        <img id="logo" alt="EduMIPS64" src={logo} className="logo"/>
        <Typography
            variant="h6"
            noWrap
            component="div"
            sx={{ flexGrow: 1, display: { xs: 'none', sm: 'block' } }}
          >
            Web Version
          </Typography>
        <Button
          
          color="inherit"
          className="load-button"
          id="load-button"
          onClick={() => {props.onLoadClick(1);}}
          startIcon={<SyncIcon />}
          disabled={!props.loadEnabled}
        >
          Load / Reset
        </Button>
        <Button
          
          color="inherit"
          className="step-button"
          id="step-button"
          onClick={() => {props.onStepClick(1);}}
          startIcon={<PlayArrowIcon />}
          disabled={!props.stepEnabled}
        >
          Single Step
        </Button>
        <Button
          
          color="inherit"
          className="multi-step-button"
          id="multi-step-button"
          onClick={() => {props.onStepClick(STEP_STRIDE);}}
          startIcon={<FastForwardIcon />}
          disabled={!props.stepEnabled}
        >
          Multi Step
        </Button>
        <Button
          
          color="inherit"
          className="run-button"
          id="run-button"
          onClick={() => {props.onStepClick(STEP_STRIDE);}}
          startIcon={<PlayCircleIcon />}
          disabled={!props.runEnabled}
        >
          Run All
        </Button>
        <Button
          
          color="inherit"
          className="stop-button"
          id="stop-button"
          onClick={() => {props.onStopClick();}}
          startIcon={<StopCircleIcon />}
          disabled={!props.stopEnabled}
        >
          Stop
        </Button>
        <ErrorDisplay parsingErrors={props.parsingErrors} />
        <Button
          
          color="inherit"
          className="help-button"
          onClick={() => {props.setDialogOpen(true)}}
          startIcon={<HelpIcon />}
        >
          Help
        </Button>
        <HelpDialog
          open={dialogOpen}
          handleClose={() => setDialogOpen(false)}
        />
      </ToolBar>
    </AppBar>
  );
}
