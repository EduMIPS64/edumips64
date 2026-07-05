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
import type { CpuStatus, SimulatorResult } from '../simulator/protocol';

// Approximate rendered size of the floating pill, used to keep it fully
// on-screen when positioning, dragging, and re-clamping on window resize.
const TOOLBAR_W = 280;
const TOOLBAR_H = 48;

// Default horizontal position: horizontally centered. Recomputed on resize
// as long as the user hasn't dragged the toolbar to a spot of their own.
const centeredX = (): number =>
  typeof window !== 'undefined'
    ? Math.max(0, Math.round(window.innerWidth / 2) - TOOLBAR_W / 2)
    : 100;

interface RunControlsToolbarProps {
  onStepClick: (n: number) => void;
  onRunClick: () => void;
  onPauseClick: () => void;
  onStopClick: () => void;
  status: CpuStatus;
  executing: boolean;
  inputRequest: SimulatorResult | null;
  multiStepCount: number;
}

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
export default function RunControlsToolbar({
  onStepClick,
  onRunClick,
  onPauseClick,
  onStopClick,
  status,
  executing,
  inputRequest,
  multiStepCount,
}: RunControlsToolbarProps) {
  const logicalState = deriveLogicalState(status, executing, inputRequest);

  // Drag position — persists across logical-state changes as long as the
  // component remains mounted.  Default: horizontally centered, below header.
  const [pos, setPos] = React.useState(() => ({ x: centeredX(), y: 80 }));

  const isDraggingRef = React.useRef(false);
  const dragOffsetRef = React.useRef({ x: 0, y: 0 });
  // Whether the user has repositioned the toolbar. Until they do, it stays
  // centered and follows window resizes; once dragged, it keeps its spot
  // (still re-clamped so a shrinking window can never hide it).
  const hasDraggedRef = React.useRef(false);

  // Keep the toolbar on-screen when the window is resized. Without this the
  // fixed-position pill keeps the x it had at mount, so narrowing the window
  // pushes it off the right edge and it appears "lost".
  React.useEffect(() => {
    const onResize = () => {
      setPos((prev) => {
        const targetX = hasDraggedRef.current ? prev.x : centeredX();
        return {
          x: Math.max(0, Math.min(window.innerWidth - TOOLBAR_W, targetX)),
          y: Math.max(0, Math.min(window.innerHeight - TOOLBAR_H, prev.y)),
        };
      });
    };
    window.addEventListener('resize', onResize);
    return () => window.removeEventListener('resize', onResize);
  }, []);

  const handlePointerDown = (e: React.PointerEvent<HTMLDivElement>) => {
    isDraggingRef.current = true;
    dragOffsetRef.current = { x: e.clientX - pos.x, y: e.clientY - pos.y };
    e.currentTarget.setPointerCapture(e.pointerId);
  };

  const handlePointerMove = (e: React.PointerEvent<HTMLDivElement>) => {
    if (!isDraggingRef.current) return;
    // A real drag has occurred: stop auto-centering on resize.
    hasDraggedRef.current = true;
    // Constrain so the toolbar stays fully on-screen.
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
        title="Runs a single step of simulation (F9)"
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
        title={`Run ${multiStepCount} steps of simulation (configurable in Settings) (F10)`}
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

      <Tooltip
        title="Run until the simulation ends (F8)"
        arrow
        placement="bottom"
      >
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

      <Tooltip title="Pause the simulation (F8)" arrow placement="bottom">
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
            : 'Stop the simulation and reset the CPU (Esc)'
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
