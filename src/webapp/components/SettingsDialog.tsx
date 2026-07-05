import React from 'react';

import Button from '@mui/material/Button';
import Dialog from '@mui/material/Dialog';
import DialogContent from '@mui/material/DialogContent';
import DialogTitle from '@mui/material/DialogTitle';
import DialogActions from '@mui/material/DialogActions';
import Tabs from '@mui/material/Tabs';
import Tab from '@mui/material/Tab';
import Box from '@mui/material/Box';
import Divider from '@mui/material/Divider';
import Typography from '@mui/material/Typography';

import UiSettingsPanel, {
  type UiSettingsPanelProps,
} from './settings/UiSettingsPanel';
import CpuSettingsPanel, {
  type CpuSettingsPanelProps,
} from './settings/CpuSettingsPanel';
import ExecutionSettingsPanel, {
  type ExecutionSettingsPanelProps,
} from './settings/ExecutionSettingsPanel';
import CacheConfig from './CacheConfig';
import { resetAllDialogSettings } from '../settings/useSetting';
import type { CacheConfig as CacheConfigType } from '../settings/schema';
import type { CpuStatus } from '../simulator/protocol';

interface TabPanelProps {
  children?: React.ReactNode;
  value: number;
  index: number;
}

function TabPanel({ children, value, index }: TabPanelProps) {
  return (
    <div
      role="tabpanel"
      hidden={value !== index}
      id={`settings-tabpanel-${index}`}
      aria-labelledby={`settings-tab-${index}`}
    >
      {value === index && <Box sx={{ p: 3 }}>{children}</Box>}
    </div>
  );
}

function SectionHeading({
  children,
  first,
}: {
  children: React.ReactNode;
  first?: boolean;
}) {
  return (
    <>
      {!first && <Divider sx={{ my: 3 }} />}
      <Typography
        variant="subtitle2"
        sx={{ fontWeight: 700, mb: 2, color: 'primary.main' }}
      >
        {children}
      </Typography>
    </>
  );
}

type SettingsDialogProps = UiSettingsPanelProps &
  Omit<CpuSettingsPanelProps, 'status'> &
  ExecutionSettingsPanelProps & {
    open: boolean;
    handleClose: () => void;
    status: CpuStatus;
    onCacheConfigChange?: (config: {
      l1d: CacheConfigType;
      l1i: CacheConfigType;
    }) => void;
  };

export default function SettingsDialog({
  open,
  handleClose,
  status,
  onCacheConfigChange,
  viMode,
  setViMode,
  fontSize,
  setFontSize,
  accordionAlerts,
  setAccordionAlerts,
  pipelineColors,
  setPipelineColors,
  themeMode,
  setThemeMode,
  forwarding,
  setForwarding,
  delaySlot,
  setDelaySlot,
  stepStride,
  setStepStride,
  executionDelayMs,
  setExecutionDelayMs,
}: SettingsDialogProps) {
  const [tabValue, setTabValue] = React.useState(0);

  const handleTabChange = (_event: React.SyntheticEvent, newValue: number) => {
    setTabValue(newValue);
  };

  const handleResetAll = () => {
    if (
      window.confirm(
        'Reset all settings to their defaults? This cannot be undone.',
      )
    ) {
      resetAllDialogSettings();
    }
  };

  return (
    <Dialog
      onClose={handleClose}
      open={open}
      maxWidth="md"
      fullWidth
      slotProps={{
        paper: {
          sx: {
            height: { xs: '100vh', sm: '70vh' },
            m: { xs: 0, sm: 4 },
            maxHeight: { xs: '100vh', sm: 'calc(100% - 64px)' },
            width: { xs: '100vw', sm: '100%' },
            borderRadius: { xs: 0, sm: 1 },
          },
        },
      }}
    >
      <DialogTitle className="settings-title">Settings</DialogTitle>
      <Box sx={{ borderBottom: 1, borderColor: 'divider' }}>
        <Tabs
          value={tabValue}
          onChange={handleTabChange}
          aria-label="settings tabs"
        >
          <Tab label="UI" id="settings-tab-0" />
          <Tab label="Simulation" id="settings-tab-1" />
        </Tabs>
      </Box>
      <DialogContent className="settings-content" sx={{ p: 0 }}>
        <TabPanel value={tabValue} index={0}>
          <UiSettingsPanel
            viMode={viMode}
            setViMode={setViMode}
            fontSize={fontSize}
            setFontSize={setFontSize}
            accordionAlerts={accordionAlerts}
            setAccordionAlerts={setAccordionAlerts}
            pipelineColors={pipelineColors}
            setPipelineColors={setPipelineColors}
            themeMode={themeMode}
            setThemeMode={setThemeMode}
          />
        </TabPanel>
        <TabPanel value={tabValue} index={1}>
          <SectionHeading first>CPU</SectionHeading>
          <CpuSettingsPanel
            forwarding={forwarding}
            setForwarding={setForwarding}
            delaySlot={delaySlot}
            setDelaySlot={setDelaySlot}
            status={status}
          />

          <SectionHeading>Execution</SectionHeading>
          <ExecutionSettingsPanel
            stepStride={stepStride}
            setStepStride={setStepStride}
            executionDelayMs={executionDelayMs}
            setExecutionDelayMs={setExecutionDelayMs}
          />

          <SectionHeading>Cache</SectionHeading>
          <CacheConfig onChange={onCacheConfigChange} status={status} />
        </TabPanel>
      </DialogContent>
      <DialogActions>
        <Button
          onClick={handleResetAll}
          variant="text"
          color="inherit"
          id="settings-reset-button"
          sx={{ mr: 'auto' }}
        >
          Reset to defaults
        </Button>
        <Button
          onClick={handleClose}
          variant="outlined"
          id="settings-close-button"
        >
          Close
        </Button>
      </DialogActions>
    </Dialog>
  );
}
