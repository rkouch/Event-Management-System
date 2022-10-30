import { Typography } from "@mui/material"
import { Box } from "@mui/system"
import React from "react"
import Emoji from "./Emoji"

export default function ReactionsList ({reactions, size=25}) {
  const reactionsToRender = []
  if (reactions.length > 0) {
    return (
      <Box sx={{display: 'flex', gap: 1, borderRadius: 3, alignItems: 'center', width: 'fit-content', p: '3px', backgroundColor: '#BBBBBB'}}>
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
  } else {
    return(
      <></>
    )
  }
}