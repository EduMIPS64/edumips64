import React from 'react';

import Paper from '@mui/material/Paper';
import Typography from '@mui/material/Typography';
import Button from '@mui/material/Button';
import Box from '@mui/material/Box';
import Accordion from '@mui/material/Accordion';
import AccordionSummary from '@mui/material/AccordionSummary';
import AccordionDetails from '@mui/material/AccordionDetails';
import ExpandMoreIcon from '@mui/icons-material/ExpandMore';

// The cracked-CPU artwork that the Swing UI's ErrorDialog has shown for
// years — reused here so the web error screen keeps the same personality.
import errorImage from '../static/error-hires.png';

/**
 * Top-level error boundary that catches render/lifecycle errors anywhere in
 * the component tree below it.
 *
 * Error boundaries MUST be class components – React does not support the
 * componentDidCatch lifecycle method in function components.
 *
 * Props:
 *   appInsights  – (optional) ApplicationInsights instance. When provided,
 *                  exceptions are tracked via trackException. A missing or
 *                  partially-initialised instance is handled safely.
 *   children     – the wrapped component tree.
 */
class AppErrorBoundary extends React.Component {
  constructor(props) {
    super(props);
    this.state = { hasError: false, error: null, errorInfo: null };
  }

  static getDerivedStateFromError(error) {
    return { hasError: true, error };
  }

  componentDidCatch(error, errorInfo) {
    this.setState({ errorInfo });
    // Guard so a missing / partially-constructed appInsights never throws.
    try {
      this.props.appInsights?.trackException?.({ exception: error });
    } catch (_) {
      // intentionally swallowed
    }
  }

  render() {
    if (!this.state.hasError) {
      return this.props.children;
    }

    const { error, errorInfo } = this.state;
    const message = error ? error.message : 'An unknown error occurred.';
    const stack = errorInfo ? errorInfo.componentStack : (error && error.stack ? error.stack : '');

    return (
      <Box
        id="error-boundary-fallback"
        sx={{
          display: 'flex',
          justifyContent: 'center',
          alignItems: 'center',
          minHeight: '100vh',
          bgcolor: 'background.default',
          p: 2,
        }}
      >
        <Paper elevation={4} sx={{ maxWidth: 600, width: '100%', p: 4 }}>
          <Box sx={{ display: 'flex', justifyContent: 'center', mb: 2 }}>
            <img
              src={errorImage}
              alt="A cracked CPU"
              style={{ width: 220, maxWidth: '60%', height: 'auto' }}
            />
          </Box>
          <Typography variant="h5" component="h1" color="error" gutterBottom>
            EduMIPS64 hit an unexpected error
          </Typography>
          <Typography variant="body1" sx={{ mb: 2 }}>
            {message}
          </Typography>
          <Accordion>
            <AccordionSummary expandIcon={<ExpandMoreIcon />}>
              <Typography variant="body2">Stack trace</Typography>
            </AccordionSummary>
            <AccordionDetails>
              <Typography
                variant="caption"
                component="pre"
                sx={{ whiteSpace: 'pre-wrap', wordBreak: 'break-all', fontSize: '0.75rem' }}
              >
                {stack}
              </Typography>
            </AccordionDetails>
          </Accordion>
          <Box sx={{ mt: 3 }}>
            <Button
              id="error-boundary-reload"
              variant="contained"
              color="primary"
              onClick={() => window.location.reload()}
            >
              Reload
            </Button>
          </Box>
        </Paper>
      </Box>
    );
  }
}

export default AppErrorBoundary;
