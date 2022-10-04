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


export default function Landing({}) {
  const [upcomingValue, setUpcomingValue] = React.useState('1');
  const upcomingChange = (event, newValue) => {
    setUpcomingValue(newValue);
  };

  const [value, setValue] = React.useState('1');
  const handleChange = (event, newValue) => {
    setValue(newValue)
  }

  return (
    <div>
      <BackdropNoBG
      >
        <Header/>
        <CentredBox
          sx={{
            height: 200,
            backgroundColor: "#F1F9F9"
          }}
        >
          <h1>
            Welcome to Tickr.
          </h1>
        </CentredBox>
        <br/>
        <Section>
          <TabContext value={value}>
            <SectionHeading>
              Hosting Events
              <Divider orientation="vertical" variant="middle" flexItem/>
              <Box sx={{display: 'flex', alignItems: 'flex-end'}}>
                <Tabs
                  onChange={handleChange}
                  aria-label="lab API tabs example"
                  textColor="secondary"
                  indicatorColor="secondary"
                  scrollButtons
                  value={value}
                  >
                  <Tab label="Category One" value="1" />
                  <Tab label="Category Two" value="2" />
                  <Tab label="Category Three" value="3" />
                </Tabs>
              </Box>
              <Box
                sx={{
                  width: 'auto',
                  display: 'flex',
                  alignItems: 'flex-end',
                  justifyContent: 'flex-end',
                  paddingBottom: '6px',
                  flexGrow: '4',
                }}
              >
                <Button color='secondary'>
                  see all
                </Button>
              </Box>
            </SectionHeading>
            <TabPanel value="1" sx={{padding: 0}}>
              <CardsBar>
                <EventCard/>
                <EventCard/>
                <EventCard/>
                <EventCard/>
              </CardsBar>
            </TabPanel>
            <TabPanel value="2" sx={{padding: 0}}>
              <CardsBar>
                Section 2
              </CardsBar>
            </TabPanel>
            <TabPanel value="3" sx={{padding: 0}}>
              <CardsBar>
                <EventCard/>
                <EventCard/>
                <EventCard/>
              </CardsBar>
            </TabPanel>
          </TabContext>
        </Section>
        {/* <br />
        <br />
        <br />
        <Section>
          <TabContext value={upcomingValue}>
            <SectionHeading>
              Upcoming Events
              <Divider orientation="vertical" variant="middle" flexItem/>
              <Box sx={{display: 'flex', alignItems: 'flex-end'}}>
                <Tabs
                  onChange={upcomingChange}
                  aria-label="lab API tabs example"
                  textColor="secondary"
                  indicatorColor="secondary"
                  scrollButtons
                  value={upcomingValue}
                  >
                  <Tab label="Category One" value="1" />
                  <Tab label="Category Two" value="2" />
                  <Tab label="Category Three" value="3" />
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
              <CardsBar>
                <EventCard>  
                </EventCard>
              </CardsBar>
            </TabPanel>
            <TabPanel value="2" sx={{padding: 0}}>
              <CardsBar>
                Section 2
              </CardsBar>
            </TabPanel>
            <TabPanel value="3" sx={{padding: 0}}>
              <CardsBar>
                Section 3
              </CardsBar>
            </TabPanel>
          </TabContext>
        </Section>
        <br />
        <br />
        <br />
        <br /> */}
      </BackdropNoBG>
    </div>
  )
}