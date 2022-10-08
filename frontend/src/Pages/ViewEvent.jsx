import React from "react";

import Header from "../Components/Header";
import { BackdropNoBG, CentredBox } from "../Styles/HelperStyles";
import OutlinedInput from "@mui/material/OutlinedInput";
import { AdapterMoment } from "@mui/x-date-pickers/AdapterMoment";
import { LocalizationProvider } from "@mui/x-date-pickers/LocalizationProvider";
import { DateTimePicker } from "@mui/x-date-pickers/DateTimePicker";
import TextField from "@mui/material/TextField";
import dayjs from "dayjs";
import FormControl, { useFormControl } from "@mui/material/FormControl";
import FormHelperText from "@mui/material/FormHelperText";
import { checkValidEmail, setFieldInState } from "../Helpers";
import Grid from "@mui/material/Unstable_Grid2";
import { H3 } from "../Styles/HelperStyles";
import ListItemText from "@mui/material/ListItemText";
import DeleteIcon from "@mui/icons-material/Delete";
import IconButton from "@mui/material/IconButton";
import { Box, FormLabel, List, ListItem, Typography } from "@mui/material";
import ShadowInput from "../Components/ShadowInput";
import { borderRadius, styled, alpha } from '@mui/system';
import EmailIcon from '@mui/icons-material/Email';
import Alert from '@mui/material/Alert';
import Collapse from '@mui/material/Collapse';
import LocalOfferIcon from '@mui/icons-material/LocalOffer';

import { ContrastInput, ContrastInputWrapper, DeleteButton, FormInput, TextButton, TkrButton } from '../Styles/InputStyles';
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
  
  const [event, setEvent] = React.useState({})

  const testEvent = {
    event_name: "Welcome Back Ray",
    location: {
      street_no: "1",
      street_name: "Station St",
      postcode: "2135",
      state: "NSW",
      country: "Australia"
    },
    start_date: dayjs(),
    end_date: dayjs(),
    description: "This is going to be a party",
    tags: ["music", "festival", "food"],
    admins: [],
  }

  React.useEffect(()=> {
    setEvent(testEvent)
  })

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
        <H3 sx={{ fontSize: "30px" }}>Create Event</H3>
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
                <Grid container spacing={2}>
                  <Grid item xs={12}>
                    <Typography>
                      {event.event_name}
                    </Typography>
                  </Grid>
                  <Grid item xs={12}>
                    <Typography>
                      {event.location.street_no} {event.location.street_name}, {event.location.postcode}, {event.location.state}, {event.location.country}
                    </Typography>
                  </Grid>

                  <LocalizationProvider dateAdapter={AdapterMoment}>
                    <Grid item xs={6}>
                      <FormControl fullWidth={false}>
                        <FormLabel sx={{"&.Mui-focused": {color: 'rgba(0, 0, 0, 0.6) '}, color: "#999999"}}>Start Time</FormLabel>
                        <ContrastInputWrapper>
                          <DateTimePicker
                            value={event.start_date}
                            inputFormat="DD/MM/YYYY HH:mm"
                            renderInput={(params) => <TextField {...params} />}
                            disabled
                          />
                        </ContrastInputWrapper>
                      </FormControl>
                    </Grid>
                    <Grid item xs={6}>
                      <FormControl fullWidth={false}>
                        <FormLabel sx={{"&.Mui-focused": {color: 'rgba(0, 0, 0, 0.6) '}, color: "#999999"}}>Start Time</FormLabel>
                        <ContrastInputWrapper>
                          <DateTimePicker
                            value={event.end_date}
                            inputFormat="DD/MM/YYYY HH:mm"
                            renderInput={(params) => <TextField {...params} />}
                            disabled
                          />
                        </ContrastInputWrapper>
                      </FormControl>
                    </Grid>
                  </LocalizationProvider>
                  <Grid item xs={7}>
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
                    
                  </Grid>
                  <Grid item xs={5}/>
                  <Grid item xs={12}>
                    <Typography>
                      {event.description}
                    </Typography>
                  </Grid>
                </Grid>
              </Grid>
              <Grid item xs={1}></Grid>
              <Grid item xs={5}>
                <Box>
                  <h3> Ticket Allocations </h3>
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
                <Box>
                  <h3> Tags </h3>
                  <TagsBar tags={event.tags} editable={false}/>
                </Box>
              </Grid>
            </Grid>
          </EventForm>
        </div>
      </Box>
    </BackdropNoBG>
  );
}
