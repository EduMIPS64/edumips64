import React from 'react';

import Box from '@mui/material/Box';
import Typography from '@mui/material/Typography';

import type {
  CyclesDiagram,
  CycleDiagramElement,
} from '../simulator/protocol';
import type { PipelineColors } from '../settings/schema';
import { DEFAULT_PIPELINE_COLORS } from '../settings/schema';

/*
 * Cycles widget for the Web UI (roadmap item U.7).
 *
 * A temporal instruction-vs-cycle diagram, mirroring the Swing `GUICycles`
 * widget: one row per instruction that entered the pipeline (in fetch
 * order), one column per CPU cycle, and each cell showing the pipeline
 * state the instruction occupied at that cycle (IF, ID, EX, MEM, WB, the
 * FP functional-unit stages, or a stall tag).
 *
 * The data comes pre-computed from the worker: the Java side serializes the
 * same `CycleBuilder` element list the Swing widget paints (see
 * `ResultFactory.getCycles()`), so the two UIs can never drift apart on
 * stall semantics.
 *
 * Colors reuse the per-stage palette shared with the Pipeline widget. One
 * deliberate deviation from Swing: all stall tags (RAW / WAW / StDiv / StEx
 * / StFun / Str / StAdd / StMul) are painted with the dedicated `Stall`
 * color instead of the stalled stage's color, matching how the Web UI's
 * Pipeline widget highlights stalls. A squashed fetch (the " " tag that
 * follows an IF whose instruction never entered ID) is painted like the IF
 * cell it prolongs, as in Swing.
 */

// Cell geometry, in px. Kept small so a typical program fits without
// scrolling; the container scrolls in both directions for longer runs.
const CELL_W = 34;
const CELL_H = 22;
const NAME_COL_W = 160;

// Rendering caps. The diagram is drawn with one DOM node per occupied cell,
// so an unbounded `Run` of a long program (tens of thousands of cycles)
// would freeze the tab. When the history exceeds these bounds we keep the
// *most recent* window — the part the user is stepping through — and say so
// in a note above the grid. Swing avoids this by clip-based repainting,
// which doesn't translate to the DOM.
const MAX_COLUMNS = 2000;
const MAX_ROWS = 500;

// States that map to a fixed palette entry. The FP Adder/Multiplier stages
// and the divider per-cycle counters (D00..D24) are matched by pattern in
// `colorKeyForState` below.
const STATIC_STATE_COLOR_KEYS: Record<string, keyof PipelineColors> = {
  IF: 'IF',
  ' ': 'IF',
  ID: 'ID',
  EX: 'EX',
  MEM: 'MEM',
  WB: 'WB',
  DIV: 'FPDivider',
  RAW: 'Stall',
  WAW: 'Stall',
  StDiv: 'Stall',
  StEx: 'Stall',
  StFun: 'Stall',
  Str: 'Stall',
  StAdd: 'Stall',
  StMul: 'Stall',
};

const FP_ADDER_RE = /^A[1-4]$/;
const FP_MULTIPLIER_RE = /^M[1-7]$/;
const FP_DIV_COUNT_RE = /^D[0-9][0-9]$/;

export const colorKeyForState = (
  state: string,
): keyof PipelineColors | null => {
  const staticKey = STATIC_STATE_COLOR_KEYS[state];
  if (staticKey) {
    return staticKey;
  }
  if (FP_ADDER_RE.test(state)) {
    return 'FPAdder';
  }
  if (FP_MULTIPLIER_RE.test(state)) {
    return 'FPMultiplier';
  }
  if (FP_DIV_COUNT_RE.test(state)) {
    return 'FPDivider';
  }
  return null;
};

// A row of the diagram after windowing: the element plus the first cycle of
// its states that is inside the rendered column window.
interface VisibleRow {
  element: CycleDiagramElement;
  // Index into element.states of the first rendered state.
  firstStateIndex: number;
  // 1-based cycle of that first rendered state.
  firstCycle: number;
}

/**
 * Apply the row/column rendering caps: keep only the last MAX_COLUMNS
 * cycles and, of the rows that still have at least one visible cell, the
 * last MAX_ROWS.
 */
const applyWindow = (diagram: CyclesDiagram) => {
  const minCycle = Math.max(1, diagram.time - MAX_COLUMNS + 1);
  const rows: VisibleRow[] = [];
  for (const element of diagram.elements) {
    const lastCycle = element.startTime + element.states.length - 1;
    if (lastCycle < minCycle) {
      continue; // Entirely left of the window.
    }
    const firstStateIndex = Math.max(0, minCycle - element.startTime);
    rows.push({
      element,
      firstStateIndex,
      firstCycle: element.startTime + firstStateIndex,
    });
  }
  const hiddenRows = Math.max(0, rows.length - MAX_ROWS);
  return {
    minCycle,
    rows: rows.slice(hiddenRows),
    truncated: hiddenRows > 0 || minCycle > 1,
  };
};

interface CyclesProps {
  cycles: CyclesDiagram;
  colors?: PipelineColors | undefined;
}

