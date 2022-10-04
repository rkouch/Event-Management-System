import React from 'react'

import Box from '@mui/material/Box';
import { borderRadius, styled, alpha } from '@mui/system';
import Grid from '@mui/material/Grid';
import logo from '../Images/TickrLogo.png'
import { CentredBox, HeaderBar, Logo } from '../Styles/HelperStyles';
import OutlinedInput from '@mui/material/OutlinedInput';
import AdjustableLogo from './AdjustableLogo';
import FormControl from '@mui/material/FormControl';
import InputAdornment from '@mui/material/InputAdornment';
import SearchSharpIcon from '@mui/icons-material/SearchSharp';
import Button from '@mui/material/Button';
import ButtonGroup from '@mui/material/ButtonGroup';
import { TkrButton, TkrButton2 } from '../Styles/InputStyles';
import { Link } from "react-router-dom";
import { Container, Divider } from '@mui/material';
import AppBar from '@mui/material/AppBar';
import { isLoggedIn } from '../Helpers';
import AccountMenu from './AccountMenu';

const Search = styled('div')(({ theme }) => ({
  position: 'relative',
  borderRadius: '5px',
  backgroundColor: alpha(theme.palette.common.white, 0.3),
  '&:hover': {
    backgroundColor: alpha(theme.palette.common.white, 0.5),
  },
  width: '100%',
}));

const SearchInput = styled(OutlinedInput)(({ theme }) => ({
  '.MuiOutlinedInput-notchedOutline': {
    borderColor: "#AFDEDC"
  },
  "&:hover .MuiOutlinedInput-notchedOutline": {
    borderColor: "#AFDEDC"
  },
  "&.Mui-focused .MuiOutlinedInput-notchedOutline": {
    borderColor: '#AFDEDC',
  },
  borderRadius: '5px'
}))

export default function Header({}) {
  return (
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
                  width: '60%'
                }}
              >
                <Search>
                  <SearchInput 
                    size="small"
                    startAdornment={
                      <InputAdornment>
                        <SearchSharpIcon/>
                      </InputAdornment>
                    }
                    fullWidth={true}
                    placeholder='Search'
                  >
                  </SearchInput>
                </Search>
              </FormControl>
              
            </CentredBox>
            
          </Grid>
          {isLoggedIn()
            ? <Grid item xs={2}>
                <Box
                  display = "flex"
                  alignItems ='center'
                  justifyContent = 'flex-end'
                  sx = {{
                    marginRight: "10px"
                  }}
                >
                  <AccountMenu> </AccountMenu>
                </Box>
              </Grid>
            : <Grid item xs={2}>
                <Box
                  display = "flex"
                  alignItems ='center'
                  justifyContent = 'flex-end'
                >
                  <Button
                    sx={{
                      color: 'white',
                      "&:hover": {
                        color: '#AE759F',
                      }
                    }}
                    component={Link}
                    to="/login"
                  >
                    Log In
                  </Button>
                  <Divider orientation="vertical" variant="middle" flexItem/>
                  <Button
                    sx={{
                      color: 'white',
                      "&:hover": {
                        color: '#AE759F'
                      }
                    }}
                    component={Link}
                    to="/register"
                  >
                    Sign Up
                  </Button>
                  {/* <ButtonGroup variant="contained" color="inherit">
                    <TkrButton2 component={Link} to="/login">Log In</TkrButton2>
                    <TkrButton component={Link} to="/register">Sign Up</TkrButton>
                  </ButtonGroup> */}
                </Box>
              </Grid>
          }
        </Grid>
    </HeaderBar>
  )
}