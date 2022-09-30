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


const router = createBrowserRouter([
  {
    path: "/",
    element: <Landing/>
  },
  {
    path: "/register",
    element: <Register/>
  },
  {
    path: "/login",
    element: <Login/>
  }
])

function App() {  
  return (
    <div className="App">
      <RouterProvider router={router}>
        <ThemeProvider theme={TickrTheme}>
          {/* <Register/> */}
          
        </ThemeProvider>  
      </RouterProvider> 
    </div>
  );
}

export default App;
