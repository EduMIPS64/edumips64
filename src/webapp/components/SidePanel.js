import React from 'react';
import Badge from '@mui/material/Badge';
import Box from '@mui/material/Box';
import Tab from '@mui/material/Tab';
import Tabs from '@mui/material/Tabs';

/**
 * IDE-style tabbed side panel.
 *
 * Replaces the accordion stack of the classic layout: one widget is shown
 * at a time, selected via a scrollable tab strip (like the panel tabs of
 * VS Code). The active tab is persisted by the caller; tabs whose data
 * changed while hidden show a pulsing dot (same visual as the accordion
 * change indicator it replaces).
 *
 * The component is purely presentational: content panels are passed in via
 * the `panels` prop, `[{ key, label, badge, showDot, content }]`, and the
 * caller owns the active-tab state.
 */
export default function SidePanel({ panels, activeTab, onTabChange }) {
  // If the persisted active tab no longer exists (e.g. renamed in a later
  // release), fall back to the first panel instead of rendering nothing.
  const validTab = panels.some((p) => p.key === activeTab)
    ? activeTab
    : panels[0].key;

  const labelFor = (panel) => {
    const label = (
      <Box component="span" sx={{ display: 'flex', alignItems: 'center' }}>
        {panel.label}
        {panel.showDot && <span className="accordion-change-indicator" />}
      </Box>
    );
    if (!panel.badge) {
      return label;
    }
    return (
      <Badge
        badgeContent={panel.badge.count}
        color={panel.badge.color}
        max={99}
        sx={{ '& .MuiBadge-badge': { right: -10 }, pr: 1 }}
      >
        {label}
      </Badge>
    );
  };

  return (
    <Box
      id="side-panel"
      sx={{ display: 'flex', flexDirection: 'column', height: '100%' }}
    >
      <Tabs
        value={validTab}
        onChange={(e, v) => onTabChange(v)}
        variant="scrollable"
        scrollButtons="auto"
        allowScrollButtonsMobile
        aria-label="Simulator panels"
        sx={{
          flexShrink: 0,
          position: 'sticky',
          top: 0,
          zIndex: 2,
          bgcolor: 'background.paper',
        }}
      >
        {panels.map((panel) => (
          <Tab
            key={panel.key}
            id={`side-tab-${panel.key}`}
            value={panel.key}
            label={labelFor(panel)}
            aria-controls={`side-tabpanel-${panel.key}`}
          />
        ))}
      </Tabs>
      {panels.map((panel) => (
        <Box
          key={panel.key}
          role="tabpanel"
          id={`side-tabpanel-${panel.key}`}
          aria-labelledby={`side-tab-${panel.key}`}
          hidden={validTab !== panel.key}
          sx={{
            flexGrow: 1,
            overflow: 'auto',
            p: 1.5,
          }}
        >
          {/* Keep every panel mounted (just hidden) so widget-internal state
              (e.g. cache config form edits) survives tab switches. */}
          {panel.content}
        </Box>
      ))}
    </Box>
  );
}
