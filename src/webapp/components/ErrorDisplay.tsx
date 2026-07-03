import React from 'react';
import ErrorIcon from '@mui/icons-material/Error';
import WarningIcon from '@mui/icons-material/Warning';
import Chip from '@mui/material/Chip';

import type { ParsingError } from '../simulator/protocol';

interface CountProps {
  count: number;
}

const ErrorCount = ({ count }: CountProps) => {
  if (count === 0) {
    return <React.Fragment />;
  }
  return (
    <Chip
      icon={<ErrorIcon />}
      label={count}
      size="small"
      color="error"
      style={{ marginRight: '5px' }}
    />
  );
};

const WarningCount = ({ count }: CountProps) => {
  if (count === 0) {
    return <React.Fragment />;
  }
  return (
    <Chip
      icon={<WarningIcon />}
      label={count}
      size="small"
      color="warning"
      style={{ marginRight: '5px' }}
    />
  );
};

interface ErrorDisplayProps {
  parsingErrors: ParsingError[] | null | undefined;
}

const ErrorDisplay = ({ parsingErrors }: ErrorDisplayProps) => {
  const warningCount = parsingErrors
    ? parsingErrors.filter((e) => e.isWarning).length
    : 0;
  const errorCount = parsingErrors
    ? parsingErrors.filter((e) => !e.isWarning).length
    : 0;

  return (
    <>
      <WarningCount count={warningCount} />
      <ErrorCount count={errorCount} />
    </>
  );
};

export default ErrorDisplay;
