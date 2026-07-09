import type React from 'react';

import Box from '@mui/material/Box';
import Collapse from '@mui/material/Collapse';
import Typography from '@mui/material/Typography';
import ExpandMoreIcon from '@mui/icons-material/ExpandMore';

interface DashboardCardProps {
  id?: string;
  title: string;
  icon?: React.ReactNode;
  action?: React.ReactNode;
  maxContentHeight?: string;
  children?: React.ReactNode;
  /**
   * Whether the card's body is shown. When omitted, the card has no
   * collapse affordance at all (e.g. `IssuesCard`, which is only ever
   * rendered while it has content to show).
   */
  expanded?: boolean;
  /**
   * Presence of this callback is what turns on the collapse affordance:
   * the whole header becomes a toggle button when it is provided.
   */
  onToggle?: () => void;
  /**
   * Optional drag-handle element (e.g. a `DragIndicator` icon button)
   * rendered at the right end of the header strip, *outside* the header's
   * own `<button>` — a `<button>` cannot contain another interactive
   * element, and the handle needs its own independent pointer/keyboard
   * listeners for drag-and-drop reordering, separate from the collapse
   * toggle.
   */
  dragHandle?: React.ReactNode;
}

/**
 * A single dashboard section, styled like the classic accordion look: a
 * flat, full-bleed row with a subtly tinted header strip (chevron on the
 * left, then the title), a hairline divider below the section, and no
 * card box, rounded corners or elevation shadow. Visually this matches
 * the pre-dashboard-card Accordion UI exactly; the only addition is the
 * optional drag handle at the right end of the header strip.
 *
 * When `onToggle` is supplied, the header becomes a clickable/keyboard
 * operable toggle (native `<button>`, so Enter/Space "just work") that
 * collapses the body with `<Collapse>`; the chevron points down when
 * collapsed and rotates up when expanded, exactly like the old
 * AccordionSummary.
 */
export default function DashboardCard({
  id,
  title,
  icon,
  action,
  maxContentHeight,
  children,
  expanded = true,
  onToggle,
  dragHandle,
}: DashboardCardProps) {
  const collapsible = onToggle !== undefined;

  return (
    <Box
      id={id}
      sx={{
        minWidth: 0,
        borderBottom: 1,
        borderStyle: 'solid',
        borderColor: 'divider',
        bgcolor: 'background.paper',
      }}
    >
      <Box
        sx={{
          display: 'flex',
          alignItems: 'stretch',
          bgcolor: (theme) =>
            theme.palette.mode === 'dark'
              ? 'rgba(141, 166, 255, 0.08)'
              : 'rgba(53, 87, 212, 0.06)',
        }}
      >
        <Box
          component={collapsible ? 'button' : 'div'}
          type={collapsible ? 'button' : undefined}
          onClick={collapsible ? onToggle : undefined}
          aria-expanded={collapsible ? expanded : undefined}
          aria-label={
            collapsible
              ? `${expanded ? 'Collapse' : 'Expand'} ${title}`
              : undefined
          }
          sx={{
            display: 'flex',
            alignItems: 'center',
            gap: 1,
            px: 2,
            minHeight: 44,
            flexGrow: 1,
            minWidth: 0,
            border: 0,
            borderRadius: 0,
            ...(collapsible
              ? {
                  width: '100%',
                  m: 0,
                  bgcolor: 'transparent',
                  font: 'inherit',
                  textAlign: 'left',
                  cursor: 'pointer',
                  transition: 'background-color 120ms ease',
                  '&:hover': {
                    bgcolor: (theme) =>
                      theme.palette.mode === 'dark'
                        ? 'rgba(141, 166, 255, 0.06)'
                        : 'rgba(53, 87, 212, 0.05)',
                  },
                  '&:focus-visible': {
                    outline: '2px solid currentColor',
                    outlineOffset: -2,
                  },
                }
              : {}),
          }}
        >
          {collapsible && (
            <ExpandMoreIcon
              fontSize="small"
              aria-hidden="true"
              sx={{
                color: 'text.secondary',
                transition: 'transform 200ms ease',
                transform: expanded ? 'rotate(180deg)' : 'rotate(0deg)',
              }}
            />
          )}
          {icon}
          <Typography
            variant="subtitle2"
            component={collapsible ? 'span' : 'h3'}
            sx={{
              fontWeight: 600,
              color: 'primary.main',
              flexGrow: 1,
              lineHeight: 1.2,
              textAlign: 'left',
            }}
          >
            {title}
          </Typography>
          {action}
        </Box>
        {dragHandle}
      </Box>
      <Collapse in={expanded} timeout="auto" unmountOnExit>
        <Box
          sx={{
            px: 2,
            pt: 0.5,
            pb: 2,
            overflow: 'auto',
            ...(maxContentHeight ? { maxHeight: maxContentHeight } : {}),
          }}
        >
          {children}
        </Box>
      </Collapse>
    </Box>
  );
}
