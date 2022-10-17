import { Box } from "@mui/system"
import React from "react"
import Header from "../Components/Header"
import { BackdropNoBG, CentredBox, H3, UploadPhoto } from "../Styles/HelperStyles"
import { useNavigate, useParams } from "react-router-dom";
import dayjs from "dayjs";
import { getEventData } from "../Helpers";
import { Divider, FormControl, Grid, InputLabel, LinearProgress, MenuItem, Select, Typography } from "@mui/material";
import { EventForm } from "./ViewEvent";
import ShoppingCartOutlinedIcon from '@mui/icons-material/ShoppingCartOutlined';
import SeatSelection from "../Components/SeatSelection";

export default function PurchaseTicket ({}) {
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

  // Set up seat selector
  React.useEffect(() => {
    for (const m in event.seating_details) {
      const section = event.seating_details[m]
      var seats = []
      var i = 0
      while (i <= 10 && i <= section.availability) {
        seats.push(i)
        i++
      }
      const section_det = {
        ticket_price: section.ticket_price,
        section: section.section,
        availability: section.availability,
        capacity: section.availability,
        seats: seats,
        quantity: 0,
        selectable: false,
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

  const handleQuantityChange = (key, qty) => {
    const new_sections = sectionDetails.map((value, key_m) => {
      if (key_m === key) {
        return {...value, quantity: qty}
      }
      return (value)
    })
    setSectionDetails(new_sections)
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
                  <Divider/>
                  <br/>
                  <br/>
                  {(sectionDetails).map((section, key) => {
                    return (
                      <SeatSelection section={section} key={key} index={key} sectionDetails={sectionDetails} setSectionDetails={setSectionDetails}/>
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
                    <Box sx={{backgroundColor: '#EEEEEE', display: 'flex', borderRadius: 5, p: 2}}>
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
                                          {section.quantity} x {section.section} -
                                        </Typography>
                                        <Typography sx={{fontSize: 17,}}>
                                          ${section.ticket_price}
                                        </Typography>
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
                                <Typography sx={{fontSize: 20, textAlign: 'right'}}>
                                  ${orderTotal} 
                                </Typography>
                              </Grid>
                            </Grid>
                          </Box>
                      }
                    </Box>
                  </Box>
                </Grid>
              </Grid>

            </EventForm>
        }
      </Box>
    </BackdropNoBG>
  )
}