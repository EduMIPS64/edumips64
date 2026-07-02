import React from 'react';
import List from '@mui/material/List';
import ListItem from '@mui/material/ListItem';
import ListItemButton from '@mui/material/ListItemButton';
import ListItemIcon from '@mui/material/ListItemIcon';
import ListItemText from '@mui/material/ListItemText';
import Typography from '@mui/material/Typography';

import ErrorOutlineOutlinedIcon from '@mui/icons-material/ErrorOutlineOutlined';
import WarningAmberOutlinedIcon from '@mui/icons-material/WarningAmberOutlined';

const DecideIconType = ({ errorType }) => {
  if (errorType) {
    return <WarningAmberOutlinedIcon color="warning" />;
  } else {
    return <ErrorOutlineOutlinedIcon color="error" />;
  }
};

/**
 * Flat "Problems"-style list of parsing errors and warnings, used as the
 * Issues tab of the side panel. Clicking an entry jumps the editor to the
 * offending line/column (same contract as the old ErrorList accordion; the
 * item markup, classes and data-* attributes are preserved so the
 * issue-jump behavior and tests keep working).
 */
const IssuesPanel = ({ parsingErrors, onIssueClick }) => {
  if (!parsingErrors || parsingErrors.length === 0) {
    return (
      <Typography sx={{ p: 2 }} color="text.secondary">
        No problems detected in the current program.
      </Typography>
    );
  }
  return (
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
  );
};

export default IssuesPanel;
