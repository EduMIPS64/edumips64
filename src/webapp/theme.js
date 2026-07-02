import { createTheme, responsiveFontSizes } from '@mui/material/styles';

/**
 * Central MUI theme for the web UI.
 *
 * Design goals:
 *  - a calmer, more modern palette than the stock MUI blue: indigo primary
 *    on a soft off-white canvas in light mode, and a slate ("midnight")
 *    canvas in dark mode instead of pure grey;
 *  - Inter as the UI typeface (with Roboto fallback), sentence-case buttons;
 *  - softer shapes: 10px corner radius, borderless flat accordions divided
 *    by hairlines instead of boxed panels with heavy backgrounds.
 *
 * Everything here is purely visual: no component behavior, ids or DOM
 * structure depend on the theme.
 */

const FONT_STACK =
  '"Inter", "Roboto", "Helvetica Neue", "Arial", sans-serif';

// Brand colors, shared by both modes.
const INDIGO = '#3557d4';
const INDIGO_LIGHT = '#8da6ff';

export function buildTheme(mode) {
  const isDark = mode === 'dark';

  const palette = isDark
    ? {
        mode: 'dark',
        primary: { main: INDIGO_LIGHT },
        secondary: { main: '#4dd0e1' },
        background: { default: '#0e1526', paper: '#141d33' },
        divider: 'rgba(148, 163, 184, 0.18)',
        text: { primary: '#e2e8f0', secondary: '#97a3b8' },
      }
    : {
        mode: 'light',
        primary: { main: INDIGO },
        secondary: { main: '#0891b2' },
        background: { default: '#f5f6fa', paper: '#ffffff' },
        divider: 'rgba(15, 23, 42, 0.10)',
        text: { primary: '#1a2233', secondary: '#5b6577' },
      };

  const base = createTheme({
    palette,
    shape: { borderRadius: 10 },
    typography: {
      fontFamily: FONT_STACK,
      button: { textTransform: 'none', fontWeight: 600, letterSpacing: 0 },
      h6: { fontWeight: 650 },
    },
    components: {
      MuiAppBar: {
        defaultProps: { elevation: 0 },
        styleOverrides: {
          root: {
            backgroundImage: 'none',
            background: isDark
              ? 'linear-gradient(90deg, #101a30 0%, #16223d 100%)'
              : `linear-gradient(90deg, #24398f 0%, ${INDIGO} 100%)`,
            borderBottom: `1px solid ${
              isDark ? 'rgba(148, 163, 184, 0.18)' : 'rgba(255,255,255,0.12)'
            }`,
          },
        },
      },
      MuiButton: {
        styleOverrides: {
          root: { borderRadius: 8 },
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
          arrow: {
            color: isDark ? '#334155' : '#1e293b',
          },
        },
      },
      MuiAccordion: {
        defaultProps: { elevation: 0 },
        styleOverrides: {
          root: {
            backgroundImage: 'none',
            backgroundColor: 'transparent',
            borderBottom: `1px solid ${palette.divider}`,
            // Remove the default top hairline MUI paints between stacked
            // accordions; the bottom border above is the single divider.
            '&::before': { display: 'none' },
          },
        },
      },
      MuiAccordionSummary: {
        styleOverrides: {
          root: {
            minHeight: 44,
            transition: 'background-color 120ms ease',
            '&:hover': {
              backgroundColor: isDark
                ? 'rgba(141, 166, 255, 0.06)'
                : 'rgba(53, 87, 212, 0.05)',
            },
          },
        },
      },
      MuiAccordionDetails: {
        styleOverrides: {
          root: { paddingTop: 4 },
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