const CyclesWidget = ({ cycles, colors }: CyclesProps) => {
  // `colors` is the persisted setting; fall back to the schema defaults so
  // the widget keeps working even if it is rendered before settings are
  // wired in (e.g. in unit tests).
  const c = { ...DEFAULT_PIPELINE_COLORS, ...(colors || {}) };

  const { minCycle, rows, truncated } = applyWindow(cycles);

  // Keep the view pinned to the most recent cycle, like Swing's
  // setViewPosition call after every draw: scroll to the right/bottom edge
  // whenever a new cycle arrives.
  const scrollRef = React.useRef<HTMLDivElement | null>(null);
  React.useEffect(() => {
    const node = scrollRef.current;
    if (node) {
      node.scrollLeft = node.scrollWidth;
      node.scrollTop = node.scrollHeight;
    }
  }, [cycles.time, rows.length]);

  if (cycles.time === 0 || rows.length === 0) {
    return (
      <Typography
        variant="body2"
        color="text.secondary"
        data-testid="cycles-widget-empty"
      >
        Load a program and execute at least one cycle to see the temporal
        instruction/cycle diagram.
      </Typography>
    );
  }

  const columnCount = cycles.time - minCycle + 1;
  const timelineWidth = columnCount * CELL_W;
  // Position of `cycle` (1-based CPU cycle) inside the timeline pane.
  const cycleX = (cycle: number) => (cycle - minCycle) * CELL_W;

  return (
    <div data-testid="cycles-widget" data-time={cycles.time}>
      {truncated && (
        <Typography variant="caption" color="text.secondary">
          Long execution: showing only the most recent {MAX_COLUMNS} cycles /{' '}
          {MAX_ROWS} instructions.
        </Typography>
      )}
      <Box
        ref={scrollRef}
        sx={{
          overflow: 'auto',
          maxHeight: 360,
          border: 1,
          borderColor: 'divider',
          fontFamily: 'monospace',
          fontSize: 11,
          lineHeight: `${CELL_H}px`,
        }}
      >
        {/* Header row: cycle numbers. Sticky at the top of the scroll pane. */}
        <Box
          sx={{
            display: 'flex',
            position: 'sticky',
            top: 0,
            zIndex: 3,
            width: NAME_COL_W + timelineWidth,
            bgcolor: 'background.paper',
            borderBottom: 1,
            borderColor: 'divider',
          }}
        >
          <Box
            sx={{
              position: 'sticky',
              left: 0,
              zIndex: 1,
              minWidth: NAME_COL_W,
              px: 1,
              bgcolor: 'background.paper',
              fontWeight: 'bold',
            }}
          >
            Instruction
          </Box>
          <div style={{ position: 'relative', width: timelineWidth, height: CELL_H }}>
            {Array.from({ length: columnCount }, (_, i) => (
              <span
                key={minCycle + i}
                style={{
                  position: 'absolute',
                  left: i * CELL_W,
                  width: CELL_W,
                  textAlign: 'center',
                  opacity: 0.7,
                }}
              >
                {minCycle + i}
              </span>
            ))}
          </div>
        </Box>

        {/* One row per instruction. Cells are absolutely positioned inside
            the timeline pane so empty cells cost no DOM nodes. */}
        {rows.map(({ element, firstStateIndex, firstCycle }, rowIndex) => (
          <Box
            key={`${element.serialNumber}-${element.startTime}`}
            data-testid="cycles-row"
            data-name={element.name}
            data-start-time={element.startTime}
            sx={{
              display: 'flex',
              width: NAME_COL_W + timelineWidth,
              bgcolor:
                rowIndex % 2 === 1 ? 'action.hover' : 'background.paper',
            }}
          >
            <Box
              sx={{
                position: 'sticky',
                left: 0,
                zIndex: 2,
                minWidth: NAME_COL_W,
                maxWidth: NAME_COL_W,
                px: 1,
                whiteSpace: 'nowrap',
                overflow: 'hidden',
                textOverflow: 'ellipsis',
                bgcolor: 'background.paper',
              }}
              title={element.name}
            >
              {element.name}
            </Box>
            <div
              style={{
                position: 'relative',
                width: timelineWidth,
                height: CELL_H,
                flexShrink: 0,
              }}
            >
              {element.states.slice(firstStateIndex).map((state, i) => {
                const cycle = firstCycle + i;
                const colorKey = colorKeyForState(state);
                return (
                  <span
                    key={cycle}
                    data-cycle={cycle}
                    data-state={state}
                    title={`${element.name} — cycle ${cycle}: ${state.trim() || 'squashed'}`}
                    style={{
                      position: 'absolute',
                      left: cycleX(cycle),
                      width: CELL_W,
                      height: CELL_H,
                      boxSizing: 'border-box',
                      textAlign: 'center',
                      overflow: 'hidden',
                      backgroundColor: colorKey ? c[colorKey] : undefined,
                      // The palette colors are all light enough for dark
                      // text, in both theme modes (same choice as the
                      // Pipeline widget).
                      color: '#111',
                      border: '1px solid rgba(0, 0, 0, 0.4)',
                    }}
                  >
                    {state}
                  </span>
                );
              })}
            </div>
          </Box>
        ))}
      </Box>
    </div>
  );
};

export default React.memo(CyclesWidget);
