import React from 'react';
import List from '@mui/material/List';
import ListItem from '@mui/material/ListItem';
import ListItemButton from '@mui/material/ListItemButton';
import ListItemIcon from '@mui/material/ListItemIcon';
import ListItemText from '@mui/material/ListItemText';

import ErrorOutlineOutlinedIcon from '@mui/icons-material/ErrorOutlineOutlined';
import WarningAmberOutlinedIcon from '@mui/icons-material/WarningAmberOutlined';
import ReportProblemOutlinedIcon from '@mui/icons-material/ReportProblemOutlined';

import DashboardCard from './DashboardCard';
import ErrorDisplay from './ErrorDisplay';
import type { ParsingError } from '../simulator/protocol';

const DecideIconType = ({ errorType }: { errorType: boolean }) => {
  if (errorType) {
    return <WarningAmberOutlinedIcon color="warning" />;
  } else {
    return <ErrorOutlineOutlinedIcon color="error" />;
  }
};

interface IssuesCardProps {
  parsingErrors: ParsingError[] | null | undefined;
  onIssueClick?: (row: number, column: number) => void;
}

/**
 * Dashboard card listing parsing errors and warnings. Rendered only when
 * there is at least one issue, spanning the full dashboard width so
 * problems are impossible to miss. Clicking an entry jumps the editor to
 * the offending line/column (same contract, item markup, classes and
 * data-* attributes as the old ErrorList accordion).
 */
const IssuesCard = ({ parsingErrors, onIssueClick }: IssuesCardProps) => {
  if (!parsingErrors || parsingErrors.length === 0) {
    return null;
  }
  return (
    <DashboardCard
      id="issues-card"
      title="Issues"
      icon={<ReportProblemOutlinedIcon fontSize="small" />}
      action={<ErrorDisplay parsingErrors={parsingErrors} />}
      maxContentHeight="30vh"
    >
      <List sx={{ width: '100%', py: 0 }} dense>
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
    </DashboardCard>
  );
};

export default IssuesCard;
