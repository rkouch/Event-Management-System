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

import { FormmInput } from '../Styles/Input_Styles';

import '../App.css';

import PasswordInput from '../Components/PasswordInput';
import { setFieldInState } from '../Helpers';

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

  const [value, setValue] = React.useState(dayjs('2014-08-18T21:11:54'));

  const handleChange = (newValue) => {
    setValue(newValue);
  };

  var validRegex = /^(([^<>()[\]\.,;:\s@\"]+(\.[^<>()[\]\.,;:\s@\"]+)*)|(\".+\"))@(([^<>()[\]\.,;:\s@\"]+\.)+[^<>()[\]\.,;:\s@\"]{2,})$/i;

  const EmailChange = (e) => {
    setFieldInState('email', e.target.value, email, setEmail);
    setFieldInState('error', false, email, setEmail);
  };

  function EmailHelper() {
    const {emailFocused} = useFormControl() || {};

    const helperText = React.useMemo(() => {
      if (!emailFocused && (email.email !== '')) {
        if (!email.email.match(validRegex)) {
          setFieldInState('error', true, email, setEmail)
          return 'Invalid Email'
        }
      }
  
      return '';
    }, [emailFocused]);
  
    return <FormHelperText>{helperText}</FormHelperText>;
  }

  return (
    <div>
      <Box disabled className='Background'>
        <Box disabled sx={{ boxShadow: 3 }} className='Form'>
          <h1>
            Tickr.
          </h1>
          <Divider/>
          <FormmInput>
            <TextField id="user_name" label="User Name" variant="outlined" onChange={(e)=>setUserName(e.target.value)}/>
            <TextField id="first_name" label="First Name" variant="outlined" onChange={(e)=>setFirstName(e.target.value)}/>
            <TextField id="last_name" label="Last Name" variant="outlined" onChange={(e)=>setLastName(e.target.value)}/>
            <FormControl>
              <TextField 
                id="email"
                label="Email"
                variant="outlined"/>
                
            </FormControl>    
            <PasswordInput id="password" label="Password"/>
            <PasswordInput id="confirm_password" label="Confirm Password"/>
            <LocalizationProvider dateAdapter={AdapterDayjs}>
              <DesktopDatePicker
                label="Date of Birth"
                inputFormat="DD/MM/YYYY"
                value={value}
                onChange={handleChange}
                renderInput={(params) => <TextField {...params} />}
              />
            </LocalizationProvider>
          </FormmInput>
          <Divider/>
          <FormmInput>
            <Button variant="contained">Register</Button>
          </FormmInput>
        </Box>
      </Box>
    </div>
  )
}