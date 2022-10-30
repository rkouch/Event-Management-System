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
import { apiFetch, getToken, loggedIn } from '../Helpers';
import dayjs from 'dayjs';
import SwipeableViews from 'react-swipeable-views';
import ArrowBackIcon from "@mui/icons-material/ArrowBack"
import KeyboardArrowLeft from '@mui/icons-material/KeyboardArrowLeft';
import KeyboardArrowRight from '@mui/icons-material/KeyboardArrowRight';
import MobileStepper from '@mui/material/MobileStepper';
import { useTheme } from '@mui/material/styles';

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

export default function UserHosting({}) {
  const theme = useTheme();
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
  const [yearGroups, setYearGroups] = React.useState([])
  const maxStepsYear = yearGroups.length;

  const handleNext = (timePeriod) => {
    switch (timePeriod) {
      case 'week':
        setActiveStepWeek((prevActiveStep) => prevActiveStep + 1);
        if (moreWeekEvents) {
          getUpcomingEvents(endOfWeek, weekEventsNum, 6, weekEvents, setWeekEvents, weekEventsNum, setWeekEventsNum, setMoreWeekEvents, setWeekGroups)
        }
        break;
      case 'month':
        setActiveStepMonth((prevActiveStep) => prevActiveStep + 1);
        if (moreMonthEvents) {
          getUpcomingEvents(endOfMonth, monthEventsNum, 6, monthEvents, setMonthEvents, monthEventsNum, setMonthEventsNum, setMoreMonthEvents, setMonthGroups)
        }
        break;
      case 'year':
        setActiveStepYear((prevActiveStep) => prevActiveStep + 1);
        if (moreYearEvents) {
          getUpcomingEvents(endOfYear, yearEventsNum, 6,yearEvents, setYearEvents, yearEventsNum, setYearEventsNum, setMoreYearEvents, setYearGroups)
        }
        break;
      default:
        console.log('Unexpect time period')
        break;

    }
  };

  const handleBack = (timePeriod) => {
    switch (timePeriod) {
      case 'week':
        setActiveStepWeek((prevActiveStep) => prevActiveStep - 1);
        break;
      case 'month':
        setActiveStepMonth((prevActiveStep) => prevActiveStep - 1);
        break;
      case 'year':
        setActiveStepYear((prevActiveStep) => prevActiveStep - 1);
        break;
      default:
        console.log('Unexpect time period')
        break;
    }
  };

  const handleStepChange = (step, timePeriod) => {
    switch (timePeriod) {
      case 'week':
        setActiveStepWeek(step);
        break;
      case 'month':
        setActiveStepMonth(step);
        break;
      case 'year':
        setActiveStepYear(step);
        break;
      default:
        console.log('Unexpect time period')
        break;
    }
  };

  // Function to get upcoming events
  const getUpcomingEvents = async (before, pageStart, maxResults,  eventIds, setEvents, eventNum, setEventNum, setMoreEvents, setEventGroups) => {
    try {
      const body = {
        auth_token: getToken(),
        max_results: maxResults,
        page_start: pageStart
      }
      const searchParams = new URLSearchParams(body)
      const response = await apiFetch('GET', `/api/event/hosting?${searchParams}`, null)

      if (pageStart === 0) {
        setEvents(response.eventIds)
        // Create event groups
        const groups_t = []
        var group_s = []
        var group_i = 0
        for (const i in response.eventIds) {
          const id = response.eventIds[i]
          if (group_i !== 5) {
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
        setEventNum(response.eventIds.length)
      } else {
        const eventIds_t = eventIds.concat(response.eventIds)
        setEvents(eventIds_t)
        // Create event groups
        const groups_t = []
        var group_s = []
        var group_i = 0
        for (const i in eventIds_t) {
          const id = eventIds_t[i]
          if (group_i !== 5) {
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
        setEventNum(eventNum + response.eventIds.length)
      }
      setMoreEvents(response.num_results === (eventNum + response.eventIds.length))
    } catch (e) {
      console.log(e)
    }
  }

  // Initial load of events
  React.useEffect(() => {
    if (loggedIn()) {
      // Fetch week events
      getUpcomingEvents(endOfWeek, 0, 6, weekEvents, setWeekEvents, weekEventsNum, setWeekEventsNum, setMoreWeekEvents, setWeekGroups)

      // Fetch month events
      // getUpcomingEvents(endOfMonth, 0, 6, monthEvents, setMonthEvents, monthEventsNum, setMonthEventsNum, setMoreMonthEvents)

      // Fetch year events
      // getUpcomingEvents(endOfYear, 0, 6, yearEvents, setYearEvents, yearEventsNum, setYearEventsNum, setMoreYearEvents)
    }
  }, [])

  return (
    <>
      {(!(yearEvents === 0) && loggedIn())
        ? <Section sx={{pt: 13}}>
            <TabContext value={upcomingValue}>
              <SectionHeading>
                My Hosted Events
                <Divider orientation="vertical" variant="middle" flexItem/>
                <Box sx={{display: 'flex', alignItems: 'flex-end'}}>
                  <Tabs
                    onChange={upcomingChange}
                    textColor="secondary"
                    indicatorColor="secondary"
                    scrollButtons
                    value={upcomingValue}
                    >
                    <Tab label="All" value="1" />
                    {/* <Tab label="This Week" value="1" />
                    <Tab label="This Month" value="2" />
                    <Tab label="This Year" value="3" /> */}
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
              <TabPanel value="1" sx={{padding: 0}}>
                <Box sx={{display: 'flex', flexDirection: 'column'}}>
                  <Box>
                    <SwipeableViews
                      axis={theme.direction === 'rtl' ? 'x-reverse' : 'x'}
                      index={activeStepWeek}
                      onChangeIndex={(e) => {handleStepChange(e, 'week')}}
                      enableMouseEvents
                    >
                      {weekGroups.map((events, key) => (
                        <div key={key}>
                          {Math.abs(activeStepWeek - key) <= 2 ? (
                            <EventCardsBar event_ids={events}/>
                          ) : null}
                        </div>
                      ))}
                    </SwipeableViews>
                  </Box>
                  <MobileStepper
                    sx={{color: 'red'}}
                    steps={maxStepsWeek}
                    position="static"
                    activeStep={activeStepWeek}
                    nextButton={
                      <Button
                        size="small"
                        onClick={(e) => {handleNext('week')}}
                        disabled={activeStepWeek === maxStepsWeek - 1}
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
                      <Button size="small" onClick={(e) => {handleBack('week')}} disabled={activeStepWeek === 0}>
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
                {/* <EventCardsBar event_ids={weekEvents}/> */}
              </TabPanel>
              <TabPanel value="2" sx={{padding: 0}}>
                {/* <EventCardsBar event_ids={monthEvents}/> */}
              </TabPanel>
              <TabPanel value="3" sx={{padding: 0}}>
                {/* <EventCardsBar event_ids={yearEvents}/> */}
              </TabPanel>
            </TabContext>
          </Section>
        : <></>
      }
    </>
  )
}