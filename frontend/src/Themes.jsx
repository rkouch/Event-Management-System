import { ThemeProvider, createTheme,  alpha} from '@mui/material/styles';

export const TickrTheme = createTheme({
  components: {
    // Name of the component
    MuiOutlinedInput: {
      styleOverrides: {
        root: {
          "&:hover .MuiOutlinedInput-notchedOutline": {
            borderColor: "#AE759F"
          },
          "&.Mui-focused .MuiOutlinedInput-notchedOutline": {
            borderColor: "#AE759F"
          }
        },
      }
    },
    MuiAvatar: {
      styleOverrides: {
        root: {
          backgroundColor: '#AE759F'
        }
      }
    },
    MuiFormLabel: {
      styleOverrides: {
        root: {
          "&:focused": {
            color: 'grey'
          }
        }
      }
    },
    MuiCheckbox: {
      styleOverrides: {
        root: {
          color: "#AE759F",
          "&.Mui-checked" : {
            color: "#AE759F",
          }
        }
      }
    }
  },
});