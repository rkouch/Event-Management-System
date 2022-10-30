import { Collapse, Grid, IconButton, Typography } from "@mui/material"
import { Box } from "@mui/system"
import React from "react"
import UserAvatar from "./UserAvatar"
import { checkIfUser, loggedIn } from "../Helpers"
import ReactBar from "./ReactBar"
import AddReactionIcon from '@mui/icons-material/AddReaction';

export default function ReplyCard({reply}) {
  const [reactBar, setReactBar] = React.useState(false)

  const toggleReactBar = (e) => {
    if (!reply.isUser) {
      setReactBar(!reactBar)
    } 
  }

  const closeReactBar = (e) => {
    if (!reply.isUser) {
      setReactBar(false)
    }
  }

  return (
    <Box sx={{display: 'flex', justifyContent: reply.isUser ? 'flex-end' : 'flex-start'}} onMouseLeave={closeReactBar}>
      <Box sx={{width: '52%', display: 'flex', gap: 1, flexDirection: 'column', justifyContent: 'center'}}>
        <Box sx={{borderRadius: '50px',  backgroundColor: '#DDDDDD', p: 1, maxWidth: 700}}>
          <Grid container sx={{height: '100%'}}>
            <Grid item xs={1}  sx={{display: 'flex', alignItems: 'center'}}>
              <UserAvatar userId={reply.authorId} size={30}/>
            </Grid>
            <Grid xs={10} item sx={{pl: 3, display: 'flex', alignItems: 'center'}}>
              <Typography>
                {reply.text}
              </Typography>
            </Grid>
            <Grid xs={1} item sx={{height: '100%', alignItems: 'center', display: 'flex'}}>
              {!reply.isUser
               ? <IconButton onClick={toggleReactBar}>
                    <AddReactionIcon sx={{color: '#AAAAAA'}}/>
                  </IconButton>
                : <></>
              }
            </Grid>
          </Grid>
        </Box>
        {(!reply.isUser && loggedIn())
          ? <Collapse in={reactBar} sx={{width: '60%',}} onMouseLeave={closeReactBar}>
              <ReactBar />
            </Collapse>
          : <></>
        }
      </Box>
    </Box>
  )
}