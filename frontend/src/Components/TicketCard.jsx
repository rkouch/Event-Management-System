import { Divider, Skeleton, Typography } from '@mui/material'
import { Box } from '@mui/system'
import React from 'react'
import { CentredBox, UploadPhoto } from '../Styles/HelperStyles'
import dayjs from 'dayjs'
import { apiFetch, getEventData, getToken, getUserData } from '../Helpers'

export default function TicketCard({event, ticket_id}) {
  
  const [ticketDetails, setTicketDetails] = React.useState(null)

  const testTicketData = {
    event_id: '77ff1d93-e3c2-4d44-9ced-6375de70048b',
    section: 'A',
    seat_num: 10,
    user_id: 'dfc689c3-e989-441a-9d1f-4ad629e929a9'
  }

  const [sectionSeating, setSectionSeating] = React.useState(false)

  const [userData, setUserData] = React.useState(null)
  const [sectionName, setSectionName] = React.useState('')

  const getTicketData = async () => {
    // Send API call to get ticket details
    const paramsObj = {
      ticket_id: ticket_id,
    }
    const searchParams = new URLSearchParams(paramsObj)
    try {
      const response = await apiFetch('GET', `/api/ticket/view?${searchParams}`)
      setTicketDetails(response)
      const name = response.section
      if (name.split(' ').length > 1) {
        const names = name.split(' ')
        setSectionName(names[0][0]+names[1][0])
      } else {
        setSectionName(name)
      }
      // getUserData(`user_id=${response.user_id}`, setUserData)
      event.seating_details.forEach(function(section) {
        if (section.section === response.section) {
          setSectionSeating(section.has_seats)
          return
        }
      })
    } catch (e) {
      console.log(e)
    }
    
  }

  React.useEffect(()=> {
    if (event !== null) {
      getTicketData()  
    }
  },[event])


  return (
    <Box sx={{boxShadow: 5, backgroundColor: '#FFFFFFF', m: 1, p: 3, borderRadius: 1}}>
      {(ticketDetails === null)
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
                      {sectionName}{ticketDetails.seat_num}
                    </Typography>
                  : <Typography
                      sx={{
                        fontSize: 30,
                        fontWeight: 'bold',
                        textAlign: 'center'
                      }}
                    >
                      {sectionName} x 1
                    </Typography>
                }
                <CentredBox sx={{gap: 1}}>
                  <Typography
                    sx={{
                      fontSize: 20,
                      textAlign: 'center'
                    }}
                  >
                    {ticketDetails.first_name} {ticketDetails.last_name}
                  </Typography>
                  <Divider orientation="vertical" flexItem />
                  <Typography
                    sx={{
                      fontSize: 20,
                      textAlign: 'center'
                    }}
                  >
                    {ticketDetails.email}
                  </Typography>
                </CentredBox>
              </Box>
            </Box>
          </>
      }
    </Box>
  )
}