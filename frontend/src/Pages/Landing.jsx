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
import { apiFetch, loggedIn } from '../Helpers';
import UpcomingEvents from '../Components/UpcomingEvents';
import UserBookings from '../Components/UserBookings';
import UserHosting from '../Components/UserHosting';


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
        
        <UserHosting/>
        <UserBookings/>
        <UpcomingEvents/>
        <br />
        <br />
        <br />
        <br />
      </BackdropNoBG>
    </div>
  )
}