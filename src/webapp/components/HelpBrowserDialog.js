import React, { useState } from 'react';

import Button from '@mui/material/Button';
import Dialog from '@mui/material/Dialog';
import DialogContent from '@mui/material/DialogContent';
import DialogTitle from '@mui/material/DialogTitle';
import FormControl from '@mui/material/FormControl';
import InputLabel from '@mui/material/InputLabel';
import MenuItem from '@mui/material/MenuItem';
import Select from '@mui/material/Select';
import { Typography } from '@mui/material';

const HELP_LANGUAGES = [
  { code: 'en', name: 'English' },
  { code: 'it', name: 'Italiano' },
  { code: 'zh', name: '中文' }
];

export default function HelpBrowserDialog(props) {
  const [selectedLanguage, setSelectedLanguage] = useState('en');
  const [currentPage, setCurrentPage] = useState('index.html');

  const handleLanguageChange = (event) => {
    setSelectedLanguage(event.target.value);
    setCurrentPage('index.html');
  };

  const helpUrl = `help/${selectedLanguage}/${currentPage}`;

  return (
    <Dialog onClose={props.handleClose} open={props.open} maxWidth="lg" fullWidth>
      <DialogTitle>
        <Typography variant="h5" component="div" sx={{ flexGrow: 1 }}>
          EduMIPS64 Help
        </Typography>
        <FormControl variant="outlined" size="small" sx={{ minWidth: 120, ml: 2 }}>
          <InputLabel>Language</InputLabel>
          <Select
            value={selectedLanguage}
            onChange={handleLanguageChange}
            label="Language"
          >
            {HELP_LANGUAGES.map((lang) => (
              <MenuItem key={lang.code} value={lang.code}>
                {lang.name}
              </MenuItem>
            ))}
          </Select>
        </FormControl>
      </DialogTitle>
      <DialogContent sx={{ height: '70vh', padding: 0 }}>
        <iframe
          src={helpUrl}
          style={{
            width: '100%',
            height: '100%',
            border: 'none',
          }}
          title="EduMIPS64 Help"
        />
      </DialogContent>
      <DialogContent sx={{ p: 1, textAlign: 'right' }}>
        <Button onClick={props.handleClose} variant="contained">
          Close
        </Button>
      </DialogContent>
    </Dialog>
  );
}