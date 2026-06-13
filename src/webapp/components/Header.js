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
import DeleteForeverIcon from '@mui/icons-material/DeleteForever';
import UploadIcon from '@mui/icons-material/Upload';
import DownloadIcon from '@mui/icons-material/Download';
import RestartAltIcon from '@mui/icons-material/RestartAlt';

import { deriveLogicalState } from '../simulatorState';

export default function Header(props) {
  const [dialogOpen, setDialogOpen] = React.useState(false);
  // Derive the logical UI state for editor-control gating.
  const logicalState = deriveLogicalState(
    props.status,
    props.executing,
    props.inputRequest,
  );

  // Editor controls are always visible; disabled only while the worker is
  // actively executing steps or waiting for user input.
  const editorDisabled =
    logicalState === 'EXECUTING' || logicalState === 'WAITING_FOR_INPUT';

  // Classify the current deployment so that users can tell at a glance
  // whether they are using the production version or a PR/dev build, and
  // jump back to the originating pull request when applicable.
  const buildInfo = React.useMemo(() => getBuildInfo(), []);

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
          {buildInfo.kind === 'candidate' && (
            <Tooltip
              title="This is a candidate build, automatically deployed from a master commit. It may be unstable."
              arrow
              placement="bottom"
            >
              <Chip
                id="candidate-build-chip"
                size="small"
                label="CANDIDATE"
                className="candidate-chip"
                aria-label="Candidate build"
                sx={{ fontWeight: 'bold' }}
              />
            </Tooltip>
          )}
        </Typography>
        <Tooltip title="The current status of the CPU" arrow placement="top">
          <div>
            <CpuStatusDisplay status={props.status} />
          </div>
        </Tooltip>
        {/* Execution controls — rendered contextually per logical state. Wrapped in
            a fixed-min-width container so the toolbar doesn't shift when the set
            of visible buttons changes between states. */}
        {/* Load button — always visible in the header. Execution controls
            (Step / Multi Step / Run / Pause / Stop) live in the floating
            RunControlsToolbar overlay mounted from Simulator.js. */}
        <Tooltip
          title="Load the current code into the simulator"
          arrow
          placement="top"
        >
          <Button
            color="inherit"
            className="load-button"
            id="load-button"
            onClick={() => props.onLoadClick()}
            startIcon={<UploadIcon />}
            disabled={!props.loadEnabled}
            sx={responsiveButtonSx}
          >
            {responsiveLabel('Load')}
          </Button>
        </Tooltip>
        <Tooltip
          title="Remove all the code from the editor, leaving only an empty assembly file"
          arrow
          placement="top"
        >
          <Button
            color="inherit"
            className="clear-code-button"
            id="clear-code-button"
            startIcon={<DeleteForeverIcon />}
            onClick={() => {
              props.onClearClick();
            }}
            disabled={editorDisabled}
            sx={responsiveButtonSx}
          >
            {responsiveLabel('clear')}
          </Button>
        </Tooltip>
        <Tooltip
          title="Restore the bundled sample program in the editor"
          arrow
          placement="top"
        >
          <Button
            color="inherit"
            className="restore-sample-button"
            id="restore-sample-button"
            startIcon={<RestartAltIcon />}
            onClick={() => {
              props.onRestoreClick();
            }}
            disabled={editorDisabled}
            sx={responsiveButtonSx}
          >
            {responsiveLabel('Restore default sample')}
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
            disabled={editorDisabled}
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
