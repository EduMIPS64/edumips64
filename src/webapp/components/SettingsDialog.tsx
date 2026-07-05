import React from 'react';

import Button from '@mui/material/Button';
import Dialog from '@mui/material/Dialog';
import DialogContent from '@mui/material/DialogContent';
import DialogTitle from '@mui/material/DialogTitle';
import DialogActions from '@mui/material/DialogActions';
import Tabs from '@mui/material/Tabs';
import Tab from '@mui/material/Tab';
import Box from '@mui/material/Box';

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

type SettingsDialogProps = UiSettingsPanelProps &
  CpuSettingsPanelProps &
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
          <Tab label="CPU" id="settings-tab-1" />
          <Tab label="Execution" id="settings-tab-2" />
          <Tab label="Cache" id="settings-tab-3" />
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
          <CpuSettingsPanel
            forwarding={forwarding}
            setForwarding={setForwarding}
            delaySlot={delaySlot}
            setDelaySlot={setDelaySlot}
            status={status}
          />
        </TabPanel>
        <TabPanel value={tabValue} index={2}>
          <ExecutionSettingsPanel
            stepStride={stepStride}
            setStepStride={setStepStride}
            executionDelayMs={executionDelayMs}
            setExecutionDelayMs={setExecutionDelayMs}
          />
        </TabPanel>
        <TabPanel value={tabValue} index={3}>
          <CacheConfig onChange={onCacheConfigChange} status={status} />
        </TabPanel>
      </DialogContent>
      <DialogActions>
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
