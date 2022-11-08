import { FormControl, FormHelperText, Grid } from "@mui/material";
import React from "react";
import { setFieldInState } from "../Helpers";
import { ContrastInput, ContrastInputWrapper } from "../Styles/InputStyles";
import { checkValidEmail, hasNumber } from "../Helpers";

export default function OrderDetails({userDetails, setError, setErrorMsg, setUserDetails}) {
  // States
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


  // handle onblur event, setState in parent element, to avoid unnecesary re-renders
  const handleOnBlur = (field, state, setState) => {
    // check valid input
    if (state === email) {
      if (!checkValidEmail(state.value)){
        setFieldInState('error', true, state, setState)
        setFieldInState('errorMsg', 'Invalid email.', state, setState)
        return
      }
    } else {
      if (state.value.length <= 0 || hasNumber(state.value)) {
        setFieldInState('error', true, state, setState)
        setFieldInState('errorMsg', `Invalid ${state.label}.`, state, setState)
        return
      }
    }

    setFieldInState(field, state.value, userDetails, setUserDetails)
  }


  return (
    <Grid container spacing={2}>
      <Grid item xs={2}></Grid>
      <Grid item xs={4}>
        <FormControl sx={{width:'100%'}}>
          <ContrastInputWrapper>
            <ContrastInput
              fullWidth
              placeholder="First Name"
              value={firstName.value}
              onChange={handleFirstNameChange}
              onBlur={() => {handleOnBlur('first_name', firstName, setFirstName)}}
            >
            </ContrastInput>
          </ContrastInputWrapper>
          <FormHelperText>{firstName.errorMsg}</FormHelperText>
        </FormControl>
      </Grid>
      <Grid item xs={4}>
        <FormControl sx={{width:'100%'}}>
          <ContrastInputWrapper>
            <ContrastInput
              fullWidth
              placeholder="Last Name"
              value={lastName.value}
              onChange={handleLastNameChange}
              onBlur={() => {handleOnBlur('last_name', lastName, setLastName)}}
            >
            </ContrastInput>
          </ContrastInputWrapper>
          <FormHelperText>{lastName.errorMsg}</FormHelperText>
        </FormControl>
      </Grid>
      <Grid item xs={2}></Grid>
      <Grid item xs={2}></Grid>
      <Grid item xs={8}>
        <FormControl sx={{width:'100%'}}>
          <ContrastInputWrapper>
            <ContrastInput
              placeholder="Email"
              fullWidth
              value={email.value}
              onChange={handleEmailChange}
              onBlur={() => {handleOnBlur('email', email, setEmail)}}
            >
            </ContrastInput>
          </ContrastInputWrapper>
          <FormHelperText>{lastName.errorMsg}</FormHelperText>
        </FormControl>
      </Grid>
      <Grid item xs={2}></Grid>
    </Grid>
  )
}