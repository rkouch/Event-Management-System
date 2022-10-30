import React from 'react'
import Box from '@mui/material/Box';
import Header from '../Components/Header'
import { Backdrop, BackdropNoBG, CentredBox, ContentBox } from '../Styles/HelperStyles';
import { styled } from '@mui/system';
import { Link } from "react-router-dom";
import Grid from '@mui/material/Grid';
import Header2 from '../Components/Header2';
import { Container, Divider } from '@mui/material';
import Tab from '@mui/material/Tab';
import Tabs from '@mui/material/Tabs';
import TabContext from '@mui/lab/TabContext';
import TabList from '@mui/lab/TabList';
import TabPanel from '@mui/lab/TabPanel';
import EventCard from '../Components/EventCard';
import Button from '@mui/material/Button';
import EventCardsBar from '../Components/EventCardsBar';
import { apiFetch } from '../Helpers';
import dayjs from 'dayjs';

const Section = styled(Box)({
  marginLeft: '1.5%',
  marginRight: '1.5%',
  height: '500px',
  fontSize: '50px',
  fontWeight: 'bold',
})

const SectionHeading = styled(Box)({
  display: 'flex',
  justifyContent: 'flex-start',
  gap: '10px',
  marginBottom: '10px'
})

export default function UpcomingEvents({}) {
  const [upcomingValue, setUpcomingValue] = React.useState('1');
  const upcomingChange = (event, newValue) => {
    setUpcomingValue(newValue);
    switch (newValue) {
      case 1:
        getUpcomingEvents(endOfWeek, 0, 6, weekEvents, setWeekEvents, weekEventsNum, setWeekEventsNum, setMoreWeekEvents)
      case 2:
        getUpcomingEvents(endOfMonth, 0, 6, monthEvents, setMonthEvents, monthEventsNum, setMonthEventsNum, setMoreMonthEvents)
      case 3:
        getUpcomingEvents(endOfYear, 0, 6, yearEvents, setYearEvents, yearEventsNum, setYearEventsNum, setMoreYearEvents)
      default:

    }
  }; 

  const endOfWeek = dayjs().endOf('week').toISOString()
  const endOfMonth = dayjs().endOf('month').toISOString()
  const endOfYear = dayjs().endOf('year').toISOString()

  const [weekEvents, setWeekEvents] = React.useState([])
  const [weekEventsNum, setWeekEventsNum] = React.useState(0)
  const [moreWeekEvents, setMoreWeekEvents] = React.useState(false)

  const [monthEvents, setMonthEvents] = React.useState([])
  const [monthEventsNum, setMonthEventsNum] = React.useState(0)
  const [moreMonthEvents, setMoreMonthEvents] = React.useState(false)

  const [yearEvents, setYearEvents] = React.useState([])
  const [yearEventsNum, setYearEventsNum] = React.useState(0)
  const [moreYearEvents, setMoreYearEvents] = React.useState(false)
  // Function to get upcoming events
  const getUpcomingEvents = async (before, pageStart, maxResults,  eventIds, setEvents, eventNum, setEventNum, setMoreEvents) => {
    try {
      const body = {
        before: before,
        max_results: maxResults,
        page_start: pageStart
      }
      const searchParams = new URLSearchParams(body)
      const response = await apiFetch('GET', `/api/home?${searchParams}`, null)
      // console.log(response)
      if (pageStart === 0) {
        setEvents(response.eventIds)
        setEventNum(response.eventIds.length)
      } else {
        const eventIds_t = eventIds.concat(response.eventIds)
        setEvents(eventIds_t)
        setEventNum(eventNum + response.eventIds.length)
      }
      setMoreEvents(response.num_results === (eventNum + response.eventIds.length))
    } catch (e) {
      console.log(e)
    }
  }

  // Initial load of events
  React.useEffect(() => {
    // Fetch week events
    getUpcomingEvents(endOfWeek, 0, 6, weekEvents, setWeekEvents, weekEventsNum, setWeekEventsNum, setMoreWeekEvents)

    // Fetch month events
    getUpcomingEvents(endOfMonth, 0, 6, monthEvents, setMonthEvents, monthEventsNum, setMonthEventsNum, setMoreMonthEvents)

    // Fetch year events
    getUpcomingEvents(endOfYear, 0, 6, yearEvents, setYearEvents, yearEventsNum, setYearEventsNum, setMoreYearEvents)
  }, [])

  return (
    <>
      {!(yearEvents === 0)
        ? <Section sx={{pt: 13}}>
            <TabContext value={upcomingValue}>
              <SectionHeading>
                Upcoming Events
                <Divider orientation="vertical" variant="middle" flexItem/>
                <Box sx={{display: 'flex', alignItems: 'flex-end'}}>
                  <Tabs
                    onChange={upcomingChange}
                    textColor="secondary"
                    indicatorColor="secondary"
                    scrollButtons
                    value={upcomingValue}
                    >
                    <Tab label="This Week" value="1" />
                    <Tab label="This Month" value="2" />
                    <Tab label="This Year" value="3" />
                  </Tabs>
                </Box>
                <Box
                  sx={{
                    width: 'auto',
                    display: 'flex',
                    alignItems: 'flex-end',
                    justifyContent: 'flex-end',
                    paddingBottom: '6px',
                    flexGrow: '4'
                  }}
                >
                  <Button color='secondary'>
                    see all
                  </Button>
                </Box>
              </SectionHeading>
              <TabPanel value="1" sx={{padding: 0}}>
                <EventCardsBar event_ids={weekEvents}/>
              </TabPanel>
              <TabPanel value="2" sx={{padding: 0}}>
                <EventCardsBar event_ids={monthEvents}/>
              </TabPanel>
              <TabPanel value="3" sx={{padding: 0}}>
                <EventCardsBar event_ids={yearEvents}/>
              </TabPanel>
            </TabContext>
          </Section>
        : <></>
      }
    </>
  )
}