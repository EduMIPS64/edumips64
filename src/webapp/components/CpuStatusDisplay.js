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
    />
    </>
  );
};

export default CpuStatusDisplay;
