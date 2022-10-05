import React from 'react'

import { BackdropNoBG, CentredBox } from '../Styles/HelperStyles'
import Grid from '@mui/material/Grid';

import Header from '../Components/Header'
import { Box, fontStyle } from '@mui/system';
import { Avatar, Button, Divider, OutlinedInput, Typography } from '@mui/material';
import EmailIcon from '@mui/icons-material/Email';
import EditIcon from '@mui/icons-material/Edit';
import SaveIcon from '@mui/icons-material/Save';
import { ContrastInput, ContrastInputWrapper, TkrButton } from '../Styles/InputStyles';

export default function Profile({isEditable = false}){
  const [editMode, setEditMode] = React.useState(false)

  const [profile, setProfile] = React.useState({
    userName: '',
    firstName: '',
    lastName: '',
    email: '',
    profilePicture: '',
    profileDescription: ''
  })

  const editModeChange = (e) => {
    e.stopPropagation()
    e.nativeEvent.stopImmediatePropagation()
    setEditMode(!editMode)
    console.log(editMode)
  }

  // Test data
  React.useEffect(() => {
    setProfile({
      userName: 'TestUser',
      firstName: 'Kennan',
      lastName: 'Wong',
      email: 'test@email.com',
      profilePicture: '',
      profileDescription: 'This is a profiles description'
    })
  }, [])

  return (
    <div>
      <BackdropNoBG >
        <Header/>
        <Box
          sx={{
            minHeight: 600,
            backgroundColor: '#FFFFFF',
            marginTop: '50px',
            marginLeft: '50px',
            marginRight: '50px',
            borderRadius: '15px',
            boxShadow: editMode ? '5' : 0,
          }}
        >
          <Grid container spacing={2}>
            <Grid item xs={4}>
              <Box 
                sx={{
                  height: '100%',
                  marginTop: '10px',
                  marginLeft: '30px',
                  display: 'flex',
                  alignItems: 'flex-start',
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
                        <ContrastInputWrapper>
                          <ContrastInput sx={{fontWeight: 'bold'}} defaultValue={profile.firstName}/>
                        </ContrastInputWrapper>
                      </Grid>
                      <Grid item xs={4}>
                        <ContrastInputWrapper>
                          <ContrastInput sx={{fontWeight: 'bold'}} defaultValue={profile.lastName}/>
                        </ContrastInputWrapper>
                      </Grid>
                      <Grid item xs={3}>
                        <Box sx={{display: 'flex', height: '100%', alignItems: 'flex-end'}}>
                          <ContrastInputWrapper>
                            <ContrastInput size='small' defaultValue={profile.userName} startAdornment={<div>@</div>} fullWidth/>
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
              <Divider></Divider>
              <br/>
              <Box 
                sx={{
                  height: '100%',
                }}
              >
                <Box
                  sx={{
                    padding: editMode ? 0 : '10px',
                    borderRadius: '10px',
                    backgroundColor: '#F1F9F9',
                  }}
                >
                  {editMode
                    ? <ContrastInputWrapper>
                        <ContrastInput multiline rows={4} defaultValue={profile.profileDescription} fullWidth/>
                      </ContrastInputWrapper>
                    : <Typography
                        sx={{
                          fontSize: '20px',
                          fontWeight: 'regular',
                          fontStyle: 'italic',
                        }}
                      >
                        {profile.profileDescription}
                      </Typography>
                  }
                </Box>
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
                      ? <ContrastInputWrapper>
                          <ContrastInput fullWidth defaultValue={profile.email}/>
                        </ContrastInputWrapper>
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
                  ? <TkrButton variant='text' startIcon={<SaveIcon/>} sx={{height: 30, width: 90, fontSize: 20, textTransform: "none", textAlign: "left"}} onClick={editModeChange}>
                      Save
                    </TkrButton>
                  : <TkrButton variant='text' startIcon={<EditIcon/>} sx={{height: 30, width: 90, fontSize: 20, textTransform: "none", textAlign: "left"}} onClick={editModeChange}>
                      Edit
                    </TkrButton>
                }
                
              </Box>
            </Grid>
          </Grid>
        </Box>
      </BackdropNoBG>
    </div>
  )
}