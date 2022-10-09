import React from "react";

import Header from "../Components/Header";
import { BackdropNoBG, CentredBox, UploadPhoto } from "../Styles/HelperStyles";
import dayjs from "dayjs";
import Grid from "@mui/material/Unstable_Grid2";
import { Box, Divider, IconButton, Tooltip, Typography } from "@mui/material";
import { styled, alpha } from '@mui/system';
import CalendarMonthIcon from '@mui/icons-material/CalendarMonth';
import LocationOnIcon from '@mui/icons-material/LocationOn';
import AvatarGroup from '@mui/material/AvatarGroup';
import LinearProgress from '@mui/material/LinearProgress';
import TagsBar from "../Components/TagsBar";
import UserAvatar from "../Components/UserAvatar";
import AdminsBar from "../Components/AdminBar";
import { useNavigate, useParams } from "react-router-dom";
import { checkIfUser, getEventData } from "../Helpers";
import Table from '@mui/material/Table';
import TableBody from '@mui/material/TableBody';
import TableCell from '@mui/material/TableCell';
import TableContainer from '@mui/material/TableContainer';
import TableHead from '@mui/material/TableHead';
import TableRow from '@mui/material/TableRow';
import EditIcon from '@mui/icons-material/Edit';

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
  const params = useParams()
  const navigate = useNavigate()
  const [editable, setEditable] = React.useState()
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
    host_id: '',
    start_date: dayjs().toISOString(),
    end_date: dayjs().toISOString(),
    description: "",
    tags: [],
    admins: [],
    picture: "",
    host_id: ''
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
    host_id: "1d14a0d0-5d09-4ed2-be9d-02c4d3cfd719",
    start_date: testDate1.toISOString(),
    end_date: testDate2.toISOString(),
    description: "This is going to be a party",
    tags: ["music", "festival", "food"],
    admins: ["8fa85163-5fe7-4183-8026-9b5ff174ee4c"],
  }

  React.useEffect(()=> {
    getEventData(params.event_id, setEvent)
    setEditable(checkIfUser())
  },[])

  const goEdit = (e) => {
    e.stopPropagation();
    e.nativeEvent.stopImmediatePropagation();
    navigate(`/edit_event/${params.event_id}`)
  }

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
        {(event.host_id === '')
          ? <Box sx={{height: '100%', display: 'flex', justifyContent: 'center', alignItems: 'center', margin: '50px', paddingTop: '100px'}}> 
              <Box sx={{ width: '90%', hieght: 50}}>
                <LinearProgress color='secondary'/>
              </Box>
            </Box>
          :<div>
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
                    {(event.picture === '')
                      ? <h3>
                          Event cover photo
                        </h3>
                      : <UploadPhoto src={event.picture}/>
                    }
                  </CentredBox>
                </Grid>
                <Grid item xs={6}>
                  <Grid container spacing={0}>
                    <Grid item xs={12}>
                      <Box sx={{display: 'flex', justifyContent: 'space-between'}}>
                        <Typography
                          sx={{
                            fontSize: 40,
                            fontWeight: 'bold',
                          }}
                        >
                          {event.event_name}
                        </Typography>
                        {(editable)
                          ? <Tooltip title="Edit Event">
                              <IconButton onClick={goEdit}>
                                <EditIcon/>
                              </IconButton>
                            </Tooltip>
                          : <></>
                        }
                      </Box>
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
                            {dayjs(event.start_date).format('LLLL')} - {dayjs(event.end_date).format('LLLL')}
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
                            {event.location.street_no} {event.location.street_name}, {event.location.suburb}, {event.location.postcode}, {event.location.state}, {event.location.country}
                          </Typography>
                        </Grid>
                      </Grid>
                    </Grid>
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
                        <AvatarGroup max={5} sx={{flexDirection: 'row', pt:2}}>
                          <UserAvatar userId={event.host_id} size={35}/>
                          {event.admins.map((value, key) => {
                            return (
                              <UserAvatar key={key} userId={value} size={35}/>
                            );
                          })}
                        </AvatarGroup>
                      </Box>
                      <br/>
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
                    <Typography
                      sx={{
                        fontWeight: 'bold',
                        fontSize: 35,
                        pt: 2,
                      }}
                    >
                      Tickets
                    </Typography>
                    <TableContainer>
                      <Table stickyHeader sx={{maxHeight: 300}}>
                        <TableHead>
                          <TableRow>
                            <TableCell sx={{fontWeight: 'bold', fontSize: 20}}>Section</TableCell>
                            <TableCell sx={{fontWeight: 'bold', fontSize: 20}} align="center">Availability</TableCell>
                          </TableRow>
                        </TableHead>
                        <TableBody>
                          {event.seating_details.map((section, key) => {
                            return (
                              <TableRow key={key}>
                                <TableCell>{section.section}</TableCell>
                                <TableCell align="center">{section.availability}</TableCell>
                              </TableRow>
                            )
                          })}
                        </TableBody>
                      </Table>
                    </TableContainer>
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
        }
      </Box>
    </BackdropNoBG>
  );
}
