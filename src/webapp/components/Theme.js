import { createMuiTheme } from '@material-ui/core/styles';
import { lightBlue, teal } from '@material-ui/core/colors';

export default createMuiTheme({
  palette: {
    primary: lightBlue,
    secondary: teal,
  },
  typography: {
    fontFamily: ['Roboto', 'Arial'],
  },
});
