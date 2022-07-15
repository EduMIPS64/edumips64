import React from 'react';

import AppBar from '@mui/material/AppBar';
import ToolBar from '@mui/material/Toolbar';
import Button from '@mui/material/Button';

import HelpDialog from './HelpDialog';
import logo from '../static/logo.png';


export default function Header() {
  const [dialogOpen, setDialogOpen] = React.useState(false);

  return (
    <AppBar position="static">
      <ToolBar disableGutters>
        <img id="logo" alt="EduMIPS64" src={logo} className="logo" />
        <Button
          variant="contained"
          color="secondary"
          className="help-button"
          onClick={() => setDialogOpen(true)}
        >
          Help
        </Button>
        <HelpDialog
          open={dialogOpen}
          handleClose={() => setDialogOpen(false)}
        />
      </ToolBar>
    </AppBar>
  );
}
