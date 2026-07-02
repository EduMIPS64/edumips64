import React from 'react';

import CssBaseline from '@mui/material/CssBaseline';
import CircularProgress from '@mui/material/CircularProgress';
import Typography from '@mui/material/Typography';
import Box from '@mui/material/Box';
import Button from '@mui/material/Button';

import Simulator from './Simulator';

/**
 * AppLoader mounts immediately and handles the worker initialisation
 * handshake.  Three states:
 *
 *   loading    – spinner is shown while waiting for the first worker message.
 *   ready      – Simulator is rendered with the parsed initial state.
 *   error      – the worker fired an 'error' event OR the 30-second timeout
 *                expired; an error panel is shown with a Reload button.
 *
 * Design choices:
 *   - The 'message' and 'error' listeners are attached synchronously in the
 *     constructor so they are guaranteed to be in place before
 *     componentDidMount calls worker.reset().  A useEffect-based approach
 *     would be equivalent for mount (effects also run after commit), but a
 *     class constructor makes the ordering explicit and avoids any ambiguity
 *     with React's concurrent scheduler.
 *   - worker.reset() is called from componentDidMount (NOT from index.js).
 *     React's commit phase runs the constructor → render → componentDidMount
 *     sequence atomically before yielding to the browser event loop, so the
 *     listener is always in place before reset() triggers the first worker
 *     message.  index.js must NOT call worker.reset() itself.
 *
 * Props:
 *   worker      – augmented Worker instance (parseResult / step / … already
 *                 monkey-patched by index.js).
 *   appInsights – (optional) ApplicationInsights instance.
 */
class AppLoader extends React.Component {
  constructor(props) {
    super(props);
    this.state = { phase: 'loading', initialState: null, errorMessage: null };
    this._timeout = null;

    // Bind handlers so they can be removed from the worker later.
    this._onMessage = this._onMessage.bind(this);
    this._onError = this._onError.bind(this);

    // Attach listeners NOW (synchronously, before reset() is called by the
    // parent).  This ensures the first message is never missed.
    this.props.worker.addEventListener('message', this._onMessage);
    this.props.worker.addEventListener('error', this._onError);

    // Arm a 30-second watchdog.  If neither a message nor an error event
    // arrives within this window we surface an error so the user is not left
    // staring at a blank spinner forever.
    this._timeout = setTimeout(() => {
      this._fail('The simulator core did not respond within 30 seconds. The worker.js file may be missing or failed to start.');
    }, 30000);
  }

  _onMessage(evt) {
    // Only handle the very first message; Simulator.js registers its own
    // listener for all subsequent messages.
    this._cleanup();
    try {
      const initialState = this.props.worker.parseResult(evt.data);
      this.setState({ phase: 'ready', initialState });
    } catch (err) {
      this._fail(`Failed to parse the worker initialisation message: ${err.message}`);
    }
  }

  _onError(evt) {
    this._cleanup();
    const msg = (evt && evt.message) ? evt.message : 'The worker encountered an error during startup.';
    this._fail(msg);
  }

  _fail(errorMessage) {
    try {
      this.props.appInsights?.trackException?.({ exception: new Error(errorMessage) });
    } catch (_) {
      // intentionally swallowed
    }
    this.setState({ phase: 'error', errorMessage });
  }

  _cleanup() {
    if (this._timeout !== null) {
      clearTimeout(this._timeout);
      this._timeout = null;
    }
    this.props.worker.removeEventListener('message', this._onMessage);
    this.props.worker.removeEventListener('error', this._onError);
  }

  componentDidMount() {
    // Kick the worker here — after the constructor has attached its listeners —
    // to guarantee the init message is never missed regardless of React's
    // scheduling behaviour.
    this.props.worker.reset();
  }

  componentWillUnmount() {
    this._cleanup();
  }

  render() {
    const { phase, initialState, errorMessage } = this.state;
    const { worker, appInsights } = this.props;

    if (phase === 'loading') {
      return (
        <>
          <CssBaseline />
          <Box
            id="app-loading"
            display="flex"
            flexDirection="column"
            alignItems="center"
            justifyContent="center"
            minHeight="100vh"
            gap={2}
          >
            <CircularProgress />
            <Typography variant="body1">
              Loading the EduMIPS64 simulator core&hellip;
            </Typography>
          </Box>
        </>
      );
    }

    if (phase === 'error') {
      return (
        <>
          <CssBaseline />
          <Box
            id="app-load-error"
            display="flex"
            flexDirection="column"
            alignItems="center"
            justifyContent="center"
            minHeight="100vh"
            gap={2}
            p={3}
          >
            <Typography variant="h6" color="error">
              Failed to load the EduMIPS64 simulator core
            </Typography>
            <Typography variant="body2" sx={{ maxWidth: 500, textAlign: 'center' }}>
              {errorMessage}
            </Typography>
            <Button
              variant="contained"
              color="primary"
              onClick={() => window.location.reload()}
            >
              Reload
            </Button>
          </Box>
        </>
      );
    }

    // phase === 'ready'
    return (
      <>
        <CssBaseline />
        <Simulator
          worker={worker}
          initialState={initialState}
          appInsights={appInsights}
        />
      </>
    );
  }
}

export default AppLoader;
