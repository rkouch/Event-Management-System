import { Alert, Collapse, Divider, Grid, Rating, Typography } from '@mui/material'
import { Box } from '@mui/system'
import React from 'react'
import { apiFetch, getToken } from '../Helpers'
import { CentredBox, ScrollableBox } from '../Styles/HelperStyles'
import { ContrastInput, ContrastInputWrapper, TkrButton } from '../Styles/InputStyles'
import { TestReview1, TestReview2, TestReview3, TestReview4, TestReview5 } from '../Test/TestData'
import ReviewCard from './ReviewCard'
import ShadowInput from './ShadowInput'

export default function EventReview({isAttendee, event_id}) {

  const [reviewTitle, setReviewTitle] = React.useState('')
  const [reviewText, setReviewText] = React.useState('')
  const [rating, setRating] = React.useState(0)
  
  const [error, setError] = React.useState(false)
  const [errorMsg, setErrorMsg] = React.useState('')
  const bottomRef = React.useRef(null)
  const bottomReviewRef = React.useRef(null)
  const topRef = React.useRef(null)

  const [reviews, setReviews] = React.useState([])
  const [postReview, setPostReview] = React.useState(false)
  const [initFetch, setInitFectch] = React.useState(false)

  const [reviewNum, setReviewNum] = React.useState(0)
  const [moreReviews, setMoreReviews] = React.useState(false)

  React.useEffect(() => {
    // After posting a review, scroll to the bottom and refresh reviews
    if (postReview) {
      console.log('Posted a review')
      fetchReviews(0, 10)
      bottomRef.current?.scrollIntoView({behavior: 'smooth'});
      setPostReview(false)
    }
  }, [postReview]);

  // Fetch Reviews
  React.useEffect(() => {
    fetchReviews(0, 10)
  }, [])


  // On initial review fetch, scroll to bottom
  React.useEffect(() => {
    if (initFetch) {
      const element = document.getElementById('reviews');
      element.scrollTop = element.scrollHeight;
      setInitFectch(false)
    }
  }, [initFetch])

  const scrollBottomReviews = () => {
    const element = document.getElementById('reviews');
    element.scrollTop = element.scrollHeight;
    setInitFectch(false)
  }

  setTimeout(scrollBottomReviews, 1000)

  // Async function to request for reviews
  const fetchReviews = async (pageStart, maxResults) => {
    try {
      const params = {
        event_id: event_id,
        page_start: pageStart,
        max_results: maxResults
      }
      const searchParams = new URLSearchParams(params)
      const response = await apiFetch('GET', `/api/event/reviews?${searchParams}`, null)
      console.log(response.reviews)
      setReviewNum(reviewNum + response.reviews.length)
      setMoreReviews((maxResults < response.reviews.length || (reviewNum+response.reviews.length === response.num_results)))

      if (pageStart === 0) {
        setReviews(response.reviews)
      } else {
        setReviews(current => [response.reviews, ...reviews])
      }
    } catch (e) {
      setInitFectch(true)
    }
  }

  // Handle posting of review
  const handlePost = async () => {
    // Check for empty fields, display error if any
    var errorStatus = false
    if (reviewTitle.length <= 0) {
      errorStatus = true
    }
    if (reviewText.length <= 0) {
      errorStatus = true
    }
    
    if (errorStatus) {
      setError(true)
      setErrorMsg('Please fill in required fields.')
      return
    }

    try {
      const body = {
        event_id: event_id,
        auth_token: getToken(),
        title: reviewTitle,
        text: reviewText,
        rating: rating,
      }
      console.log(body)
      const response = await apiFetch('POST', '/api/event/review/create', body)
      setPostReview(true)
      window.location.reload();
    } catch (e) {
      setPostReview(false)
      setError(true)
      setErrorMsg(e.reason)
    }
  }

  return (
    <Box sx={{width: '100%'}} ref={topRef}>
      <br/>
      <br/>
      <br/>
      <br/>
      <br/>
      <Divider sx={{ml: 5, mr:5}}>
        <Typography sx={{fontSize: 20, fontWeight: 'bold'}}>
          Event Reviews
        </Typography>
      </Divider>
      <br/>
      <ScrollableBox sx={{display: 'flex', justifyContent: 'flex-start', ml: 15, mr:15, flexDirection: 'column', gap: 3, maxHeight: 1000}} id='reviews'>
        {reviews.map((review, key) => {
          if (key === reviews.length-1) {
            return (
              <ReviewCard key={key} review={review} innerRef={bottomReviewRef}/>
            )
          } else {
            return (
              <ReviewCard key={key} review={review}/>
            )
          }
        })}
      </ScrollableBox>
      {isAttendee
        ? <>
            <br/>
            <Divider sx={{ml: 20, mr: 20}}>Leave a Review</Divider>
            <br/>
            <Box sx={{display: 'flex', justifyContent: 'center'}} ref={bottomRef}>
              <Grid container sx={{maxWidth: 700}}>
                <Grid item xs={10}>
                  <Grid container>
                    <Grid item xs={9}>
                      <ContrastInputWrapper sx={{width: '100%', height: '100%'}}>
                        <ContrastInput
                          fullWidth
                          required
                          placeholder="Review Title"
                          sx={{fontWeight: 'bold'}}
                          onChange={(e) => {
                            setReviewTitle(e.target.value)
                            setError(false)
                            setErrorMsg('')
                          }}
                        />
                      </ContrastInputWrapper>
                    </Grid>
                    <Grid item xs={3} sx={{display: 'flex', alignItems: 'center'}}>
                      <ContrastInputWrapper 
                        sx={{
                          display: 'flex', 
                          justifyContent: 'flex-end', 
                          alignItems: 'center',
                          height: '98%',
                          width: '99%',
                        }}
                      >
                        <Rating sx={{pr: '14px'}} precision={0.5} onChange={(e, value) => {setRating(value)}}/>
                      </ContrastInputWrapper>
                    </Grid>
                  </Grid>
                  <ContrastInputWrapper sx={{width: '100%', }}>
                    <ContrastInput
                      multiline
                      rows={4}
                      fullWidth
                      placeholder="Review..."
                      onChange={(e) => {
                        setReviewText(e.target.value)
                        setError(false)
                        setErrorMsg('')
                      }}
                    />
                  </ContrastInputWrapper>
                </Grid>
                <Grid item xs>
                  <TkrButton sx={{width: '100%', height: '100%'}} onClick={handlePost}>
                    Post
                  </TkrButton>
                </Grid>
              </Grid>
            </Box>
            <CentredBox sx={{pt: 1}}>
              <Collapse in={error}>
                <Alert severity="error">{errorMsg}.</Alert>
              </Collapse>
            </CentredBox>
          </>
        : <></>

      }

    </Box> 
  )
}