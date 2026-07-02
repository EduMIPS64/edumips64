import { createTheme, responsiveFontSizes } from '@mui/material/styles';

/**
 * Central MUI theme for the "IDE workbench" web UI.
 *
 * Design goals, modeled on modern code editors (VS Code in particular):
 *  - compact, information-dense chrome: slim toolbars, small tabs, 6px
 *    corner radius, no heavy drop shadows;
 *  - an editor-first palette: neutral greys with a single strong accent
 *    blue, a graphite (#1e1e1e-family) canvas in dark mode;
 *  - system font stack for UI chrome (monospace stays for data).
 *
 * Everything here is purely visual: no component behavior depends on it.
 */

const FONT_STACK =
  '-apple-system, BlinkMacSystemFont, "Segoe UI", "Roboto", "Helvetica Neue", Arial, sans-serif';

// Single accent color, in a light- and a dark-friendly variant.
export const ACCENT = '#0969da';
export const ACCENT_DARK_MODE = '#4da3ff';

// Status bar backgrounds per palette mode (exported for StatusBar).
export const STATUS_BAR_BG = { light: '#0969da', dark: '#0e4429' };

export function buildTheme(mode) {
  const isDark = mode === 'dark';

  const palette = isDark
    ? {
        mode: 'dark',
        primary: { main: ACCENT_DARK_MODE },
        secondary: { main: '#39d2c0' },
        background: { default: '#1e1e1e', paper: '#252526' },
        divider: 'rgba(255, 255, 255, 0.12)',
        text: { primary: '#d4d4d4', secondary: '#9d9d9d' },
      }
    : {
        mode: 'light',
        primary: { main: ACCENT },
        secondary: { main: '#0d9488' },
        background: { default: '#f6f8fa', paper: '#ffffff' },
        divider: 'rgba(31, 35, 40, 0.15)',
        text: { primary: '#1f2328', secondary: '#59636e' },
      };

  const base = createTheme({
    palette,
    shape: { borderRadius: 6 },
    typography: {
      fontFamily: FONT_STACK,
      button: { textTransform: 'none', fontWeight: 600, letterSpacing: 0 },
    },
    components: {
      MuiAppBar: {
        defaultProps: { elevation: 0 },
        styleOverrides: {
          root: {
            backgroundImage: 'none',
            backgroundColor: isDark ? '#2d2d30' : '#24292f',
            borderBottom: `1px solid ${palette.divider}`,
          },
        },
      },
      MuiButton: {
        styleOverrides: {
          root: { borderRadius: 6 },
        },
      },
      MuiTab: {
        styleOverrides: {
          root: {
            minHeight: 38,
            minWidth: 60,
            padding: '6px 10px',
            fontSize: '0.78rem',
            fontWeight: 600,
          },
        },
      },
      MuiTabs: {
        styleOverrides: {
          root: {
            minHeight: 38,
            borderBottom: `1px solid ${palette.divider}`,
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
            borderRadius: 6,
            fontSize: '0.75rem',
            backgroundColor: isDark ? '#3c3c3c' : '#24292f',
          },
          arrow: { color: isDark ? '#3c3c3c' : '#24292f' },
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
