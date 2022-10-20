import React from "react";

import Header from "../Components/Header";
import { BackdropNoBG, CentredBox, UploadPhoto } from "../Styles/HelperStyles";
import { AdapterMoment } from "@mui/x-date-pickers/AdapterMoment";
import { LocalizationProvider } from "@mui/x-date-pickers/LocalizationProvider";
import { DateTimePicker } from "@mui/x-date-pickers/DateTimePicker";
import TextField from "@mui/material/TextField";
import dayjs from "dayjs";
import FormControl from "@mui/material/FormControl";
import FormHelperText from "@mui/material/FormHelperText";
import { apiFetch, checkValidEmail, getToken, getUserData, setFieldInState, fileToDataUrl, getEventData, checkIfUser } from "../Helpers";
import Grid from "@mui/material/Unstable_Grid2";
import { H3 } from "../Styles/HelperStyles";
import ListItemText from "@mui/material/ListItemText";
import DeleteIcon from "@mui/icons-material/Delete";
import IconButton from "@mui/material/IconButton";
import { Backdrop, Box, Divider, FormGroup, FormLabel, InputAdornment, List, ListItem, Typography } from "@mui/material";
import ShadowInput from "../Components/ShadowInput";
import { styled, alpha } from '@mui/system';
import EmailIcon from '@mui/icons-material/Email';
import Alert from '@mui/material/Alert';
import Collapse from '@mui/material/Collapse';
import SaveIcon from '@mui/icons-material/Save';
import LocalOfferIcon from '@mui/icons-material/LocalOffer';
import LoadingButton from "../Components/LoadingButton"
import {CircularProgress} from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import PhotoCamera from '@mui/icons-material/PhotoCamera';
import Button from '@mui/material/Button';
import FormControlLabel from '@mui/material/FormControlLabel';
import Checkbox from '@mui/material/Checkbox';
import AvatarGroup from '@mui/material/AvatarGroup';
import UserAvatar from "../Components/UserAvatar";

import { ContrastInput, ContrastInputWrapper, DeleteButton, FormInput, TextButton, TkrButton, TkrButton2 } from '../Styles/InputStyles';
import TagsBar from "../Components/TagsBar";
import AdminsBar from "../Components/AdminBar";
import { useNavigate, useParams } from "react-router-dom";


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

