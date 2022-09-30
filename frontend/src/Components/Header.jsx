import React from 'react'

import Box from '@mui/material/Box';
import { borderRadius, styled } from '@mui/system';
import Grid from '@mui/material/Grid';
import logo from '../Images/TickrLogo.png'
import { CentredBox, Logo } from '../Styles/HelperStyles';
import OutlinedInput from '@mui/material/OutlinedInput';
import AdjustableLogo from './AdjustableLogo';
import FormControl from '@mui/material/FormControl';
import InputAdornment from '@mui/material/InputAdornment';
import SearchSharpIcon from '@mui/icons-material/SearchSharp';
import Button from '@mui/material/Button';
import ButtonGroup from '@mui/material/ButtonGroup';
import { TkrButton, TkrButton2 } from '../Styles/InputStyles';
import { Link } from "react-router-dom";
import AppBar from '@mui/material/AppBar';

export const HeaderBar = styled(Box) ({
  width: '90%',
  height: '40px',
  marginTop: '15px',
  marginLeft: '5%',
  marginRight: '5%',
  backgroundColor: 'white',
  padding: '10px',
  borderRadius: '2px',
})

export default function Header({}) {
  return (
    <AppBar position="static">
      <HeaderBar>
        <Grid container >
          <Grid item xs={2}>
            <Box
              display = "flex"
              alignItems ='center'
              justifyContent = 'flex-start'
            >
              <AdjustableLogo width='100px' height='auto'/>
            </Box>
          </Grid>
          <Grid item xs={8}>
            <CentredBox>
              <FormControl
                sx={{
                  width: '80%'
                }}
              >
                <OutlinedInput 
                  size="small"
                  startAdornment={
                    <InputAdornment>
                      <SearchSharpIcon/>
                    </InputAdornment>
                  }
                  placeholder='Search'
                >
                </OutlinedInput>
              </FormControl>
              
            </CentredBox>
            
          </Grid>
          <Grid item xs={2}>
            <Box
              display = "flex"
              alignItems ='center'
              justifyContent = 'flex-end'
            >
              <ButtonGroup variant="contained" color="inherit">
                <TkrButton2 component={Link} to="/login">Log In</TkrButton2>
                <TkrButton component={Link} to="/register">Sign Up</TkrButton>
              </ButtonGroup>
            </Box>
          </Grid>
        </Grid>
      </HeaderBar>
    </AppBar>
  )
}