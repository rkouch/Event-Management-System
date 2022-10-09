import React from 'react'
import Avatar from '@mui/material/Avatar';
import Tooltip from '@mui/material/Tooltip';
import Skeleton from '@mui/material/Skeleton';
import { useNavigate } from 'react-router-dom';
import IconButton from '@mui/material/IconButton';
import { apiFetch, getToken, getUserData} from '../Helpers';


export default function UserAvatar({userId, size=35}) {
  const navigate = useNavigate()

  const [userData, setUserData] = React.useState({
    userName: '',
    firstName: '',
    lastName: '',
    email: '',
    profilePicture: '',
    events: []
  })

  React.useEffect(()=> {
    if (userId.length !== 0) {
      getUserData(`user_id=${userId}`,setUserData)
    }
  },[userId])


  const handleClick = async (e) => {
    e.stopPropagation();
    e.nativeEvent.stopImmediatePropagation();
    console.log('viewing profile')
    try {
      const response = await apiFetch('GET',`/api/user/profile?auth_token=${getToken()}`)
      const response_2 = await apiFetch('GET',`/api/user/search?email=${response.email}`)
      if (userId === response_2.user_id) {
        window.open('/my_profile')
        // navigate(`/my_profile`)
      } else {
        // navigate(`/view_profile/${userId}`)
        window.open(`/view_profile/${userId}`)
      }
    } catch (e) {
      console.log(e)
    }
  }

  return (
    <IconButton disableRipple={true} onClick={handleClick}>
      <Tooltip title={`${userData.firstName} ${userData.lastName}`}>
        {(userData.userName !== '')
          ? <Avatar sx={{height: size, width: size}}>{userData.firstName[0]}{userData.lastName[0]}</Avatar>
            // For later use with profile pictures
            // {(userData.profilePicture === '')
            //   ? <Avatar sx={{height: height, width: width}}>{userData.firstName[0]}{userData.lastName[0]}</Avatar>
            //   : <Avatar src={userData.profilePicture}/>
            // }
          : <Skeleton variant="circular" width={size} height={size} />
        }
      </Tooltip>
    </IconButton>
    
  )
}