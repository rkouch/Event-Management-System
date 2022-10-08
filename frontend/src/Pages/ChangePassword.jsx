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
import FormControlLabel from '@mui/material/FormControlLabel';
import FormLabel from '@mui/material/FormLabel';

import { FormInput, TextButton, TkrButton} from '../Styles/InputStyles';

import '../App.css';

import { apiFetch, getToken, setFieldInState, setToken } from '../Helpers';
import { FlexRow, Logo, H3 } from '../Styles/HelperStyles';
import HelperText from '../Components/HelperText';
import { Link, useNavigate } from "react-router-dom";
import StandardLogo from "../Components/StandardLogo";
import PasswordInput from "../Components/PasswordInput";


export default function ChangePassword({}) {
  const [currentPW, setCurrentPW] = React.useState({
    password: '',
    error: false,
    errorMsg: '',
    visibility: false,
  })

  const [newPW, setnewPW] = React.useState({
    password: '',
    error: false,
    errorMsg: '',
    visibility: false,
  })
  const [confirmNewPW, setconfirmNewPW] = React.useState({
    password: '',
    error: false,
    errorMsg: '',
    visibility: false,
  })

  return (
    <div>
      <StandardLogo/>
      <Box disabled className='Background'> 
        <Box disabled sx={{ boxShadow: 3, width:500, borderRadius:2, backgroundColor: '#F5F5F5', padding: 1}} >
          <H3
            sx={{
              fontSize: '30px',
            }}
          >
            Change Password
          </H3>
          <Divider/>
          <br/>
          <FormInput>
            <Grid container spacing={2} sx={{paddingLeft: 5, paddingRight: 5}}>
              {(getToken() != null)
                ? <Grid xs={12}>
                    <FormControl fullWidth={true}>
                      <FormLabel sx={{"&.Mui-focused": {color: 'rgba(0, 0, 0, 0.6) '}}}>Confirm current password</FormLabel>
                      <PasswordInput state={currentPW} setState={setCurrentPW} placeholder="Current Password"/>
                    </FormControl>
                  </Grid>
                : <div></div>
              }
              
              <Grid xs={12}>
                <FormControl fullWidth={true}>
                  <FormLabel sx={{"&.Mui-focused": {color: 'rgba(0, 0, 0, 0.6) '}}}>New Password</FormLabel>
                  <PasswordInput state={currentPW} setState={setCurrentPW} placeholder="New Password"/>
                </FormControl>
              </Grid>
              <Grid xs={12}>
                <FormControl fullWidth={true}>
                  <FormLabel sx={{"&.Mui-focused": {color: 'rgba(0, 0, 0, 0.6) '}}}>Confirm new password</FormLabel>
                  <PasswordInput state={currentPW} setState={setCurrentPW} placeholder="Confirm Password"/>
                </FormControl>
              </Grid>
            </Grid>
          </FormInput>
          <br/>
          <Divider/>
          <br/>
          <FormInput>
            <TkrButton
              variant="contained"
            >
              Sign in
            </TkrButton>
            <Collapse in={true}>
              <Alert severity="error">{}</Alert>
            </Collapse>
            <TextButton component={Link} to="/register">Don&#39;t have an account?</TextButton>
          </FormInput>
        </Box>
      </Box>
    </div>
  )
}