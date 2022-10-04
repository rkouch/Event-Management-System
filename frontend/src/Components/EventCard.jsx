import React from 'react'
import Box from '@mui/material/Box';
import { Typography } from '@mui/material';
import { fontWeight } from '@mui/system';

export default function EventCard({}) {
  return (
    <Box
      sx={{
        width: '250px',
        height: '400px',
        backgroundColor: '#FFFFFF',
        borderRadius: '5px',
        '&:hover': {
          boxShadow: '4',
          cursor: 'pointer',
        },
      }}
    >
      <Box 
        sx={{
          height: '125px',
          backgroundColor: '#c9c9c9'
        }}
      >
        This Will be the event photo
      </Box>
      <Box
        sx={{
          padding: '5px'
        }}
      >
        <Typography
          sx={{
            fontSize:"20px",
            fontWeight: "bold"
          }}
        >
          Event Name
        </Typography>
        <Typography
          sx={{
            fontSize:"15px",
            fontWeight: "regular",
            color: "#AE759F",
          }}
        >
          Event Date
        </Typography>
        <Typography
          sx={{
            fontSize:"13px",
            fontWeight: "light",
          }}
        >
          Event Location
        </Typography>
      </Box>
    </Box>
  )
}