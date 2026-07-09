import type React from 'react';

import Box from '@mui/material/Box';
import Dialog from '@mui/material/Dialog';
import DialogTitle from '@mui/material/DialogTitle';
import DialogContent from '@mui/material/DialogContent';
import DialogActions from '@mui/material/DialogActions';
import Button from '@mui/material/Button';
import Typography from '@mui/material/Typography';

// The cracked-CPU artwork used by the Swing UI's ErrorDialog, shown to the
// left of the message like a classic JOptionPane error icon.
import errorImage from '../static/error-hires.png';

// ---------------------------------------------------------------------------
// Props type
// ---------------------------------------------------------------------------

interface RuntimeErrorDialogProps {
  /** Controls dialog visibility. */
  open: boolean;
  /** Human-readable error message to display (may contain newlines). */
  message: string;
  /** Callback invoked when the user clicks OK or dismisses the dialog. */
  onClose: () => void;
}

/**
 * RuntimeErrorDialog – informational MUI Dialog shown when the simulator
 * encounters a runtime error (e.g. integer overflow, unsupported syscall).
 *
 * This is a pure UI component: the caller (Simulator.js) is responsible for
 * all CPU state transitions (stopCode / dispatch) which must happen
 * immediately when the error result arrives, *not* when the user dismisses
 * this dialog.  The dialog is purely informational and can be closed at any
 * time without side-effects.
 */
function RuntimeErrorDialog({
  open,
  message,
  onClose,
}: RuntimeErrorDialogProps): React.ReactElement {
  return (
    <Dialog
      id="runtime-error-dialog"
      open={Boolean(open)}
      onClose={onClose}
      aria-labelledby="runtime-error-dialog-title"
      maxWidth="sm"
      fullWidth
    >
      <DialogTitle id="runtime-error-dialog-title">Runtime error</DialogTitle>
      <DialogContent>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
          <img
            src={errorImage}
            alt=""
            aria-hidden="true"
            style={{ width: 140, height: 'auto', flexShrink: 0 }}
          />
          <Typography
            variant="body2"
            component="pre"
            sx={{ whiteSpace: 'pre-line', fontFamily: 'inherit', m: 0 }}
          >
            {message}
          </Typography>
        </Box>
      </DialogContent>
      <DialogActions>
        <Button
          id="runtime-error-ok"
          onClick={onClose}
          variant="contained"
          color="primary"
          autoFocus
        >
          OK
        </Button>
      </DialogActions>
    </Dialog>
  );
}

export default RuntimeErrorDialog;
