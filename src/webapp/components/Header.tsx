import React from 'react';

import AppBar from '@mui/material/AppBar';
import ToolBar from '@mui/material/Toolbar';
import Box from '@mui/material/Box';
import Button from '@mui/material/Button';
import Divider from '@mui/material/Divider';
import IconButton from '@mui/material/IconButton';
import ListItemIcon from '@mui/material/ListItemIcon';
import ListItemText from '@mui/material/ListItemText';
import Menu from '@mui/material/Menu';
import MenuItem from '@mui/material/MenuItem';
import Typography from '@mui/material/Typography';
import Tooltip from '@mui/material/Tooltip';
import Chip from '@mui/material/Chip';
import Link from '@mui/material/Link';

import HelpDialog from './HelpDialog';
import SettingsDialog from './SettingsDialog';
import CpuStatusDisplay from './CpuStatusDisplay';
import logoDark from '../static/logo-dark.png';
import logoBright from '../static/logo.png';
import { getBuildInfo } from '../buildInfo';

import ArrowDropDownIcon from '@mui/icons-material/ArrowDropDown';
import DeleteForeverIcon from '@mui/icons-material/DeleteForever';
import DownloadIcon from '@mui/icons-material/Download';
import FolderOpenIcon from '@mui/icons-material/FolderOpen';
import HelpIcon from '@mui/icons-material/Help';
import RestartAltIcon from '@mui/icons-material/RestartAlt';
import SettingsIcon from '@mui/icons-material/Settings';
import UploadIcon from '@mui/icons-material/Upload';

import { deriveLogicalState } from '../simulatorState';
import type { CpuStatus, ParsingError, SimulatorResult } from '../simulator/protocol';
import type { GeneralSettingsPanelProps } from './settings/GeneralSettingsPanel';
import type { EditorSettingsPanelProps } from './settings/EditorSettingsPanel';
import type { PipelineColorsPanelProps } from './settings/PipelineColorsPanel';
import type { CpuSettingsPanelProps } from './settings/CpuSettingsPanel';
import type { CacheConfig as CacheConfigType } from '../settings/schema';

type SettingsProps = GeneralSettingsPanelProps &
  EditorSettingsPanelProps &
  PipelineColorsPanelProps &
  Omit<CpuSettingsPanelProps, 'status'> & {
    onCacheConfigChange?: (config: { l1d: CacheConfigType; l1i: CacheConfigType }) => void;
  };

interface HeaderProps extends SettingsProps {
  onLoadClick: () => void;
  loadEnabled: boolean;
  onClearClick: () => void;
  onOpenClick: () => void;
  onSaveClick: () => void;
  onRestoreClick: () => void;
  // parsingErrors is passed from Simulator but not consumed in the Header render;
  // included in the interface so the prop is accepted without a TS error.
  parsingErrors?: ParsingError[] | null;
  version: string;
  status: CpuStatus;
  executing: boolean;
  inputRequest: SimulatorResult | null;
  prefersDarkMode: boolean;
}

