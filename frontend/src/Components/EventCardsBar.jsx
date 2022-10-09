import React from 'react'
import Box from '@mui/material/Box';
import Header from '../Components/Header'
import { Backdrop, BackdropNoBG, CentredBox, ContentBox } from '../Styles/HelperStyles';
import { styled } from '@mui/system';
import { Link } from "react-router-dom";
import Grid from '@mui/material/Grid';
import Header2 from '../Components/Header2';
import { Card, Container, Divider } from '@mui/material';
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
  gap: '15px',
  flexWrap: 'nowrap'
})

export default function EventCardsBar({filterKeys=[], filterValues=[]}) {
  const [eventIds, setEventIds] = React.useState([])

  const paramsObj = {
    page_start: 0,
    max_results: 10,
  }

  const searchParams = new URLSearchParams(paramsObj)
  const getEventIds = async () => {
    try {
      const response = await apiFetch('GET', `/api/event/search?${searchParams}`, null)
      console.log(response)
    } catch (error) {
      console.log(error)
    }
  }

  React.useEffect(() => {
    getEventIds()
  }, [])

  return (
    <CardsBar>
      Hi
    </CardsBar>
  )
}