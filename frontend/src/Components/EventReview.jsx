import { Divider, Grid, Rating, Typography } from '@mui/material'
import { Box } from '@mui/system'
import React from 'react'
import { CentredBox } from '../Styles/HelperStyles'
import { ContrastInput, ContrastInputWrapper, TkrButton } from '../Styles/InputStyles'
import { TestReview1, TestReview2 } from '../Test/TestData'
import ReviewCard from './ReviewCard'
import ShadowInput from './ShadowInput'

export default function EventReview({isAttendee}) {
  return (
    <Box sx={{width: '100%'}}>
      <br/>
      <br/>
      <br/>
      <br/>
      <br/>
      <Divider sx={{ml: 5, mr:5}}>
        <Typography sx={{fontSize: 20, fontWeight: 'bold'}}>
          Event Reviews
        </Typography>
      </Divider>
      <br/>
      <Box sx={{display: 'flex', justifyContent: 'flex-start', ml: 10, mr:10, flexDirection: 'column', gap: 5}}>
        <ReviewCard review={TestReview1}/>
        <ReviewCard review={TestReview2}/>
      </Box>
      {isAttendee
        ? <>
            <br/>
            <Divider sx={{ml: 20, mr: 20}}>Leave a Review</Divider>
            <br/>
            <Box sx={{display: 'flex', justifyContent: 'center'}}>
              <Grid container sx={{maxWidth: 700}}>
                <Grid item xs={10}>
                  <ContrastInputWrapper sx={{width: '100%'}}>
                    <ContrastInput
                      fullWidth
                      placeholder="Review Title"
                    />
                  </ContrastInputWrapper>
                  <ContrastInputWrapper sx={{width: '100%'}}>
                    <ContrastInput
                      multiline
                      rows={4}
                      fullWidth
                      placeholder="Review..."
                    />
                  </ContrastInputWrapper>
                  <ContrastInputWrapper 
                    sx={{
                      display: 'flex', 
                      justifyContent: 'space-between', 
                      alignItems: 'center', 
                      gap: 1
                    }}
                  >
                    <Typography sx={{color: "#999999", pt:'14px', pl: '14px', pb: '14px'}}>
                      Rating
                    </Typography>
                    <Rating sx={{pr: '14px'}} precision={0.5}/>
                  </ContrastInputWrapper>
                </Grid>
                <Grid item xs>
                  <TkrButton sx={{width: '100%', height: '100%'}}>
                    Post
                  </TkrButton>
                </Grid>
              </Grid>
            </Box>
          </>
        : <></>

      }

    </Box> 
  )
}