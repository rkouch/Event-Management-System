import { Typography } from '@mui/material';
import { Box } from '@mui/system';
import React from 'react'
import { alpha } from '@mui/system';

// Spotify Oauth token 
const token = 'BQCkr11utn29egcXbc6cEmudxJaubjMeXPw3xj1HwomBQfJp6GCu-aqMjABFgF7SgQ2CkrVRZWbspz9kE6GtPCUYql8Bcr5YOSApHYBFmOLjBXb0Jau4rpULvq8eDMfe_o0IFnRNwHTwh6dOEHeVvHCgrl7Ectue4hO6MCBpBed10qp7byvqEYKFhk-u'


export default function SpotifyPlayer({link, setValidURL, editable=false}) {
  var Spotify = require('spotify-web-api-js');
  var sApi = new Spotify();
  sApi.setAccessToken(token)
  const url = new URL(link);
  console.log(url.pathname.replace('/playlist/',''))

  const [error, setError] = React.useState(false)

  // Verify playlist
  sApi.getPlaylist(url.pathname.replace('/playlist/','')).then(
    function (data) {
      setError(false)
      if (editable) {
        setValidURL(true)
      }
    },
    function (err) {
      setError(true)
      if (editable) {
        setValidURL(false)
      }
    }
  )

  if (!error) {
    return (
      <iframe
        width={'100%'}
        height={'100%'}
        frameBorder={0}
        title="Spotify Web Player"
        src={`https://open.spotify.com/embed${url.pathname}?utm_source=generator`}
        style={{
          borderRadius: 8
        }}
        allow="clipboard-write; encrypted-media; fullscreen; picture-in-picture" 
        loading="lazy"
      />
    )
  } else {
    return (
      <Box sx={{p: 5, backgroundColor: alpha('#6A7B8A', 0.3), borderRadius: 3}}>
        <Typography sx={{textAlign: 'center', color: '#555555', fontWeight: 'bold', fontSize: 20}}>
          Can't find playlist. Try another URL
        </Typography>
      </Box>
    )
  }
}
