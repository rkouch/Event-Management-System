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
            console.log("Defaulted fetch response")
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

export const getUserData = async (body, setUserData) => {
  try {
    const response = await apiFetch('GET',`/api/user/profile?${body}`)
    const ret = {
      userName: response.user_name,
      firstName: response.first_name,
      lastName: response.last_name,
      profileDescription: response.profile_description,
      email: response.email,
      events: response.events
    }
    console.log(ret)
    setUserData(ret)
  } catch (error) {
    console.log(error)
  }
}