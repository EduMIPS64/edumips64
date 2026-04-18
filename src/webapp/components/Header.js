import React from 'react';

import AppBar from '@mui/material/AppBar';
import ToolBar from '@mui/material/Toolbar';
import Button from '@mui/material/Button';
import IconButton from '@mui/material/IconButton';
import Typography from '@mui/material/Typography';
import TextField from '@mui/material/TextField';
import Tooltip from '@mui/material/Tooltip';

import HelpDialog from './HelpDialog';
import CpuStatusDisplay from './CpuStatusDisplay';
import logoDark from '../static/logo-dark.png';
import logoBright from '../static/logo.png';

import HelpIcon from '@mui/icons-material/Help';
import PlayArrowIcon from '@mui/icons-material/PlayArrow';
import FastForwardIcon from '@mui/icons-material/FastForward';
import PlayCircleIcon from '@mui/icons-material/PlayCircle';
import PauseCircleIcon from '@mui/icons-material/PauseCircle';
import DeleteForeverIcon from '@mui/icons-material/DeleteForever';
import StopCircleIcon from '@mui/icons-material/StopCircle';
import UploadIcon from '@mui/icons-material/Upload';
import DownloadIcon from '@mui/icons-material/Download';

export default function Header(props) {
  const [dialogOpen, setDialogOpen] = React.useState(false);
  const [multiStepCount, setMultiStepCount] = React.useState(500);
  const [fileContent, setFileContent] = React.useState('');


  const handleFileLoad = (event) => {
    const file = event.target.files[0];
    if (file) {
      const reader = new FileReader();
      reader.onload = (e) => {
        props.onChangeValue(e.target.result);
      };

      reader.readAsText(file);
    }
  };

  return (
    <AppBar position="static">
      <ToolBar>
        <img
          id="logo"
          alt="EduMIPS64"
          src={props.prefersDarkMode ? logoDark : logoBright}
          className="logo"
        />
        <Typography
          variant="h6"
          noWrap
          component="div"
          sx={{ flexGrow: 1, display: { xs: 'none', lg: 'block' } }}
        >
          Web Version
        </Typography>
        <Tooltip title="The current status of the CPU" arrow placement="top">
          <div>
            <CpuStatusDisplay status={props.status} />
          </div>
        </Tooltip>
        <Tooltip title="Load the current code into the simulator" arrow placement="top">
          <Button
            color="inherit"
            className="load-button"
            id="load-button"
            onClick={() => props.onLoadClick()}
            startIcon={<UploadIcon />}
            disabled={!props.loadEnabled}
            sx={{
              display: props.status === 'RUNNING' ? 'none' : 'inline-flex'
            }}
          >
            Load
          </Button>
        </Tooltip>
        <Tooltip title="Runs a single step of simulation" arrow placement="top">
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
        </Tooltip>
        <Tooltip title="Number of steps that the 'Multi step' button will execute" arrow placement="top">
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
            disabled={props.pauseEnabled}
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
        </Tooltip>
        <Tooltip title="Run multiple steps of simulation" arrow placement="top">
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
        </Tooltip>
        <Tooltip title="Run until the simulation ends" arrow placement="top">
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
        </Tooltip>
        <Tooltip title="Pause the simulation" arrow placement="top">
          <Button
            color="inherit"
            className="pause-button"
            id="pause-button"
            onClick={() => {
              props.onPauseClick();
            }}
            startIcon={<PauseCircleIcon />}
            disabled={!props.pauseEnabled}
          >
            Pause
          </Button>
        </Tooltip>
        <Tooltip title="Stop the simulation and reset the CPU" arrow placement="top">
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
        </Tooltip>
        <Tooltip title="Remove all the code from the editor, leaving only an empty assembly file" arrow placement="top">
          <Button
            color="inherit"
            className="clear-code-button"
            id="clear-code-button"
            startIcon={<DeleteForeverIcon />}
            onClick={() => {
              props.onClearClick();
            }}
            disabled={props.status === 'RUNNING'}
          >
            clear
          </Button>
        </Tooltip>
        <Tooltip title="Open code from file" arrow placement="top">
          <Button
            color="inherit"
            className="load-code-button"
            id="load-code-button"
            startIcon={<UploadIcon />}
            onClick={() => {
              props.onOpenClick();
            }}
            disabled={props.status === 'RUNNING'}
            component="label"
          >
            Open Code
          </Button>
        </Tooltip>
        <Tooltip title="Save code to file" arrow placement="top">
          <Button
              color="inherit"
              className="save-code-button"
              id="save-code-button"
              startIcon={<DownloadIcon />}
              onClick={() => {
                props.onSaveClick();
              }}
              disabled={props.status === 'RUNNING'}
              component="label"
          >
            Save Code
          </Button>
        </Tooltip>
        <IconButton
          color="inherit"
          className="help-button"
          id="help-button"
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
