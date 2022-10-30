import { Typography } from "@mui/material"
import { Box } from "@mui/system"
import React from "react"
import Emoji from "./Emoji"

export default function ReactionsList ({reactions}) {
  const reactionsToRender = []

  return (
    <Box sx={{display: 'flex', gap: 1}}>
      {reactions.map((reaction, key) => {
        return (
          <Box sx={{display: 'flex', alignItems: 'center'}}>
            <Emoji label={reaction.react_type}/>
            <Typography>
              {reaction.react_num}
            </Typography>
          </Box>
        )
      })

      }
    </Box>
  )
}