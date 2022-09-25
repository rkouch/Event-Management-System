import React from 'react';

import TextField from '@mui/material/TextField';

export default function PasswordInput ({id, label}) {

  return (
    <TextField id={id} label={label} variant="outlined" type="password"/>
  )
}