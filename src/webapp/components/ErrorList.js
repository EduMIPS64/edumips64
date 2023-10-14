import React from 'react';
import List from '@mui/material/List';
import ListItem from '@mui/material/ListItem';
import ListItemIcon from '@mui/material/ListItemIcon';
import ListItemText from '@mui/material/ListItemText';
import ErrorDisplay from './ErrorDisplay';
import Accordion from '@mui/material/Accordion';
import AccordionDetails from '@mui/material/AccordionDetails';
import Typography from '@mui/material/Typography';

import ExpandMoreIcon from '@mui/icons-material/ExpandMore';
import ErrorOutlineOutlinedIcon from '@mui/icons-material/ErrorOutlineOutlined';
import WarningAmberOutlinedIcon from '@mui/icons-material/WarningAmberOutlined';

const DecideIconType = ({ errorType }) => {
    if (errorType) {
        return <WarningAmberOutlinedIcon />;
    } else {
        return <ErrorOutlineOutlinedIcon />;
    }
}

const ErrorList = ({ parsingErrors, AccordionSummary }) => {

    if (parsingErrors == undefined) {
        return <React.Fragment />;
    }
    return (
        <>
            <Accordion defaultExpanded disableGutters className='error-accordion'>
                <AccordionSummary expandIcon={<ExpandMoreIcon />}>
                    <Typography sx={{ flexGrow: 1 }}>Issues</Typography>
                    <Typography><ErrorDisplay parsingErrors={parsingErrors} /></Typography>
                </AccordionSummary>
                <AccordionDetails>
                    <List sx={{ width: '100%' }} dense>
                        {parsingErrors.map((value) => (
                            <ListItem
                                key={value}
                                disableGutters
                            >
                                <ListItemIcon className='error-list-item'>
                                    <DecideIconType errorType={value.isWarning} />
                                </ListItemIcon>
                                <ListItemText
                                    primary={`Line ${value.row} Position ${value.column}: ${value.description}`}
                                />
                            </ListItem>
                        ))}
                    </List>
                </AccordionDetails>
            </Accordion>
        </>
    );
};

export default ErrorList;
