import React from 'react'

import { BackdropNoBG, CentredBox, H3  } from '../Styles/HelperStyles'
import Grid from '@mui/material/Grid';

import Header from '../Components/Header'
import { Box, fontStyle } from '@mui/system';
import { Avatar, Button, CircularProgress, Collapse, Divider, OutlinedInput, Typography } from '@mui/material';
import EmailIcon from '@mui/icons-material/Email';
import EditIcon from '@mui/icons-material/Edit';
import SaveIcon from '@mui/icons-material/Save';
import DeleteIcon from '@mui/icons-material/Delete';
import Tooltip from '@mui/material/Tooltip';
import { ContrastInput, ContrastInputWrapper, DeleteButton, TextButton, TkrButton } from '../Styles/InputStyles';
import { setFieldInState, getToken, getUserData, loggedIn } from '../Helpers';
import ShadowInput from '../Components/ShadowInput';
import LinearProgress from '@mui/material/LinearProgress';
import FormLabel from '@mui/material/FormLabel';
import FormControl from '@mui/material/FormControl';
import FormGroup from '@mui/material/FormGroup';
import FormControlLabel from '@mui/material/FormControlLabel';
import Checkbox from '@mui/material/Checkbox';
import ConfirmPassword from '../Components/ConfirmPassword';

import { Link } from 'react-router-dom';

