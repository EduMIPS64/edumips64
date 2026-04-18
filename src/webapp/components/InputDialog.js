import React from 'react';

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

    if (value.length > request.inputMaxLength) {
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

  return (
    <Dialog open={Boolean(request)} onClose={cancelInput} maxWidth="xs" fullWidth>
      <DialogTitle>{request?.inputDialogTitle || 'EduMIPS64 - Input'}</DialogTitle>
      <DialogContent>
        <Typography sx={{ mb: 2 }}>
          {request?.inputPromptMessage || 'Please input your text'}
        </Typography>
        <TextField
          autoFocus
          fullWidth
          value={value}
          onChange={(event) => {
            setValue(event.target.value);
            if (error) {
              setError('');
            }
          }}
          error={Boolean(error)}
          helperText={error || ' '}
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
