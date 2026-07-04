import React from 'react';

import Typography from '@mui/material/Typography';
import Tooltip from '@mui/material/Tooltip';

export interface BinaryValueProps {
  hexString: string;
  value?: string | number | null;
}

/**
 * Shared component that renders a binary value as a monospaced hex string
 * with an optional tooltip showing the numeric value.
 */
const BinaryValue = ({ hexString, value }: BinaryValueProps) => {
  return (
    <Tooltip disableFocusListener title={value ?? ''}>
      <Typography
        sx={{
          fontFamily: "Menlo, Monaco, 'Courier New', monospace",
          padding: '0.1em 10px',
          textAlign: 'right',
          fontSize: '0.75rem',
        }}
      >
        {hexString}
      </Typography>
    </Tooltip>
  );
};

export default BinaryValue;
