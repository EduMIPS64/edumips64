import React from 'react';

import CssBaseline from '@mui/material/CssBaseline';
import CircularProgress from '@mui/material/CircularProgress';
import Typography from '@mui/material/Typography';
import Box from '@mui/material/Box';
import Button from '@mui/material/Button';

// Simulator is still a .js component (converted in phase 3); TypeScript
// treats the import as the inferred JS export shape (checkJs: false).
import Simulator from './Simulator';

import type { SimulatorResult, SimulatorWorker } from '../simulator/protocol';
import type { ITelemetryClient } from '../telemetry';

// The cracked-CPU artwork that the Swing UI's ErrorDialog has shown for
// years — reused here so the web error screen keeps the same personality.
import errorImage from '../static/error-hires.png';

// ---------------------------------------------------------------------------
// Types
// ---------------------------------------------------------------------------

/**
 * Discriminated union of the three loading phases.
 *   loading  – waiting for the first worker message.
 *   ready    – Simulator can render with the parsed initial state.
 *   error    – the worker errored or the 30-second watchdog expired.
 */
type LoaderPhase = 'loading' | 'ready' | 'error';

interface AppLoaderProps {
  /**
   * Augmented Worker instance (parseResult / step / reset / … methods are
   * already monkey-patched by index.js before AppLoader mounts).
   */
  worker: SimulatorWorker;
  /** Optional telemetry client; load failures are reported via trackException. */
  appInsights?: ITelemetryClient;
}

interface AppLoaderState {
  phase: LoaderPhase;
  /** Set when phase === 'ready'; the parsed initial CPU state from the worker. */
  initialState: SimulatorResult | null;
  /** Set when phase === 'error'; human-readable description of the failure. */
  errorMessage: string | null;
}

/**
 * AppLoader mounts immediately and handles the worker initialisation
 * handshake.  Three phases:
 *
 *   loading  – spinner is shown while waiting for the first worker message.
 *   ready    – Simulator is rendered with the parsed initial state.
 *   error    – the worker fired an 'error' event OR the 30-second timeout
 *              expired; an error panel is shown with a Reload button.
 *
 * Design choices:
 *   - The 'message' and 'error' listeners are attached in componentDidMount,
 *     immediately before worker.reset() is called in the same method. The
 *     worker only speaks when spoken to, so attaching and kicking in the same
 *     synchronous block guarantees the init message is never missed. Doing
 *     this in the constructor would be a side effect during render, which
 *     React.StrictMode intentionally double-invokes — leaking a listener and
 *     a watchdog timer per extra invocation.
 *   - worker.reset() is called from componentDidMount (NOT from index.js);
 *     index.js must NOT call worker.reset() itself. Under StrictMode's
 *     dev-only mount→unmount→remount cycle reset() runs twice; the second
 *     init message is consumed by the Simulator's own listener as a regular
 *     READY result, which is harmless.
 */
class AppLoader extends React.Component<AppLoaderProps, AppLoaderState> {
  private _timeout: ReturnType<typeof setTimeout> | null = null;

  constructor(props: AppLoaderProps) {
    super(props);
    this.state = { phase: 'loading', initialState: null, errorMessage: null };

    // Bind handlers so they can be removed from the worker later.
    this._onMessage = this._onMessage.bind(this);
    this._onError = this._onError.bind(this);
  }

  private _onMessage(evt: MessageEvent): void {
    // Only handle the very first message; Simulator.js registers its own
    // listener for all subsequent messages.
    this._cleanup();
    try {
      const initialState = this.props.worker.parseResult(
        evt.data as Record<string, unknown>,
      );
      this.setState({ phase: 'ready', initialState });
    } catch (err) {
      this._fail(
        `Failed to parse the worker initialisation message: ${(err as Error).message}`,
      );
    }
  }

  private _onError(evt: ErrorEvent): void {
    this._cleanup();
    const msg =
      evt && evt.message
        ? evt.message
        : 'The worker encountered an error during startup.';
    this._fail(msg);
  }

  private _fail(errorMessage: string): void {
    try {
      this.props.appInsights?.trackException?.({
        exception: new Error(errorMessage),
      });
    } catch {
      // intentionally swallowed
    }
    this.setState({ phase: 'error', errorMessage });
  }

  private _cleanup(): void {
    if (this._timeout !== null) {
      clearTimeout(this._timeout);
      this._timeout = null;
    }
    this.props.worker.removeEventListener('message', this._onMessage);
    this.props.worker.removeEventListener('error', this._onError);
  }

  componentDidMount(): void {
    // Attach listeners and kick the worker in the same synchronous block:
    // the worker only emits messages in response to requests, so nothing can
    // arrive between addEventListener and reset(). Listeners live in
    // componentDidMount (not the constructor) so StrictMode's double-invoked
    // constructor cannot leak them; componentWillUnmount cleans them up.
    this.props.worker.addEventListener('message', this._onMessage);
    this.props.worker.addEventListener('error', this._onError);

    // Arm a 30-second watchdog. If neither a message nor an error event
    // arrives within this window we surface an error so the user is not left
    // staring at a blank spinner forever.
    this._timeout = setTimeout(() => {
      this._fail(
        'The simulator core did not respond within 30 seconds. The worker.js file may be missing or failed to start.',
      );
    }, 30000);

    this.props.worker.reset();
  }

  componentWillUnmount(): void {
    this._cleanup();
  }

  render(): React.ReactNode {
    const { phase, initialState, errorMessage } = this.state;
    const { worker, appInsights } = this.props;

    if (phase === 'loading') {
      return (
        <>
          <CssBaseline />
          <Box
            id="app-loading"
            sx={{
              display: 'flex',
              flexDirection: 'column',
              alignItems: 'center',
              justifyContent: 'center',
              minHeight: '100vh',
              gap: 2,
            }}
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
            sx={{
              display: 'flex',
              flexDirection: 'column',
              alignItems: 'center',
              justifyContent: 'center',
              minHeight: '100vh',
              gap: 2,
              p: 3,
            }}
          >
            <img
              src={errorImage}
              alt="A cracked CPU"
              style={{ width: 220, maxWidth: '60%', height: 'auto' }}
            />
            <Typography variant="h6" color="error">
              Failed to load the EduMIPS64 simulator core
            </Typography>
            <Typography
              variant="body2"
              sx={{ maxWidth: 500, textAlign: 'center' }}
            >
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

    // phase === 'ready': initialState is guaranteed non-null because the
    // 'ready' transition only fires from setState({ phase: 'ready', initialState })
    // where initialState is the parsed worker result.  The non-null assertion
    // here avoids a misleading conditional guard in the hot rendering path.
    if (!initialState) return null;
    return (
      <>
        <CssBaseline />
        <Simulator
          worker={worker}
          initialState={initialState}
          appInsights={appInsights!}
        />
      </>
    );
  }
}

export default AppLoader;
