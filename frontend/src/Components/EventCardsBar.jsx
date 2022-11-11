import React from 'react'
import Box from '@mui/material/Box';
import Header from '../Components/Header'
import { Backdrop, BackdropNoBG, CentredBox, ContentBox } from '../Styles/HelperStyles';
import { styled } from '@mui/system';
import { Link } from "react-router-dom";
import Grid from '@mui/material/Grid';
import { Card, Container, Divider, Typography } from '@mui/material';
import Tab from '@mui/material/Tab';
import Tabs from '@mui/material/Tabs';
import TabContext from '@mui/lab/TabContext';
import TabList from '@mui/lab/TabList';
import TabPanel from '@mui/lab/TabPanel';
import EventCard from '../Components/EventCard';
import Button from '@mui/material/Button';
import { apiFetch } from '../Helpers';

const CardsBar = styled(Box)({
  backgroundColor: '#F6F6F6',
  height: '400px',
  borderRadius: '10px',
  padding: '15px',
  fontSize: '15px',
  fontWeight: 'light',
  display: 'flex',
  justifyContent: 'flex-start',
  gap: 20,
  flexWrap: 'nowrap'
})

export default function EventCardsBar({event_ids = [], filterKeys=[], filterValues=[]}) {
  const [eventIds, setEventIds] = React.useState(event_ids)

  React.useEffect(() => {
    setEventIds(event_ids)
    console.log('new events list')
  }, [event_ids])
  // const paramsObj = {
  //   page_start: 0,
  //   max_results: 10,
  // }

  // const searchParams = new URLSearchParams(paramsObj)
  // const getEventIds = async () => {
  //   try {
  //     const response = await apiFetch('GET', `/api/event/search?${searchParams}`, null)
  //     setEventIds(response.event_ids)
  //   } catch (error) {
  //     console.log(error)
  //   }
  // }

  // React.useEffect(() => {
  //   getEventIds()
  // }, [])

  return (
    <Box sx={{display: 'flex', width: '100%', backgroundColor: '#F6F6F6', borderRadius: 3}}>
      {(event_ids.length > 0)
        ? <CardsBar>
            {eventIds.map((value, key) => {
              return (
                <EventCard key={key} event_id={value}/>
              )
            })}
          </CardsBar>
        : <CardsBar sx={{alignItems: 'center'}}>
            <Typography sx={{fontSize: 45, color: '#CCCCCC'}}>
              No Events
            </Typography>
          </CardsBar>
      }
      
    </Box>
  )
}