export default function Header({
  onLoadClick,
  loadEnabled,
  onClearClick,
  onOpenClick,
  onSaveClick,
  onRestoreClick,
  version,
  status,
  executing,
  inputRequest,
  prefersDarkMode,
  onCacheConfigChange,
  viMode,
  setViMode,
  fontSize,
  setFontSize,
  accordionAlerts,
  setAccordionAlerts,
  pipelineColors,
  setPipelineColors,
  themeMode,
  setThemeMode,
  forwarding,
  setForwarding,
  delaySlot,
  setDelaySlot,
  stepStride,
  setStepStride,
  executionDelayMs,
  setExecutionDelayMs,
}: HeaderProps) {
  const [dialogOpen, setDialogOpen] = React.useState(false);
  const [settingsDialogOpen, setSettingsDialogOpen] = React.useState(false);
  const [programAnchor, setProgramAnchor] = React.useState<HTMLElement | null>(null);
  const menuOpen = Boolean(programAnchor);
  // Derive the logical UI state for editor-control gating.
  const logicalState = deriveLogicalState(status, executing, inputRequest);

  // The Program menu manages the editor's program (New / Open… / Save… / Load
  // Example). It must be unavailable whenever a program is loaded in the
  // simulator (i.e. the simulator is running). It stays available in EMPTY
  // (nothing loaded yet) and ENDED (program finished — the user needs these
  // controls to start a new program, and there is no Stop button in ENDED).
  const programMenuDisabled =
    logicalState === 'READY' ||
    logicalState === 'EXECUTING' ||
    logicalState === 'WAITING_FOR_INPUT';

  // Classify the current deployment so that users can tell at a glance
  // whether they are using the production version or a PR/dev build, and
  // jump back to the originating pull request when applicable.
  const buildInfo = React.useMemo(() => getBuildInfo(), []);

  const handleProgramMenuClose = () => setProgramAnchor(null);

  // On small viewports the buttons collapse to icon-only. Keep the text
  // visually hidden on `xs` so it still provides an accessible name,
  // while showing it inline on larger screens. This avoids the toolbar
  // overflowing on phones / tablets where 9+ full-text buttons plus
  // status chips can't possibly fit on one row.
  const responsiveLabel = (text: string) => (
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
          src={prefersDarkMode ? logoDark : logoBright}
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
                href={buildInfo.prUrl ?? ''}
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
          {buildInfo.kind === 'archive-build' && (
            <Tooltip
              title="This is an archived per-commit build (a promoted snapshot or a candidate), not the live production version."
              arrow
              placement="bottom"
            >
              <Chip
                id="candidate-build-chip"
                size="small"
                label="ARCHIVED"
                className="candidate-chip"
                aria-label="Archived build"
                sx={{ fontWeight: 'bold' }}
              />
            </Tooltip>
          )}
        </Typography>
        <Tooltip title="The current status of the CPU" arrow placement="top">
          <div>
            <CpuStatusDisplay status={status} />
          </div>
        </Tooltip>
        {/* Execution controls — rendered contextually per logical state. Wrapped in
            a fixed-min-width container so the toolbar doesn't shift when the set
            of visible buttons changes between states. */}
        {/* Load button — always visible in the header. Execution controls
            (Step / Multi Step / Run / Pause / Stop) live in the floating
            RunControlsToolbar overlay mounted from Simulator.tsx. */}
        <Tooltip
          title="Load the current code into the simulator (F2)"
          arrow
          placement="top"
        >
          <Button
            color="inherit"
            className="load-button"
            id="load-button"
            onClick={() => onLoadClick()}
            startIcon={<UploadIcon />}
            disabled={!loadEnabled}
            sx={responsiveButtonSx}
          >
            {responsiveLabel('Load')}
          </Button>
        </Tooltip>
        <Tooltip
          title="Manage the program: new, open, save, or load the example"
          arrow
          placement="top"
        >
          <span>
            <Button
              id="program-menu-button"
              color="inherit"
              className="program-menu-button"
              startIcon={<FolderOpenIcon />}
              endIcon={<ArrowDropDownIcon />}
              disabled={programMenuDisabled}
              onClick={(e) => setProgramAnchor(e.currentTarget)}
              aria-haspopup="true"
              aria-controls={menuOpen ? 'program-menu' : undefined}
              aria-expanded={menuOpen ? 'true' : undefined}
              sx={responsiveButtonSx}
            >
              {responsiveLabel('Program')}
            </Button>
          </span>
        </Tooltip>
        <Menu
          id="program-menu"
          anchorEl={programAnchor}
          open={menuOpen}
          onClose={handleProgramMenuClose}
          slotProps={{ list: { 'aria-labelledby': 'program-menu-button' } }}
        >
          <MenuItem
            id="clear-code-button"
            onClick={() => {
              onClearClick();
              handleProgramMenuClose();
            }}
          >
            <ListItemIcon>
              <DeleteForeverIcon fontSize="small" />
            </ListItemIcon>
            <ListItemText>New</ListItemText>
          </MenuItem>
          <Divider />
          <MenuItem
            id="load-code-button"
            onClick={() => {
              onOpenClick();
              handleProgramMenuClose();
            }}
          >
            <ListItemIcon>
              <UploadIcon fontSize="small" />
            </ListItemIcon>
            <ListItemText>Open…</ListItemText>
          </MenuItem>
          <MenuItem
            id="save-code-button"
            onClick={() => {
              onSaveClick();
              handleProgramMenuClose();
            }}
          >
            <ListItemIcon>
              <DownloadIcon fontSize="small" />
            </ListItemIcon>
            <ListItemText>Save…</ListItemText>
          </MenuItem>
          <Divider />
          <MenuItem
            id="restore-sample-button"
            onClick={() => {
              onRestoreClick();
              handleProgramMenuClose();
            }}
          >
            <ListItemIcon>
              <RestartAltIcon fontSize="small" />
            </ListItemIcon>
            <ListItemText>Load Example</ListItemText>
          </MenuItem>
        </Menu>
        <Tooltip title="Settings" arrow placement="top">
          <IconButton
            color="inherit"
            className="settings-button"
            id="settings-button"
            aria-label="Open settings"
            onClick={() => {
              setSettingsDialogOpen(true);
            }}
          >
            <SettingsIcon />
          </IconButton>
        </Tooltip>
        <SettingsDialog
          open={settingsDialogOpen}
          handleClose={() => setSettingsDialogOpen(false)}
          status={status}
          onCacheConfigChange={onCacheConfigChange}
          viMode={viMode}
          setViMode={setViMode}
          fontSize={fontSize}
          setFontSize={setFontSize}
          accordionAlerts={accordionAlerts}
          setAccordionAlerts={setAccordionAlerts}
          pipelineColors={pipelineColors}
          setPipelineColors={setPipelineColors}
          themeMode={themeMode}
          setThemeMode={setThemeMode}
          forwarding={forwarding}
          setForwarding={setForwarding}
          delaySlot={delaySlot}
          setDelaySlot={setDelaySlot}
          stepStride={stepStride}
          setStepStride={setStepStride}
          executionDelayMs={executionDelayMs}
          setExecutionDelayMs={setExecutionDelayMs}
        />
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
          ver={version}
        />
      </ToolBar>
    </AppBar>
  );
}
