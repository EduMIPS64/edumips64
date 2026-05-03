import React from 'react';

import AppBar from '@mui/material/AppBar';
import ToolBar from '@mui/material/Toolbar';
import Box from '@mui/material/Box';
import Button from '@mui/material/Button';
import IconButton from '@mui/material/IconButton';
import Typography from '@mui/material/Typography';
import Tooltip from '@mui/material/Tooltip';
import Chip from '@mui/material/Chip';
import Link from '@mui/material/Link';

import HelpDialog from './HelpDialog';
import CpuStatusDisplay from './CpuStatusDisplay';
import logoDark from '../static/logo-dark.png';
import logoBright from '../static/logo.png';
import { getBuildInfo } from '../buildInfo';

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
  const [fileContent, setFileContent] = React.useState('');

  // The multi-step size used to be a piece of local state managed by an
  // inline `<TextField>` in this toolbar. It is now driven by the persisted
  // `stepStride` setting, so it lives alongside the other execution
  // parameters in the Settings panel and survives page reloads.
  const multiStepCount = props.multiStepCount;

  // Classify the current deployment so that users can tell at a glance
  // whether they are using the production version or a PR/dev build, and
  // jump back to the originating pull request when applicable.
  const buildInfo = React.useMemo(() => getBuildInfo(), []);


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

  // On small viewports the buttons collapse to icon-only. Keep the text
  // visually hidden on `xs` so it still provides an accessible name,
  // while showing it inline on larger screens. This avoids the toolbar
  // overflowing on phones / tablets where 9+ full-text buttons plus
  // status chips can't possibly fit on one row.
  const responsiveLabel = (text) => (
    <Box
      component="span"
      sx={{
        position: { xs: 'absolute', md: 'static' },
        width: { xs: 1, md: 'auto' },
        height: { xs: 1, md: 'auto' },
        p: 0,
        m: { xs: -1, md: 0 },
        overflow: { xs: 'hidden', md: 'visible' },
        clip: { xs: 'rect(0 0 0 0)', md: 'auto' },
        whiteSpace: { xs: 'nowrap', md: 'normal' },
        border: 0,
        display: 'inline',
      }}
    >
      {text}
    </Box>
  );

  // Tighter padding for the toolbar buttons on small screens so the
  // icon-only buttons stop carrying the wide desktop label gutter.
  const responsiveButtonSx = {
    minWidth: { xs: 'auto', md: 64 },
    px: { xs: 1, md: 2 },
    '& .MuiButton-startIcon': {
      mr: { xs: 0, md: 1 },
      ml: { xs: 0, md: -0.5 },
    },
  };

  return (
    <AppBar position="static">
      <ToolBar
        // On narrow viewports the toolbar would otherwise overflow off
        // the right edge of the screen (the buttons have full text
        // labels). Allow it to wrap onto multiple rows and tighten the
        // horizontal gutters so more buttons fit per row.
        sx={{
          flexWrap: { xs: 'wrap', md: 'nowrap' },
          rowGap: { xs: 0.5, md: 0 },
          minHeight: { xs: 'auto', md: 64 },
          py: { xs: 0.5, md: 0 },
          px: { xs: 1, sm: 2 },
        }}
      >
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
          sx={{
            flexGrow: 1,
            display: { xs: 'none', lg: 'flex' },
            alignItems: 'center',
            gap: 1,
          }}
        >
          {buildInfo.kind === 'dev' ? 'Web Version (dev)' : 'Web Version'}
          {buildInfo.kind === 'pr' && (
            <Tooltip
              title="This is a preview build for a pull request. Click to open the PR on GitHub."
              arrow
              placement="bottom"
            >
              <Chip
                id="pr-build-chip"
                component={Link}
                href={buildInfo.prUrl}
                target="_blank"
                rel="noreferrer"
                clickable
                color="warning"
                size="small"
                label={`PR #${buildInfo.prNumber}`}
                sx={{ fontWeight: 'bold' }}
              />
            </Tooltip>
          )}
          {buildInfo.kind === 'dev' && (
            <Chip
              id="dev-build-chip"
              color="info"
              size="small"
              label="dev"
              sx={{ fontWeight: 'bold' }}
            />
          )}
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
              ...responsiveButtonSx,
              display: props.status === 'RUNNING' ? 'none' : 'inline-flex'
            }}
          >
            {responsiveLabel('Load')}
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
            sx={responsiveButtonSx}
          >
            {responsiveLabel('Single Step')}
          </Button>
        </Tooltip>
        <Tooltip title={`Run ${multiStepCount} steps of simulation (configurable in Settings)`} arrow placement="top">
          <Button
            color="inherit"
            className="multi-step-button"
            id="multi-step-button"
            onClick={() => {
              props.onStepClick(multiStepCount);
            }}
            startIcon={<FastForwardIcon />}
            disabled={!props.stepEnabled}
            sx={responsiveButtonSx}
          >
            {responsiveLabel('Multi Step')}
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
            sx={responsiveButtonSx}
          >
            {responsiveLabel('Run All')}
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
            sx={responsiveButtonSx}
          >
            {responsiveLabel('Pause')}
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
            sx={responsiveButtonSx}
          >
            {responsiveLabel('Stop')}
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
            sx={responsiveButtonSx}
          >
            {responsiveLabel('clear')}
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
            sx={responsiveButtonSx}
          >
            {responsiveLabel('Open Code')}
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
              sx={responsiveButtonSx}
          >
            {responsiveLabel('Save Code')}
          </Button>
        </Tooltip>
        <IconButton
          color="inherit"
          className="help-button"
          id="help-button"
          aria-label="Open help"
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
