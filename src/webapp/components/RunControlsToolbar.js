import React from 'react';

import Paper from '@mui/material/Paper';
import Box from '@mui/material/Box';
import IconButton from '@mui/material/IconButton';
import Tooltip from '@mui/material/Tooltip';

import SkipNextIcon from '@mui/icons-material/SkipNext';
import FastForwardIcon from '@mui/icons-material/FastForward';
import PlayCircleIcon from '@mui/icons-material/PlayCircle';
import PauseCircleIcon from '@mui/icons-material/PauseCircle';
import StopCircleIcon from '@mui/icons-material/StopCircle';
import DragIndicatorIcon from '@mui/icons-material/DragIndicator';

import { deriveLogicalState } from '../simulatorState';

/**
 * Floating, draggable debug-style toolbar for execution run controls.
 *
 * Modeled after the VSCode debug toolbar: icon-only buttons in a compact
 * rounded bar that floats over the content area and can be repositioned by
 * dragging the grip handle at the left edge.
 *
 * Visibility rules (mirrors the logical-state matrix in the design doc):
 *   EMPTY / ENDED / WAITING_FOR_INPUT → toolbar not rendered at all.
 *   READY     → Step, Multi Step, Run All, Stop enabled; Pause disabled.
 *   EXECUTING → Pause enabled; Step, Multi Step, Run All, Stop disabled.
 *               Stop tooltip changes to "Pause before stopping" when disabled.
 */
export default function RunControlsToolbar(props) {
  const {
    onStepClick,
    onRunClick,
    onPauseClick,
    onStopClick,
    status,
    executing,
    inputRequest,
    multiStepCount,
  } = props;

  const logicalState = deriveLogicalState(status, executing, inputRequest);

  // Drag position — persists across logical-state changes as long as the
  // component remains mounted.  Default: horizontally centered, below header.
  const [pos, setPos] = React.useState(() => ({
    x:
      typeof window !== 'undefined'
        ? Math.max(0, Math.round(window.innerWidth / 2) - 130)
        : 100,
    y: 80,
  }));

  const isDraggingRef = React.useRef(false);
  const dragOffsetRef = React.useRef({ x: 0, y: 0 });

  const handlePointerDown = (e) => {
    isDraggingRef.current = true;
    dragOffsetRef.current = { x: e.clientX - pos.x, y: e.clientY - pos.y };
    e.currentTarget.setPointerCapture(e.pointerId);
  };

  const handlePointerMove = (e) => {
    if (!isDraggingRef.current) return;
    // Constrain so the toolbar stays fully on-screen.
    const TOOLBAR_W = 280;
    const TOOLBAR_H = 48;
    const x = Math.max(
      0,
      Math.min(
        window.innerWidth - TOOLBAR_W,
        e.clientX - dragOffsetRef.current.x,
      ),
    );
    const y = Math.max(
      0,
      Math.min(
        window.innerHeight - TOOLBAR_H,
        e.clientY - dragOffsetRef.current.y,
      ),
    );
    setPos({ x, y });
  };

  const handlePointerUp = () => {
    isDraggingRef.current = false;
  };

  // Disabled state per logical state — all five buttons are always rendered
  // when the toolbar is visible; they are greyed out when not actionable.
  const stepDisabled = logicalState !== 'READY';
  const multiStepDisabled = logicalState !== 'READY';
  const runDisabled = logicalState !== 'READY';
  const pauseDisabled = logicalState !== 'EXECUTING';
  const stopDisabled = logicalState === 'EXECUTING';

  // Toolbar is not shown outside of an active simulation session.
  if (
    logicalState === 'EMPTY' ||
    logicalState === 'ENDED' ||
    logicalState === 'WAITING_FOR_INPUT'
  ) {
    return null;
  }

  return (
    <Paper
      id="run-controls-toolbar"
      elevation={8}
      sx={{
        position: 'fixed',
        left: pos.x,
        top: pos.y,
        display: 'flex',
        alignItems: 'center',
        borderRadius: '24px',
        px: 0.5,
        py: 0.5,
        zIndex: 1200,
        userSelect: 'none',
        // Prevent text-selection cursor leaking through during drag.
        WebkitUserSelect: 'none',
      }}
    >
      {/* Drag handle — all pointer events captured here for smooth dragging. */}
      <Box
        onPointerDown={handlePointerDown}
        onPointerMove={handlePointerMove}
        onPointerUp={handlePointerUp}
        sx={{
          cursor: 'grab',
          display: 'flex',
          alignItems: 'center',
          color: 'text.secondary',
          px: 0.5,
          '&:active': { cursor: 'grabbing' },
        }}
        aria-label="Drag toolbar"
        role="presentation"
      >
        <DragIndicatorIcon fontSize="small" />
      </Box>

      {/* Tooltip wraps a <span> so it still fires while the button is disabled. */}
      <Tooltip
        title="Runs a single step of simulation"
        arrow
        placement="bottom"
      >
        <span>
          <IconButton
            id="step-button"
            aria-label="Single Step"
            size="small"
            onClick={() => onStepClick(1)}
            disabled={stepDisabled}
          >
            <SkipNextIcon />
          </IconButton>
        </span>
      </Tooltip>

      <Tooltip
        title={`Run ${multiStepCount} steps of simulation (configurable in Settings)`}
        arrow
        placement="bottom"
      >
        <span>
          <IconButton
            id="multi-step-button"
            aria-label="Multi Step"
            size="small"
            onClick={() => onStepClick(multiStepCount)}
            disabled={multiStepDisabled}
          >
            <FastForwardIcon />
          </IconButton>
        </span>
      </Tooltip>

      <Tooltip title="Run until the simulation ends" arrow placement="bottom">
        <span>
          <IconButton
            id="run-button"
            aria-label="Run All"
            size="small"
            onClick={onRunClick}
            disabled={runDisabled}
          >
            <PlayCircleIcon />
          </IconButton>
        </span>
      </Tooltip>

      <Tooltip title="Pause the simulation" arrow placement="bottom">
        <span>
          <IconButton
            id="pause-button"
            aria-label="Pause"
            size="small"
            onClick={onPauseClick}
            disabled={pauseDisabled}
          >
            <PauseCircleIcon />
          </IconButton>
        </span>
      </Tooltip>

      <Tooltip
        title={
          stopDisabled
            ? 'Pause before stopping'
            : 'Stop the simulation and reset the CPU'
        }
        arrow
        placement="bottom"
      >
        {/* Tooltip requires a non-disabled child to function; wrap in span. */}
        <span>
          <IconButton
            id="stop-button"
            aria-label="Stop"
            size="small"
            onClick={onStopClick}
            disabled={stopDisabled}
          >
            <StopCircleIcon />
          </IconButton>
        </span>
      </Tooltip>
    </Paper>
  );
}
