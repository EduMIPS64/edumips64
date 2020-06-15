import React from 'react';

import AppBar from '@material-ui/core/AppBar';
import ToolBar from '@material-ui/core/Toolbar';
import Button from '@material-ui/core/Button';
import { makeStyles } from '@material-ui/styles';

import HelpDialog from './HelpDialog';
import logo from '../static/logo.png';

const useStyles = makeStyles((theme) => ({
  logo: {
    height: '4em',
    paddingLeft: '0.5em',
  },
  helpButton: {
    marginLeft: 'auto',
    marginRight: '1em',
  },
}));

export default function Header() {
  const classes = useStyles();
  const [dialogOpen, setDialogOpen] = React.useState(false);

  return (
    <AppBar position="static">
      <ToolBar disableGutters>
        <img id="logo" alt="EduMIPS64" src={logo} className={classes.logo} />
        <Button
          variant="contained"
          color="secondary"
          className={classes.helpButton}
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
