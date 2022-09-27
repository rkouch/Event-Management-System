import { ThemeProvider, createTheme } from '@mui/material/styles';

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
  },
});