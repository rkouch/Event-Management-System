import React from 'react'
import Box from '@mui/material/Box';
import Header from './Header'
import { Backdrop, BackdropNoBG, CentredBox, ContentBox } from '../Styles/HelperStyles';
import { styled } from '@mui/system';
import { Link } from "react-router-dom";
import Grid from '@mui/material/Grid';
import { Container, Divider } from '@mui/material';
import Tab from '@mui/material/Tab';
import Tabs from '@mui/material/Tabs';
import TabContext from '@mui/lab/TabContext';
import TabList from '@mui/lab/TabList';
import TabPanel from '@mui/lab/TabPanel';
import EventCard from './EventCard';
import Button from '@mui/material/Button';
import EventCardsBar from './EventCardsBar';
import { apiFetch, getToken } from '../Helpers';
import dayjs from 'dayjs';
import SwipeableViews from 'react-swipeable-views';
import ArrowBackIcon from "@mui/icons-material/ArrowBack"
import KeyboardArrowLeft from '@mui/icons-material/KeyboardArrowLeft';
import KeyboardArrowRight from '@mui/icons-material/KeyboardArrowRight';
import MobileStepper from '@mui/material/MobileStepper';
import { useTheme } from '@mui/material/styles';
import { useParams } from 'react-router-dom';

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

