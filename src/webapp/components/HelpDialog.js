import React from 'react';

import Button from '@material-ui/core/Button';
import Dialog from '@material-ui/core/Dialog';
import DialogContent from '@material-ui/core/DialogContent';
import DialogTitle from '@material-ui/core/DialogTitle';
import Link from '@material-ui/core/Link';
import { Typography } from '@material-ui/core';
import { makeStyles } from '@material-ui/styles';

const useStyles = makeStyles((theme) => ({
  dialog: {
    padding: theme.spacing(1),
  },
  title: {
    padding: theme.spacing(1),
  },
}));

export default function HelpDialog(props) {
  const classes = useStyles();
  return (
    <Dialog onClose={props.handleClose} open={props.open}>
      <DialogTitle className={classes.title}>
        <Typography variant="h4">
          EduMIPS64 Experimental Web Frontend
        </Typography>
      </DialogTitle>
      <DialogContent classname={classes.dialog}>
        <Typography gutterBottom variant="h5">
          Help
        </Typography>
        <Typography gutterBottom>
          For help on how to use EduMIPS64, please go to{' '}
          <Link href="https://edumips64.readthedocs.io/en/latest/">
            Read the docs
          </Link>
          .
        </Typography>
        <Typography gutterBottom>
          It contains information on the desktop version, but everything except
          the UI part is applicable to this simulator (with some exceptions like
          SYSCALL)
        </Typography>
        <Typography gutterBottom>
          Once you load a program, hover over any instruction to see information
          about it. You will be able to see the address, its binary
          representation, the opcode and the CPU stage in which the instruction
          is in the current step, if it is in the pipeline.
        </Typography>
        <Typography gutterBottom>
          CPU stages are also encoded by colors. (a legend will be added.)
        </Typography>
        <Typography gutterBottom variant="h5">
          About
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
        <Button autoFocus onClick={props.handleClose} variant="text">
          Close
        </Button>
      </DialogContent>
    </Dialog>
  );
}
