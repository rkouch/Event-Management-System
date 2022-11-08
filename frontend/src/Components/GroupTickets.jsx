import { Collapse, FormControl, FormHelperText, Divider, Grid, IconButton, Tooltip, Typography, Button, styled, alpha } from "@mui/material";
import { Box } from "@mui/system";
import React from "react";
import { CentredBox } from "../Styles/HelperStyles";
import { TextButton, TextButton2, TicketOption, TkrButton } from "../Styles/InputStyles";
import GroupTicket from "./GroupTicket";
import InfoOutlinedIcon from '@mui/icons-material/InfoOutlined';
import Stepper from '@mui/material/Stepper';
import Step from '@mui/material/Step';
import StepLabel from '@mui/material/StepLabel';
import ExpandMoreIcon from '@mui/icons-material/ExpandMore';
import { checkValidEmail, setFieldInState } from "../Helpers";
import { ContrastInput, ContrastInputWrapper } from "../Styles/InputStyles";

const steps = ['Select assigned ticket', 'Invite group members']

const ExpandMore = styled((props) => {
  const { expand, ...other } = props;
  return <IconButton {...other} />;
})(({ theme, expand }) => ({
  transform: !expand ? 'rotate(0deg)' : 'rotate(180deg)',
  marginLeft: 'auto',
  transition: theme.transitions.create('transform', {
    duration: theme.transitions.duration.shortest,
  }),
}));