export default function Profile({editable = false, id=null}){

  const [editMode, setEditMode] = React.useState(false)

  const [profile, setProfile] = React.useState({
    userName: '',
    firstName: '',
    lastName: '',
    email: '',
    profilePicture: '',
    events: []
  })

  const [notifications, setNotification] = React.useState({
    email: false,
  });

  const [changePW, setChangePW] = React.useState(false)

  const [loading, setLoading] = React.useState(false)

  const handleChangePW = (e) => {
    setChangePW(!changePW)
  }
  
  const handleChangePWClose = () => {
    setChangePW(false);
    setLoading(false)
  };

  React.useEffect(() => {
    if (editable) {
      console.log('Getting user data from profile page')
      getUserData(`auth_token=${getToken()}`, setProfile)
    } else {
      console.log('Getting user data from profile page')
      getUserData(`auth_token=${getToken()}`, setProfile)
    }
    
  }, [editMode])

  const editModeChange = (e) => {
    e.stopPropagation()
    e.nativeEvent.stopImmediatePropagation()
    setEditMode(!editMode)
  }
  
  const saveChanges = (e) => {
    e.stopPropagation()
    e.nativeEvent.stopImmediatePropagation()
    setEditMode(!editMode)
  }

  const discardChanges = (e) => {
    e.stopPropagation()
    e.nativeEvent.stopImmediatePropagation()
    setEditMode(!editMode)
  }

  const notificationChange = (e) => {
    setNotification({...notificationChange, [e.target.name]: e.target.checked})
  }

  const submitPasswordChange = (e) => {
    setLoading(true)
  }

  const [delAcc, setDelAcc] = React.useState(false)

  const handleDelAcc = (e) => {
    setDelAcc(!delAcc)
  }

  return (
    <div>
      <BackdropNoBG >
        <Header/>
        <Box
          sx={{
            minHeight: 600,
            maxWidth: 1500,
            marginLeft: 'auto',
            marginRight: 'auto',
            width: '95%',
            backgroundColor: '#FFFFFF',
            marginTop: '50px',
            borderRadius: '15px',
            boxShadow: editMode ? '5' : 0,
            paddingBottom: 5,
          }}
        >
          {(profile.userName == '')
            ? <Box sx={{height: '100%', display: 'flex', justifyContent: 'center', alignItems: 'center', margin: '50px', paddingTop: '100px'}}> 
                <Box sx={{ width: '90%', hieght: 50}}>
                  <LinearProgress color='secondary'/>
                </Box>
              </Box>
            : <Grid container spacing={2}>
                <Grid item xs={4}>
                  <Box 
                    sx={{
                      height: '100%',
                      marginTop: '10px',
                      marginLeft: '30px',
                      display: 'flex',
                      alignItems: 'flex-start',
                      justifyContent: 'center',
                    }}
                  >
                    <CentredBox
                      sx={{
                        width: '300px',
                        height: '300px',
                        borderRadius: '15px',
                      }}
                    >
                      <Avatar sx={{width: 300, height: 300, fontSize: 100}}>
                        {profile.firstName[0]}{profile.lastName[0]}
                      </Avatar>
                    </CentredBox>
                  </Box>
                </Grid>
                <Grid item xs={6}>
                  {editMode
                    ? <CentredBox sx={{justifyContent: 'flex-start', gap: '10px', height: '60px', alignItems: 'baseline'}}>
                        <Grid container spacing={2}>
                          <Grid item xs={4}>
                            <ShadowInput sx={{fontWeight: 'bold'}} state={profile} setState={setProfile} defaultValue={profile.firstName} field='firstName'/>
                          </Grid>
                          <Grid item xs={4}>
                            <ShadowInput sx={{fontWeight: 'bold'}} state={profile} setState={setProfile} defaultValue={profile.lastName} field='lastName'/>
                          </Grid>
                          <Grid item xs={3}>
                            <Box sx={{display: 'flex', height: '100%', alignItems: 'flex-end'}}>
                              <ContrastInputWrapper>
                                <ContrastInput size='small' defaultValue={profile.userName} startAdornment={<div>@</div>} fullWidth onChange={(e) => {
                                  setFieldInState('userName', e.target.value, profile, setProfile)
                                }}/>
                              </ContrastInputWrapper>
                            </Box>
                          </Grid>
                        </Grid>
                      </CentredBox>
                    : <CentredBox sx={{justifyContent: 'flex-start', gap: '10px', height: '60px', alignItems: 'baseline'}}>
                        <Typography 
                          sx={{
                            fontSize: '40px',
                            fontWeight: 'bold'
                          }}
                        >
                          {profile.firstName} {profile.lastName}
                        </Typography>
                        <Divider orientation="vertical" variant="middle" flexItem/>
                        <Typography 
                          sx={{
                            fontSize: '20px',
                            fontWeight: 'light',
                            fontStyle: 'italic',
                            color: '#454545',
                          }}
                        >
                          @{profile.userName}
                        </Typography>
                      </CentredBox>
                  }
                  <Divider/>
                  <br/>
                  <Box 
                    sx={{
                      height: '100%',
                    }}
                  >
                    {editMode
                      ? <Box
                          sx={{
                            padding: editMode ? 0 : '10px',
                            borderRadius: '10px',
                            backgroundColor: '#F1F9F9',
                          }}
                        > 
                          <ContrastInputWrapper>
                              <ContrastInput multiline placeholder={'Enter a description'} rows={4} defaultValue={profile.profileDescription} fullWidth onChange={(e) => {
                                  setFieldInState('profileDescription', e.target.value, profile, setProfile)
                                }}/>
                            </ContrastInputWrapper>
                        </Box>
                      : <div>
                          {(profile.profileDescription != '')
                            ? <Box
                                sx={{
                                  padding: editMode ? 0 : '10px',
                                  borderRadius: '10px',
                                  backgroundColor: '#F1F9F9',
                                }}
                              > 
                                <Typography
                                  sx={{
                                    fontSize: '20px',
                                    fontWeight: 'regular',
                                    fontStyle: 'italic',
                                  }}
                                >
                                  {profile.profileDescription}
                                </Typography>
                                </Box>
                            : <div></div>
                          }
                        </div>
                    }
                    <br/>
                    <Grid container spacing={2}>
                      <Grid item xs={3} sx={{display: 'flex', alignItems: 'center'}}>
                        <CentredBox sx={{gap: '5px', justifyContent: 'flex-start'}}>
                          <EmailIcon sx={{color: '#AE759F'}}/>
                          <Typography
                            sx={{
                              fontSize: '20px',
                              fontWeight: 'bold',
                              color: '#AE759F',
                            }}
                          >
                            Email
                          </Typography>
                        </CentredBox>
                      </Grid>
                      <Grid item xs={9}>
                        {editMode
                          ? <ShadowInput state={profile} setState={setProfile} defaultValue={profile.email} field='email'/>
                          : <Typography
                              sx={{
                                fontSize: '20px',
                                fontWeight: 'regular'
                              }}
                            >
                              {profile.email}
                            </Typography>
                        }
                      </Grid>
                    </Grid>
                    <br/>
                  </Box>
                </Grid>
                <Grid item xs={2}>
                  <Box
                    sx={{
                      display: 'flex',
                      paddingLeft: '15px',
                      marginTop: '20px'
                    }}
                  >
                    {editMode
                      ? <TkrButton variant='text' startIcon={<SaveIcon/>} sx={{height: 30, width: 90, fontSize: 20, textTransform: "none", textAlign: "left"}} onClick={saveChanges}>
                          Save
                        </TkrButton>
                      : <TkrButton variant='text' startIcon={<EditIcon/>} sx={{height: 30, width: 90, fontSize: 20, textTransform: "none", textAlign: "left"}} onClick={editModeChange}>
                          Edit
                        </TkrButton>
                    }
                  </Box>
                  {editMode
                    ? <Box
                        sx={{
                          display: 'flex',
                          paddingLeft: '15px',
                          marginTop: '20px'
                        }}
                      >
                        <Tooltip title="Discard changes">
                          <DeleteButton variant='text' startIcon={<DeleteIcon/>} sx={{height: 30, width: 90, fontSize: 15, textTransform: "none", textAlign: "left"}} onClick={discardChanges}>
                            Discard
                          </DeleteButton>
                        </Tooltip>
                      </Box>
                    : <div></div>
                  }
                </Grid>
                <Grid item xs={4}></Grid>
                <Grid item xs={6}>
                  {editable
                    ? <Box sx={{}}>
                        <Typography
                          sx={{
                            fontSize: '20px',
                            fontWeight: 'regular'
                          }}
                        > Account Settings</Typography>
                        <Divider/>
                        <FormControl sx={{paddingTop: 2, width: 250}} component="fieldset" variant="standard">
                          <FormLabel sx={{"&.Mui-focused": {color: 'rgba(0, 0, 0, 0.6) '}}}>Notification</FormLabel>
                          <Divider variant="fullWidth"/>
                          <FormGroup>
                            <FormControlLabel
                              control={
                                <Checkbox  name="email" checked={notifications.email} onChange={notificationChange}/>
                              }
                              label="Enable email notifications"
                            />
                          </FormGroup>
                        </FormControl>
                        <br/>
                        <FormControl sx={{paddingTop: 2}} component="fieldset" variant="standard">
                          <FormLabel sx={{"&.Mui-focused": {color: 'rgba(0, 0, 0, 0.6) '}}}>Password Management</FormLabel>
                          <Divider variant="fullWidth"/>
                          <Box sx={{paddingTop: 1, width: 250}}>
                            <TkrButton variant='text' sx={{height: 30, fontSize: 20, textTransform: "none"}} onClick={handleChangePW} component={Link} to="/change_password">Change Password</TkrButton>
                          </Box>
                        </FormControl>
                        <br/>
                        <FormControl sx={{paddingTop: 2, width: 250}} component="fieldset" variant="standard">
                          <FormLabel sx={{"&.Mui-focused": {color: 'rgba(0, 0, 0, 0.6) '}}}>Account Management</FormLabel>
                          <Divider variant="fullWidth"/>
                          <Box sx={{paddingTop: 1}}>
                            <DeleteButton 
                              variant='text'
                              sx={{
                                height: 30,
                                fontSize: 20, 
                                textTransform: "none", 
                                textAlign: "left", 
                                "&:hover": {
                                  backgroundColour: "#AA4344"
                                }
                              }} 
                              startIcon={<DeleteIcon/>}
                              onClick = {handleDelAcc}
                            >
                              Delete Account
                            </DeleteButton>
                            <ConfirmPassword open={delAcc} handleOpen={handleDelAcc}/>
                          </Box>
                        </FormControl>
                      </Box>
                    : <div></div>
                  }
                </Grid>
                <Grid item xs={2}></Grid>
              </Grid>
          }
        </Box>
        
      </BackdropNoBG>
    </div>
  )
}