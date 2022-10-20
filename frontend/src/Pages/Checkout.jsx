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
import ShadowInput from "../Components/ShadowInput";

export default function Checkout ({ticketOrder}) {
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

  const [orderDetails, setOrderDetails] = React.useState(ticketOrder.ticket_details)

  const [selectedSeats, setSelectedSeats] = React.useState([])
  const [orderTotal, setOrderTotal] = React.useState(0)

  const [error, setError] = React.useState(false)
  const [errorMsg, setErrorMsg] = React.useState('')

  const [firstName, setFirstName] = React.useState({
    value: '',
    error: false,
    errorMsg: ''
  })

  React.useEffect(()=> {
    getEventData(params.event_id, setEvent)
    console.log(ticketOrder)
    setOrderDetails(ticketOrder.ticket_details)
  },[])

  React.useEffect(() => {
    var total = 0
    for (const m in orderDetails) {
      const order = orderDetails[m]
      total += order.ticket_price*order.quantity
    }
    setOrderTotal(total)
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

  const handlePayment = async () => {
    console.log(orderDetails)
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
                    </Grid>
                    <Grid item xs={10}>
                      <CentredBox sx={{flexDirection: 'column'}}>
                        <Typography sx={{fontWeight: 'bold', fontSize: 40, pt: 1}}>
                          Order Checkout
                        </Typography>
                      </CentredBox>
                    </Grid>
                    <Grid item xs={1}>
                    </Grid>
                  </Grid>
                  <Divider/>
                  <br/>
                  <CentredBox sx={{width: '100%', flexDirection: 'column'}}>
                    <Grid container spacing={2}>
                      <Grid item xs={6}>
                      <ShadowInput 
                        state={firstName} 
                        setState={setFirstName} 
                        sx={{
                          '.MuiOutlinedInput-notchedOutline': {
                            borderColor: firstName.error ? "red" : "rgba(0,0,0,0)"
                          },
                        }}
                        defaultValue={firstName.value} 
                        field='value' 
                        placeholder="First Name"
                        setError={setError}
                      />
                      </Grid>
                    </Grid>
                    
                  </CentredBox>
                </Grid>
                <Divider orientation="vertical" flexItem></Divider>
                <Grid item xs>
                  <Box sx={{boxShadow: 5, backgroundColor: '#FFFFFFF', m: 1, p: 3, borderRadius: 1}}>
                    <CentredBox sx={{flexDirection: 'column', widht: '100%'}}>
                      <UploadPhoto src={event.picture}/>
                      <Typography sx={{fontWeight: 'bold', fontSize: 40, pt: 1, texAlign: 'center'}}>
                        {event.event_name}
                      </Typography>
                      <Typography sx={{fontSize: 15, fontWeight: "regular", color: "#AE759F", texAlign: 'center'}}>
                        {dayjs(event.start_date).format('lll')} - {dayjs(event.end_date).format('lll')}
                      </Typography>
                    </CentredBox>
                    <br/>
                    <Box sx={{backgroundColor: '#EEEEEE', display: 'flex', borderRadius: 2, p: 2}}>
                      <Box sx={{width: '100%'}}>
                        <Typography
                          sx={{textAlign: 'center', fontSize: 20, pb: 1}}
                        >
                          Order Summary
                        </Typography>
                        <Divider/>
                        <br/>
                        <Grid container spacing={2} sx={{pl: 1, pr: 1}}>
                          {orderDetails.map((section, key) => {
                            return (
                              <Grid item key={key} sx={{width: '100%'}}>
                                <Grid container spacing={2} sx={{pl: 1, pr: 1}}>
                                  <Grid item xs={9}>
                                    <Typography sx={{fontSize: 20}}>
                                      {section.quantity} x {section.section} - ${section.ticket_price}
                                    </Typography>
                                    {(section.seatsSelected)
                                      ? <Box sx={{display: 'flex', gap: 1, flexWrap: 'wrap'}}>
                                          {section.seatsSelected.map((seat, key) => {
                                            if (key !== section.seatsSelected.length - 1) {
                                              return (
                                                <Typography key={key} sx={{color: 'rgba(0, 0, 0, 0.6)'}}>
                                                  {section.section[0]}{seat},
                                                </Typography>
                                              )
                                            } else {
                                              return (
                                                <Typography key={key} sx={{color: 'rgba(0, 0, 0, 0.6)'}}>
                                                  {section.section[0]}{seat}
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
                            <Typography sx={{fontSize: 25, fontWeight: 'bold'}}>
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
                    </Box>
                    <br/>
                    {allSeatsSelected()
                      ? <TkrButton
                          sx={{width: '100%'}}
                          onClick={handlePayment}
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
                  </Box>
                </Grid>
              </Grid>

            </EventForm>
        }
      </Box>
    </BackdropNoBG>
  )
}