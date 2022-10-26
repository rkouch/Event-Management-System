import React from "react"
import { BackdropNoBG, CentredBox } from "../Styles/HelperStyles"
import Header from "../Components/Header"
import { getEventData } from "../Helpers"
import { useParams, useNavigate } from "react-router-dom"
import { EventForm } from "./CreateEvent"
import { Box } from "@mui/system"
import EventCard from "../Components/EventCard"
import TicketCard from "../Components/TicketCard"
import { useTheme } from '@mui/material/styles';
import MobileStepper from '@mui/material/MobileStepper';
import Paper from '@mui/material/Paper';
import Typography from '@mui/material/Typography';
import Button from '@mui/material/Button';
import KeyboardArrowLeft from '@mui/icons-material/KeyboardArrowLeft';
import KeyboardArrowRight from '@mui/icons-material/KeyboardArrowRight';
import SwipeableViews from 'react-swipeable-views';
import { autoPlay } from 'react-swipeable-views-utils';
import { Divider, Grid, IconButton, Tooltip } from "@mui/material"
import ArrowBackIcon from "@mui/icons-material/ArrowBack"

export default function ViewTicket({}) {
  const params = useParams()
  const navigate = useNavigate()
  const theme = useTheme();

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
    start_date: '',
    end_date: '',
    description: "",
    tags: [],
    admins: [],
    picture: "",
    host_id: ''
  })

  const [ticketIds, setTicketIds] = React.useState([])

  const [activeStep, setActiveStep] = React.useState(0);
  const maxSteps = ticketIds.length;

  const handleNext = () => {
    setActiveStep((prevActiveStep) => prevActiveStep + 1);
  };

  const handleBack = () => {
    setActiveStep((prevActiveStep) => prevActiveStep - 1);
  };

  const handleStepChange = (step) => {
    setActiveStep(step);
  };

  React.useEffect(() => {
    getEventData(params.event_id, setEvent)
    setTicketIds(testTicketIds)
  }, [])

  const testTicketIds = ['asdasdasd', '123asd21e', 'qse1da2d1w']

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
        <Grid container spacing={2}>
          <Grid item xs={1}>
            <Box sx={{p: 3, pl: 10}}>
              <Tooltip title="Back to event">
                <IconButton onClick={()=>{navigate(`/view_event/${params.event_id}`)}}>
                  <ArrowBackIcon/>
                </IconButton>
              </Tooltip>
            </Box>
          </Grid>
          <Grid item xs={10}>
            <CentredBox sx={{display: 'flex', flexDirection: 'column', height: '100%', alignItems: 'center'}}>
              <br/>
              <Box sx={{display: 'flex', flexDirection: 'column', width: '60%'}}>
                <SwipeableViews
                  axis={theme.direction === 'rtl' ? 'x-reverse' : 'x'}
                  index={activeStep}
                  onChangeIndex={handleStepChange}
                  enableMouseEvents
                >
                  {ticketIds.map((ticketId, key) => (
                    <div key={key}>
                      {Math.abs(activeStep - key) <= 2 ? (
                        <TicketCard event={event} ticket_id={ticketId}/>
                      ) : null}
                    </div>
                  ))}
                </SwipeableViews>
                <MobileStepper
                  sx={{color: 'red'}}
                  steps={maxSteps}
                  position="static"
                  activeStep={activeStep}
                  nextButton={
                    <Button
                      size="small"
                      onClick={handleNext}
                      disabled={activeStep === maxSteps - 1}
                    >
                      Next
                      {theme.direction === 'rtl' ? (
                        <KeyboardArrowLeft />
                      ) : (
                        <KeyboardArrowRight />
                      )}
                    </Button>
                  }
                  backButton={
                    <Button size="small" onClick={handleBack} disabled={activeStep === 0}>
                      {theme.direction === 'rtl' ? (
                        <KeyboardArrowRight />
                      ) : (
                        <KeyboardArrowLeft />
                      )}
                      Back
                    </Button>
                  }
                />
              </Box>
            </CentredBox>
          </Grid>
          <Grid item xs={1}></Grid>
        </Grid>
        
      </Box>
      
    </BackdropNoBG>
  )
}
