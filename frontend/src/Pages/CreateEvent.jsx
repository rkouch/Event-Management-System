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
import { Box, FormLabel, List, ListItem } from "@mui/material";
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

export default function CreateEvent({}) {
  // States

  const [start, setStartValue] = React.useState({
    start: dayjs("2014-08-18T21:11:54"),
    error: false,
  });

  const [end, setEndValue] = React.useState({
    end: dayjs("2014-08-18T21:11:54"),
    error: false,
    errorMsg: "",
  });

  const [eventName, setEventName] = React.useState({
    value: '',
    error: false,
    errorMsg: '',
  })

  const [description, setDescription] = React.useState({
    value: '',
    error: false
  })

  const [address, setAddress] = React.useState({
    value: '',
    error: false,
  })

  const [postcode, setPostcode] = React.useState({
    value: '',
    error: false
  })

  const [state, setState] = React.useState({
    value: '',
    error: false
  })

  const [country, setCountry] = React.useState({
    value: '',
    error: false
  })

  const [newAdmin, setNewAdmin] = React.useState({
    email: '',
    error: false,
    errorMsg: ''
  });

  const [adminList, setAdminList] = React.useState([]);

  const [seatingList, setSeatingList] = React.useState([]);

  const [tags, setTags] = React.useState([])

  const [errorStatus, setErrorStatus] = React.useState(false)

  const [errorMsg, setErrorMsg] = React.useState('')

  React.useEffect(()=> {
    if(!errorStatus) {
      setErrorStatus(false)
      setErrorMsg('')
    }
  }, [errorStatus])

  const handleStartChange = (newValue) => {
    setFieldInState("start", newValue, start, setStartValue)
    setFieldInState("error", false, start, setStartValue)
    if (end.end < start.start) {
      setFieldInState("error", true, end, setEndValue);
      setFieldInState(
        "errorMsg",
        "End date must be after start date",
        end,
        setEndValue
      );
    } else {
      setFieldInState("error", false, end, setEndValue);
      setFieldInState("errorMsg", "", end, setEndValue);
    }
  };

  const handleEndChange = (newValue) => {
    setFieldInState("end", newValue, end, setEndValue);
    setFieldInState("error", true, end, setEndValue);
    console.log(end.end);
    if (end.end <= start) {
      setFieldInState("error", true, end, setEndValue);
      setFieldInState(
        "errorMsg",
        "End date must be after start date",
        end,
        setEndValue
      );
    } else {
      setFieldInState("error", false, end, setEndValue);
      setFieldInState("errorMsg", "", end, setEndValue);
    }
  };

  const handleNewAdmin = (e) => {
    setFieldInState('error', false, newAdmin, setNewAdmin)
    setFieldInState('errorMsg', '', newAdmin, setNewAdmin)
    setFieldInState('email', e.target.value, newAdmin, setNewAdmin)
  };

  const addAdmin = (e) => {
    e.stopPropagation();
    e.nativeEvent.stopImmediatePropagation();

    // Check form input
    if (newAdmin.email.length === 0 || !checkValidEmail(newAdmin.email)) {
      setFieldInState('error', true, newAdmin, setNewAdmin)
      setFieldInState('errorMsg', 'Please enter a valid email', newAdmin, setNewAdmin)
      return
    }

    // Check if user exists
    const adminList_t = [...adminList];
    adminList_t.push({ admin: newAdmin.email });
    setAdminList(adminList_t);
    setFieldInState('email', '', newAdmin, setNewAdmin)
  };

  const removeAdmin = (index) => {
    const admin_list = [...adminList];
    admin_list.splice(index, 1);
    setAdminList(admin_list);
  };

  const addSection = (e) => {
    setSeatingList([...seatingList, { sectionName: "", sectionCapacity: 0 }]);
    console.log(seatingList);
  };

  const removeSeating = (index) => {
    const list = [...seatingList];
    list.splice(index, 1);
    setSeatingList(list);
  };

  const handleSectionChange = (e, index) => {
    const list = [...seatingList];
    list[index].sectionName = e.target.value;
    setSeatingList(list);
    console.log(seatingList);
  };

  const handleCapacityChange = (e, index) => {
    const { name, value } = e.target;
    const list = [...seatingList];
    list[index][name] = value;
    setSeatingList(list);
    console.log(seatingList);
  };

  const submitEvent = async (e) => {
    // Check fields
    var error = false
    if (eventName.value.length === 0) {
      console.log("event empty")
      console.log(eventName)
      setFieldInState('error', true, eventName, setEventName)
      error = true
    }
    if (address.value.length === 0) {
      setFieldInState('error', true, address, setAddress)
      error = true
    }
    if (postcode.value.length === 0) {
      setFieldInState('error', true, postcode, setPostcode)
      error = true
    }
    if (state.value.length === 0) {
      setFieldInState('error', true, state, setState)
      error = true
    }
    if (country.value.length === 0) {
      setFieldInState('error', true, country, setCountry)
      error = true
    }
    if (description.value.length === 0) {
      setFieldInState('error', true, description, setDescription)
      error = true
    }

    if (error) {
      setErrorStatus(true)
      setErrorMsg('Please fill in required fields')
      // return
    }

    if (end.end <= start.start) {
      setFieldInState("error", true, end, setEndValue);
      setFieldInState(
        "errorMsg",
        "End date must be after start date",
        end,
        setEndValue
      );
      console.log('start date error')
    }

    const locationBody = {
      address: address.address,
      postcode: address.postcode,
      state: address.state,
      country: address.country
    };

    const body = {

    };
  };

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
                <h3> Event Details </h3>
                <Grid container spacing={2}>
                  <Grid item xs={12}>
                    <ShadowInput
                      sx={{
                        fontWeight: 'bold',
                        '.MuiOutlinedInput-notchedOutline': {
                          borderColor: eventName.error ? "red" : "rgba(0,0,0,0)"
                        },
                      }}
                      state={eventName}
                      setState={setEventName}
                      defaultValue={eventName.value}
                      field='value'
                      placeholder="Event Name"
                      setError={setErrorStatus}
                    />
                  </Grid>
                  <Grid item xs={12}>
                    <ShadowInput 
                      state={address}
                      sx={{
                        '.MuiOutlinedInput-notchedOutline': {
                          borderColor: address.error ? "red" : "rgba(0,0,0,0)"
                        },
                      }}
                      setState={setAddress}
                      defaultValue={address.value}
                      field='value'
                      placeholder="Street Address"
                      setError={setErrorStatus}
                    />
                  </Grid>
                  <Grid item xs={3}>
                    <ShadowInput 
                      state={postcode} 
                      setState={setPostcode} 
                      sx={{
                        '.MuiOutlinedInput-notchedOutline': {
                          borderColor: postcode.error ? "red" : "rgba(0,0,0,0)"
                        },
                      }}
                      defaultValue={postcode.value} 
                      field='value' 
                      placeholder="Postcode"
                      setError={setErrorStatus}
                    />
                  </Grid>
                  <Grid item xs={4}>
                  <ShadowInput 
                      state={state} 
                      setState={setState} 
                      sx={{
                        '.MuiOutlinedInput-notchedOutline': {
                          borderColor: state.error ? "red" : "rgba(0,0,0,0)"
                        },
                      }}
                      defaultValue={state.value} 
                      field='value' 
                      placeholder="State"
                      setError={setErrorStatus}
                    />
                  </Grid>
                  <Grid item xs={5}>
                    <ShadowInput 
                      state={country} 
                      setState={setCountry} 
                      sx={{
                        '.MuiOutlinedInput-notchedOutline': {
                          borderColor: country.error ? "red" : "rgba(0,0,0,0)"
                        },
                      }}
                      defaultValue={state.value} 
                      field='value' 
                      placeholder="Country"
                      setError={setErrorStatus}
                    />
                  </Grid>

                  <LocalizationProvider dateAdapter={AdapterMoment}>
                    <Grid item xs={6}>
                      <FormControl fullWidth={false}>
                        <FormLabel sx={{"&.Mui-focused": {color: 'rgba(0, 0, 0, 0.6) '}, color: start.error ? "red" : "#999999"}}>Start Time</FormLabel>
                        <ContrastInputWrapper>
                          <DateTimePicker
                            value={start.start}
                            onChange={handleStartChange}
                            inputFormat="DD/MM/YYYY HH:mm"
                            renderInput={(params) => <TextField {...params} />}
                            disablePast = {true}
                            sx={{
                              '.MuiOutlinedInput-notchedOutline': {
                                borderColor: country.error ? "red" : "rgba(0,0,0,0)"
                              },
                            }}
                          />
                        </ContrastInputWrapper>
                      </FormControl>
                    </Grid>
                    <Grid item xs={6}>
                      <FormControl fullWidth={false}>
                        <FormLabel sx={{"&.Mui-focused": {color: 'rgba(0, 0, 0, 0.6) ',}, color: end.error ? "red" : "#999999"}}>End Time</FormLabel>
                        <ContrastInputWrapper>
                          <DateTimePicker
                            value={end.end}
                            onChange={handleEndChange}
                            inputFormat="DD/MM/YYYY HH:mm"
                            renderInput={(params) => <TextField {...params} />}
                            disablePast = {true}
                          />
                        </ContrastInputWrapper>
                        <FormHelperText>{end.errorMsg}</FormHelperText>
                      </FormControl>
                    </Grid>
                  </LocalizationProvider>

                  <Grid item xs={8}>
                    <FormControl>
                      <ContrastInputWrapper>
                        <ContrastInput 
                          value = {newAdmin.email}
                          placeholder="New Admin" 
                          startAdornment={<CentredBox sx={{pr: 1}}><EmailIcon sx={{color: "rgba(0,0,0,0.45)"}}/></CentredBox>} 
                          fullWidth 
                          onChange={handleNewAdmin}
                          sx={{
                            '.MuiOutlinedInput-notchedOutline': {
                              borderColor: newAdmin.error ? "red" : "rgba(0,0,0,0)"
                            },
                          }}
                        />
                      </ContrastInputWrapper>
                      <FormHelperText>{newAdmin.errorMsg}</FormHelperText>
                    </FormControl>
                  </Grid>
                  <Grid item xs={4}>
                    <TkrButton variant="contained" disabled={(newAdmin.email.length > 0) ? false : true} onClick={addAdmin} sx={{mt: 1, fontSize: 15}}>
                      Add Admin
                    </TkrButton>
                  </Grid>
                  <Grid item xs={7}>
                    {(adminList.length !== 0)
                      ? <div>
                          <h3>Admin List:</h3>
                          <List>
                            {adminList.map((value, key) => {
                              return (
                                <div key={key}>
                                  <ContrastInputWrapper >
                                    <ListItem secondaryAction={
                                      <IconButton
                                        edge="end"
                                        aria-label="delete"
                                        onClick={() => removeAdmin(key)}
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
                      </div>
                      : <div></div>
                    }
                    
                  </Grid>
                  <Grid item xs={5}/>
                  <Grid item xs={12}>
                    <ContrastInputWrapper>
                      <ContrastInput
                        multiline
                        placeholder={'Enter a description'}
                        rows={4}
                        defaultValue={description.value}
                        fullWidth onChange={(e) => {
                          setFieldInState('value', e.target.value, description, setDescription)
                          setFieldInState('error', false, description, setDescription)
                          setErrorStatus(false)
                        }}
                        sx={{
                          '.MuiOutlinedInput-notchedOutline': {
                            borderColor: description.error ? "red" : "rgba(0,0,0,0)"
                          },
                        }}
                      />
                    </ContrastInputWrapper>
                  </Grid>
                </Grid>
              </Grid>
              <Grid item xs={1}></Grid>
              <Grid item xs={5}>
                <Box>
                  <h3> Ticket Allocations </h3>
                  {/* <OutlinedInput placeholder="Country" variant="outlined" sx={{paddingLeft: '15px', borderRadius: 2}} fullWidth={true}/> */}
                  <Box>
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
                  </Box>
                  <Box sx={{marginRight: 4, pl: 1, width: '100%'}}>
                    <TkrButton variant="contained" onClick={addSection}>
                      Add Section
                    </TkrButton>
                  </Box>
                </Box>
                <br/>
                <Box>
                  <h3> Tags </h3>
                  <TagsBar tags={tags} setTags={setTags} editable={true}/>
                </Box>
              </Grid>
            </Grid>
          </EventForm>
          <FormInput>
            <CentredBox>
              <TkrButton variant="contained" onClick={submitEvent}>Create Event</TkrButton>
            </CentredBox>
            <Collapse in={errorStatus}>
              <Alert severity="error">{errorMsg}.</Alert>
            </Collapse>
          </FormInput>
        </div>
      </Box>
    </BackdropNoBG>
  );
}
