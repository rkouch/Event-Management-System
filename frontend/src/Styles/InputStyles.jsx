import { borderRadius, styled, alpha } from '@mui/system';
import { ThemeProvider, createTheme} from '@mui/material/styles';
import OutlinedInput, { OutlinedInputProps } from '@mui/material/OutlinedInput';
import Button from '@mui/material/Button';
import { autocompleteClasses } from '@mui/material';
import Checkbox from '@mui/material/Checkbox';

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
    backgroundColor: "#AE759F"
  },
  color: 'white',
  fontSize: '20px',
  
})

export const TkrButton2 = styled(Button)({
  backgroundColor: "#92C5DD",
  "&:hover": {
    backgroundColor: "#73B5D3"
  },
  color: 'white',
  fontSize: '20px',
})

export const DeleteButton = styled(Button)({
  backgroundColor: "#FFB6B3",
  "&:hover": {
    backgroundColor: "#E07464"
  },
  color: 'white',
  fontSize: '20px',
})

export const TextButton = styled(Button)({
  variant: 'text',
  color: '#ABC7ED',
  "&:hover": {
    color: "#424C55"
  }
})

export const TextButton2 = styled(Button)({
  variant: 'text',
  color: '#CCCCCC',
  "&:hover": {
    color: "#444444"
  },
  textTransform: 'none'
})

export const ContrastInput = styled(OutlinedInput)(({ theme }) => ({
  '.MuiOutlinedInput-notchedOutline': {
    borderColor: "#FFFFFF"
  },
  "&:hover .MuiOutlinedInput-notchedOutline": {
    borderColor: "#FFFFFF"
  },
  "&.Mui-focused .MuiOutlinedInput-notchedOutline": {
    borderColor: '#FFFFFF',
  },
  "&.Mui-focused": {
    backgroundColor: alpha('#6A7B8A', 0.5),
  },
  borderRadius: '5px'
}))

export const ContrastInputNoOutline = styled(OutlinedInput)(({ theme }) => ({
  '.MuiOutlinedInput-notchedOutline': {
    borderColor: "rgba(0,0,0,0)"
  },
  "&:hover .MuiOutlinedInput-notchedOutline": {
    borderColor: "rgba(0,0,0,0)"
  },
  "&.Mui-focused .MuiOutlinedInput-notchedOutline": {
    borderColor: 'rgba(0,0,0,0)',
  },
  "&.Mui-focused": {
    backgroundColor: alpha('#6A7B8A', 0.5),
  },
  borderRadius: '5px'
}))

export const ReplyInput = styled(OutlinedInput)(({ theme }) => ({
  '.MuiOutlinedInput-notchedOutline': {
    borderColor: "rgba(0,0,0,0)"
  },
  "&:hover .MuiOutlinedInput-notchedOutline": {
    borderColor: "rgba(0,0,0,0)"
  },
  "&.Mui-focused .MuiOutlinedInput-notchedOutline": {
    borderColor: 'rgba(0,0,0,0)',
  },
  borderRadius: 5
}))

export const ContrastInputWrapper = styled('div')(({ theme }) => ({
  position: 'relative',
  borderRadius: '5px',
  backgroundColor: alpha('#6A7B8A', 0.3),
  '&:hover': {
    backgroundColor: alpha('#6A7B8A', 0.5),
  },
  "&.Mui-focused": {
    backgroundColor: alpha('#6A7B8A', 0.5),
  },
  width: '100%',
}));

export const TicketOption = styled(Button)({
  backgroundColor: alpha('#6A7B8A', 0.3),
  '&:hover': {
    backgroundColor: '#D9BFD2'
  },
  color: '#444444'
})


