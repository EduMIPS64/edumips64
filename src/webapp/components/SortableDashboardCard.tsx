import type React from 'react';

import { useSortable } from '@dnd-kit/sortable';
import { CSS } from '@dnd-kit/utilities';

import Box from '@mui/material/Box';
import IconButton from '@mui/material/IconButton';
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
  fullWidth?: boolean;
  expanded?: boolean;
  onToggle?: () => void;
  children?: React.ReactNode;
}

/**
 * Wraps `DashboardCard` with dnd-kit's `useSortable` so it can be
 * drag-and-drop reordered within the dashboard's `SortableContext`
 * (see `Simulator.tsx`).
 *
 * The drag handle is a dedicated `IconButton` rendered to the left of the
 * card header, entirely separate from the header's own click-to-collapse
 * `<button>` (see `DashboardCard`'s `dragHandle` slot) so dragging never
 * conflicts with expanding/collapsing the card.
 *
 * `useSortable`'s `setNodeRef`/`style` (transform + transition) are applied
 * to an outer wrapping `<div>` — a plain grid item — rather than to
 * `DashboardCard`'s own root `Card`, since `DashboardCard` is also used
 * standalone (e.g. `IssuesCard`) without any sortable behavior.
 */
export default function SortableDashboardCard({
  id,
  htmlId,
  title,
  icon,
  maxContentHeight,
  fullWidth,
  expanded,
  onToggle,
  children,
}: SortableDashboardCardProps) {
  const {
    attributes,
    listeners,
    setNodeRef,
    transform,
    transition,
    isDragging,
  } = useSortable({ id });

  return (
    <div
      ref={setNodeRef}
      style={{
        gridColumn: fullWidth ? '1 / -1' : 'auto',
        // While this card is the one being dragged, `Simulator`'s
        // `DragOverlay` renders a floating clone that follows the pointer at
        // a fixed size (see its own comment for why), so this in-grid
        // original must NOT also chase the pointer via dnd-kit's transform —
        // doing both at once is exactly what made the dragged card visually
        // warp into whatever slot it passed over. It stays put as a plain
        // dimmed placeholder (no shadow — that lives on the overlay clone
        // now, see `Simulator`) marking the reserved spot, while its
        // siblings still animate into their new positions via their own
        // (non-dragging) transform below.
        transform: isDragging ? undefined : CSS.Transform.toString(transform),
        transition: isDragging ? undefined : (transition ?? undefined),
        opacity: isDragging ? 0.35 : 1,
        position: 'relative',
      }}
    >
      <DashboardCard
        id={htmlId}
        title={title}
        icon={icon}
        maxContentHeight={maxContentHeight}
        fullWidth={fullWidth}
        expanded={expanded}
        onToggle={onToggle}
        dragHandle={
          <Box
            sx={{
              display: 'flex',
              alignItems: 'center',
              pl: 0.5,
            }}
          >
            <IconButton
              size="small"
              aria-label={`Reorder ${title}`}
              sx={{
                cursor: isDragging ? 'grabbing' : 'grab',
                color: 'text.secondary',
                touchAction: 'none',
              }}
              {...attributes}
              {...listeners}
            >
              <DragIndicatorIcon fontSize="small" />
            </IconButton>
          </Box>
        }
      >
        {children}
      </DashboardCard>
    </div>
  );
}
