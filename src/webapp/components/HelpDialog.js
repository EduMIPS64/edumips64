import React from 'react';

import Button from '@mui/material/Button';
import Dialog from '@mui/material/Dialog';
import DialogContent from '@mui/material/DialogContent';
import DialogTitle from '@mui/material/DialogTitle';
import DialogActions from '@mui/material/DialogActions';
import Link from '@mui/material/Link';
import Tabs from '@mui/material/Tabs';
import Tab from '@mui/material/Tab';
import Box from '@mui/material/Box';
import Select from '@mui/material/Select';
import MenuItem from '@mui/material/MenuItem';
import FormControl from '@mui/material/FormControl';
import InputLabel from '@mui/material/InputLabel';
import { Typography } from '@mui/material';


function TabPanel(props) {
  const { children, value, index, ...other } = props;

  return (
    <div
      role="tabpanel"
      hidden={value !== index}
      id={`help-tabpanel-${index}`}
      aria-labelledby={`help-tab-${index}`}
      {...other}
    >
      {value === index && <Box sx={{ p: 3 }}>{children}</Box>}
    </div>
  );
}

export default function HelpDialog(props) {
  const [tabValue, setTabValue] = React.useState(0);
  const [language, setLanguage] = React.useState('en');

  const handleTabChange = (event, newValue) => {
    setTabValue(newValue);
  };

  const handleLanguageChange = (event) => {
    setLanguage(event.target.value);
  };

  return (
    <Dialog 
      onClose={props.handleClose} 
      open={props.open}
      maxWidth="lg"
      fullWidth
    >
      <DialogTitle className='help-title'>
        <Typography variant="h4">
          EduMIPS64 Web Frontend
        </Typography>
      </DialogTitle>
      <Box sx={{ borderBottom: 1, borderColor: 'divider' }}>
        <Tabs value={tabValue} onChange={handleTabChange} aria-label="help tabs">
          <Tab label="User Manual" id="help-tab-0" />
          <Tab label="About" id="help-tab-1" />
        </Tabs>
      </Box>
      <DialogContent className='help-content' sx={{ minHeight: '500px' }}>
        <TabPanel value={tabValue} index={0}>
          <Box sx={{ mb: 2 }}>
            <FormControl size="small" sx={{ minWidth: 120 }}>
              <InputLabel id="language-select-label">Language</InputLabel>
              <Select
                labelId="language-select-label"
                id="language-select"
                value={language}
                label="Language"
                onChange={handleLanguageChange}
              >
                <MenuItem value="en">English</MenuItem>
                <MenuItem value="it">Italiano</MenuItem>
                <MenuItem value="zh">中文</MenuItem>
              </Select>
            </FormControl>
          </Box>
          <iframe
            src={`docs/${language}/html/index.html`}
            style={{
              width: '100%',
              height: '600px',
              border: '1px solid #ccc',
              borderRadius: '4px'
            }}
            title="EduMIPS64 User Manual"
            id="help-iframe"
          />
        </TabPanel>
        <TabPanel value={tabValue} index={1}>
          <Typography>Version: {props.ver}</Typography>
          <Typography gutterBottom variant="h6" sx={{ mt: 2 }}>
            Quick Start
          </Typography>
          <Typography gutterBottom>
            Once you load a program, hover over any instruction to see information
            about it. You will be able to see the address, its binary
            representation, the opcode and the CPU stage in which the instruction
            is in the current step, if it is in the pipeline.
          </Typography>
          <Typography gutterBottom>
            CPU stages are also encoded by colors.
          </Typography>
          <Typography gutterBottom variant="h6" sx={{ mt: 2 }}>
            About EduMIPS64
          </Typography>
          <Typography gutterBottom>
            This is the web version of the{' '}
            <Link href="https://www.edumips.org">EduMIPS64 CPU simulator</Link>.
          </Typography>
          <Typography gutterBottom>
            This is currently <strong>work-in-progress</strong>, very early stages
            and not fully functional. See{' '}
            <Link
              href="https://github.com/EduMIPS64/edumips64/issues?q=is%3Aissue+is%3Aopen+label%3Acomponent%3Aweb-ui"
              target="_blank"
              rel="noreferrer"
            >
              known issues
            </Link>
            .
          </Typography>
          <Typography gutterBottom>
            The core of the simulator is cross-compiled from Java to JavaScript,
            and the UI is developed with React.
          </Typography>
          <Typography gutterBottom>
            If you are interested in the evolution of this web application or want
            to contribute to it, please get in touch via{' '}
            <Link
              href="https://github.com/EduMIPS64/edumips64"
              target="_blank"
              rel="noreferrer"
            >
              GitHub
            </Link>
            !
          </Typography>
        </TabPanel>
      </DialogContent>
      <DialogActions>
        <Button onClick={props.handleClose} variant="outlined">
          Close
        </Button>
      </DialogActions>
    </Dialog>
  );
}
