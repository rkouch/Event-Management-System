import { Box } from '@mui/system'
import React from 'react'
import ReplyCard from './ReplyCard'

export default function ReviewReplies({replies}){
  return (
    <Box sx={{display: 'flex', flexDirection: 'column', gap: 2}}>
      {replies.map((reply, key) => {
        return (
          <ReplyCard key={key} reply={reply}/>
        )
      })}
    </Box>
  )
}