export default function HostEvents({userDetails}) {
  const theme = useTheme();
  const params = useParams()
  const [upcomingValue, setUpcomingValue] = React.useState('1');
  
  const upcomingChange = (event, newValue) => {
    setUpcomingValue(newValue);
    switch (newValue) {
      case 1:
        getUpcomingEvents(endOfWeek, 0, 6, weekEvents, setWeekEvents, weekEventsNum, setWeekEventsNum, setMoreWeekEvents)
        break
      case 2:
        getUpcomingEvents(endOfMonth, 0, 6, monthEvents, setMonthEvents, monthEventsNum, setMonthEventsNum, setMoreMonthEvents)
        break
      case 3:
        getUpcomingEvents(endOfYear, 0, 6, yearEvents, setYearEvents, yearEventsNum, setYearEventsNum, setMoreYearEvents)
        break
      default:

    }
  }; 

  const endOfWeek = dayjs().endOf('week').toISOString()
  const endOfMonth = dayjs().endOf('month').toISOString()
  const endOfYear = dayjs().endOf('year').toISOString()

  const [weekEvents, setWeekEvents] = React.useState([])
  const [weekEventsNum, setWeekEventsNum] = React.useState(0)
  const [moreWeekEvents, setMoreWeekEvents] = React.useState(false)
  const [activeStepWeek, setActiveStepWeek] = React.useState(0); 
  const [weekGroups, setWeekGroups] = React.useState([])
  const maxStepsWeek = weekGroups.length;
  
  const [monthEvents, setMonthEvents] = React.useState([])
  const [monthEventsNum, setMonthEventsNum] = React.useState(0)
  const [moreMonthEvents, setMoreMonthEvents] = React.useState(false)
  const [activeStepMonth, setActiveStepMonth] = React.useState(0);
  const [monthGroups, setMonthGroups] = React.useState([])
  const maxStepsMonth = monthGroups.length;

  const [yearEvents, setYearEvents] = React.useState([])
  const [yearEventsNum, setYearEventsNum] = React.useState(0)
  const [moreYearEvents, setMoreYearEvents] = React.useState(false)
  const [activeStepYear, setActiveStepYear] = React.useState(0);
  const [yearGroups, setyearGroups] = React.useState([])
  const maxStepsYear = yearGroups.length;

  const [futureEvents, setFutureEvents] = React.useState([])
  const [futureEventsNum, setFutureEventsNum] = React.useState(0)
  const [moreFutureEvents, setMoreFutureEvents] = React.useState(false)
  const [activeStepFuture, setActiveStepFuture] = React.useState(0);
  const [futureGroups, setFutureGroups] = React.useState([])
  const maxStepsFuture = futureGroups.length;

  const [pastEvents, setPastEvents] = React.useState([])
  const [pastEventsNum, setPastEventsNum] = React.useState(0)
  const [morePastEvents, setMorePastEvents] = React.useState(false)
  const [activeStepPast, setActiveStepPast] = React.useState(0);
  const [pastGroups, setPastGroups] = React.useState([])
  const maxStepsPast = pastGroups.length;

  const handleNext = (timePeriod) => {
    switch (timePeriod) {
      case 'future':
        setActiveStepFuture((prevActiveStep) => prevActiveStep + 1);
        if (moreFutureEvents) {
          getUpcomingEvents('future', 0, 6, futureEvents, setFutureEvents, futureEventsNum, setFutureEventsNum, setMoreFutureEvents, setFutureGroups)
        }
        break;
      case 'past':
        setActiveStepPast((prevActiveStep) => prevActiveStep + 1);
        if (morePastEvents) {
          getUpcomingEvents('past', 0, 6, pastEvents, setPastEvents, pastEventsNum, setPastEventsNum, setMorePastEvents, setPastGroups)
        }


        break;
      default:
        console.log('Unexpect time period')
        break;

    }
  };

  const handleBack = (timePeriod) => {
    switch (timePeriod) {
      case 'past':
        setActiveStepPast((prevActiveStep) => prevActiveStep - 1);
        break;
      case 'month':
        setActiveStepFuture((prevActiveStep) => prevActiveStep - 1);
        break;
      default:
        console.log('Unexpect time period')
        break;
    }
  };

  const handleStepChange = (step, timePeriod) => {
    switch (timePeriod) {
      case 'past':
        setActiveStepPast(step);
        break;
      case 'future':
        setActiveStepFuture(step);
        break;
      default:
        console.log('Unexpect time period')
        break;
    }
  };

  // Function to get upcoming events
  const getUpcomingEvents = async (period, pageStart, maxResults,  eventIds, setEvents, eventNum, setEventNum, setMoreEvents, setEventGroups) => {
    try {
      const body = {
        max_results: maxResults,
        page_start: pageStart,
        email: userDetails.email
      }
      const searchParams = new URLSearchParams(body)
      var response 
      if (period === 'future') {
        response = await apiFetch('GET', `/api/user/hosting/future?${searchParams}`, null)
      } else if (period === 'past') {
        response = await apiFetch('GET', `/api/user/hosting/past?${searchParams}`, null)
      } else {
        return
      }
      

      if (pageStart === 0) {
        setEvents(response.eventIds)
        // Create event groups
        const groups_t = []
        var group_s = []
        var group_i = 0
        for (const i in response.event_ids) {
          const id = response.event_ids[i]
          if (group_i !== 3) {
            group_s.push(id)
          } else {
            group_i = 0
            groups_t.push(group_s)
            group_s = [id]
          }
          group_i += 1
        }
        groups_t.push(group_s)
        console.log(groups_t)
        setEventGroups(groups_t)
        setEventNum(response.event_ids.length)
      } else {
        const eventIds_t = eventIds.concat(response.event_ids)
        setEvents(eventIds_t)
        // Create event groups
        const groups_t = []
        var group_s = []
        var group_i = 0
        for (const i in eventIds_t) {
          const id = eventIds_t[i]
          if (group_i !== 3) {
            group_s.push(id)
          } else {
            group_i = 0
            groups_t.push(group_s)
            group_s = [id]
          }
          group_i += 1
        }
        groups_t.push(group_s)
        console.log(groups_t)
        setEventGroups(groups_t)
        setEventNum(eventNum + response.event_ids.length)
      }
      setMoreEvents(response.num_results === (eventNum + response.event_ids.length))
    } catch (e) {
      console.log(e)
    }
  }

  // Initial load of events
  React.useEffect(() => {
    // Fetch week events
    getUpcomingEvents('future', 0, 6, futureEvents, setFutureEvents, futureEventsNum, setFutureEventsNum, setMoreFutureEvents, setFutureGroups)

    // Fetch month events
    getUpcomingEvents('past', 0, 6, pastEvents, setPastEvents, pastEventsNum, setPastEventsNum, setMorePastEvents, setPastGroups)
  }, [])

  return (
    <>
      {!(yearEvents === 0)
        ? <Section sx={{ml: 0, mr: 0}}>
            <TabContext value={upcomingValue}>
              <SectionHeading>
                <Box sx={{display: 'flex', alignItems: 'flex-end'}}>
                  <Tabs
                    onChange={upcomingChange}
                    textColor="secondary"
                    indicatorColor="secondary"
                    scrollButtons
                    value={upcomingValue}
                    >
                    <Tab label="Upcoming Events" value="1" />
                    <Tab label="Past Events" value="2" />
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
                  {/* <Button color='secondary'>
                    see all
                  </Button> */}
                </Box>
              </SectionHeading>
              {/* Future Pannel */}
              <TabPanel value="1" sx={{padding: 0}}>
              <Box sx={{display: 'flex', flexDirection: 'column'}}>
                  <Box>
                    <SwipeableViews
                      axis={theme.direction === 'rtl' ? 'x-reverse' : 'x'}
                      index={activeStepFuture}
                      onChangeIndex={(e) => {handleStepChange(e, 'future')}}
                      enableMouseEvents
                    >
                      {futureGroups.map((events, key) => (
                        <div key={key}>
                          {Math.abs(activeStepFuture - key) <= 2 ? (
                            <EventCardsBar event_ids={events}/>
                          ) : null}
                        </div>
                      ))}
                    </SwipeableViews>
                  </Box>
                  <MobileStepper
                    sx={{color: 'red'}}
                    steps={maxStepsFuture}
                    position="static"
                    activeStep={activeStepFuture}
                    nextButton={
                      <Button
                        size="small"
                        onClick={(e) => {handleNext('future')}}
                        disabled={activeStepFuture === maxStepsFuture - 1}
                      >
                        Next
                        {theme.direction === 'rtl' ? (
                          <KeyboardArrowLeft />
                        ) : (
                          <KeyboardArrowRight />
                        )}
                      </Button>
                    }
                    backButton={
                      <Button size="small" onClick={(e) => {handleBack('future')}} disabled={activeStepFuture === 0}>
                        {theme.direction === 'rtl' ? (
                          <KeyboardArrowRight />
                        ) : (
                          <KeyboardArrowLeft />
                        )}
                        Back
                      </Button>
                    }
                  />
                </Box>
              </TabPanel>
              {/* Past Pannel */}
              <TabPanel value="2" sx={{padding: 0}}>
              <Box sx={{display: 'flex', flexDirection: 'column'}}>
                  <Box>
                    <SwipeableViews
                      axis={theme.direction === 'rtl' ? 'x-reverse' : 'x'}
                      index={activeStepPast}
                      onChangeIndex={(e) => {handleStepChange(e, 'past')}}
                      enableMouseEvents
                    >
                      {pastGroups.map((events, key) => (
                        <div key={key}>
                          {Math.abs(activeStepPast - key) <= 2 ? (
                            <EventCardsBar event_ids={events}/>
                          ) : null}
                        </div>
                      ))}
                    </SwipeableViews>
                  </Box>
                  <MobileStepper
                    sx={{color: 'red'}}
                    steps={maxStepsPast}
                    position="static"
                    activeStep={activeStepPast}
                    nextButton={
                      <Button
                        size="small"
                        onClick={(e) => {handleNext('past')}}
                        disabled={activeStepPast === maxStepsPast - 1}
                      >
                        Next
                        {theme.direction === 'rtl' ? (
                          <KeyboardArrowLeft />
                        ) : (
                          <KeyboardArrowRight />
                        )}
                      </Button>
                    }
                    backButton={
                      <Button size="small" onClick={(e) => {handleBack('past')}} disabled={activeStepPast === 0}>
                        {theme.direction === 'rtl' ? (
                          <KeyboardArrowRight />
                        ) : (
                          <KeyboardArrowLeft />
                        )}
                        Back
                      </Button>
                    }
                  />
                </Box>
              </TabPanel>
            </TabContext>
          </Section>
        : <></>
      }
    </>
  )
}