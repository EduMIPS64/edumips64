import React, { useState, useRef, useEffect } from 'react';

import Button from '@mui/material/Button';
import Dialog from '@mui/material/Dialog';
import DialogContent from '@mui/material/DialogContent';
import DialogTitle from '@mui/material/DialogTitle';
import FormControl from '@mui/material/FormControl';
import InputLabel from '@mui/material/InputLabel';
import MenuItem from '@mui/material/MenuItem';
import Select from '@mui/material/Select';
import { Typography, Box, useTheme } from '@mui/material';

const HELP_LANGUAGES = [
  { code: 'en', name: 'English' },
  { code: 'it', name: 'Italiano' },
  { code: 'zh', name: '中文' }
];

export default function HelpBrowserDialog(props) {
  const [selectedLanguage, setSelectedLanguage] = useState('en');
  const iframeRef = useRef(null);
  const theme = useTheme();

  const handleLanguageChange = (event) => {
    setSelectedLanguage(event.target.value);
    // Navigate to index page when language changes
    if (iframeRef.current) {
      iframeRef.current.src = `help/${event.target.value}/index.html`;
    }
  };

  // Function to inject theme-aware styles into the iframe
  const injectThemeStyles = () => {
    if (!iframeRef.current || !iframeRef.current.contentDocument) return;

    const isDark = theme.palette.mode === 'dark';
    const doc = iframeRef.current.contentDocument;
    
    // Remove existing theme styles
    const existingStyle = doc.getElementById('theme-override-styles');
    if (existingStyle) {
      existingStyle.remove();
    }

    // Create new style element with theme-aware CSS
    const style = doc.createElement('style');
    style.id = 'theme-override-styles';
    style.textContent = `
      ${isDark ? `
        /* Dark theme overrides */
        body {
          background-color: #121212 !important;
          color: #ffffff !important;
        }
        
        div.document {
          background-color: #121212 !important;
        }
        
        div.body {
          background-color: #121212 !important;
          color: #ffffff !important;
        }
        
        /* Links */
        a:link {
          color: #90caf9 !important;
        }
        
        a:visited {
          color: #ce93d8 !important;
        }
        
        /* Headers */
        h1, h2, h3, h4, h5, h6 {
          color: #ffffff !important;
        }
        
        /* Code blocks */
        div.highlight {
          background-color: #1e1e1e !important;
        }
        
        pre {
          background-color: #1e1e1e !important;
          color: #ffffff !important;
          border: 1px solid #333333 !important;
        }
        
        code {
          background-color: #1e1e1e !important;
          color: #ffffff !important;
        }
        
        /* Tables */
        table {
          background-color: #1e1e1e !important;
          color: #ffffff !important;
        }
        
        th {
          background-color: #333333 !important;
          color: #ffffff !important;
        }
        
        td {
          background-color: #1e1e1e !important;
          color: #ffffff !important;
          border-color: #333333 !important;
        }
        
        /* Sidebar and navigation */
        div.sphinxsidebar {
          background-color: #1e1e1e !important;
          color: #ffffff !important;
        }
        
        /* Search */
        div.search {
          background-color: #1e1e1e !important;
        }
        
        input[type="text"] {
          background-color: #333333 !important;
          color: #ffffff !important;
          border-color: #555555 !important;
        }
        
        /* Admonitions */
        div.admonition {
          background-color: #1e1e1e !important;
          border-color: #555555 !important;
          color: #ffffff !important;
        }
        
        /* Note boxes */
        div.note {
          background-color: #1a237e !important;
          border-color: #3f51b5 !important;
        }
        
        div.warning {
          background-color: #e65100 !important;
          border-color: #ff9800 !important;
        }
        
        /* Highlighted text */
        span.highlighted {
          background-color: #ffb74d !important;
          color: #000000 !important;
        }
        
        /* Footer */
        div.footer {
          background-color: #1e1e1e !important;
          color: #ffffff !important;
        }
      ` : `
        /* Light theme - minimal overrides to ensure good contrast */
        body {
          background-color: #ffffff !important;
          color: #000000 !important;
        }
        
        /* Ensure proper contrast for code blocks */
        div.highlight {
          background-color: #f5f5f5 !important;
        }
        
        pre {
          background-color: #f5f5f5 !important;
          color: #000000 !important;
        }
        
        code {
          background-color: #f5f5f5 !important;
          color: #000000 !important;
        }
      `}
    `;
    
    doc.head.appendChild(style);
  };

  // Effect to inject styles when iframe loads
  useEffect(() => {
    const iframe = iframeRef.current;
    if (iframe) {
      const handleLoad = () => {
        // Add a small delay to ensure content is fully loaded
        setTimeout(injectThemeStyles, 100);
      };
      
      iframe.addEventListener('load', handleLoad);
      return () => iframe.removeEventListener('load', handleLoad);
    }
  }, [theme.palette.mode]);

  // Effect to update styles when theme changes
  useEffect(() => {
    injectThemeStyles();
  }, [theme.palette.mode]);

  const helpUrl = `help/${selectedLanguage}/index.html`;

  return (
    <Dialog onClose={props.handleClose} open={props.open} maxWidth="lg" fullWidth>
      <DialogTitle>
        <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
          <Typography variant="h5" component="div">
            EduMIPS64 Help
          </Typography>
          <FormControl variant="outlined" size="small" sx={{ minWidth: 120 }}>
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
        </Box>
      </DialogTitle>
      <DialogContent sx={{ height: '70vh', padding: 0 }}>
        <iframe
          ref={iframeRef}
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