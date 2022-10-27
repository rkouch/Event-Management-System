import React from 'react';
import { Route } from 'react-router';

// Function to make api calls
export const apiFetch = (method, route, body) => {
  const requestOptions = {
    method: method,
    headers: { 'Content-Type': 'application/json' },
    body: null,
  };

  if (method !== 'GET' && body !== null) {
    requestOptions.body = JSON.stringify(body);
  }

  return new Promise((resolve, reject) => {
    fetch(`${route}`, requestOptions)
      .then((response) => {
        switch (response.status) {
          case 200:
            response.json().then((data) => {
              resolve(data);
            });
            break;
          case 400:
            response.json().then((data) => {
              console.log(data);
              reject(data);
            });
            break;
          case 403:
            response.json().then((data) => {
              reject(data);
            });
            break;
          default:
            reject("Defaulted fetch response")
        }
      })
      .catch((response) => {
        console.log(response);
        response.json().then((data) => {
          resolve(data);
        });
      });
  });
};

// Function to set a specific field within a state
export const setFieldInState = (field, value, state, setState) => {
  const stateCopy = state;
  stateCopy[field] = value;
  setState({
    ...state,
    stateCopy,
  });
}

export const setToken = (token) => {
  if (token == null) {
    localStorage.removeItem('token');
  } else {
    localStorage.setItem('token', token);
  }
}

export const getToken = () => {
  return localStorage.getItem('token');
}

export const loggedIn = () => {
  return (localStorage.getItem('token') != null)
}

export function fileToDataUrl (file) {
  const validFileTypes = ['image/jpeg', 'image/png', 'image/jpg']
  const valid = validFileTypes.find(type => type === file.type);
  // Bad data, let's walk away.
  if (!valid) {
    throw Error('provided file is not a png, jpg or jpeg image.');
  }

  const reader = new FileReader();
  const dataUrlPromise = new Promise((resolve, reject) => {
    reader.onerror = reject;
    reader.onload = () => resolve(reader.result);
  });
  reader.readAsDataURL(file);
  return dataUrlPromise;
}

// Either use
// auth_token=...
// user_id=....
export const getUserData = async (body, setUserData=null) => {
  try {
    const response = await apiFetch('GET',`/api/user/profile?${body}`)
    const ret = {
      userName: response.user_name,
      firstName: response.first_name,
      lastName: response.last_name,
      profileDescription: response.profile_description,
      email: response.email,
      events: response.events,
      profilePicture: response.profile_picture
    }
    if (setUserData != null) {
      setUserData(ret)
    } else {
      return ret
    }
  } catch (error) {
    console.log(error)
  }
}

export const getEventData = async (eventId, setEventData=null) => {
  try {
    const response = await apiFetch('GET', `/api/event/view?event_id=${eventId}`, null)
    sortSection(response.seating_details)
    setEventData(response)
  } catch (error) {
    console.log(error)
  }
}

export const passwordCheck = (password) => {
  var hasUpper = password.match(/[A-Z]/);
  var hasDigit = password.match(/[0-9]/);
  var hasSpecial = password.match(/[!@#$%^&*]/);
  var hasLength = (password.length >= 8);

  var validPassword = true;
  var errorMsg = 'Password must contain';

  if (!hasUpper) {
    errorMsg = errorMsg + ' an uppercase character';
    validPassword = false;
  } 
  if (!hasDigit) {
    if (errorMsg !== 'Password must contain') {
      errorMsg = errorMsg + ', a digit';
    } else {
      errorMsg = errorMsg + ' a digit';
    }
    validPassword = false;
  } 
  if (!hasSpecial) {
    if (errorMsg !== 'Password must contain') {
      errorMsg = errorMsg + ', a special character';
    } else {
      errorMsg = errorMsg + ' a special character';
    }
    validPassword = false;
  } 

  if (!hasLength) {
    if (errorMsg !== 'Password must contain') {
      errorMsg = errorMsg + ', 8 characters';
    } else {
      errorMsg = errorMsg + ' 8 characters';
    }
    validPassword = false;
  } 

  return validPassword
}

export const checkValidEmail = (email) => {
  var validEmail = /^(([^<>()[\]\.,;:\s@\"]+(\.[^<>()[\]\.,;:\s@\"]+)*)|(\".+\"))@(([^<>()[\]\.,;:\s@\"]+\.)+[^<>()[\]\.,;:\s@\"]{2,})$/i;
  return email.match(validEmail)
}

export function stringToColor(string) {
  // Custom colouring
  if (string === "food") {
    return "#eb7e63"
  }

  let hash = 0;
  let i;

  /* eslint-disable no-bitwise */
  for (i = 0; i < string.length; i += 1) {
    hash = string.charCodeAt(i) + ((hash << 5) - hash);
  }

  let color = '#';

  for (i = 0; i < 3; i += 1) {
    const value = (hash >> (i * 8)) & 0xff;
    color += `00${value.toString(16)}`.slice(-2);
  }
  /* eslint-enable no-bitwise */

  return color;
}

export const checkIfUser= async (userId, setState) => {
  try {
    const response = await apiFetch('GET',`/api/user/profile?auth_token=${getToken()}`)
    const response_2 = await apiFetch('GET',`/api/user/search?email=${response.email}`)
    if (userId === response_2.user_id) {
      setState(true)
      // navigate(`/my_profile`)
    } 
  } catch (e) {
    console.log(e)
  }
}

export function doNothing() {  
}

export const getTicketIds = async (event_id, setTicketIds) => {
  const paramsObj = {
    auth_token: getToken(),
    event_id: event_id
  }
  const searchParams = new URLSearchParams(paramsObj)
  try {
    const response = await apiFetch('GET', `/api/event/bookings?${searchParams}`, null)
    await setTicketIds([])
    await setTicketIds(response.tickets)
    return response.tickets
  } catch (e) {
    console.log(e)
  }
}

// Sort a section based upon if it has seats and then by alphabetically by section name
export const sortSection = (section) => {
  section.sort((a,b) => (a.hasSeats && !b.hasSeats) ? 1: (a.hasSeats === b.hasSeats) ? ((a.section.localeCompare(b.section)) ? 1 : -1) : -1)
}