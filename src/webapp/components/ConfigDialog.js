import React from 'react';

import Button from '@mui/material/Button';
import Dialog from '@mui/material/Dialog';
import DialogActions from '@mui/material/DialogActions';
import DialogContent from '@mui/material/DialogContent';
import DialogTitle from '@mui/material/DialogTitle';
import Divider from '@mui/material/Divider';
import Typography from '@mui/material/Typography';

import Settings from './Settings';
import CacheConfig from './CacheConfig';

/**
 * Modal configuration dialog for the dashboard layout.
 *
 * The dashboard shows all data widgets simultaneously, so the two
 * configuration panels (General Settings and Cache Configuration) move out
 * of the main canvas into this dialog, reachable from the gear button in
 * the header. Both child components are unchanged; this is just a new
 * container for them.
 */
export default function ConfigDialog({
  open,
  onClose,
  settingsProps,
  cacheProps,
}) {
  return (
    <Dialog
      open={open}
      onClose={onClose}
      fullWidth
      maxWidth="sm"
      aria-labelledby="config-dialog-title"
      id="config-dialog"
    >
      <DialogTitle id="config-dialog-title">Configuration</DialogTitle>
      <DialogContent dividers>
        <Typography
          variant="subtitle1"
          component="h3"
          sx={{ fontWeight: 700, color: 'primary.main', mb: 1 }}
        >
          General Settings
        </Typography>
        <Settings {...settingsProps} />
        <Divider sx={{ my: 2 }} />
        <Typography
          variant="subtitle1"
          component="h3"
          sx={{ fontWeight: 700, color: 'primary.main', mb: 1 }}
        >
          Cache Configuration
        </Typography>
        <CacheConfig {...cacheProps} />
      </DialogContent>
      <DialogActions>
        <Button id="config-dialog-close" onClick={onClose}>
          Close
        </Button>
      </DialogActions>
    </Dialog>
  );
}
