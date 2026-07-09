import React from 'react';

import Box from '@mui/material/Box';
import Typography from '@mui/material/Typography';
import IconButton from '@mui/material/IconButton';
import Tooltip from '@mui/material/Tooltip';
import useMediaQuery from '@mui/material/useMediaQuery';
import ChevronLeftIcon from '@mui/icons-material/ChevronLeft';
import ChevronRightIcon from '@mui/icons-material/ChevronRight';
import KeyboardArrowDownIcon from '@mui/icons-material/KeyboardArrowDown';
import KeyboardArrowUpIcon from '@mui/icons-material/KeyboardArrowUp';

import type { WorkspaceLayout } from '../settings/schema';
import {
  MIN_RIGHT_WIDTH_PCT,
  MAX_RIGHT_WIDTH_PCT,
  MIN_BOTTOM_HEIGHT_PCT,
  MAX_BOTTOM_HEIGHT_PCT,
} from '../settings/schema';

/*
 * WorkspaceLayout — the resizable, collapsible IDE-style shell.
 *
 * Desktop layout (three regions, pinned to the viewport):
 *
 *   ┌──────────────────────────┬──────────┐
 *   │                          │          │
 *   │   left  (code editor)    │  right   │  ← upper region
 *   │                          │ (widgets)│
 *   ├──────────────────────────┴──────────┤
 *   │        bottom (Cycles diagram)      │  ← full-width bottom region
 *   └─────────────────────────────────────┘
 *
 * A vertical drag handle between left and right resizes the widgets column;
 * a horizontal handle between the upper and bottom regions resizes the
 * Cycles diagram. Each region has a collapse toggle and a minimum size, so
 * neither can be dragged small enough to hide the code editor. The geometry
 * (two percentages + two collapse flags) is owned by the parent and
 * persisted to localStorage.
 *
 * The proportional split is done with flexBasis:0 + flexGrow set to the
 * region's percentage, so the two siblings divide the *remaining* space (the
 * fixed-size drag handle is subtracted first) in exactly the stored ratio —
 * no handle-thickness arithmetic needed.
 *
 * Below 900px the split doesn't fit a phone, so the regions stack vertically
 * in normal document flow and the handles/collapse toggles are dropped.
 */

// Fixed sizes, in px.
const HANDLE = 6; // drag-handle thickness
const BAR = 34; // collapsed-region bar / header thickness
const KEYBOARD_STEP = 3; // % change per arrow-key press on a focused handle

const clamp = (v: number, min: number, max: number) =>
  Math.min(max, Math.max(min, v));

interface WorkspaceLayoutProps {
  left: React.ReactNode;
  right: React.ReactNode;
  bottom: React.ReactNode;
  /** Title shown on the bottom region's header bar. */
  bottomTitle: React.ReactNode;
  /** Persisted geometry. */
  layout: WorkspaceLayout;
  /** Commit a new geometry (persists to localStorage). */
  onLayoutChange: (next: WorkspaceLayout) => void;
}

