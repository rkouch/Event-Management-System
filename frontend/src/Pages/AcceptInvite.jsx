import { Typography } from '@mui/material'
import { Box } from '@mui/system'
import React from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import Header from '../Components/Header'
import StandardLogo from '../Components/StandardLogo'
import { apiFetch, getToken, loggedIn } from '../Helpers'
import { Backdrop, BackdropNoBG, CentredBox } from '../Styles/HelperStyles'
import { DeleteButton, TkrButton } from '../Styles/InputStyles'

import '../App.css';

export default function AcceptInvite({}) {
  const params = useParams()
  const navigate = useNavigate()
  const inviteId = params.invite_id
  
  const [accepted, setAccepted] = React.useState(false)
  const [reserveId, setReserveId] = React.useState('')

  React.useEffect(() => {
    if (!loggedIn()) {
      navigate(`/invite/login/${inviteId}`)
    }
  },[])

  React.useEffect(() => {
    if (accepted) {

    }
  }, [accepted])

  const handleAccept = async () => {
    const body = {
      auth_token: getToken(),
      invite_id: inviteId
    }

    try {
      const response = await apiFetch('POST', '/api/group/accept', body)
      setReserveId(response.reserve_id)
      setAccepted(true)
    } catch (e) {
      console.log(e)
    }
  }

  const handleDecline = async () => {
    const body = {
      invite_id: inviteId
    }
    try {
      const response = await apiFetch('POST', '/api/group/deny')
      navigate('/')
    } catch (e) {
      console.log(e)
    }
  }

  return (
    <Box>
      {!accepted
        ? <Backdrop>
            <StandardLogo/>
            <Box sx={{height: '100%', display: 'flex', alignItems: 'center'}}>
              <Box
                sx={{
                  width: 800,
                  marginLeft: "auto",
                  marginRight: "auto",
                  backgroundColor: "#FFFFFF",
                  marginTop: "50px",
                  borderRadius: 3,
                  paddingBottom: 5,
                  paddingTop: 1,
                  boxShadow: 3
                }}
              >
                <Typography sx={{fontSize: 50, fontFamily: 'Segoe UI', textAlign: 'center'}}>
                  Accept invite to join group?
                </Typography>
                <Box sx={{pt: 5, pl: 30, pr: 30, display: 'flex', justifyContent: 'space-between'}}>
                  <DeleteButton onClick={handleDecline}>Decline</DeleteButton>
                  <TkrButton onClick={handleAccept}>Accept</TkrButton>
                </Box>
              </Box>
            </Box>
          </Backdrop> 
        : <BackdropNoBG>
            <Header/>
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
              
            </Box>
          </BackdropNoBG>
        }
    </Box>
  )
}