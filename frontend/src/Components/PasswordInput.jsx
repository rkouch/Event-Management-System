import React from "react";

import OutlinedInput from '@mui/material/OutlinedInput';
import InputAdornment from '@mui/material/InputAdornment';
import IconButton from '@mui/material/IconButton';
import Visibility from '@mui/icons-material/Visibility';
import VisibilityOff from '@mui/icons-material/VisibilityOff';

import { FormInput, TextButton, TkrButton} from '../Styles/InputStyles';

import '../App.css';

import { apiFetch, setFieldInState, setToken } from '../Helpers';
import { FlexRow, Logo, H3 } from '../Styles/HelperStyles';
import HelperText from '../Components/HelperText';
import { Link, useNavigate } from "react-router-dom";
import StandardLogo from "../Components/StandardLogo";


export default function PasswordInput ({state, setState, placeholder='Password'}) {

  const handleChange = (e) => {
    setFieldInState('password', e.target.value, state, setState)
  }

  return (
    <OutlinedInput
      id="password"
      placeholder={placeholder}
      error={state.error}
      type={!state.visibility ? "password" : "text"}
      onChange={(e) => setFieldInState('password', e.target.value, state, setState)}
      sx={{borderRadius: 2}}
      endAdornment={
        <InputAdornment position="end">
          <IconButton
            aria-label="toggle password visibility"
            onClick={(e) => setFieldInState('visibility', !state.visibility, state, setState)}
            onMouseDown={(e) => e.preventDefault()}
            edge="end"
          >
            {state.visibility ? <VisibilityOff /> : <Visibility />}
          </IconButton>
        </InputAdornment>
      }
    />
  )
}