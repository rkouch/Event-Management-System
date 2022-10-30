import { Collapse, Grid, IconButton, Typography } from "@mui/material"
import { Box } from "@mui/system"
import React from "react"
import UserAvatar from "./UserAvatar"
import { apiFetch, checkIfUser, loggedIn } from "../Helpers"
import ReactBar from "./ReactBar"
import AddReactionIcon from '@mui/icons-material/AddReaction';
import ReactionsList from "./ReactionsList"

export default function ReplyCard({reply_details, reply_num, review_id}) {
  const [reply, setReply] = React.useState(reply_details)
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

  // On React update review details
  const onReact = async () => {
    try {
      const body = {
        review_id: review_id,
        page_start: reply_num,
        max_results: 1,
      }
      const searchParams = new URLSearchParams(body)
      const response = await apiFetch('GET', `/api/event/reviews/replies?${searchParams}`, null)
      setReply(response.replies[0])
      console.log(response)
    } catch (e) {
      console.log(e)
    }
  }


  return (
    <Box sx={{display: 'flex', justifyContent: reply.isUser ? 'flex-end' : 'flex-start'}} onMouseLeave={closeReactBar}>
      <Box sx={{width: '52%', display: 'flex', gap: 1, flexDirection: 'column', justifyContent: 'center'}}>
        <Box sx={{borderRadius: 15,  backgroundColor: '#DDDDDD', p: 1, maxWidth: 700}}>
          <Grid container >
            <Grid item xs={1}  sx={{display: 'flex', alignItems: 'center'}}>
              <UserAvatar userId={reply.authorId} size={30}/>
            </Grid>
            <Grid xs={10} item sx={{pl: 3, display: 'flex', alignItems: 'center'}}>
              <Typography>
                {reply.text}
              </Typography>
            </Grid>
            <Grid xs={1} item sx={{height: '100%', alignItems: 'center', display: 'flex'}}>
              {(!reply.isUser && loggedIn())
               ? <IconButton onClick={toggleReactBar}>
                    <AddReactionIcon sx={{color: '#AAAAAA'}}/>
                  </IconButton>
                : <></>
              }
            </Grid>
            <Grid item xs={1}></Grid>
            <Grid item xs={10} sx={{pl: 3}}>
              <ReactionsList reactions={reply.reactions} size={17}/>
            </Grid>
          </Grid>
        </Box>
        {(!reply.isUser && loggedIn())
          ? <Collapse in={reactBar} sx={{width: '60%',}} onMouseLeave={closeReactBar}>
              <ReactBar onReact={onReact} comment_id={reply.replyId}/>
            </Collapse>
          : <></>
        }
      </Box>
    </Box>
  )
}