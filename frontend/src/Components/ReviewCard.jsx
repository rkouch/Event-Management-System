import { Button, Collapse, Grid, IconButton, Typography } from '@mui/material'
import { Box, alpha } from '@mui/system'
import React from 'react'
import UserAvatar from './UserAvatar'
import Rating from '@mui/material/Rating';
import { ContrastInputNoOutline, ContrastInputWrapper, ReplyInput, TkrButton } from '../Styles/InputStyles';
import AddReaction from '@mui/icons-material/AddReaction';
import { CentredBox } from '../Styles/HelperStyles';
import Emoji from './Emoji';
import ReplyIcon from '@mui/icons-material/Reply';
import SendIcon from '@mui/icons-material/Send';
import { apiFetch, checkIfUser, getToken, loggedIn } from '../Helpers';
import ReplyCard from './ReplyCard';
import EventReplies from './ReviewReplies';
import ReviewReplies from './ReviewReplies';
import CircularProgress from '@mui/material/CircularProgress';
import ReactBar from './ReactBar';
import ReactionsList from './ReactionsList';

export default function ReviewCard({review, innerRef, id}) {
  const [reactBar, setReactBar] = React.useState(false)
  const [reply, setReply] = React.useState('')
  const [showReply, setShowReply] = React.useState(false)
  const [replies, setReplies] = React.useState([])
  const [replyCount, setReplyCount] = React.useState(0)
  const [repliesLoading, setRepliesLoading] = React.useState(false)

  const replyRef = React.useRef(null)

  const handleReactToggle = (e) => {
    setReactBar(!reactBar)
  }

  const openReactBar = (e) => {
    if (!reply.isUser) {
      setReactBar(true)
    } 
  }

  const closeReactBar = (e) => {
    if (!reply.isUser) {
      setReactBar(false)
    }
  }

  const toggleShowReply = (e) => {
    setShowReply(!showReply)
  }

  const showReplyInput = () => {
    replyRef.current?.scrollIntoView({behavior: 'smooth', block: 'nearest'})
  }

  // Scoll to bottom of show reply
  React.useEffect(() => {
    if (showReply) {
      setTimeout(showReplyInput, 250)
    }
  }, [showReply])

  const handleReply = (e) => {
    setReply(e.target.value)
  }


  // post reply to review, fetch replies on send
  const handleSendReply = async (e) => {
    const body = {
      auth_token: getToken(),
      review_id: review.reviewId,
      reply: reply
    }
    
    try {
      const response = await apiFetch('POST', '/api/event/review/reply', body)
      setReply('')
      fetchReplies(0, 10)
    } catch (e) {
      console.log(e)
    }
  }

  // Function to fetch replies
  const fetchReplies = async (pageStart, maxResults) => {
    var loggedInId = ''
    // Get the id of the user logged in
    if (loggedIn()) {
      const profile_response = await apiFetch('GET',`/api/user/profile?auth_token=${getToken()}`)
      const user_response = await apiFetch('GET',`/api/user/search?email=${profile_response.email}`)

      loggedInId = user_response.user_id
    } 
    


    const body = {
      review_id: review.reviewId,
      page_start: pageStart,
      max_results: maxResults
    }
    const searchParams = new URLSearchParams(body)
    try {
      const response = await apiFetch('GET', `/api/event/reviews/replies?${searchParams}`, null) 
      if (response.replies.length > 0) {
        setRepliesLoading(true)
      }
      for (const i in response.replies) {
        const reply = response.replies[i]
        reply.isUser = (reply.authorId === loggedInId) 
      }  
      if (pageStart === 0) {
        setRepliesLoading(false)
        setReplies(response.replies)
        setReplyCount(response.num_results)
      } else {
        setRepliesLoading(false)
        setReplies(current => [response.replies, ...replies])
        setReplyCount(replyCount + response.num_results)
      }
    } catch (e) {
      console.log(e)
    }
  }

  // Initial Fetch of replies
  React.useEffect(() => {
    if (review.reviewId !== undefined) {
      fetchReplies(0, 10)
    }
  }, [review])


  return (
    <Box>
      <Box sx={{maxWidth: 700, width: '100%', display: 'flex', justifyContent: 'center', flexDirection: 'column', gap: 1}} onMouseLeave={closeReactBar}>
        <Box ref={innerRef} id={id} sx={{borderRadius: '50px',  backgroundColor: '#EEEEEE', p: 2}} >
          <Grid container sx={{height: '100%'}}>
            <Grid item xs={1} >
              <UserAvatar userId={review.authorId} size={50}/>
            </Grid>
            <Grid xs={11} item sx={{pl: 3}}>
              <Box sx={{display: 'flex', justifyContent: 'space-between', alignItems: 'center', pr: 1}}>
                <Typography sx={{fontWeight: 'bold', fontSize: 20}}>
                  {review.title}
                </Typography> 
                <Rating precision={0.5} defaultValue={review.rating} readOnly/>
              </Box>
              <Typography sx={{pt: '5px'}}>
                {review.text}
              </Typography>
            </Grid>
          </Grid>
          <br/>
          {loggedIn()
            ? <Grid container>
                <Grid item xs={1}>
                  <Box 
                    sx={{display: 'flex', pl:1}}
                  >
                    <IconButton
                      onClick={() => {
                        handleReactToggle();
                      }}
                    >
                      <AddReaction />
                    </IconButton>
                  </Box>
                </Grid>
                <Grid item xs={5}>
                  <ReactionsList reactions={review.reactions}/>
                </Grid>
                <Grid item xs={6}>
                  <Box sx={{display: 'flex', pr:1, justifyContent: 'flex-end', alignItems: 'center', height: '100%'}}>
                    <CentredBox>
                      <Button variant='text' sx={{textTransform: 'none', color: "#999999"}} startIcon={<ReplyIcon/>} onClick={toggleShowReply}>
                        Reply
                      </Button>
                    </CentredBox>
                  </Box>
                </Grid>
              </Grid>
            : <></>
          }
        </Box>
        <>
          {loggedIn()
            ? <Collapse in={reactBar} sx={{width: '40%', ml: 4}} onMouseLeave={closeReactBar}>
                <Box>
                  <ReactBar comment_id={review.reviewId}/>
                </Box>
              </Collapse>
            : <></>
          }
        </>  
      </Box>
      <Box sx={{mr: 10, ml: 10, pt: 2, display: 'flex', gap: 2, flexDirection: 'column'}}>
        {repliesLoading
          ? <CentredBox> <CircularProgress sx={{color: '#AE759F'}}/> </CentredBox>
          : <ReviewReplies replies={replies}/>
        }
        <Collapse in={showReply}>
          <Box sx={{display: 'flex', justifyContent: 'flex-end'}} ref={replyRef}>
            <Box sx={{display: 'flex', justifyContent: 'space-between', backgroundColor: '#CCCCCC', gap: 1, borderRadius: 30, width: '50%', p: 1, pr: 1, pl: 1}}>
              <ReplyInput value={reply} size="small" placeholder='Reply' fullWidth onChange={handleReply}/>
              <IconButton disabled={(reply.length === 0)} onClick={handleSendReply}>
                <SendIcon/>
              </IconButton>
            </Box>
            <br/>
          </Box>
        </Collapse>
      </Box>
    </Box>
  )
}