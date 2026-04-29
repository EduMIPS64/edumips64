import React from 'react';
import Chip from '@mui/material/Chip';

const CpuStatusDisplay = ({ status }) => {
    var color = 'default';
    switch (status)  {
        case 'READY':
            color = 'success';
            break;
        case 'RUNNING':
            color = 'warning';
            break;
        case 'STOPPED':
        case 'STOPPING':
            color = 'error';
            break;
        default:
            color = 'default';
    }
  return (
    <>
    <Chip
      label={status}
      color={color}
      size="small"
      style={{ textAlign: 'center', marginRight: "5px" }}
      // Make the chip a polite live region so screen readers announce
      // CPU state transitions (READY → RUNNING → STOPPED/HALTED).
      role="status"
      aria-live="polite"
      aria-atomic="true"
      aria-label={`CPU status: ${status}`}
    />
    </>
  );
};

export default CpuStatusDisplay;
