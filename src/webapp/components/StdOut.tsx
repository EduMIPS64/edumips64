import React from 'react';
import { useTheme } from '@mui/material/styles';

interface StdOutProps {
  stdout?: string;
}

const StdOut = ({ stdout }: StdOutProps) => {
  const theme = useTheme();
  return (
    <textarea
      readOnly
      value={stdout || ''}
      id="stdout-view"
      style={{
        width: '100%',
        minHeight: '100px',
        fontFamily: 'monospace',
        fontSize: '0.75rem',
        color: theme.palette.text.primary,
        backgroundColor: theme.palette.background.paper,
      }}
    />
  );
};

export default React.memo(StdOut);
