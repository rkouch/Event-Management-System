import logo from './logo.svg';
import React from 'react'
import './App.css';
import Register from './Pages/Register';
import Login from './Pages/Login';
import { ThemeProvider, createTheme } from '@mui/material/styles';
import { TickrTheme } from './Themes';
import {
  BrowserRouter,
  createBrowserRouter,
  RouterProvider,
} from "react-router-dom";
import Landing from './Pages/Landing';
import CreateEvent from './Pages/CreateEvent';
import Profile from './Pages/Profile';
import PageNotFound from './Pages/PageNotFound';
import ChangePassword from './Pages/ChangePassword';
import { apiFetch, getToken, getUserData, loggedIn } from './Helpers';
import RequestChangePassword from './Pages/RequestChangePassword';
import ViewEvent from './Pages/ViewEvent';
import EditEvent from './Pages/EditEvent';
import PurchaseTicket from './Pages/PurchaseTickets';
import Checkout from './Pages/Checkout';
import ViewTicket from './Pages/ViewTicket';


function App() {  
  const [ticketOrder, setTicketOrder] = React.useState({})

  const router = createBrowserRouter([
    {
      path: "/",
      element: <Landing/>,
      // errorElement: <PageNotFound/>
    },
    {
      path: "/register",
      element: <Register/>
    },
    {
      path: "/login",
      element: <Login/>
    },
    {
      path: "/create_event",
      element: <CreateEvent/>
    },
    {
      path: "/my_profile",
      element: <Profile editable={true}/>
    },
    {
      path: "/change_password",
      element: <ChangePassword/>
    },
    {
      path: "/change_password/:email/:resetToken",
      element: <ChangePassword/>
    },
    {
      path: "error",
      element: <PageNotFound/>
    },
    {
      path: "/request_change_password",
      element: <RequestChangePassword/>
    },
    {
      path: "/view_event/:event_id",
      element: <ViewEvent/>
    },
    {
      path: "/view_profile/:user_id",
      element: <Profile editable={false}/>
    },
    {
      path: "/edit_event/:event_id",
      element: <EditEvent/>
    },
    {
      path: "/purchase_ticket/:event_id",
      element: <PurchaseTicket setTicketOrder={setTicketOrder} ticketOrder={ticketOrder}/>
    },
    {
      path: "/view_ticket/:event_id",
      element: <ViewTicket/>
    }
  ])

  return (
    <div className="App">
      <ThemeProvider theme={TickrTheme}>
        <RouterProvider router={router}>
         
        </RouterProvider> 
      </ThemeProvider> 
      
    </div>
  );
}

export default App;
