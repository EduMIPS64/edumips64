import React from 'react';

import Box from '@mui/material/Box';
import Card from '@mui/material/Card';
import Typography from '@mui/material/Typography';

/**
 * A single dashboard widget: a card with a compact colored header (icon +
 * title + optional trailing element) and a content area that can scroll
 * internally when `maxContentHeight` is set, so a long Registers or Memory
 * table never blows up the dashboard grid.
 */
export default function DashboardCard({
  id,
  title,
  icon,
  action,
  maxContentHeight,
  fullWidth,
  children,
}) {
  return (
    <Card
      id={id}
      sx={{
        display: 'flex',
        flexDirection: 'column',
        minWidth: 0,
        gridColumn: fullWidth ? '1 / -1' : 'auto',
      }}
    >
      <Box
        sx={{
          display: 'flex',
          alignItems: 'center',
          gap: 1,
          px: 1.5,
          py: 0.75,
          borderBottom: 1,
          borderColor: 'divider',
          color: 'primary.main',
        }}
      >
        {icon}
        <Typography
          variant="subtitle2"
          component="h3"
          sx={{ fontWeight: 700, flexGrow: 1, lineHeight: 1.2 }}
        >
          {title}
        </Typography>
        {action}
      </Box>
      <Box
        sx={{
          p: 1.5,
          overflow: 'auto',
          ...(maxContentHeight ? { maxHeight: maxContentHeight } : {}),
        }}
      >
        {children}
      </Box>
    </Card>
  );
}
