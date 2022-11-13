import React from 'react'
import Box from '@mui/material/Box';
import Header from '../Components/Header'
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
import EventCard from '../Components/EventCard';
import Button from '@mui/material/Button';
import EventCardsBar from '../Components/EventCardsBar';
import { apiFetch, getToken } from '../Helpers';
import dayjs from 'dayjs';
import SwipeableViews from 'react-swipeable-views';
import ArrowBackIcon from "@mui/icons-material/ArrowBack"
import KeyboardArrowLeft from '@mui/icons-material/KeyboardArrowLeft';
import KeyboardArrowRight from '@mui/icons-material/KeyboardArrowRight';
import MobileStepper from '@mui/material/MobileStepper';
import { useTheme } from '@mui/material/styles';
import { useRef } from 'react';
import EventsBar from './EventsBar';

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

const EVENT_CARD_WIDTH = 250

const categories_t = [
  'Food',
  'Music',
  'Travel & Outdoor',
  'Health',
  'Sport & Fitness',
  'Hobbies',
  'Business',
  'Free',
  'Tourism',
  'Education'
]

export default function CategoriesEventsBar({}) {
  const theme = useTheme();
  const ref = useRef(null)
  const [upcomingValue, setUpcomingValue] = React.useState('0');
  const [categories, setCategories] = React.useState([])
  const upcomingChange = (event, newValue) => {
    setUpcomingValue(newValue);
  }; 

  const getCategories = async () => {
    try {
      const response = await apiFetch('GET', '/api/events/categories/list', null)
      setCategories(response.categories)
    } catch (e) {
      console.log(e)
    }
  }

  React.useEffect(() => {
    getCategories()
  }, [])

  return (
    <Section ref={ref} sx={{pt: 2, pb: 10}}>
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
              {categories.map((category, key) => {
                return(
                  <Tab label={category} value={(key).toString()} key={key}/>
                )
              })}
            </Tabs>
          </Box>
        </SectionHeading>
        {categories.map((category, key) => {
          return (
            <TabPanel key={key} value={(key).toString()} sx={{padding: 0}}>
              <EventsBar endpoint={'/api/events/category'} additionalParams={{category: category}} responseField={'event_ids'}/>
            </TabPanel>
          )
        })}
      </TabContext>
    </Section>
  )
}