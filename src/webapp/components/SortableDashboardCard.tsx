import React from 'react';

import { useSortable } from '@dnd-kit/sortable';
import { CSS } from '@dnd-kit/utilities';

import Box from '@mui/material/Box';
import DragIndicatorIcon from '@mui/icons-material/DragIndicator';

import DashboardCard from './DashboardCard';
import type { DashboardWidgetId } from '../settings/schema';

interface SortableDashboardCardProps {
  /** Stable widget id used both as the dnd-kit sortable id and, prefixed,
   * as the rendered DOM `id` attribute (e.g. `"stats"` -> `#stats-card`). */
  id: DashboardWidgetId;
  /** DOM `id` attribute for the rendered card, e.g. `"stats-card"`. */
  htmlId: string;
  title: string;
  icon?: React.ReactNode;
  maxContentHeight?: string;
  expanded?: boolean;
  onToggle?: () => void;
  children?: React.ReactNode;
}

/**
 * Wraps `DashboardCard` with dnd-kit's `useSortable` so it can be
 * drag-and-drop reordered within the dashboard's `SortableContext`
 * (see `Simulator.tsx`).
 *
 * The whole card header doubles as the pointer drag handle: the mouse and
 * touch activation listeners are spread onto the header itself (see
 * `DashboardCard`'s `headerDragProps`), so grabbing a card anywhere on its
 * header strip starts a drag, while a plain click still toggles
 * collapse/expand — the sensors' activation constraints (movement
 * distance for mouse, long-press for touch) are what tell the two apart.
 *
 * Keyboard reordering can't share the header the same way (Space/Enter on
 * the header button must keep toggling collapse), so a dedicated
 * visually-hidden "Reorder <Title>" button carries dnd-kit's keyboard
 * listeners: it's the next tab stop after the header, invisible until
 * keyboard-focused, and operates with Space/Enter to lift, arrow keys to
 * move, Space/Enter to drop.
 *
 * `useSortable`'s `setNodeRef`/`style` (transform + transition) are applied
 * to an outer wrapping `<div>` rather than to `DashboardCard`'s own root,
 * since `DashboardCard` is also used standalone (e.g. `IssuesCard`)
 * without any sortable behavior.
 */
export default function SortableDashboardCard({
  id,
  htmlId,
  title,
  icon,
  maxContentHeight,
  expanded,
  onToggle,
  children,
}: SortableDashboardCardProps) {
  const {
    attributes,
    listeners,
    setNodeRef,
    setActivatorNodeRef,
    transform,
    transition,
    isDragging,
  } = useSortable({ id });

  // After a pointer drag that started and ended over this card's header,
  // the browser still synthesizes a `click` on the header button, which
  // would collapse/expand the card the user just finished dragging.
  // Swallow it by ignoring toggles for a short window after the drag ends:
  // the synthetic click arrives within a few milliseconds of the drop
  // (a timestamp window is used rather than a "consume next click" flag
  // because that click isn't guaranteed to arrive at all — it only fires
  // when press and release hit the same element), while a genuine,
  // deliberate click always comes much later.
  // useLayoutEffect, not useEffect: the timestamp must be recorded
  // synchronously in the same commit that flips `isDragging` to false.
  // A passive effect runs after paint, and the synthetic click can be
  // dispatched between the commit and the effect — it would then read a
  // fresh `isDragging === false` closure but a stale timestamp.
  const dragEndTimeRef = React.useRef(Number.NEGATIVE_INFINITY);
  const wasDraggingRef = React.useRef(false);
  React.useLayoutEffect(() => {
    if (wasDraggingRef.current && !isDragging) {
      dragEndTimeRef.current = performance.now();
    }
    wasDraggingRef.current = isDragging;
  }, [isDragging]);

  const handleToggle =
    onToggle &&
    (() => {
      if (isDragging || performance.now() - dragEndTimeRef.current < 400) {
        return;
      }
      onToggle();
    });

  return (
    <div
      ref={setNodeRef}
      style={{
        // While this card is the one being dragged, `Simulator`'s
        // `DragOverlay` renders a floating clone that follows the pointer at
        // a fixed size (see its own comment for why), so this in-place
        // original must NOT also chase the pointer via dnd-kit's transform —
        // doing both at once is exactly what made the dragged card visually
        // warp into whatever slot it passed over. It stays put as a plain
        // dimmed placeholder marking the reserved spot, while its siblings
        // still animate into their new positions via their own
        // (non-dragging) transform below.
        transform: isDragging ? undefined : CSS.Transform.toString(transform),
        transition: isDragging ? undefined : (transition ?? undefined),
        opacity: isDragging ? 0.35 : 1,
        position: 'relative',
        // The wrapper, not the Card, is the dashboard grid's child, so the
        // full-width span lives here (cf. `DashboardCard`'s `fullWidth`).
        gridColumn: '1 / -1',
      }}
    >
      <DashboardCard
        id={htmlId}
        title={title}
        icon={icon}
        maxContentHeight={maxContentHeight}
        expanded={expanded}
        onToggle={handleToggle}
        headerDragProps={{
          // dnd-kit types its listeners map as Record<string, Function>;
          // narrow each handler to the React event type of its prop.
          onMouseDown: listeners?.onMouseDown as
            | React.MouseEventHandler
            | undefined,
          onTouchStart: listeners?.onTouchStart as
            | React.TouchEventHandler
            | undefined,
        }}
        dragHandle={
          <Box
            component="button"
            type="button"
            ref={setActivatorNodeRef}
            aria-label={`Reorder ${title}`}
            {...attributes}
            onKeyDown={
              listeners?.onKeyDown as React.KeyboardEventHandler | undefined
            }
            sx={{
              // Visually hidden (but focusable) 1x1 button over the header's
              // right edge; on keyboard focus it expands into a small chip so
              // sighted keyboard users can see what they're operating.
              position: 'absolute',
              top: 6,
              right: 6,
              // Explicit px units: in MUI's sx, a bare `width: 1` means 100%.
              width: '1px',
              height: '1px',
              p: 0,
              border: 0,
              overflow: 'hidden',
              bgcolor: 'transparent',
              color: 'primary.main',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              cursor: 'grab',
              '&:focus-visible': {
                width: 'auto',
                height: 'auto',
                p: 0.25,
                overflow: 'visible',
                bgcolor: 'background.paper',
                borderRadius: 1,
                outline: '2px solid currentColor',
                outlineOffset: -2,
                zIndex: 1,
              },
            }}
          >
            <DragIndicatorIcon fontSize="small" aria-hidden="true" />
          </Box>
        }
      >
        {children}
      </DashboardCard>
    </div>
  );
}