const WorkspaceLayoutView = ({
  left,
  right,
  bottom,
  bottomTitle,
  layout,
  onLayoutChange,
}: WorkspaceLayoutProps) => {
  // Below this width the resizable split is replaced by a stacked flow.
  const isSmall = useMediaQuery('(max-width:900px)');

  // `live` drives rendering during a drag so we don't write to localStorage on
  // every pointermove; the persisted value is committed on pointer-up. Keep it
  // in sync when the persisted layout changes from elsewhere (e.g. reset).
  const [live, setLive] = React.useState(layout);
  React.useEffect(() => setLive(layout), [layout]);
  const liveRef = React.useRef(live);
  liveRef.current = live;

  const containerRef = React.useRef<HTMLDivElement | null>(null);
  // Which handle is being dragged, if any.
  const dragging = React.useRef<null | 'vertical' | 'horizontal'>(null);

  const applyLive = (partial: Partial<WorkspaceLayout>) =>
    setLive((prev) => ({ ...prev, ...partial }));

  const commit = (next: WorkspaceLayout) => {
    setLive(next);
    onLayoutChange(next);
  };

  // --- Drag handling -------------------------------------------------------

  const onPointerMove = (e: React.PointerEvent) => {
    if (!dragging.current || !containerRef.current) return;
    const rect = containerRef.current.getBoundingClientRect();
    if (dragging.current === 'vertical') {
      // Right column width = distance from the pointer to the right edge.
      const pct = ((rect.right - e.clientX) / rect.width) * 100;
      applyLive({
        rightWidthPct: clamp(pct, MIN_RIGHT_WIDTH_PCT, MAX_RIGHT_WIDTH_PCT),
      });
    } else {
      // Bottom region height = distance from the pointer to the bottom edge.
      const pct = ((rect.bottom - e.clientY) / rect.height) * 100;
      applyLive({
        bottomHeightPct: clamp(
          pct,
          MIN_BOTTOM_HEIGHT_PCT,
          MAX_BOTTOM_HEIGHT_PCT,
        ),
      });
    }
  };

  const endDrag = (e: React.PointerEvent) => {
    if (!dragging.current) return;
    dragging.current = null;
    try {
      e.currentTarget.releasePointerCapture(e.pointerId);
    } catch {
      // Pointer capture may already be gone; ignore.
    }
    // Persist the geometry reached at the end of the drag.
    onLayoutChange(liveRef.current);
  };

  const startDrag =
    (which: 'vertical' | 'horizontal') => (e: React.PointerEvent) => {
      dragging.current = which;
      e.currentTarget.setPointerCapture(e.pointerId);
      e.preventDefault();
    };

  // Keyboard resize on a focused handle (accessibility).
  const onHandleKeyDown =
    (which: 'vertical' | 'horizontal') => (e: React.KeyboardEvent) => {
      if (which === 'vertical') {
        const dir =
          e.key === 'ArrowLeft' ? 1 : e.key === 'ArrowRight' ? -1 : 0;
        if (!dir) return;
        e.preventDefault();
        commit({
          ...liveRef.current,
          rightWidthPct: clamp(
            liveRef.current.rightWidthPct + dir * KEYBOARD_STEP,
            MIN_RIGHT_WIDTH_PCT,
            MAX_RIGHT_WIDTH_PCT,
          ),
        });
      } else {
        const dir = e.key === 'ArrowUp' ? 1 : e.key === 'ArrowDown' ? -1 : 0;
        if (!dir) return;
        e.preventDefault();
        commit({
          ...liveRef.current,
          bottomHeightPct: clamp(
            liveRef.current.bottomHeightPct + dir * KEYBOARD_STEP,
            MIN_BOTTOM_HEIGHT_PCT,
            MAX_BOTTOM_HEIGHT_PCT,
          ),
        });
      }
    };

  // Bottom-region header bar (title + collapse toggle), shared by the desktop
  // and mobile layouts so the Cycles region is always clearly labelled.
  const bottomHeader = (
    <Box
      sx={{
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        flexShrink: 0,
        minHeight: BAR,
        px: 1,
      }}
    >
      <Typography
        variant="subtitle2"
        sx={{ fontWeight: 600, color: 'primary.main' }}
      >
        {bottomTitle}
      </Typography>
      <Tooltip
        title={live.bottomCollapsed ? 'Expand cycles' : 'Collapse cycles'}
        placement="left"
      >
        <IconButton
          size="small"
          aria-label={live.bottomCollapsed ? 'Expand cycles' : 'Collapse cycles'}
          data-testid="toggle-bottom"
          onClick={() =>
            commit({ ...live, bottomCollapsed: !live.bottomCollapsed })
          }
        >
          {live.bottomCollapsed ? (
            <KeyboardArrowUpIcon fontSize="small" />
          ) : (
            <KeyboardArrowDownIcon fontSize="small" />
          )}
        </IconButton>
      </Tooltip>
    </Box>
  );

  // --- Mobile: stacked, non-resizable flow ---------------------------------

  if (isSmall) {
    return (
      <Box id="main-grid" data-testid="workspace-layout">
        <Box id="left-panel" sx={{ height: '60vh', minHeight: 320 }}>
          {left}
        </Box>
        <Box
          id="right-panel"
          sx={{
            borderTop: 1,
            borderColor: 'divider',
            bgcolor: 'background.default',
          }}
        >
          {right}
        </Box>
        <Box
          id="bottom-panel"
          sx={{
            borderTop: 1,
            borderColor: 'divider',
            display: 'flex',
            flexDirection: 'column',
            // Collapsed → just the header; otherwise a bounded, scrollable
            // region so the stacked page stays navigable on a phone.
            height: live.bottomCollapsed ? 'auto' : '60vh',
          }}
        >
          {bottomHeader}
          {!live.bottomCollapsed && (
            <Box sx={{ flex: 1, minHeight: 0, px: 1, pb: 1 }}>{bottom}</Box>
          )}
        </Box>
      </Box>
    );
  }

  // --- Desktop: resizable split --------------------------------------------

  const { rightWidthPct, bottomHeightPct, rightCollapsed, bottomCollapsed } =
    live;

  return (
    <Box
      id="main-grid"
      ref={containerRef}
      data-testid="workspace-layout"
      onPointerMove={onPointerMove}
      onPointerUp={endDrag}
      onPointerLeave={endDrag}
      sx={{
        display: 'flex',
        flexDirection: 'column',
        height: 'calc(100vh - 64px)',
        overflow: 'hidden',
      }}
    >
      {/* Upper region: code editor + right widgets column. */}
      <Box
        id="upper-region"
        sx={{
          display: 'flex',
          flexDirection: 'row',
          minHeight: 0,
          // Divide the vertical space with the bottom region in the stored
          // ratio (flexBasis:0 + proportional grow). When the bottom region
          // is collapsed the upper region takes all remaining space.
          flexGrow: bottomCollapsed ? 1 : 100 - bottomHeightPct,
          flexShrink: 1,
          flexBasis: 0,
        }}
      >
        <Box
          id="left-panel"
          sx={{
            flexGrow: rightCollapsed ? 1 : 100 - rightWidthPct,
            flexShrink: 1,
            flexBasis: 0,
            minWidth: 0,
            height: '100%',
          }}
        >
          {left}
        </Box>

        {rightCollapsed ? (
          // Collapsed: a thin bar on the right with an expand button.
          <Box
            sx={{
              width: BAR,
              flexShrink: 0,
              display: 'flex',
              flexDirection: 'column',
              alignItems: 'center',
              borderLeft: 1,
              borderColor: 'divider',
              bgcolor: 'background.default',
            }}
          >
            <Tooltip title="Expand panels" placement="left">
              <IconButton
                size="small"
                aria-label="Expand panels"
                data-testid="expand-right"
                onClick={() => commit({ ...live, rightCollapsed: false })}
              >
                <ChevronLeftIcon fontSize="small" />
              </IconButton>
            </Tooltip>
            <Typography
              variant="caption"
              sx={{
                mt: 1,
                color: 'text.secondary',
                writingMode: 'vertical-rl',
                transform: 'rotate(180deg)',
                userSelect: 'none',
              }}
            >
              Panels
            </Typography>
          </Box>
        ) : (
          <>
            {/* Vertical drag handle. */}
            <Box
              role="separator"
              aria-orientation="vertical"
              aria-label="Resize panels"
              aria-valuenow={Math.round(rightWidthPct)}
              aria-valuemin={MIN_RIGHT_WIDTH_PCT}
              aria-valuemax={MAX_RIGHT_WIDTH_PCT}
              tabIndex={0}
              data-testid="resize-vertical"
              onPointerDown={startDrag('vertical')}
              onKeyDown={onHandleKeyDown('vertical')}
              sx={{
                width: HANDLE,
                flexShrink: 0,
                cursor: 'col-resize',
                bgcolor: 'divider',
                transition: 'background-color 120ms',
                '&:hover, &:focus-visible': { bgcolor: 'primary.main' },
                touchAction: 'none',
              }}
            />
            <Box
              id="right-panel"
              sx={{
                flexGrow: rightWidthPct,
                flexShrink: 1,
                flexBasis: 0,
                minWidth: 0,
                height: '100%',
                display: 'flex',
                flexDirection: 'column',
                // Distinct from the DashboardCard's own paper background so
                // each card's elevation/shadow reads as a floating tile
                // instead of blending into the panel (matches the
                // dashboard-layout proposal's right-panel treatment).
                bgcolor: 'background.default',
              }}
            >
              {/* Slim header with the collapse toggle. */}
              <Box
                sx={{
                  display: 'flex',
                  justifyContent: 'flex-end',
                  alignItems: 'center',
                  flexShrink: 0,
                  minHeight: BAR,
                  px: 0.5,
                  borderBottom: 1,
                  borderColor: 'divider',
                }}
              >
                <Tooltip title="Collapse panels" placement="left">
                  <IconButton
                    size="small"
                    aria-label="Collapse panels"
                    data-testid="collapse-right"
                    onClick={() => commit({ ...live, rightCollapsed: true })}
                  >
                    <ChevronRightIcon fontSize="small" />
                  </IconButton>
                </Tooltip>
              </Box>
              <Box sx={{ flex: 1, minHeight: 0, overflow: 'auto' }}>
                {right}
              </Box>
            </Box>
          </>
        )}
      </Box>

      {/* Horizontal drag handle (hidden when the bottom region is collapsed,
          since its header already sits flush against the upper region). */}
      {!bottomCollapsed && (
        <Box
          role="separator"
          aria-orientation="horizontal"
          aria-label="Resize cycles diagram"
          aria-valuenow={Math.round(bottomHeightPct)}
          aria-valuemin={MIN_BOTTOM_HEIGHT_PCT}
          aria-valuemax={MAX_BOTTOM_HEIGHT_PCT}
          tabIndex={0}
          data-testid="resize-horizontal"
          onPointerDown={startDrag('horizontal')}
          onKeyDown={onHandleKeyDown('horizontal')}
          sx={{
            height: HANDLE,
            flexShrink: 0,
            cursor: 'row-resize',
            bgcolor: 'divider',
            transition: 'background-color 120ms',
            '&:hover, &:focus-visible': { bgcolor: 'primary.main' },
            touchAction: 'none',
          }}
        />
      )}

      {/* Bottom region: full-width Cycles diagram. */}
      <Box
        id="bottom-panel"
        sx={{
          display: 'flex',
          flexDirection: 'column',
          minHeight: 0,
          flexGrow: bottomCollapsed ? 0 : bottomHeightPct,
          flexShrink: bottomCollapsed ? 0 : 1,
          flexBasis: bottomCollapsed ? 'auto' : 0,
          borderTop: bottomCollapsed ? 1 : 0,
          borderColor: 'divider',
          bgcolor: 'background.paper',
        }}
      >
        {/* Header bar: title + collapse toggle (shared with the mobile
            layout). */}
        {bottomHeader}
        {/* Content (hidden when collapsed so only the header bar shows). */}
        {!bottomCollapsed && (
          <Box sx={{ flex: 1, minHeight: 0, px: 1, pb: 1 }}>{bottom}</Box>
        )}
      </Box>
    </Box>
  );
};

export default WorkspaceLayoutView;
