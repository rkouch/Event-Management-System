import React from 'react';
import { Route } from 'react-router';

// Function to make api calls
export const apiFetch = (method, route, TOKEN, body) => {
  const requestOptions = {
    method: method,
    headers: { 'Content-Type': 'application/json' },
    body: null,
  };

  if (method !== 'GET' && body !== null) {
    requestOptions.body = JSON.stringify(body);
  }

  if (TOKEN !== null) {
    requestOptions.headers.Authorization = `Bearer ${TOKEN}`;
  } else {
    console.log('empty token');
  }

  return new Promise((resolve, reject) => {
    fetch(`http://localhost:5005${route}`, requestOptions)
      .then((response) => {
        switch (response.status) {
          case 200:
            response.json().then((data) => {
              resolve(data);
            });
            break;
          case 400:
            console.log('responseError', response);
            response.json().then((data) => {
              console.log(data.error);
              reject(data.error);
            });
            break;
          case 403:
            response.json().then((data) => {
              reject(data.error);
            });
            break;
        }
      })
      .catch((response) => {
        console.log(response);
        response.json().then((data) => {
          resolve(data.error);
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
