import React from 'react'

import {CentredBox, H3  } from '../Styles/HelperStyles'

import { Box } from '@mui/system';
import {CircularProgress,Divider, OutlinedInput, Typography } from '@mui/material';

import { TextButton, TkrButton } from '../Styles/InputStyles';
import Backdrop from '@mui/material/Backdrop';

export default function ConfirmPassword ({open, handleOpen}) {
  const [loading, setLoading] = React.useState(false)
  const submitPasswordChange = (e) => {
    setLoading(true)
  }

  React.useEffect(() => {
    setLoading(false)
  }, [open])

  return (
    <Backdrop
      sx={{ color: '#fff', zIndex: (theme) => theme.zIndex.drawer + 1 }}
      open={open}
    >
      <Box sx={{width: 400, backgroundColor: "#FFFFFF", borderRadius: 2}}>
        <H3
          sx={{
            fontSize: '30px',
            color: 'black'
          }}
        >
          Confirm Password
        </H3>
        <Divider/>
        <br/>
        <CentredBox>
          <OutlinedInput placeholder='Password'></OutlinedInput>
        </CentredBox>
        <br/>
        <CentredBox sx={{position: 'relative'}}>
          <TkrButton disabled={loading} variant="contained" onClick={submitPasswordChange}>Proceed</TkrButton>
          {loading && (
            <CircularProgress 
              size={24}
              sx={{
                color: "#AE759F",
                position: 'absolute',
                top: '50%',
                left: '50%',
                marginTop: '-12px',
                marginLeft: '-12px',
              }}
            />
          )}
        </CentredBox>
        <br/>
        <CentredBox>
          <TextButton onClick={handleOpen}>
            Cancel
          </TextButton>
        </CentredBox>
        <br/>
      </Box>
    </Backdrop>
  )
}