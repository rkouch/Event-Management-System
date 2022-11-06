import { Grid } from "@mui/material";
import React from "react";
import { setFieldInState } from "../Helpers";
import { ContrastInput, ContrastInputWrapper } from "../Styles/InputStyles";

export default function OrderDetails({userDetails, setError, setErrorMsg, setUserDetails}) {
  return (
    <Grid container spacing={2}>
      <Grid item xs={2}></Grid>
      <Grid item xs={4}>
        <ContrastInputWrapper>
          <ContrastInput
            fullWidth
            placeholder="First Name"
            value={userDetails.firstName}
            onChange={(e) => {
              setError(false)
              setErrorMsg('')
              setFieldInState('firstName', e.target.value, userDetails, setUserDetails)
            }}
          >
          </ContrastInput>
        </ContrastInputWrapper>
      </Grid>
      <Grid item xs={4}>
        <ContrastInputWrapper>
          <ContrastInput
            fullWidth
            placeholder="Last Name"
            value={userDetails.lastName}
            onChange={(e) => {
              setError(false)
              setErrorMsg('')
              setFieldInState('lastName', e.target.value, userDetails, setUserDetails)
            }}
          >
          </ContrastInput>
        </ContrastInputWrapper>
      </Grid>
      <Grid item xs={2}></Grid>
      <Grid item xs={2}></Grid>
      <Grid item xs={8}>
        <ContrastInputWrapper>
          <ContrastInput
            placeholder="Email"
            fullWidth
            value={userDetails.email}
            onChange={(e) => {
              setError(false)
              setErrorMsg('')
              setFieldInState('email', e.target.value, userDetails, setUserDetails)
            }}
          >
          </ContrastInput>
        </ContrastInputWrapper>
      </Grid>
      <Grid item xs={2}></Grid>
    </Grid>
  )
}