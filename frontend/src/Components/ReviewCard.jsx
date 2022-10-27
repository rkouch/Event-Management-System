import { Grid, Typography } from '@mui/material'
import { Box } from '@mui/system'
import React from 'react'
import UserAvatar from './UserAvatar'
import Rating from '@mui/material/Rating';

export default function ReviewCard({review}) {

  return (
    <Box sx={{borderRadius: '50px', minHeight: 70, height: '100%', backgroundColor: '#EEEEEE', p: 2, maxWidth: 700, width: '100%'}}>
      <Grid container>
        <Grid item xs={1} >
          <UserAvatar userId={review.author} size={50}/>
        </Grid>
        <Grid xs item sx={{pl: 3}}>
          <Box sx={{display: 'flex', justifyContent: 'space-between', alignItems: 'center'}}>
            <Typography sx={{fontWeight: 'bold', fontSize: 20}}>
              {review.title}
            </Typography> 
            <Rating precision={0.5} defaultValue={review.rating} readOnly/>
          </Box>
          <Typography sx={{pt: '5px'}}>
            {review.text}
          </Typography>
        </Grid>
      </Grid>
      
    </Box>
  )
}