import React from 'react'
import Box from '@mui/material/Box';
import Header from '../Components/Header'
import { Backdrop, ContentBox } from '../Styles/HelperStyles';
import { styled } from '@mui/system';
import { Link } from "react-router-dom";
import Grid from '@mui/material/Grid';


export default function Landing({}) {

  
  return (
    <div>
      <Backdrop>
        <Header/>
        <Grid container spacing={2}>
          <Grid item xs={12}>
            <ContentBox sx={{height: 100}}>
              This contains content
            </ContentBox>
          </Grid>
          <Grid item xs={12}>
            <ContentBox sx={{height: 100}}>
              This contains content
            </ContentBox>
          </Grid>
        </Grid>
        
        
        <Box>
          this is more content
        </Box>
      </Backdrop>
    </div>
  )
}