import logo from './logo.svg';
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
import Profile from './Pages/Profile';
import PageNotFound from './Pages/PageNotFound';
import ChangePassword from './Pages/ChangePassword';
import { getToken, loggedIn } from './Helpers';
import RequestChangePassword from './Pages/RequestChangePassword';
import CreateEvent from './Pages/CreateEvent';

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
    path: "/change_password/:resetToken",
    element: <ChangePassword/>
  },
  {
    path: "error",
    element: <PageNotFound/>
  },
  {
    path: "/request_change_password",
    element: <RequestChangePassword/>
  }
])

function App() {  
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
