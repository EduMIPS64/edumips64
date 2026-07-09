import React from 'react';
import List from '@mui/material/List';
import ListItem from '@mui/material/ListItem';
import ListItemButton from '@mui/material/ListItemButton';
import ListItemIcon from '@mui/material/ListItemIcon';
import ListItemText from '@mui/material/ListItemText';
import ErrorDisplay from './ErrorDisplay';
import Accordion from '@mui/material/Accordion';
import AccordionDetails from '@mui/material/AccordionDetails';
import Typography from '@mui/material/Typography';
import type { AccordionSummaryProps } from '@mui/material/AccordionSummary';

import ExpandMoreIcon from '@mui/icons-material/ExpandMore';
import ErrorOutlineOutlinedIcon from '@mui/icons-material/ErrorOutlineOutlined';
import WarningAmberOutlinedIcon from '@mui/icons-material/WarningAmberOutlined';

import type { ParsingError } from '../simulator/protocol';

interface DecideIconTypeProps {
  errorType: boolean;
}

const DecideIconType = ({ errorType }: DecideIconTypeProps) => {
  if (errorType) {
    return <WarningAmberOutlinedIcon />;
  } else {
    return <ErrorOutlineOutlinedIcon />;
  }
};

interface ErrorListProps {
  parsingErrors: ParsingError[] | null | undefined;
  // A styled MUI AccordionSummary component passed from Simulator (so the
  // heading style is shared with all right-panel accordions).
  AccordionSummary: React.ComponentType<AccordionSummaryProps>;
  onIssueClick?: (row: number, column: number) => void;
}

const ErrorList = ({
  parsingErrors,
  AccordionSummary,
  onIssueClick,
}: ErrorListProps) => {
  if (parsingErrors === undefined) {
    return <React.Fragment />;
  }
  return (
    <Accordion defaultExpanded disableGutters className="error-accordion">
      <AccordionSummary expandIcon={<ExpandMoreIcon />}>
        <Typography
          variant="h6"
          sx={{ flexGrow: 1, fontWeight: 'bold', color: 'primary.main' }}
        >
          Issues
        </Typography>
        <ErrorDisplay parsingErrors={parsingErrors} />
      </AccordionSummary>
      <AccordionDetails>
        <List sx={{ width: '100%' }} dense>
          {parsingErrors.map((value) => (
            <ListItem key={JSON.stringify(value)} disableGutters disablePadding>
              <ListItemButton
                onClick={
                  onIssueClick
                    ? () => onIssueClick(value.row, value.column)
                    : undefined
                }
                className="error-list-item-button"
                aria-label={`Jump to line ${value.row}, position ${value.column}: ${value.description}`}
                data-issue-row={value.row}
                data-issue-column={value.column}
              >
                <ListItemIcon className="error-list-item">
                  <DecideIconType errorType={value.isWarning} />
                </ListItemIcon>
                <ListItemText
                  primary={`Line ${value.row} Position ${value.column}: ${value.description}`}
                />
              </ListItemButton>
            </ListItem>
          ))}
        </List>
      </AccordionDetails>
    </Accordion>
  );
};

export default ErrorList;
