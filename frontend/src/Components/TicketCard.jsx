import { Divider, Skeleton, Typography } from '@mui/material'
import { Box } from '@mui/system'
import React from 'react'
import { CentredBox, UploadPhoto } from '../Styles/HelperStyles'
import dayjs from 'dayjs'
import { getEventData, getToken, getUserData } from '../Helpers'

export default function TicketCard({event, ticket_id}) {
  
  const [ticketDetails, setTicketDetails] = React.useState({})

  const testTicketData = {
    event_id: '77ff1d93-e3c2-4d44-9ced-6375de70048b',
    section: 'A',
    seat_num: 10,
    user_id: 'dfc689c3-e989-441a-9d1f-4ad629e929a9'
  }

  const [sectionSeating, setSectionSeating] = React.useState(false)

  const [userData, setUserData] = React.useState(null)

  const getTicketData = async () => {
    // Send API call to get ticket details
    setTicketDetails(testTicketData)
    getUserData(`auth_token=${getToken()}`, setUserData)
    event.seating_details.forEach(function(section) {
      if (section.section === ticketDetails.section) {
        setSectionSeating(section.has_seats)
        return
      }
    })
  }

  React.useEffect(()=> {
    if (event.seating_details != null) {
      getTicketData()  
    }
  },[event])


  return (
    <Box sx={{boxShadow: 5, backgroundColor: '#FFFFFFF', m: 1, p: 3, borderRadius: 1}}>
      {(userData === null)
        ? <>
            <CentredBox sx={{flexDirection: 'column', width: '100%'}}>
              <Skeleton variant="rounded" width={560} height={400}/>
              <Skeleton variant="text"  width={360} sx={{ fontSize: 50 }} />
              <Skeleton variant="text"  width={260} sx={{ fontSize: 20 }} />
            </CentredBox>
            <br/>
            <Box sx={{backgroundColor: '#EEEEEE', display: 'flex', borderRadius: 2, p: 2}}>
              <CentredBox sx={{width: '100%', flexDirection: 'column'}}>
                <Skeleton variant="text" width={360} sx={{ fontSize: 30 }} />
                <CentredBox sx={{gap: 1}}>
                  <Skeleton variant="text" width={160} sx={{ fontSize: 20 }} />
                  <Divider orientation="vertical" flexItem />
                  <Skeleton variant="text" width={160} sx={{ fontSize: 20 }} />
                </CentredBox>
              </CentredBox>
            </Box>
          </>
        : <>
            <CentredBox sx={{flexDirection: 'column', width: '100%'}}>
              <UploadPhoto sx={{height: '100%', width: '100%'}} src={event.picture}/>
              <Typography sx={{fontWeight: 'bold', fontSize: 40, pt: 1, texAlign: 'center'}}>
                {event.event_name}
              </Typography>
              <Typography sx={{fontSize: 20, fontWeight: "regular", color: "#AE759F", texAlign: 'center'}}>
                {dayjs(event.start_date).format('lll')} - {dayjs(event.end_date).format('lll')}
              </Typography>
            </CentredBox>
            <br/>
            <Box sx={{backgroundColor: '#EEEEEE', display: 'flex', borderRadius: 2, p: 2}}>
              <Box sx={{width: '100%'}}>
                {(sectionSeating)
                  ? <Typography
                      sx={{
                        fontSize: 30,
                        fontWeight: 'bold',
                        textAlign: 'center'
                      }}
                    >
                      {ticketDetails.section}{ticketDetails.seat_num}
                    </Typography>
                  : <Typography
                      sx={{
                        fontSize: 30,
                        fontWeight: 'bold',
                        textAlign: 'center'
                      }}
                    >
                      {ticketDetails.section} x 1
                    </Typography>
                }
                <CentredBox sx={{gap: 1}}>
                  <Typography
                    sx={{
                      fontSize: 20,
                      textAlign: 'center'
                    }}
                  >
                    {userData.firstName} {userData.lastName}
                  </Typography>
                  <Divider orientation="vertical" flexItem />
                  <Typography
                    sx={{
                      fontSize: 20,
                      textAlign: 'center'
                    }}
                  >
                    {userData.email}
                  </Typography>
                </CentredBox>
              </Box>
            </Box>
          </>
      }
    </Box>
  )
}