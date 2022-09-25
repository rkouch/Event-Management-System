import React from 'react';
import dayjs from 'dayjs';

import Container from '@mui/material/Container';
import Box from '@mui/material/Box';
import Divider from '@mui/material/Divider';
import TextField from '@mui/material/TextField';
import Button from '@mui/material/Button';
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider';
import { AdapterDayjs } from '@mui/x-date-pickers/AdapterDayjs';
import { MobileDatePicker } from '@mui/x-date-pickers/MobileDatePicker';
import { DesktopDatePicker } from '@mui/x-date-pickers/DesktopDatePicker';
import FormControl, { useFormControl } from '@mui/material/FormControl';
import FormHelperText from '@mui/material/FormHelperText';
import Grid from '@mui/material/Unstable_Grid2';

import { FormInput } from '../Styles/InputStyles';

import '../App.css';

import PasswordInput from '../Components/PasswordInput';
import { setFieldInState } from '../Helpers';
import { FlexRow } from '../Styles/HelperStyles';

export default function Register ({}) {
  // States
  const [userName, setUserName] = React.useState('')
  const [firstName, setFirstName] = React.useState('')
  const [lastName, setLastName] = React.useState('')
  const [email, setEmail] = React.useState({
    email: '',
    error: false,
    errorMsg: ''
  })

  const [password, setPassword] = React.useState({
    password: '',
    error: false,
    errorMsg: ''
  })

  const [confirmPassword, setConfirmPassword] = React.useState({
    password: '',
    error: false,
    errorMsg: '',
    disabled: true
  })

  const [formProgress, setFormProgress] = React.useState(0)

  const [DOB, setDOB] = React.useState(dayjs())

  const userNameChange = (e) => {
    setUserName(e.target.value)
    if (userName === '' && formProgress !== 0) {
      setFormProgress(formProgress-1)
    } else {
      setFormProgress(formProgress+1)
    }
  }

  const firstNameChange = (e) => {
    setFirstName(e.target.value)
    if (firstName === '' && formProgress !== 0) {
      setFormProgress(formProgress-1)
    } else {
      setFormProgress(formProgress+1)
    }
  }

  const lastNameChange = (e) => {
    setLastName(e.target.value)
    if (lastName === '' && formProgress !== 0) {
      setFormProgress(formProgress-1)
    } else {
      setFormProgress(formProgress+1)
    }
  }

  var validEmail = /^(([^<>()[\]\.,;:\s@\"]+(\.[^<>()[\]\.,;:\s@\"]+)*)|(\".+\"))@(([^<>()[\]\.,;:\s@\"]+\.)+[^<>()[\]\.,;:\s@\"]{2,})$/i;
  const EmailChange = (e) => {
    setFieldInState('email', e.target.value, email, setEmail);
    if (!email.email.match(validEmail)) {
      setFieldInState('error', true, email, setEmail);
      setFieldInState('errorMsg', 'Enter a valid email', email, setEmail);
      console.log('Invalid')
    } else {
      setFieldInState('error', false, email, setEmail);
      setFieldInState('errorMsg', '', email, setEmail);
      console.log('Valid email')
    }
    if (email.email === '' && formProgress !== 0) {
      setFormProgress(formProgress-1)
    } else {
      setFormProgress(formProgress+1)
    }
  };

  var validPassword = /^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[^A-Za-z0-9])(?=.{8,})$/i;

  const PasswordChange = (e) => {
    setFieldInState('password', e.target.value, password, setPassword);
    // Check for digit

    var hasUpper = password.password.match(/[A-Z]/);
    var hasDigit = password.password.match(/[0-9]/);
    var hasSpecial = password.password.match(/[!@#$%^&*]/);

    var validPassword = true;
    var errorMsg = 'Password must contain';

    if (!hasUpper) {
      errorMsg = errorMsg + ' an uppercase character';
      validPassword = false;
    } 
    if (!hasDigit) {
      if (errorMsg !== 'Password must contain') {
        errorMsg = errorMsg + ', a digit';
      } else {
        errorMsg = errorMsg + ' a digit';
      }
      validPassword = false;
    } 
    if (!hasSpecial) {
      if (errorMsg !== 'Password must contain') {
        errorMsg = errorMsg + ', a special character';
      } else {
        errorMsg = errorMsg + ' a special character';
      }
      validPassword = false;
    } 

    if (!validPassword) {
      setFieldInState('error', validPassword, password, setPassword)
      setFieldInState('errorMsg', errorMsg, password, setPassword)
      setFieldInState('disabled', true, confirmPassword, setConfirmPassword)
    } else {
      setFieldInState('disabled', false, confirmPassword, setConfirmPassword)
      setFieldInState('error', false, confirmPassword, setConfirmPassword)
      setFieldInState('errorMsg', '', password, setPassword)
      setFieldInState('errorMsg', '', confirmPassword, setConfirmPassword)
    }

    if (password.password === '' && formProgress !== 0) {
      setFormProgress(formProgress-1)
    } else {
      setFormProgress(formProgress+1)
    }
  }

  const ConfirmPasswordChange = (e) => {
    console.log(e.target.value)
    setFieldInState('password', e.target.value, confirmPassword, setConfirmPassword);
    if (confirmPassword.password !== password.password) {
      console.log('passwords dont match')
      setFieldInState('error', true, password, setPassword)
      setFieldInState('error', true, confirmPassword, setConfirmPassword)
      setFieldInState('errorMsg', 'Passwords do not match', password, setPassword)
      setFieldInState('errorMsg', 'Passwords do not match', confirmPassword, setConfirmPassword)
    } else {
      setFieldInState('error', false, password, setPassword)
      setFieldInState('error', false, confirmPassword, setConfirmPassword)
      setFieldInState('errorMsg', '', password, setPassword)
      setFieldInState('errorMsg', '', confirmPassword, setConfirmPassword)
    }
    if (confirmPassword.password === '' && formProgress !== 0) {
      setFormProgress(formProgress-1)
    } else {
      setFormProgress(formProgress+1)
    }
  }

  const DOBChange = (e) => {
    setDOB(e.target.value)
    if (DOB === dayjs() && formProgress !== 0) {
      setFormProgress(formProgress-1)
    } else {
      setFormProgress(formProgress+1)
    }
  }

  const SubmitRegister = (e) => {
    console.log("Send Register")
  }

  return (
    <div>
      <Box disabled className='Background'>
        <Box disabled sx={{ boxShadow: 3 }} className='Form'>
          <h1>
            Tickr.
          </h1>
          <Divider/>
          <FormInput>
            <Grid container spacing={2}>
              <Grid xs={6}>
                <TextField id="first_name" label="First Name" variant="outlined" onChange={firstNameChange} size="small"/>
              </Grid>
              <Grid xs={6}>
                <TextField id="last_name" label="Last Name" variant="outlined" onChange={lastNameChange} size="small"/>
              </Grid>
              <Grid xs={12}>
                <TextField 
                  id="email"
                  label="Email"
                  variant="outlined"
                  fullWidth={true}
                  size="small"
                  onChange={EmailChange}
                  error={email.error}
                  helperText={email.errorMsg}
                />
              </Grid>
              <Grid xs={6}>
                <TextField 
                  id='password'
                  label='Password'
                  variant="outlined"
                  type="password"
                  size="small"
                  error={password.error}
                  helperText={password.errorMsg}
                  onChange={PasswordChange}
                />
              </Grid>
              <Grid xs={6}>
                <TextField
                  id='confirmPassword'
                  label='Confirm Password'
                  variant="outlined"
                  type="password"
                  size="small"
                  error={confirmPassword.error}
                  helperText={confirmPassword.errorMsg}
                  onChange={ConfirmPasswordChange}
                  disabled={confirmPassword.disabled}
                />
              </Grid>
              <Grid xs={3}>
              </Grid>
              <Grid xs={6}>
                <TextField id="user_name" label="Display Name" variant="outlined" onChange={userNameChange} fullWidth={true} />
              </Grid>
              <Grid xs={3}>
              </Grid>
              <Grid xs={3}>
              </Grid>
              <Grid xs={6}>
                  <LocalizationProvider dateAdapter={AdapterDayjs}>
                  <DesktopDatePicker
                    label="Date of Birth"
                    inputFormat="DD/MM/YYYY"
                    value={DOB}
                    onChange={setDOB}
                    renderInput={(params) => <TextField {...params} />}
                  />
                </LocalizationProvider>
              </Grid>
              <Grid xs={3}>
              </Grid>
            </Grid>
          </FormInput>
          <Divider/>
          <FormInput>
            {(formProgress === 7)
              ? <div>
                  <Button
                    variant="contained"
                    onClick={SubmitRegister}
                  >
                    Register
                  </Button>
              </div>
              : <div>
                <Button
                  variant="contained"
                  disabled={true}
                >
                  Fill in all fields
                </Button>
              </div> 
            }
            
          </FormInput>
        </Box>
      </Box>
    </div>
  )
}