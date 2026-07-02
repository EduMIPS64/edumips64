import { createTheme, responsiveFontSizes } from '@mui/material/styles';

/**
 * Central MUI theme for the "dashboard" web UI.
 *
 * Design goals:
 *  - a mission-control feel: every widget is a card on a tinted canvas,
 *    all visible at once, with clear card headers;
 *  - a distinctive violet/teal identity (vs. the stock MUI blue), tuned
 *    for both a light and a deep-navy dark canvas;
 *  - Inter as the UI typeface (Roboto fallback), sentence-case buttons,
 *    generous 14px card radius.
 *
 * Everything here is purely visual: no component behavior depends on it.
 */

const FONT_STACK =
  '"Inter", "Roboto", "Helvetica Neue", "Arial", sans-serif';

const VIOLET = '#5b34c7';
const VIOLET_LIGHT = '#b39afc';

export function buildTheme(mode) {
  const isDark = mode === 'dark';

  const palette = isDark
    ? {
        mode: 'dark',
        primary: { main: VIOLET_LIGHT },
        secondary: { main: '#2dd4bf' },
        background: { default: '#0a0f1e', paper: '#141a2e' },
        divider: 'rgba(148, 163, 184, 0.16)',
        text: { primary: '#e2e8f0', secondary: '#94a3b8' },
      }
    : {
        mode: 'light',
        primary: { main: VIOLET },
        secondary: { main: '#0d9488' },
        background: { default: '#edf0f7', paper: '#ffffff' },
        divider: 'rgba(15, 23, 42, 0.10)',
        text: { primary: '#171c2b', secondary: '#586174' },
      };

  const base = createTheme({
    palette,
    shape: { borderRadius: 12 },
    typography: {
      fontFamily: FONT_STACK,
      button: { textTransform: 'none', fontWeight: 600, letterSpacing: 0 },
      h6: { fontWeight: 700 },
    },
    components: {
      MuiAppBar: {
        defaultProps: { elevation: 0 },
        styleOverrides: {
          root: {
            backgroundImage: 'none',
            background: isDark
              ? 'linear-gradient(90deg, #131a33 0%, #1d1440 100%)'
              : `linear-gradient(90deg, #3b1d8f 0%, ${VIOLET} 100%)`,
          },
        },
      },
      MuiButton: {
        styleOverrides: {
          root: { borderRadius: 10 },
        },
      },
      MuiCard: {
        defaultProps: { elevation: 0 },
        styleOverrides: {
          root: {
            border: `1px solid ${palette.divider}`,
            boxShadow: isDark
              ? '0 1px 3px rgba(0, 0, 0, 0.4)'
              : '0 1px 3px rgba(23, 28, 43, 0.06)',
          },
        },
      },
      MuiChip: {
        styleOverrides: {
          root: { fontWeight: 600 },
        },
      },
      MuiTooltip: {
        styleOverrides: {
          tooltip: {
            borderRadius: 8,
            fontSize: '0.75rem',
            backgroundColor: isDark ? '#334155' : '#1e293b',
          },
          arrow: { color: isDark ? '#334155' : '#1e293b' },
        },
      },
      MuiPaper: {
        styleOverrides: {
          root: { backgroundImage: 'none' },
        },
      },
    },
  });

  return responsiveFontSizes(base);
}
