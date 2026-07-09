import type React from 'react';

import Box from '@mui/material/Box';
import Card from '@mui/material/Card';
import Collapse from '@mui/material/Collapse';
import Typography from '@mui/material/Typography';
import ExpandMoreIcon from '@mui/icons-material/ExpandMore';

interface DashboardCardProps {
  id?: string;
  title: string;
  icon?: React.ReactNode;
  action?: React.ReactNode;
  maxContentHeight?: string;
  fullWidth?: boolean;
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
}

/**
 * A single dashboard widget: a card with a compact colored header (icon +
 * title + optional trailing element) and a content area that can scroll
 * internally when `maxContentHeight` is set, so a long Registers or Memory
 * table never blows up the dashboard grid.
 *
 * When `onToggle` is supplied, the header becomes a clickable/keyboard
 * operable toggle (native `<button>`, so Enter/Space "just work") that
 * collapses the body with `<Collapse>`. The header itself always stays
 * visible — only the body height animates to zero — so a collapsed card is
 * simply its header, and since nothing but the Card's own rounded
 * background is ever visible below the header's hairline, the collapsed
 * card keeps fully rounded corners on all four sides, exactly like an
 * expanded one. The header's bottom hairline is only painted while
 * expanded (there's nothing to divide from otherwise), so a collapsed card
 * never shows a stray line sitting on top of the rounded corner.
 */
export default function DashboardCard({
  id,
  title,
  icon,
  action,
  maxContentHeight,
  fullWidth,
  children,
  expanded = true,
  onToggle,
}: DashboardCardProps) {
  const collapsible = onToggle !== undefined;
  const showDivider = !collapsible || expanded;

  return (
    <Card
      id={id}
      sx={{
        display: 'flex',
        flexDirection: 'column',
        minWidth: 0,
        overflow: 'hidden',
        gridColumn: fullWidth ? '1 / -1' : 'auto',
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
          px: 1.5,
          py: 0.75,
          color: 'primary.main',
          border: 0,
          borderBottom: showDivider ? 1 : 0,
          borderStyle: 'solid',
          borderColor: 'divider',
          borderRadius: 0,
          ...(collapsible
            ? {
                width: '100%',
                m: 0,
                bgcolor: 'transparent',
                font: 'inherit',
                textAlign: 'left',
                cursor: 'pointer',
                '&:focus-visible': {
                  outline: '2px solid currentColor',
                  outlineOffset: -2,
                },
              }
            : {}),
        }}
      >
        {icon}
        <Typography
          variant="subtitle2"
          component={collapsible ? 'span' : 'h3'}
          sx={{
            fontWeight: 700,
            flexGrow: 1,
            lineHeight: 1.2,
            textAlign: 'left',
          }}
        >
          {title}
        </Typography>
        {action}
        {collapsible && (
          <ExpandMoreIcon
            fontSize="small"
            aria-hidden="true"
            sx={{
              transition: 'transform 200ms ease',
              transform: expanded ? 'rotate(0deg)' : 'rotate(-90deg)',
            }}
          />
        )}
      </Box>
      <Collapse in={expanded} timeout="auto" unmountOnExit>
        <Box
          sx={{
            p: 1.5,
            overflow: 'auto',
            ...(maxContentHeight ? { maxHeight: maxContentHeight } : {}),
          }}
        >
          {children}
        </Box>
      </Collapse>
    </Card>
  );
}
