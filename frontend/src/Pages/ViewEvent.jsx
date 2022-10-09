import React from "react";

import Header from "../Components/Header";
import { BackdropNoBG, CentredBox } from "../Styles/HelperStyles";
import dayjs from "dayjs";
import Grid from "@mui/material/Unstable_Grid2";
import { Box, Divider, Typography } from "@mui/material";
import { styled, alpha } from '@mui/system';
import CalendarMonthIcon from '@mui/icons-material/CalendarMonth';
import LocationOnIcon from '@mui/icons-material/LocationOn';

import TagsBar from "../Components/TagsBar";

export const EventForm = styled("div")({
  display: "flex",
  justifyContent: "center",
  flexDirection: "column",
  paddingTop: "15px",
  paddingBottom: "15px",
  paddingLeft: "15px",
  paddingRight: "15px",
  margin: "auto",
  alignItems: "center",
  gap: "10px",
});

export default function ViewEvent({}) {
  var calendar = require('dayjs/plugin/calendar')
  dayjs.extend(calendar)
  
  const testDate1 = dayjs().add(7, 'day')
  const testDate2 = testDate1.add(7, 'hour')

  const [event, setEvent] = React.useState({
    event_name: "",
    location: {
      street_no: "",
      street_name: "",
      postcode: "",
      state: "",
      country: ""
    },
    start_date: dayjs().toISOString(),
    end_date: dayjs().toISOString(),
    description: "",
    tags: [],
    admins: [],
  })

  const testEvent = {
    event_name: "Welcome Back Ray",
    location: {
      street_no: "1",
      street_name: "Station St",
      postcode: "2135",
      state: "NSW",
      country: "Australia"
    },
    start_date: testDate1.toISOString(),
    end_date: testDate2.toISOString(),
    description: "This is going to be a party",
    tags: ["music", "festival", "food"],
    admins: [],
  }

  React.useEffect(()=> {
    setEvent(testEvent)
  },[])

  console.log(event.start_date)
  return (
    <BackdropNoBG>
      <Header />
      <Box
        sx={{
          minHeight: 600,
          maxWidth: 1500,
          marginLeft: "auto",
          marginRight: "auto",
          width: "95%",
          backgroundColor: "#FFFFFF",
          marginTop: "50px",
          borderRadius: "15px",
          paddingBottom: 5,
          paddingTop: 1,
        }}
      >
        <div>
          <EventForm>
            <Grid
              container
              spacing={2}
              sx={{
                marginLeft: 5,
                marginRight: 5,
                maxWidth: "1200px",
                width: "100%",
              }}
            >
              <Grid item xs={12}>
                <CentredBox
                  sx={{
                    height: 200,
                    backgroundColor: alpha('#6A7B8A', 0.3),
                    borderRadius: 5,
                  }}
                >
                  <h3>
                    Event cover photo
                  </h3>
                </CentredBox>
              </Grid>
              <Grid item xs={6}>
                <Grid container spacing={0}>
                  <Grid item xs={12}>
                    <Typography
                      sx={{
                        fontSize: 40,
                        fontWeight: 'bold',
                      }}
                    >
                      {event.event_name}
                    </Typography>
                  </Grid>
                  <Grid item xs={12}>
                    <Grid container>
                      <Grid item xs={1} sx={{display: 'flex', alignItems: 'center'}}>
                        <CentredBox sx={{gap: '5px', justifyContent: 'flex-start'}}>
                          <CalendarMonthIcon sx={{color: '#AE759F'}}/>
                          
                        </CentredBox>
                      </Grid>
                      <Grid item xs={11}>
                        <Typography
                          sx={{
                            fontSize: 17,
                            color: "#AE759F",
                          }}
                        >
                          {dayjs(event.start_date).format('llll')} - {dayjs(event.end_date).format('llll')}
                        </Typography>
                      </Grid>
                    </Grid>
                  </Grid>
                  <Grid item xs={12}>
                    <Grid container>
                      <Grid item xs={1} sx={{display: 'flex', alignItems: 'center'}}>
                        <CentredBox sx={{gap: '5px', justifyContent: 'flex-start'}}>
                          <LocationOnIcon sx={{color: '#000000'}}/>
                        </CentredBox>
                      </Grid>
                      <Grid item xs={11}>
                        <Typography>
                          {event.location.street_no} {event.location.street_name}, {event.location.postcode}, {event.location.state}, {event.location.country}
                        </Typography>
                      </Grid>
                    </Grid>
                  </Grid>
                  {/* <Grid item xs={7}>
                    <h3>Admin List:</h3>
                    <List>
                      {event.admins.map((value, key) => {
                        return (
                          <div key={key}>
                            <ContrastInputWrapper >
                              <ListItem secondaryAction={
                                <IconButton
                                  edge="end"
                                  aria-label="delete"
                                >
                                  <DeleteIcon />
                                </IconButton>
                              }>
                                <ListItemText primary={`@${value.admin}`}/>
                              </ListItem>
                            </ContrastInputWrapper>
                            <br/>
                          </div>
                        );
                      })}
                    </List>
                  </Grid> */}
                  <Grid item xs={12}>
                    <br/>
                    <Typography
                      sx={{
                        color: "#999999"
                      }}
                    >
                      About this event
                    </Typography>
                    <Divider/>
                    <Typography sx={{pt: 2}}>
                      {event.description}
                    </Typography>
                  </Grid>
                  <Grid item xs={12}>
                    <br/>
                    <Box>
                      <Typography
                        sx={{
                          color: "#999999"
                        }}
                      >
                        Hosts
                      </Typography>
                      <Divider sx={{width: "50px"}}/>
                    </Box>
                    
                  </Grid>
                  <Grid item xs={12}>
                    <br/>
                    <Box>
                      <Typography
                        sx={{
                          fontWeight: 'bold'
                        }}
                      >
                        Tags
                      </Typography>
                      <TagsBar tags={event.tags} editable={false}/>
                    </Box>
                  </Grid>
                </Grid>
              </Grid>
              <Grid item xs={1}></Grid>
              <Grid item xs={5}>
                <Box>
                  <h3> Tickets </h3>
                  {/* <Box>
                    {seatingList.map((value, index) => {
                      return (
                        <div key={index}>
                          <Grid container spacing={1}>
                            <Grid item xs={7}>
                              <ContrastInputWrapper>
                                <ContrastInput placeholder="Section Name" fullWidth onChange={(e) => handleSectionChange(e, index)}/>
                              </ContrastInputWrapper>
                            </Grid>
                            <Grid item xs={3}>
                              <ContrastInputWrapper>
                                <ContrastInput placeholder="Spots" fullWidth onChange={handleCapacityChange}/>
                              </ContrastInputWrapper>
                            </Grid>
                            <Grid item xs={2}>
                              <ContrastInputWrapper sx={{height: "100%", width: '100%', display: 'flex', justifyContent: 'center', alignItems: 'center'}}>
                                <IconButton
                                  edge="end"
                                  aria-label="delete"
                                  onClick={() => removeSeating(index)}
                                  sx={{marginRight: 0}}
                                >
                                  <DeleteIcon />
                                </IconButton>
                              </ContrastInputWrapper>
                            </Grid>
                          </Grid>
                        </div>
                      );
                    })}
                  </Box> */}
                </Box>
                <br/>
              </Grid>
            </Grid>
          </EventForm>
        </div>
      </Box>
    </BackdropNoBG>
  );
}
