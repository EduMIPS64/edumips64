import React, { useState } from 'react';

import Button from '@mui/material/Button';
import Dialog from '@mui/material/Dialog';
import DialogContent from '@mui/material/DialogContent';
import DialogTitle from '@mui/material/DialogTitle';
import Link from '@mui/material/Link';
import { Typography } from '@mui/material';
import HelpBrowserDialog from './HelpBrowserDialog';


export default function HelpDialog(props) {
  const [helpBrowserOpen, setHelpBrowserOpen] = useState(false);

  const handleOpenHelpBrowser = () => {
    setHelpBrowserOpen(true);
  };

  const handleCloseHelpBrowser = () => {
    setHelpBrowserOpen(false);
  };
  return (
    <Dialog onClose={props.handleClose} open={props.open}>
      <DialogTitle className='help-title'>
        <Typography variant="h4">
          EduMIPS64 Experimental Web Frontend
        </Typography>
      </DialogTitle>
      <DialogContent className='help-content'>
        <Typography>Version: {props.ver}</Typography>
        <Typography gutterBottom variant="h5">
          Help
        </Typography>
        <Typography gutterBottom>
          <Button 
            variant="contained" 
            onClick={handleOpenHelpBrowser}
            sx={{ mr: 2, mb: 1 }}
          >
            Open EduMIPS64 Documentation
          </Button>
        </Typography>
        <Typography gutterBottom>
          The documentation contains information on how to use EduMIPS64, including
          detailed descriptions of all supported instructions, the user interface, 
          and file formats.
        </Typography>
        <Typography gutterBottom>
          For additional help, you can also visit{' '}
          <Link href="https://edumips64.readthedocs.io/en/latest/">
            the online documentation
          </Link>
          {' '}(requires internet connection).
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
        <Button autoFocus onClick={props.handleClose} variant="outlined">
          Close
        </Button>
      </DialogContent>
      <HelpBrowserDialog 
        open={helpBrowserOpen}
        handleClose={handleCloseHelpBrowser}
      />
    </Dialog>
  );
}
