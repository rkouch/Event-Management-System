import { Typography } from "@mui/material"
import { Box } from "@mui/system"
import React from "react"
import Header from "../Components/Header"
import { apiFetch, clearReservedTicketsLocal, getReservedTicketsLocal, getToken } from "../Helpers"
import { BackdropNoBG } from "../Styles/HelperStyles"

export default function CancelReservations({ticketOrder}){

  const cancelReservations = async () => {
    const reservation_ids = getReservedTicketsLocal()
    const body = {
      auth_token: getToken(),
      reservations: reservation_ids
    }
    try {
      const response = await apiFetch('DELETE', '/api/ticket/reserve/cancel', body)
      window.location.replace('http://localhost:3000/')
      clearReservedTicketsLocal()
    } catch (e) {
      console.log(e)
    }
  }

  React.useEffect(() => {
    cancelReservations()
  })

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
          paddingTop: 20,
          display: 'flex',
          justifyContent: 'center'
        }}
      >
        <Typography sx={{fontSize: 30}}>
          Cancelling reservation...
        </Typography>
      </Box>
    </BackdropNoBG>
  )
}