import { styled } from '@mui/system';
import { ThemeProvider, createTheme } from '@mui/material/styles';
import OutlinedInput, { OutlinedInputProps } from '@mui/material/OutlinedInput';
import Button from '@mui/material/Button';
import { autocompleteClasses } from '@mui/material';

export const FormInput = styled('div')({
  display: 'flex',
  justifyContent: 'center',
  flexDirection: 'column',
  paddingTop: '15px',
  paddingBottom: '15px',
  paddingLeft: '15px',
  paddingRight: '15px',
  margin: 'auto',
  alignItems: 'center',
  gap: '10px',
});


export const TkrButton = styled(Button)({
  backgroundColor: "#D9BFD2",
  "&:hover": {
    backgroundColor: "#CAA5C0"
  },
  color: 'white'
})

export const TkrButton2 = styled(Button)({
  backgroundColor: "#92C5DD",
  "&:hover": {
    backgroundColor: "#73B5D3"
  },
  color: 'white'
})

export const TextButton = styled(Button)({
  variant: 'text',
  color: '#ABC7ED',
  "&:hover": {
    color: "#424C55"
  }
})