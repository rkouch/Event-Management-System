import { Box } from "@mui/system"
import React from "react"
import Header from "../Components/Header"
import { BackdropNoBG, CentredBox, H3, UploadPhoto } from "../Styles/HelperStyles"
import { useNavigate, useParams } from "react-router-dom";
import dayjs from "dayjs";
import { getEventData, getToken } from "../Helpers";
import { Alert, Collapse, Divider, FormControl, FormControlLabel, FormHelperText, Grid, IconButton, InputLabel, LinearProgress, MenuItem, Select, Tooltip, Typography } from "@mui/material";
import { EventForm } from "./ViewEvent";
import ShoppingCartOutlinedIcon from '@mui/icons-material/ShoppingCartOutlined';
import SeatSelector from "../Components/SeatSelection";
import QuantitySelector from "../Components/QuantitySelector";
import { TkrButton } from "../Styles/InputStyles";
import ArrowBackIcon from '@mui/icons-material/ArrowBack';

export default function PurchaseTicket ({setTicketOrder}) {
  const params = useParams()
  const navigate = useNavigate()
  var calendar = require('dayjs/plugin/calendar')
  dayjs.extend(calendar)

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

  const [sectionDetails, setSectionDetails] = React.useState([])

  const [selectedSeats, setSelectedSeats] = React.useState([])
  const [orderTotal, setOrderTotal] = React.useState(0)

  const [error, setError] = React.useState(false)
  const [errorMsg, setErrorMsg] = React.useState('')

  // Set up seat selector
  React.useEffect(() => {
    for (const m in event.seating_details) {
      const section = event.seating_details[m]
      var seats = []
      var i = 0
      while (i <= 10 && i <= section.available_seats) {
        seats.push(i)
        i++
      }
      const section_det = {
        ticket_price: section.ticket_price,
        section: section.section,
        available_seats: section.available_seats,
        capacity: section.available_seats,
        seats: seats,
        quantity: 0,
        selectable: section.has_seats,
        seatsSelected: []
      }
      setSectionDetails(current => [section_det, ...current])
    }
  }, [event.event_name])

  React.useEffect(() => {
    setSelectedSeats([])
    var total = 0
    const new_selected = []
    for  (const i in sectionDetails) {
      if (sectionDetails[i].quantity > 0) {
        const section = sectionDetails[i]
        new_selected.push(section)
        total += (section.ticket_price*section.quantity)
      }
    }
    setSelectedSeats(new_selected)
    setOrderTotal(total)
  }, [sectionDetails])

  React.useEffect(()=> {
    getEventData(params.event_id, setEvent)
  },[])

  React.useEffect(() => {
    if (error) {
      setError(false)
      setErrorMsg('')
    }
  },[sectionDetails])

  const allSeatsSelected = () => {
    var allSelected = true
    for (const i in sectionDetails) {
      if ((sectionDetails[i].quantity !== sectionDetails[i].seatsSelected.length) && sectionDetails[i].selectable) {
        allSelected = false
      }
    }
    return allSelected
  }

  const handleCheckout = async () => {
    // Prepare data package
    const ticketDetails = []
    sectionDetails.forEach(function (section) {
      if (section.quantity > 0) {
        const seatsSelected = []

        section.seatsSelected.forEach(function (seat) {
          seatsSelected.push(seat.slice(1))
        })
        console.log(seatsSelected)
        const sectionBody = {
          section: section.section,
          quantity: section.quantity,
          seatsSelected: seatsSelected.length > 0 ? seatsSelected : null,
          ticket_price: section.ticket_price
        }

        ticketDetails.push(sectionBody)
      }
    })

    const body = {
      auth_token: getToken(),
      event_id: params.event_id,
      ticket_datetime: Date(),
      ticket_details: ticketDetails,
    }

    try {
      // Send API call
      console.log('Make api call')

      setTicketOrder(body)
      // Navigate to purchasing page
      navigate(`/checkout/${params.event_id}`)

    } catch (error) {
      // Display error message in error collapse
      setError(true)
      setErrorMsg(error.reason)
    }

    
  }

  return (
    <BackdropNoBG>
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
        {(event.host_id === '')
          ? <Box sx={{height: '100%', display: 'flex', justifyContent: 'center', alignItems: 'center', margin: '50px', paddingTop: '100px'}}> 
              <Box sx={{ width: '90%', hieght: 50}}>
                <LinearProgress color='secondary'/>
              </Box>
            </Box>
          : <EventForm>
              <Grid container spacing={2}>
                <Grid item xs={7}>
                  <Grid container>
                    <Grid item xs={1}>
                      <CentredBox sx={{height: '100%'}}>
                        <Tooltip title="Back to event">
                          <IconButton onClick={()=>{navigate(`/view_event/${params.event_id}`)}}>
                            <ArrowBackIcon/>
                          </IconButton>
                        </Tooltip>
                      </CentredBox>
                    </Grid>
                    <Grid item xs={10}>
                      <CentredBox sx={{flexDirection: 'column'}}>
                        <Typography sx={{fontWeight: 'bold', fontSize: 40, pt: 1}}>
                          {event.event_name}
                        </Typography>
                        <Typography
                          sx={{
                            fontSize: 15,
                            fontWeight: "regular",
                            color: "#AE759F",
                          }}
                        >
                          {dayjs(event.start_date).format('lll')} - {dayjs(event.end_date).format('lll')}
                        </Typography>
                      </CentredBox>
                    </Grid>
                    <Grid item xs={1}>
                    </Grid>
                  </Grid>
                  <Divider/>
                  <br/>
                  <br/>
                  {(sectionDetails).map((section, key) => {
                    return (
                      <div key={key}>
                        {section.selectable
                          ? <SeatSelector section={section} key={key} index={key} sectionDetails={sectionDetails} setSectionDetails={setSectionDetails}/>
                          : <QuantitySelector section={section} key={key} index={key} sectionDetails={sectionDetails} setSectionDetails={setSectionDetails}/>
                        }
                      </div>
                      
                    )
                  })}
                </Grid>
                <Divider orientation="vertical" flexItem></Divider>
                <Grid item xs={4}>
                  <Box sx={{height: "100%"}}>
                    <CentredBox sx={{flexDirection: 'column', widht: '100%'}}>
                      <UploadPhoto src={event.picture}/>
                    </CentredBox>
                    <br/>
                    <Divider/>
                    <br/>
                    <Box sx={{backgroundColor: '#EEEEEE', display: 'flex', borderRadius: 2, p: 2}}>
                      {(selectedSeats.length === 0)
                        ? <CentredBox sx={{width: '100%'}}>
                            <ShoppingCartOutlinedIcon sx={{color: 'rgba(0, 0, 0, 0.6)'}}/>
                            <Typography sx={{color: 'rgba(0, 0, 0, 0.6)'}}>
                              Empty Cart
                            </Typography>
                          </CentredBox>
                        : <Box sx={{width: '100%'}}>
                            <Typography
                              sx={{textAlign: 'center', fontSize: 20, pb: 1}}
                            >
                              Order Summary
                            </Typography>
                            <Divider/>
                            <br/>
                            <Grid container spacing={2} sx={{pl: 1, pr: 1}}>
                              {selectedSeats.map((section, key) => {
                                return (
                                  <Grid item key={key} sx={{width: '100%'}}>
                                    <Grid container spacing={2} sx={{pl: 1, pr: 1}}>
                                      <Grid item xs={9}>
                                        <Typography sx={{fontSize: 20}}>
                                          {section.quantity} x {section.section} - ${section.ticket_price}
                                        </Typography>
                                        {section.selectable
                                          ? <Box sx={{display: 'flex', gap: 1, flexWrap: 'wrap'}}>
                                              {section.seatsSelected.map((seat, key) => {
                                                if (key !== section.seatsSelected.length - 1) {
                                                  return (
                                                    <Typography key={key} sx={{color: 'rgba(0, 0, 0, 0.6)'}}>
                                                      {seat},
                                                    </Typography>
                                                  )
                                                } else {
                                                  return (
                                                    <Typography key={key} sx={{color: 'rgba(0, 0, 0, 0.6)'}}>
                                                      {seat}
                                                    </Typography>
                                                  )
                                                }
                                              })}
                                              
                                            </Box>
                                          : <>
                                            </>
                                        }
                                      </Grid>
                                      <Grid item xs={3}>
                                        <Typography sx={{fontSize: 20, textAlign: 'right'}}>
                                          ${(section.ticket_price*section.quantity)}
                                        </Typography>
                                      </Grid>
                                    </Grid>
                                    <br/>
                                  </Grid>
                                )
                              })}
                            </Grid>
                            <br/>
                            <Divider/>
                            <br/>
                            <Grid container spacing={2} sx={{pl: 2, pr: 2}}>
                              <Grid item xs={9}>
                                <Typography sx={{fontSize: 20}}>
                                  Total: 
                                </Typography>
                              </Grid>
                              <Grid item xs={3}>
                                <Typography sx={{fontSize: 25, textAlign: 'right'}}>
                                  ${orderTotal} 
                                </Typography>
                              </Grid>
                            </Grid>
                          </Box>
                      }
                    </Box>
                    <br/>
                    {(selectedSeats.length === 0)
                      ? <></>
                      : <CentredBox sx={{width: '100%', flexDirection: 'column'}}>
                          {allSeatsSelected()
                            ? <TkrButton
                                sx={{width: '100%'}}
                                onClick={handleCheckout}
                                startIcon={<ShoppingCartOutlinedIcon/>}
                              >
                                Checkout
                              </TkrButton>
                            : <FormControl sx={{width: '100%'}}>
                                <TkrButton
                                  sx={{width: '100%'}}
                                  disabled={true}
                                  startIcon={<ShoppingCartOutlinedIcon/>}
                                >
                                  Checkout
                                </TkrButton>
                                <FormHelperText sx={{display:'flex', justifyContent: 'center'}} required={true}>
                                  <Typography sx={{color: 'rgba(0, 0, 0, 0.6)', textAlign: 'center'}} component={'span'}>
                                    Select all seats to purchase
                                  </Typography>
                                </FormHelperText>
                              </FormControl>
                          }
                          <br/>
                          <Collapse in={error}>
                            <Alert severity="error">{errorMsg}</Alert>
                          </Collapse>
                        </CentredBox>
                    }
                  </Box>
                </Grid>
              </Grid>

            </EventForm>
        }
      </Box>
    </BackdropNoBG>
  )
}