export default function EditEvent({}) {
  const params = useParams()
  const navigate = useNavigate()
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

  const [suburb, setSuburb] = React.useState({
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
    errorMsg: '',
    responseState: false,
    response: '',
  });

  const [adminList, setAdminList] = React.useState([]);

  const [seatingList, setSeatingList] = React.useState([]);

  const [tags, setTags] = React.useState([])

  const [errorStatus, setErrorStatus] = React.useState(false)

  const [errorMsg, setErrorMsg] = React.useState('')

  const [adminLoading, setAdminLoading] = React.useState(false)

  const [newSection, setNewSection] = React.useState({
    section: '',
    available_seats: 0,
    ticket_price: 0,
    has_seats: false,
    error: false,
    errorMsg: '',
  });
  const [eventPicture, setEventPicture] = React.useState('')

  const [toggleUpload, setToggleUpload] = React.useState(true)

  const [published, setPublished] = React.useState(false)

  const [isHost, setIsHost] = React.useState(false)

  const [newHostMenu, setNewHostMenu] = React.useState(false)

  const [newHost, setNewHost] = React.useState({})

  const [newPhoto, setNewPhoto] = React.useState(false)

  const [openDeleteMenu, setOpenDeleteMenu] = React.useState(false)

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
    host_id: '',
    published: true,
  })
  
  React.useState(() => {
    getEventData(params.event_id, setEvent)
  }, [])

  // Set Values
  React.useEffect(() => {
    console.log(event)
    setFieldInState('value', event.event_name, eventName, setEventName)
    setFieldInState('value', event.description, description, setDescription)
    const eventLocation = event.location
    setFieldInState('value', eventLocation.street_no.toString() + ' ' + eventLocation.street_name, address, setAddress)
    setFieldInState('value', eventLocation.postcode, postcode, setPostcode)
    setFieldInState('value', eventLocation.suburb, suburb, setSuburb)
    setFieldInState('value', eventLocation.state, state, setState)
    setFieldInState('value', eventLocation.country, country, setCountry)
    setAdminList(event.admins)
    setTags(event.tags)
    setFieldInState('start', dayjs(event.start_date), start, setStartValue)
    setFieldInState('end', dayjs(event.end_date), end, setEndValue)
    setEventPicture(event.picture)
    setPublished(published)

    const currentSeatingDetails = []
    for (const i in event.seating_details) {
      const section = event.seating_details[i]
      console.log(section)
      section['availability'] = section.total_seats
      currentSeatingDetails.push(section)
    }
    console.log(currentSeatingDetails)
    setSeatingList(currentSeatingDetails)
    // setPublished(response.published)

    // Set if it is the host who is logged in
    checkIfUser(event.host_id, setIsHost)
  }, [event])


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
  

  const addAdmin = async (e) => {
    console.log(adminList)

    e.stopPropagation();
    e.nativeEvent.stopImmediatePropagation();

    // Check form input
    if (newAdmin.email.length === 0 || !checkValidEmail(newAdmin.email)) {
      setFieldInState('error', true, newAdmin, setNewAdmin)
      setFieldInState('errorMsg', 'Please enter a valid email', newAdmin, setNewAdmin)
      return
    }

    setAdminLoading(true)
    // Check if you are adding yourself to an event or user already is in event
    try {
      const response = await apiFetch('GET',`/api/user/profile?auth_token=${getToken()}`)
      if (newAdmin.email === response.email) {
        setFieldInState('error', true, newAdmin, setNewAdmin)
        setFieldInState('errorMsg', 'Cannot add yourself as admin', newAdmin, setNewAdmin)
        setAdminLoading(false)
        return
      }
    } catch (e) {
      console.log(e)
    }

    // Check if user exists
    try {
      const response = await apiFetch('GET', `/api/user/search?email=${newAdmin.email}`, null)
      console.log(response)
      // Check if user already added as an admin
      if (adminList.includes(response.user_id)) {
        setFieldInState('email', '', newAdmin, setNewAdmin)
        setAdminLoading(false)
        return
      }
      const adminList_t = [...adminList];
      adminList_t.push(response.user_id);
      setAdminList(adminList_t);
      setFieldInState('email', '', newAdmin, setNewAdmin)
      setAdminLoading(false)
    } catch (error) {
      console.log('error')
      setFieldInState('error', true, newAdmin, setNewAdmin)
      setFieldInState('errorMsg', error.reason, newAdmin, setNewAdmin)
      setAdminLoading(false)
    }
  };

  const removeAdmin = (index) => {
    const admin_list = [...adminList];
    admin_list.splice(index, 1);
    setAdminList(admin_list);
  };

  const addSection = (e) => {
    const sectionList = [...seatingList];
    sectionList.push(newSection.stateCopy);
    setSeatingList(sectionList);
    setFieldInState('section', '', newSection, setNewSection)
    setFieldInState('available_seats', 0, newSection, setNewSection)
    setFieldInState('ticket_price', 0, newSection, setNewSection)
    setFieldInState('has_seats', false, newSection, setNewSection)
  };

  const removeSeating = (index) => {
    const list = [...seatingList];
    list.splice(index, 1);
    setSeatingList(list);
  };

  const handleNewHost = async () => {
    console.log(newHost)
    // Send api request for admin
    const body = {
      auth_token: getToken(),
      event_id: params.event_id,
      new_host_email: newHost.email
    }
    try {
      const response = await apiFetch('PUT', '/api/event/make_host', body)
      navigate(`/view_event/${params.event_id}`)
    } catch (e) {
      console.log(e)
    }
  }

  const handleDeleteEvent = async () => {
    try {
      const body = {
        auth_token: getToken(),
        event_id: params.event_id
      }
      const response = await apiFetch('DELETE', '/api/event/cancel', body)
      navigate('/')

    } catch(error) {
      console.log(error)
    }
  }

  React.useEffect(() => {
    if (newAdmin.response) {
    }
  }, [newAdmin.response])

  const saveEvent = async (e) => {
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
    if (suburb.value.length === 0) {
      setFieldInState('error', true, suburb, setSuburb)
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
      return
    }

    // Check seating allocations
    if (seatingList.length === 0) {
      setErrorStatus(true)
      setFieldInState('error', true, newSection, setNewSection)
      setErrorMsg('Please allocate seating')
      return
    }
    

    // Check if a valid street address
    const streetAddress = address.value.split(' ')
    if (streetAddress.length !== 3) {
      setFieldInState('error', true, address, setAddress)
      setErrorStatus(true)
      setErrorMsg('Please enter a valid street address. "{Street No} {Street Name}"')
      return
    }
    if (isNaN(parseInt(streetAddress[0]))) {
      setFieldInState('error', true, address, setAddress)
      setErrorStatus(true)
      setErrorMsg('Please enter a valid street number. "{Street No} {Street Name}"')
      return
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
      setErrorMsg('End date must be after start date')
    }

    const locationBody = {
      street_no: +streetAddress[0],
      street_name: streetAddress[1] + ' ' + streetAddress[2],
      unitNo: '',
      suburb: suburb.value,
      postcode: postcode.value,
      state: state.value,
      country: country.value,
      longitude: '',
      latitude: ''
    };

    const body = {
      auth_token: getToken(),
      event_id: params.event_id,
      event_name: eventName.value,
      picture: newPhoto ? eventPicture : null,
      location: locationBody,
      start_date: start.start.toISOString(),
      end_date: end.end.toISOString(),
      description: description.value,
      seating_details: seatingList,
      categories: [],
      tags: [],
      admins: adminList,
      published:published,
    };

    

    try {
      const response = await apiFetch('PUT', '/api/event/edit', body)
      console.log(response)
      navigate(`/view_event/${params.event_id}`)
    } catch (error) {
      console.log(error)
    }
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
          boxShadow: 5,
        }}
      >
        {(eventName.value !== '')
          ? <>
            <Grid 
              container
              spacing={2}
              sx={{
                marginLeft: 1,
                marginRight: 5,
              }}
            >
              <Grid item xs={3} sx={{p: 2}}>
                {isHost
                  ? <FormInput>
                      <CentredBox>
                        <DeleteButton variant="contained" sx={{textTransform: "none", textAlign: "left",  width: 200}} startIcon={<DeleteIcon/>} onClick={()=>setOpenDeleteMenu(true)}>
                          Delete Event
                        </DeleteButton>
                      </CentredBox>
                    </FormInput>
                  : <></>
                }
              </Grid>
              <Grid item xs={6}>
                <H3 sx={{ fontSize: "30px" }}>Edit Event</H3>
              </Grid>
              <Grid item xs={3}>
                <Box sx={{display: 'flex', justifyContent: 'flex-end', pr: 2, height: '100%', alignItems: 'center'}}>
                  {isHost
                    ? <FormGroup sx={{alignItems: 'right', justifyContent:'flex-end'}}>
                        <FormControlLabel sx={{alignItems: 'right', justifyContent:'flex-end', pr: 0}} control={<Checkbox disabled={(event.published)} checked={(event.published)} sx={{ '& .MuiSvgIcon-root': { fontSize: 28 } }}/>} label="Published" onChange={(e) => setPublished(!published)}/>
                        {!event.published
                          ? <FormHelperText>Once published an event cannot be unpublished</FormHelperText>
                          : <></>
                        }
                      </FormGroup>
                    : <FormGroup sx={{alignItems: 'right', justifyContent:'flex-end'}}>
                        <FormControlLabel sx={{alignItems: 'right', justifyContent:'flex-end'}} control={<Checkbox disabled={true} checked={(event.published)} sx={{ '& .MuiSvgIcon-root': { fontSize: 28 } }}/>} label="Published" />
                        {!event.published
                          ? <FormHelperText>Only the host can publish an event</FormHelperText>
                          : <></>
                        }
                      </FormGroup>
                  }        
                </Box>
              </Grid>
            </Grid>
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
                  <ContrastInputWrapper>
                      <CentredBox
                        sx={{
                          height: 200,
                          borderRadius: 5,
                        }}
                        onMouseOver={() => {
                          setToggleUpload(false);
                        }}
                        onMouseOut={() => {
                          setToggleUpload(true);
                        }}
                      > 
                        {!toggleUpload
                          ? <Button 
                              sx={{
                                backgroundColor: "#92C5DD",
                                "&:hover": {
                                  backgroundColor: "#73B5D3"
                                },
                                color: 'white'
                              }}
                              variant="contained"
                              component="label"
                              startIcon={<PhotoCamera/>
                            }>
                              Upload Event Photo
                              <input
                                hidden
                                accept="image/*"
                                multiple
                                type="file" 
                                onChange={async (e) => {
                                  const image = await fileToDataUrl(e.target.files[0])
                                  setEventPicture(image)
                                  setNewPhoto(true)
                                  console.log("uploaded image")
                                }}
                              />
                            </Button>
                          : <>
                              {(eventPicture === '')
                                ? <h3>
                                    Event cover photo
                                  </h3>
                                : <UploadPhoto src={eventPicture}/>
                              }
                            </>                      
                        }
                      </CentredBox>
                    </ContrastInputWrapper>
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
                      <Grid item xs={8}>
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
                      <Grid tiem xs={4}>
                        <ShadowInput 
                          state={suburb}
                          sx={{
                            '.MuiOutlinedInput-notchedOutline': {
                              borderColor: suburb.error ? "red" : "rgba(0,0,0,0)"
                            },
                          }}
                          setState={setSuburb}
                          defaultValue={suburb.value}
                          field='value'
                          placeholder="Suburb"
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
                      {isHost
                        ? <>
                            <Grid item xs={8}>
                              <FormControl fullWidth={true}>
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
                              <CentredBox sx={{position: 'relative'}}>
                                <TkrButton
                                  variant="contained"
                                  disabled={(adminLoading || (newAdmin.email.length <= 0))}
                                  sx={{mt: 1, fontSize: 15}}
                                  onClick={addAdmin}
                                  startIcon={<AddIcon/>}
                                >
                                  Add Admin
                                </TkrButton>
                                {adminLoading && (
                                  <CircularProgress 
                                    size={24}
                                    sx={{
                                      color: "#AE759F",
                                      position: 'absolute',
                                      top: '50%',
                                      left: '50%',
                                      marginTop: '-12px',
                                      marginLeft: '-12px',
                                    }}
                                  />
                                )}
                              </CentredBox>
                            </Grid>
                            <Grid item xs={7}>
                              <AdminsBar editable={true} adminsList={adminList} removeAdmin={removeAdmin} editEvent={true} openHostMenu={setNewHostMenu} setNewHost={setNewHost}/>
                            </Grid>
                          </>
                        : <>
                            <Grid item xs={7}>
                              <br/>
                              <Typography>
                                Host and Admins
                              </Typography>
                              <Divider/>
                              <AvatarGroup max={5} sx={{flexDirection: 'row', pt:2}}>
                                {event.admins.map((value, key) => {
                                  return (
                                    <UserAvatar key={key} userId={value} size={35}/>
                                  );
                                })}
                                <UserAvatar userId={event.host_id} size={35} host={true}/>
                              </AvatarGroup>
                              <br/>
                            </Grid>
                          </>

                      }
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
                    {!event.published
                      ? <Box>
                          <h3> Ticket Allocations </h3>
                          <Grid container spacing={2}>
                            <Grid item xs={3}>
                              <Typography sx={{fontWeight: 'bold'}}>
                                Section
                              </Typography>
                              <Divider/>
                            </Grid>
                            <Grid item xs={3}>
                              <Typography sx={{fontWeight: 'bold'}}>
                                Availability
                              </Typography>
                              <Divider/>
                            </Grid>
                            <Grid item xs={3}>
                              <Typography sx={{fontWeight: 'bold'}}>
                                Cost
                              </Typography>
                              <Divider/>
                            </Grid>
                            <Grid item xs={2}>
                              <Typography sx={{fontWeight: 'bold'}}>
                                Seating
                              </Typography>
                              <Divider/>
                            </Grid>
                            {seatingList.map((value, index) => {
                              return (
                                <Grid item key={index} sx={{width: '100%'}}>
                                  <ContrastInputWrapper>
                                    <Grid container spacing={1}>
                                      <Grid item xs={3}>
                                        <Box sx={{display: 'flex', alignItems:'center', height: '100%', width: '100%'}}>
                                          <Typography
                                            sx={{
                                              fontWeight: 'bold',
                                            }}
                                          >
                                            {value.section}
                                          </Typography>
                                        </Box>
                                      </Grid>
                                      <Grid item xs={3}>
                                        <Box sx={{display: 'flex', alignItems:'center', height: '100%', width: '100%'}}>
                                          <Typography
                                            sx={{
                                              fontWeight: 'bold',
                                              width: '100%'
                                            }}
                                          >
                                            {value.available_seats}
                                          </Typography>
                                        </Box>
                                      </Grid>
                                      <Grid item xs={3}>
                                        <Box sx={{display: 'flex', alignItems:'center', height: '100%', width: '100%'}}>
                                          <Typography
                                            sx={{
                                              fontWeight: 'bold',
                                              width: '100%'
                                            }}
                                          >
                                            ${value.ticket_price}
                                          </Typography>
                                        </Box>
                                      </Grid>
                                      <Grid item xs={2}>
                                        <Box sx={{height: "100%", width: '100%', display: 'flex', justifyContent: 'center', alignItems: 'center'}}>
                                          <Checkbox disabled checked={value.has_seats}/>
                                        </Box>
                                      </Grid>
                                      <Grid item xs={1}>
                                        <Box sx={{height: "100%", width: '100%', display: 'flex', justifyContent: 'center', alignItems: 'center'}}>
                                          <IconButton
                                            edge="end"
                                            aria-label="delete"
                                            onClick={() => removeSeating(index)}
                                            sx={{marginRight: 0}}
                                          >
                                            <DeleteIcon />
                                          </IconButton>
                                        </Box>
                                      </Grid>
                                    </Grid>
                                  </ContrastInputWrapper>
                                </Grid>
                              );
                            })}
                          </Grid>
                          <Box sx={{marginRight: 4, width: '100%'}}>
                            <Grid container spacing={1}>
                              <Grid item xs={3}>
                                <ContrastInputWrapper>
                                  <ContrastInput
                                    placeholder={'Section'}
                                    fullWidth 
                                    onChange={(e) => {
                                      setFieldInState('section', e.target.value, newSection, setNewSection)
                                      setFieldInState('error', false, newSection, setNewSection)
                                      setErrorStatus(false)
                                    }}
                                    sx={{
                                      '.MuiOutlinedInput-notchedOutline': {
                                        borderColor: newSection.error ? "red" : "rgba(0,0,0,0)"
                                      },
                                    }}
                                    value = {newSection.section}
                                  />
                                </ContrastInputWrapper>
                              </Grid>
                              <Grid item xs={3}>
                                <ContrastInputWrapper>
                                  <ContrastInput 
                                    type="number"
                                    placeholder="Spots"
                                    fullWidth 
                                    onChange={(e) => {
                                      const val = e.target.value
                                      if (val < 0) {
                                        setFieldInState('available_seats', 0, newSection, setNewSection)
                                      } else {
                                        setFieldInState('available_seats', val, newSection, setNewSection)
                                      } 
                                      setFieldInState('error', false, newSection, setNewSection)
                                      setErrorStatus(false)
                                    }}
                                    sx={{
                                      '.MuiOutlinedInput-notchedOutline': {
                                        borderColor: newSection.error ? "red" : "rgba(0,0,0,0)"
                                      },
                                    }}
                                    value = {newSection.available_seats}
                                  />
                                </ContrastInputWrapper>
                              </Grid>
                              <Grid item xs={3}>
                                <ContrastInputWrapper>
                                  <ContrastInput 
                                    type="number"
                                    placeholder="Cost"
                                    fullWidth 
                                    onChange={(e) => {
                                      const val = e.target.value
                                      if (val < 0) {
                                        setFieldInState('ticket_price', 0, newSection, setNewSection)
                                      } else {
                                        setFieldInState('ticket_price', val, newSection, setNewSection)
                                      } 
                                      setFieldInState('error', false, newSection, setNewSection)
                                      setErrorStatus(false)
                                    }}
                                    sx={{
                                      '.MuiOutlinedInput-notchedOutline': {
                                        borderColor: newSection.error ? "red" : "rgba(0,0,0,0)"
                                      },
                                    }}
                                    startAdornment={<InputAdornment position="start">$</InputAdornment>}
                                    value = {newSection.ticket_price}
                                  />
                                </ContrastInputWrapper>
                              </Grid>
                              <Grid item xs={2}>
                                <ContrastInputWrapper sx={{ height: '100%'}}>
                                  <CentredBox sx={{ height: '100%'}}>
                                    <Checkbox
                                      checked={newSection.has_seats}
                                      onChange={(e) => {
                                        setFieldInState('has_seats', e.target.checked, newSection, setNewSection)
                                        setFieldInState('error', false, newSection, setNewSection)
                                        setErrorStatus(false)
                                      }}
                                    />
                                  </CentredBox>
                                </ContrastInputWrapper>
                              </Grid>
                              <Grid item xs={1}>
                                <ContrastInputWrapper 
                                  sx={{
                                    height: "100%",
                                    width: '100%',
                                    display: 'flex',
                                    justifyContent: 'center',
                                    alignItems: 'center',
                                    backgroundColor: ((newSection.section.length > 0) && (newSection.available_seats > 0)) ? alpha('#6A7B8A', 0.3) : "rgba(0, 0, 0, 0.08)",
                                    '&:hover': {
                                      backgroundColor: ((newSection.section.length > 0) && (newSection.available_seats > 0)) ? alpha('#6A7B8A', 0.5): "rgba(0, 0, 0, 0.08)",
                                    },
                                  }}
                                >
                                  <IconButton
                                    edge="end"
                                    onClick={addSection}
                                    sx={{marginRight: 0}}
                                    disabled = {((newSection.section.length === 0)|| (newSection.available_seats === 0))}
                                  >
                                    <AddIcon/>
                                  </IconButton>
                                </ContrastInputWrapper>
                              </Grid>
                            </Grid>
                          </Box>
                        </Box>
                      : <Box>
                          <h3> Ticket Allocations </h3>
                          <Grid container spacing={2}>
                            <Grid item xs={4}>
                              <Typography sx={{fontWeight: 'bold'}}>
                                Section
                              </Typography>
                              <Divider/>
                            </Grid>
                            <Grid item xs={3}>
                              <Typography sx={{fontWeight: 'bold'}}>
                                Availability
                              </Typography>
                              <Divider/>
                            </Grid>
                            <Grid item xs={3}>
                              <Typography sx={{fontWeight: 'bold'}}>
                                Cost
                              </Typography>
                              <Divider/>
                            </Grid>
                            <Grid item xs={2}>
                              <Typography sx={{fontWeight: 'bold'}}>
                                Seats
                              </Typography>
                              <Divider/>
                            </Grid>
                            {seatingList.map((value, index) => {
                              return (
                                <Grid item key={index} sx={{width: '100%'}}>
                                  <ContrastInputWrapper>
                                    <Grid container spacing={1}>
                                      <Grid item xs={4}>
                                        <Box sx={{display: 'flex', alignItems:'center', height: '100%'}}>
                                          <Typography
                                            sx={{
                                              fontWeight: 'bold',
                                            }}
                                          >
                                            {value.section}
                                          </Typography>
                                        </Box>
                                      </Grid>
                                      <Grid item xs={3}>
                                        <Box sx={{display: 'flex', alignItems:'center', height: '100%'}}>
                                          <Typography
                                            sx={{
                                              fontWeight: 'bold',
                                            }}
                                          >
                                            {value.available_seats}
                                          </Typography>
                                        </Box>
                                      </Grid>
                                      <Grid item xs={3}>
                                        <Box sx={{display: 'flex', alignItems:'center', height: '100%'}}>
                                          <Typography
                                            sx={{
                                              fontWeight: 'bold',
                                            }}
                                          >
                                            ${value.ticket_price}
                                          </Typography>
                                        </Box>
                                      </Grid>
                                      <Grid item xs={2}>
                                        <Box sx={{display: 'flex', alignItems:'center', height: '100%'}}>
                                          <Checkbox checked={value.has_seats} disabled/>
                                        </Box>
                                      </Grid>
                                    </Grid>
                                  </ContrastInputWrapper>
                                </Grid>
                              );
                            })}
                          </Grid>
                      </Box>
                    }
                    <br/>
                    <Box>
                      <h3> Tags </h3>
                      <TagsBar tags={tags} setTags={setTags} editable={true}/>
                    </Box>
                  </Grid>
                </Grid>
              </EventForm>
              <Grid container spacing={2} sx={{width: '100%', pr: 5, pl: 5}}>
                <Grid item xs={3}>
                  <FormInput>
                    <CentredBox>
                      <TkrButton2 variant="contained" onClick={() => navigate(`/view_event/${params.event_id}`)} startIcon={<DeleteIcon/>} sx={{textTransform: "none", textAlign: "left"}}>
                        <Typography
                          sx={{
                            fontSize: "20px"
                          }}
                        >
                          Discard Changes
                        </Typography>
                      </TkrButton2>
                    </CentredBox>
                  </FormInput>
                </Grid>
                <Grid item xs={6}>
                </Grid> 
                <Grid item xs={3}>
                  <FormInput>
                    <CentredBox>
                      <TkrButton variant="contained" onClick={saveEvent} startIcon={<SaveIcon/>} sx={{textTransform: "none", textAlign: "left", width: 200}}>
                        Save Changes
                      </TkrButton>
                    </CentredBox>
                    <Collapse in={errorStatus}>
                      <Alert severity="error">{errorMsg}.</Alert>
                    </Collapse>
                  </FormInput>
                </Grid>
              </Grid>
              <Box sx={{display: 'flex', justifyContent: 'space-between'}}>
              </Box>
            </div>
          </> 
        : <div></div>
        }
      </Box>
      <Backdrop open={newHostMenu} onClick={() => setNewHostMenu(false)}>
        <Box sx={{width: 400, backgroundColor: "#FFFFFF", borderRadius: 2, p: 5}}>
          <H3
            sx={{
              fontSize: '30px',
              color: 'black',
              mb: 2,
            }}
          >
            Make {newHost.firstName} {newHost.lastName} the host of this event?
          </H3>
          <Typography
            sx={{
              fontSize: '15px',
              color: 'black',
              textAlign: 'center',
              mb: 2,
            }}
          >
            *This cannot be reversed
          </Typography>
          <Divider/>
          <br/>
          <CentredBox>
            <TkrButton onClick={handleNewHost}>
              Confirm
            </TkrButton>
          </CentredBox>
        </Box>
      </Backdrop>
      <Backdrop open={openDeleteMenu} onClick={() => setOpenDeleteMenu(false)}>
        <Box sx={{width: 400, backgroundColor: "#FFFFFF", borderRadius: 2, p: 5}}>
          <H3
            sx={{
              fontSize: '30px',
              color: 'black',
              mb: 2,
            }}
          >
            Delete Event?
          </H3>
          <Typography
            sx={{
              fontSize: '15px',
              color: 'black',
              textAlign: 'center',
              mb: 2,
            }}
          >
            *This cannot be reversed
          </Typography>
          <Divider/>
          <br/>
          <CentredBox>
            <TkrButton onClick={handleDeleteEvent}>
              Confirm
            </TkrButton>
          </CentredBox>
        </Box>
      </Backdrop>
    </BackdropNoBG>
  );
}
