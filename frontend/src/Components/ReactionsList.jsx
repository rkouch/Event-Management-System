import { Typography } from "@mui/material"
import { Box } from "@mui/system"
import React from "react"
import Emoji from "./Emoji"

export default function ReactionsList ({reactions, size=25}) {
  const reactionsToRender = []

  return (
    <Box sx={{display: 'flex', gap: 1, height: '100%'}}>
      {reactions.map((reaction, key) => {
        return (
          <Box key={key} sx={{display: 'flex', height: '100%', alignItems: 'center'}}>
            <Emoji label={reaction.react_type} fontSize={size}/>
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