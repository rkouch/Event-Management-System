import React from "react"
import { Alert, Divider, FormControl, FormControlLabel, FormGroup, FormHelperText, FormLabel, Grid, IconButton, InputLabel, LinearProgress, MenuItem, Select, Tooltip, Typography } from "@mui/material";
import Collapse from '@mui/material/Collapse';
import ExpandMoreIcon from '@mui/icons-material/ExpandMore';
import { Box } from "@mui/system"
import { ContrastInput, ContrastInputWrapper, TkrButton } from "../Styles/InputStyles";
import { styled, alpha } from '@mui/system';
import { BackdropNoBG, CentredBox, H3, UploadPhoto } from "../Styles/HelperStyles"

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

export default function SectionDetails ({section, getTicketDetails, handleTicketInput, handleSectionExpanded, reserve_id}) {
  return (
    <>
      <Box sx={{display: 'flex', justifyContent: 'center', boxShadow: 5, p: 1, borderRadius: 2, flexDirection: 'column'}}>
        <Grid container spacing={2}>
          <Grid item xs={8}>
            <Typography sx={{fontSize: 30}}>
              {section.quantity} x {section.section}
            </Typography>
          </Grid>
          <Grid item xs={4}>
          </Grid>
        </Grid>
        {section.reserved_seats.map((seat, key) => {
          return (
            <Ticket key={key} seatNum={seat.seat_number} reserve_id={seat.reserve_id} section={section} getTicketDetails={getTicketDetails} handleTicketInput={handleTicketInput}/>
          )
        })}
      </Box>
      <br/>
    </>
  )
}

function Ticket ({seatNum, section, getTicketDetails, reserve_id, handleTicketInput}) {
  return (
    <Box sx={{pt: 1, pb: 1}}>
      <Grid container spacing={2}>
        <Grid item xs={2}>
        </Grid>
        <Grid item xs={2}>
          <CentredBox sx={{backgroundColor: alpha('#6A7B8A', 0.3), height: '100%', width: '100%', borderRadius: 3}}>
            <Typography xs={{}}>
              {section.section[0]}{seatNum}
            </Typography>
          </CentredBox>
        </Grid>
        <Grid item xs>
          <Grid container spacing={1}>
            <Grid item xs={6}>
              <ContrastInputWrapper>
                <ContrastInput
                  fullWidth
                  placeholder="First Name"
                  onChange={(e) => {handleTicketInput(reserve_id, 'first_name', e.target.value)}}
                  defaultValue={getTicketDetails('first_name', reserve_id)}
                >
                </ContrastInput>
              </ContrastInputWrapper>
            </Grid>
            <Grid item xs={6}>
              <ContrastInputWrapper>
                <ContrastInput
                  fullWidth
                  placeholder="Last Name"
                  onChange={(e) => {handleTicketInput(reserve_id, 'last_name', e.target.value)}}
                  defaultValue={getTicketDetails('last_name', reserve_id)}
                >
                </ContrastInput>
              </ContrastInputWrapper>
            </Grid>
            <Grid item xs={12}>
              <ContrastInputWrapper>
                <ContrastInput
                  placeholder="Email"
                  fullWidth
                  onChange={(e) => {handleTicketInput(reserve_id, 'email', e.target.value)}}
                  defaultValue={getTicketDetails('email', reserve_id)}
                >
                </ContrastInput>
              </ContrastInputWrapper>
            </Grid>
          </Grid>
        </Grid>
        <Grid item xs={2}>
        </Grid>
      </Grid>
    </Box>
  )
}