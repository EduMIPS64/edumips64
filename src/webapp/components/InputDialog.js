import React from 'react';

import Box from '@mui/material/Box';
import Button from '@mui/material/Button';
import Dialog from '@mui/material/Dialog';
import DialogActions from '@mui/material/DialogActions';
import DialogContent from '@mui/material/DialogContent';
import DialogTitle from '@mui/material/DialogTitle';
import TextField from '@mui/material/TextField';
import Typography from '@mui/material/Typography';

const InputDialog = ({ request, onSubmit, onCancel }) => {
  const [value, setValue] = React.useState('');
  const [error, setError] = React.useState('');

  const maxLength = request?.inputMaxLength ?? 0;

  React.useEffect(() => {
    if (!request) {
      setValue('');
      setError('');
    }
  }, [request]);

  const submitValue = () => {
    if (!request) {
      return;
    }

    if (value.length > maxLength) {
      setError(request.inputTooLongMessage);
      return;
    }

    setError('');
    onSubmit(value);
  };

  const cancelInput = () => {
    setValue('');
    setError('');
    onCancel();
  };

  const promptMessage = request?.inputPromptMessage || 'Please input your text';
  const promptWithMax =
    maxLength > 0 ? `${promptMessage} (max ${maxLength} characters)` : promptMessage;

  return (
    <Dialog open={Boolean(request)} onClose={cancelInput} maxWidth="xs" fullWidth>
      <DialogTitle>{request?.inputDialogTitle || 'EduMIPS64 - Input'}</DialogTitle>
      <DialogContent>
        <Typography sx={{ mb: 2 }}>{promptWithMax}</Typography>
        <TextField
          autoFocus
          fullWidth
          value={value}
          inputProps={{ maxLength: maxLength > 0 ? maxLength : undefined }}
          onChange={(event) => {
            const next =
              maxLength > 0
                ? event.target.value.slice(0, maxLength)
                : event.target.value;
            setValue(next);
            if (error) {
              setError('');
            }
          }}
          error={Boolean(error)}
          helperText={
            <Box
              component="span"
              sx={{ display: 'flex', justifyContent: 'space-between' }}
            >
              <span>{error || ' '}</span>
              {maxLength > 0 && (
                <span data-testid="input-counter">
                  {value.length} / {maxLength}
                </span>
              )}
            </Box>
          }
          onKeyDown={(event) => {
            if (event.key === 'Enter') {
              event.preventDefault();
              submitValue();
            }
          }}
        />
      </DialogContent>
      <DialogActions>
        <Button onClick={cancelInput}>Cancel</Button>
        <Button onClick={submitValue} variant="contained">
          OK
        </Button>
      </DialogActions>
    </Dialog>
  );
};

export default InputDialog;
