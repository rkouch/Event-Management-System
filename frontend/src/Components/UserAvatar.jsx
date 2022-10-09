import React from 'react'
import Avatar from '@mui/material/Avatar';
import { getUserData } from '../Helpers';
import Tooltip from '@mui/material/Tooltip';

export default function UserAvatar({userId, size=24}) {
  const [userData, setUserData] = React.useState({
    userName: '',
    firstName: '',
    lastName: '',
    email: '',
    profilePicture: '',
    events: []
  })

  React.useEffect(()=> {
    getUserData(`user_id=${userId}`,setUserData)
  },[])

  return (
    <Tooltip title={`${userData.firstName} ${userData.lastName}`}>
      <Avatar sx={{height: size, width: size}}>{userData.firstName[0]}{userData.lastName[0]}</Avatar>
      {/* For later use with profile pictures */}
      {/* {(userData.profilePicture === '')
        ? <Avatar sx={{height: height, width: width}}>{userData.firstName[0]}{userData.lastName[0]}</Avatar>
        : <Avatar src={userData.profilePicture}/>
      } */}
    </Tooltip>
  )
}