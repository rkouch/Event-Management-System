import React from "react";

import Box from '@mui/material/Box';
import Divider from '@mui/material/Divider';
import TextField from '@mui/material/TextField';
import OutlinedInput from '@mui/material/OutlinedInput';
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider';
import { AdapterDayjs } from '@mui/x-date-pickers/AdapterDayjs';
import { MobileDatePicker } from '@mui/x-date-pickers/MobileDatePicker';
import { DesktopDatePicker } from '@mui/x-date-pickers/DesktopDatePicker';
import FormControl, { useFormControl } from '@mui/material/FormControl';
import FormHelperText from '@mui/material/FormHelperText';
import Grid from '@mui/material/Unstable_Grid2';
import InputAdornment from '@mui/material/InputAdornment';
import IconButton from '@mui/material/IconButton';
import Visibility from '@mui/icons-material/Visibility';
import VisibilityOff from '@mui/icons-material/VisibilityOff';
import Alert from '@mui/material/Alert';
import Collapse from '@mui/material/Collapse';
import logo from '../Images/TickrLogo.png'

import { FormInput, TextButton, TkrButton} from '../Styles/InputStyles';

import '../App.css';

import { setFieldInState } from '../Helpers';
import { FlexRow, Logo, H3 } from '../Styles/HelperStyles';
import HelperText from '../Components/HelperText';
import { Link } from "react-router-dom";
import StandardLogo from "../Components/StandardLogo";


export default function Login({}) {
  const [email, setEmail] = React.useState({
    email: '',
    error: false,
    errorMsg: ''
  })

  const [password, setPassword] = React.useState({
    password: '',
    visibility: false,
    error: false
  })

  const [error, setError] = React.useState(false)

  var validEmail = /^(([^<>()[\]\.,;:\s@\"]+(\.[^<>()[\]\.,;:\s@\"]+)*)|(\".+\"))@(([^<>()[\]\.,;:\s@\"]+\.)+[^<>()[\]\.,;:\s@\"]{2,})$/i;
  const EmailChange = (e) => {
    setFieldInState('email', e.target.value, email, setEmail);
    setError(false)
    if (!email.email.match(validEmail)) {
      setFieldInState('error', true, email, setEmail);
      setFieldInState('errorMsg', 'Enter a valid email', email, setEmail);
      console.log('Invalid')
    } else {
      setFieldInState('error', false, email, setEmail);
      setFieldInState('errorMsg', '', email, setEmail);
      console.log('Valid email')
    }
  };

  const PasswordChange = (e) => {
    setError(false)
    setFieldInState('password', e.target.value, password, setPassword)
    setFieldInState('error', false, password, setPassword)
  }
  
  const submitLogin = () => {
    console.log(email)
    console.log(password.password)

    // Send request to login

    const response = false

    if (!response) {
      setError(!response)
      setFieldInState('error', true, email, setEmail)
      setFieldInState('error', true, password, setPassword)
    }
  }

  return (
    <div>
      <StandardLogo/>
      <div>
      <Box disabled className='Background'> 
        <Box disabled sx={{ boxShadow: 3, width:350, borderRadius:2, backgroundColor: '#F5F5F5', padding: 1}} >
          <H3>
            Login
          </H3>
          <Divider/>
          <FormInput>
            <Grid container spacing={2} sx={{paddingLeft: 5, paddingRight: 5}}>
              <Grid xs={12}>
                <FormControl fullWidth={true}>
                  <OutlinedInput
                    id="email"
                    placeholder="Email"
                    variant="outlined"
                    size="small"
                    sx={{borderRadius: 2}} 
                    fullWidth={true} 
                    error={email.error}
                    onChange={EmailChange}
                  />
                  <FormHelperText>{email.errorMsg}</FormHelperText>
                </FormControl>
              </Grid>
              <Grid xs={12}>
                <FormControl fullWidth={true}>
                  <OutlinedInput
                    id="password"
                    placeholder="Password"
                    type={!password.visibility ? "password" : "text"}
                    size="small"
                    error={password.error}
                    onChange={PasswordChange}
                    sx={{borderRadius: 2}}
                    endAdornment={
                      <InputAdornment position="end">
                        <IconButton
                          aria-label="toggle password visibility"
                          onClick={(e) => setFieldInState('visibility', !password.visibility, password, setPassword)}
                          onMouseDown={(e) => e.preventDefault()}
                          edge="end"
                        >
                          {password.visibility ? <VisibilityOff /> : <Visibility />}
                        </IconButton>
                      </InputAdornment>
                    }
                  />
                </FormControl>
              </Grid>
            </Grid>
          </FormInput>
          <Divider/>
          <FormInput>
            <TkrButton
              variant="contained"
              onClick={submitLogin}
            >
              Sign in
            </TkrButton>
            <Collapse in={error}>
              <Alert severity="error">Email and password does not match</Alert>
            </Collapse>
            <TextButton component={Link} to="/register">Don&#39;t have an account?</TextButton>
          </FormInput>
        </Box>
      </Box>
      </div>
    </div>
  )
}