import React from 'react';
import Box from '@mui/material/Box';
import Tooltip from '@mui/material/Tooltip';

import ErrorOutlineOutlinedIcon from '@mui/icons-material/ErrorOutlineOutlined';
import WarningAmberOutlinedIcon from '@mui/icons-material/WarningAmberOutlined';
import MemoryIcon from '@mui/icons-material/Memory';

/**
 * VS Code-style bottom status bar.
 *
 * Always visible, one line tall. Shows, left to right:
 *  - CPU status (colored segment, mirrors the logical simulator state);
 *  - live execution counters (cycles, instructions, CPI);
 *  - problem counts (errors / warnings) — clicking focuses the Issues tab;
 *  - forwarding on/off and the simulator core version on the right.
 *
 * The colored CPU segment doubles as the aria-live status region that used
 * to be provided by the CpuStatusDisplay chip in the header.
 */

const STATE_COLORS = {
  READY: '#1a7f37',
  RUNNING: '#9a6700',
  STOPPED: '#cf222e',
  STOPPING: '#cf222e',
};

const Item = ({ id, title, onClick, children }) => {
  const content = (
    <Box
      id={id}
      onClick={onClick}
      sx={{
        display: 'flex',
        alignItems: 'center',
        gap: 0.5,
        px: 1,
        height: '100%',
        whiteSpace: 'nowrap',
        cursor: onClick ? 'pointer' : 'default',
        '&:hover': onClick ? { backgroundColor: 'rgba(255,255,255,0.12)' } : {},
      }}
    >
      {children}
    </Box>
  );
  return title ? (
    <Tooltip title={title} arrow placement="top">
      {content}
    </Tooltip>
  ) : (
    content
  );
};

export default function StatusBar({
  status,
  stats,
  parsingErrors,
  forwarding,
  version,
  onProblemsClick,
}) {
  const errors = (parsingErrors || []).filter((e) => !e.isWarning).length;
  const warnings = (parsingErrors || []).filter((e) => e.isWarning).length;
  const cycles = stats?.cycles ?? 0;
  const instructions = stats?.instructions ?? 0;
  const cpi = instructions === 0 ? '—' : (cycles / instructions).toFixed(2);
  const stateColor = STATE_COLORS[status] || 'rgba(255,255,255,0.25)';

  return (
    <Box
      id="status-bar"
      sx={{
        height: 26,
        flexShrink: 0,
        display: 'flex',
        alignItems: 'stretch',
        fontSize: '0.75rem',
        color: '#ffffff',
        bgcolor: (theme) =>
          theme.palette.mode === 'dark' ? '#0e639c' : '#0969da',
        userSelect: 'none',
      }}
    >
      <Item id="statusbar-cpu" title="The current status of the CPU">
        <Box
          component="span"
          role="status"
          aria-live="polite"
          aria-atomic="true"
          aria-label={`CPU status: ${status}`}
          sx={{
            display: 'flex',
            alignItems: 'center',
            gap: 0.5,
            backgroundColor: stateColor,
            alignSelf: 'stretch',
            px: 1,
            mx: -1,
            fontWeight: 700,
          }}
        >
          <MemoryIcon sx={{ fontSize: 14 }} />
          {status}
        </Box>
      </Item>
      <Item id="statusbar-cycles" title="Cycles executed">
        <span>{cycles} cycles</span>
      </Item>
      <Item id="statusbar-instructions" title="Instructions executed">
        <span>{instructions} instr</span>
      </Item>
      <Item id="statusbar-cpi" title="Cycles per instruction">
        <span>CPI {cpi}</span>
      </Item>
      <Item
        id="statusbar-problems"
        title="Problems in the current program (click to open Issues)"
        onClick={onProblemsClick}
      >
        <ErrorOutlineOutlinedIcon sx={{ fontSize: 14 }} />
        <span id="errorCount">{errors}</span>
        <WarningAmberOutlinedIcon sx={{ fontSize: 14 }} />
        <span id="warningCount">{warnings}</span>
      </Item>
      <Box sx={{ flexGrow: 1 }} />
      <Item
        id="statusbar-forwarding"
        title="Whether forwarding is enabled (configurable in Settings)"
      >
        <span>Forwarding: {forwarding ? 'on' : 'off'}</span>
      </Item>
      <Item id="statusbar-version" title="Simulator core version">
        <span>EduMIPS64 {version}</span>
      </Item>
    </Box>
  );
}
