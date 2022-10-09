import React from "react"

import { apiFetch, checkValidEmail, getToken, getUserData, setFieldInState } from "../Helpers";
import Grid from "@mui/material/Unstable_Grid2";
import { H3 } from "../Styles/HelperStyles";
import ListItemText from "@mui/material/ListItemText";
import DeleteIcon from "@mui/icons-material/Delete";
import IconButton from "@mui/material/IconButton";
import { Box, FormLabel, List, ListItem } from "@mui/material";
import { borderRadius, styled, alpha } from '@mui/system';
import { ContrastInput, ContrastInputWrapper, DeleteButton, FormInput, TextButton, TkrButton } from '../Styles/InputStyles';
import ListItemIcon from '@mui/material/ListItemIcon';
import UserAvatar from "./UserAvatar";
import Skeleton from '@mui/material/Skeleton';

export default function AdminItem({index, adminID, removeAdmin}) {
  const [userData, setUserData] = React.useState({
    userName: '',
    firstName: '',
    lastName: '',
    email: '',
    profilePicture: '',
    events: []
  })

  React.useEffect(()=> {
    getUserData(`user_id=${adminID}`,setUserData)
  },[])

  return (
    <ListItem 
      secondaryAction={
        <IconButton
          edge="end"
          aria-label="delete"
          onClick={() => removeAdmin(index)}
        >
          <DeleteIcon />
        </IconButton>
      }
    >
      <ListItemIcon>
        <UserAvatar userId={adminID} size={50}/>
      </ListItemIcon>
      {(userData.userName === '')
        ? <Skeleton animation="wave"  width={"100%"}/>
        : <ListItemText primary={`@${userData.userName}`}/>
      }
      
    </ListItem>
  )
}