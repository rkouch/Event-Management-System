import { Alert, alpha, Collapse, FormControl, FormHelperText, Grid, IconButton, Tooltip, Typography } from "@mui/material";
import { Box } from "@mui/system";
import React from "react";
import { CentredBox } from "../Styles/HelperStyles";
import { ContrastInput, ContrastInputNoOutline, ContrastInputWrapper } from "../Styles/InputStyles";
import AddIcon from '@mui/icons-material/Add';
import { apiFetch, checkValidEmail, getToken } from "../Helpers";
import DeleteOutlineIcon from '@mui/icons-material/DeleteOutline';


export default function GroupTicket({ticket, setInvites, invites, groupId}) {
  const [email, setEmail] = React.useState('')
  const [error, setError] = React.useState(false)
  const [helperMsg, setHelperMsg] = React.useState('')
  const [inviteSent, setInviteSent] = React.useState(false)


  const handleInvite = async () => {
    if (!checkValidEmail(email)) {
      setError(true)
      setHelperMsg('Invalid email')
      return
    }
    // Check if user exists
    try {     
      const response = await apiFetch('GET', `/api/user/search?email=${email}`, null)
    } catch (error) {
      console.log(error)
      setError(true)
      setHelperMsg(error.reason)
      return
    }

    setInviteSent(true)

    // Add email to invites list, check if reserve already has invites s
    const invites_t = invites
    invites_t.push({
      email: email,
      reserve_id: ticket.reserve_id
    })
    console.log(invites_t)
    setInvites(invites_t)
  }

  const handleChange = (e) => {
    setEmail(e.target.value)
    setError(false)
    setHelperMsg('')
  }

  const handleRemoveInvite = (e) => {
    const invites_t = invites
    for (const i in invites_t) {
      const invite = invites[i]
      if (invite.reserve_id === ticket.reserve_id) {
        invites_t.splice(i, 1)
      }
    }
    console.log(invites_t)
    setInvites(invites_t)
    setEmail('')
    setInviteSent(false)
  }

  return (
    <Box sx={{width: '100%', pt: 2, pb: 2}}>
      <Grid container spacing={2}>
        <Grid item xs={1}></Grid>
        <Grid item xs={3}>
          <CentredBox sx={{backgroundColor: alpha('#6A7B8A', 0.3), height: '100%', width: '100%', borderRadius: 3}}>
            <Typography>
              {ticket.section[0]}{ticket.seat_number}
            </Typography>
          </CentredBox>
        </Grid>
        <Grid item xs={7}>
          <Box>
            <ContrastInputWrapper>
              <ContrastInput
                fullWidth
                endAdornment={
                  <>
                    {inviteSent
                      ? <Tooltip title="Remove invite">
                          <IconButton onClick={handleRemoveInvite}>
                            <DeleteOutlineIcon/>
                          </IconButton>
                        </Tooltip>
                      : <IconButton
                          disabled={email.length <= 0}
                          onClick={handleInvite}
                        >
                          <AddIcon/>
                        </IconButton>
                    }
                  </>
                }
                placeholder="Email"
                onChange={handleChange}
                error={error}
                disabled={inviteSent}
                value={email}
              >
              </ContrastInput>
            </ContrastInputWrapper>
          </Box>
        </Grid>
        <Grid item xs={1}>
        </Grid>
      </Grid>
      <CentredBox sx={{pt: 1}}>
        <Collapse in={error}>
          <Alert severity="error">
            {helperMsg}
          </Alert>
        </Collapse>
      </CentredBox>
    </Box>
  )
}