export default function GroupTickets({reservedTickets, setGroupTicketBuy, createGroup}) {
  // States
  
  const [info, setInfo] = React.useState(true)
  const [activeStep, setActiveStep] = React.useState(0);
  const [showSelect, setShowSelect] = React.useState(true)
  const [showInvite, setShowInvite] = React.useState(false)
  const [selectTicket, setSelectTicket] = React.useState('')
  const [invites, setInvites] = React.useState([])
  const [groupId, setGroupId] = React.useState('')

  const [firstName, setFirstName] = React.useState({
    label: 'first name',
    value: '',
    error: false,
    errorMsg: ''
  })
  const [lastName, setLastName] = React.useState({
    label: 'last name',
    value: '',
    error: false,
    errorMsg: ''
  })
  const [email, setEmail] = React.useState({
    value: '',
    error: false,
    errorMsg: ''
  })

  // Handles for state changes
  const handleFirstNameChange = (e) => {
    // Clear error
    setFieldInState('error', false, firstName, setFirstName)
    setFieldInState('errorMsg', '', firstName, setFirstName)

    // Check valid first name
    setFieldInState('value', e.target.value, firstName, setFirstName)
  }

  const handleLastNameChange = (e) => {
    // Clear error
    setFieldInState('error', false, lastName, setLastName)
    setFieldInState('errorMsg', '', lastName, setLastName)

    // Check valid last name
    setFieldInState('value', e.target.value, lastName, setLastName)
  }

  const handleEmailChange = (e) => {
    // Clear error
    setFieldInState('error', false, email, setEmail)
    setFieldInState('errorMsg', '', email, setFirstName)

    // check valid email
    setFieldInState('value', e.target.value, email, setEmail)
  }

  const handleOnBlur = () => {
    return
  }

  const cancelTicketBuy = () => {
    setGroupTicketBuy(false)
  }

  const handleShowInfo = () => {
    setInfo(!info)
  }

  const handleShowSelect = () => {
    setShowSelect(!showSelect)
  }

  const handleShowInvite = () => {
    setShowInvite(!showInvite)
  }

  const handleSelectTicket = (reserve_id) => {
    setInfo(false)
    setShowInvite(true)
    setSelectTicket(reserve_id)
    setInvites([])
  }

  return (
    <CentredBox sx={{flexDirection: 'column', ml: 5, mr: 5}}>
      <Box sx={{width: '100%'}}>
        <Box sx={{display: 'flex', justifyContent: 'space-between'}}>
          <Typography
            sx={{
              fontFamily: "Segoe UI",
              fontSize: 25,
              textAlign: 'start',
            }}
          >
            Group Ticket Buying
          </Typography>
          <Tooltip title="More about group buying">
            <IconButton onClick={handleShowInfo}>
              <InfoOutlinedIcon/>
            </IconButton>
          </Tooltip>
        </Box>
        <Collapse in={info}>
          <Box sx={{p: 3, backgroundColor: '#DDDDDD', borderRadius: 5, mb: 1}}>
            <Typography
              sx={{
                fontFamily: "Segoe UI",
                fontSize: 17,
              }}
            >
              Group ticket buying allows for on person to reserve tickets for a group
              The group leader is able to send invites to other users of Tickr to purchase
              their delegated seat.
            </Typography>
          </Box>
        </Collapse>
      </Box>
      <Box sx={{width: '100%'}}>
        {/* Select gorup leaders ticket */}
        <Box sx={{display: 'flex', justifyContent: 'space-between'}}>
          <Typography
            sx={{
              fontFamily: "Segoe UI",
              fontSize: 20,
              textAlign: 'start',
              width: '100%',
              fontWeight: 'bold'
            }}
          >
            1. Select your ticket
          </Typography>  
          <ExpandMore
            expand={showSelect}
            onClick={handleShowSelect}
          >
            <ExpandMoreIcon/>
          </ExpandMore>
        </Box>
        <Collapse in={showSelect}>
          <CentredBox sx={{gap: 5}}>
            {reservedTickets.map((reserve, key) => {
              return (
                <TicketOption 
                  sx={{
                    widht: 70,
                    height: 70,
                    backgroundColor: (reserve.reserve_id === selectTicket) ? "#AE759F" : alpha('#6A7B8A', 0.3),
                    color: ((reserve.reserve_id === selectTicket) ? '#FFFFFF' : '#444444')
                  }} 
                  key={key} 
                  onClick={() => {handleSelectTicket(reserve.reserve_id)}}
                >
                  {reserve.section[0]}{reserve.seat_number}
                </TicketOption>
              )
            })}
          </CentredBox>
          <CentredBox sx={{ml: 10, mr: 10}}>
            <Grid container spacing={1}>
              <Grid item xs={6}>
                <FormControl sx={{width: '100%'}}>
                  <ContrastInputWrapper>
                    <ContrastInput
                      fullWidth
                      placeholder="First Name"
                      onChange={handleFirstNameChange}
                      defaultValue={firstName.value}
                      error={firstName.error}
                      onBlur={() => {handleOnBlur('first_name', firstName, setFirstName)}}
                    >
                    </ContrastInput>
                  </ContrastInputWrapper>
                  <FormHelperText>{firstName.errorMsg}</FormHelperText>
                </FormControl>
              </Grid>
              <Grid item xs={6}>
                <FormControl sx={{width: '100%'}}>
                  <ContrastInputWrapper>
                    <ContrastInput
                      fullWidth
                      placeholder="Last Name"
                      onChange={handleLastNameChange}
                      value={lastName.value}
                      error={lastName.error}
                      onBlur={() => {handleOnBlur('last_name', lastName, setLastName)}}
                    />
                  </ContrastInputWrapper>
                  <FormHelperText>{lastName.errorMsg}</FormHelperText>
                </FormControl>
              </Grid>
              <Grid item xs={12}>
                <FormControl sx={{width: '100%'}}>
                  <ContrastInputWrapper>
                    <ContrastInput
                      placeholder="Email"
                      fullWidth
                      onChange={handleEmailChange}
                      value={email.value}
                      error={email.error}
                      onBlur={() => {handleOnBlur('email', email, setEmail)}}
                    >
                    </ContrastInput>
                  </ContrastInputWrapper>
                  <FormHelperText>{email.errorMsg}</FormHelperText>
                </FormControl>
              </Grid>
            </Grid>
          </CentredBox>
        </Collapse>
      </Box>
      <Box sx={{width: '100%'}}>
        <Box sx={{display: 'flex', justifyContent: 'space-between'}}>
          <Typography
            sx={{
              fontFamily: "Segoe UI",
              fontSize: 20,
              textAlign: 'start',
              width: '100%',
              fontWeight: 'bold',
              color: (selectTicket === '') ? '#EEEEEE' : '#333333'
            }}
          >
            2. Invite Group members
          </Typography>
          <ExpandMore
            expand={showInvite}
            onClick={handleShowInvite}
            disabled={(selectTicket === '')}
          >
            <ExpandMoreIcon/>
          </ExpandMore>
        </Box>
        <Collapse in={showInvite}>
          <CentredBox sx={{flexDirection: 'column'}}>
            {reservedTickets.map((ticket, key) => {
              if (ticket.reserve_id !== selectTicket) {
                return (
                  <GroupTicket key={key} ticket={ticket} setInvites={setInvites} invites={invites} groupId={groupId}/>
                )
              } 
            })}
          </CentredBox>
          <CentredBox>
            <TkrButton
              onClick={() => {console.log(invites)}}
            >
              Send Invites
            </TkrButton>
          </CentredBox>
        </Collapse>
      </Box>
      
      <Grid container spacing={2}>
        <Grid item xs={1}>
        </Grid>
        <Grid item xs={11}>
          <Box sx={{display: 'flex', justifyContent: 'flex-end', alignItems: 'center'}}>
            <TextButton2 onClick={cancelTicketBuy}>
              Cancel group ticket
            </TextButton2>
          </Box>
        </Grid>
      </Grid>
    </CentredBox>
  )
}