import React from 'react'
import { styled } from '@mui/system';
import Header from '../Components/Header'
import { BackdropNoBG } from '../Styles/HelperStyles'
import OutlinedInput from '@mui/material/OutlinedInput';
import { AdapterMoment } from '@mui/x-date-pickers/AdapterMoment';
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider';
import { DateTimePicker } from '@mui/x-date-pickers/DateTimePicker';
import TextField from '@mui/material/TextField';
import dayjs from 'dayjs';
import FormControl, { useFormControl } from '@mui/material/FormControl';
import FormHelperText from '@mui/material/FormHelperText';
import { setFieldInState } from '../Helpers';
import Grid from '@mui/material/Unstable_Grid2';
import { H3 } from '../Styles/HelperStyles';
import { TkrButton } from '../Styles/InputStyles';
import ListItemText from '@mui/material/ListItemText';
import DeleteIcon from '@mui/icons-material/Delete';
import IconButton from '@mui/material/IconButton';

export const EventForm = styled('div')({
    display: 'flex',
    justifyContent: 'center',
    flexDirection: 'column',
    paddingTop: '15px',
    paddingBottom: '15px',
    paddingLeft: '15px',
    paddingRight: '15px',
    margin: 'auto',
    alignItems: 'center',
    gap: '10px',
});

export default function CreateEvent({}) {
    // States 
    
    const [start, setStartValue] = React.useState(dayjs('2014-08-18T21:11:54'));

    const [end, setEndValue] = React.useState({
        end: dayjs('2014-08-18T21:11:54'),
        error: false, 
        errorMsg: ''
    });

    const [eventName, setEventName] = React.useState(""); 

    const [address, setAddress] = React.useState(""); 

    const [postcode, setPostcode] = React.useState(""); 

    const [state, setState] = React.useState(""); 

    const [country, setCountry] = React.useState(""); 

    const [description, setDescription] = React.useState(""); 

    const [newAdmin, setNewAdmin] = React.useState("");

    const [adminList, setAdminList] = React.useState([{ admin: "" }]);

    const [seatingList, setSeatingList] = React.useState([{ sectionName: "", sectionCapacity: 0 }])

    const handleEventNameChange = (e) => {
        setEventName(e.target.value)
    }

    const handleAddressChange = (e) => {
        setAddress(e.target.value)
    }

    const handlePostcodeChange = (e) => {
        setPostcode(e.target.value)
    }

    const handleStateChange = (e) => {
        setState(e.target.value)
    }

    const handleCountryChange = (e) => {
        setCountry(e.target.value)
    }

    const handleStartChange = (newValue) => {
        setStartValue(newValue);
        if (end.end < start) {
            setFieldInState('error', true, end, setEndValue);
            setFieldInState('errorMsg', 'End date must be after start date', end, setEndValue);
        } else {
            setFieldInState('error', false, end, setEndValue);
            setFieldInState('errorMsg', '', end, setEndValue);
        }
    };    

    const handleEndChange = (newValue) => {
        setFieldInState('end', newValue, end, setEndValue);
        console.log(end.end);
        if (end.end < start) {
            setFieldInState('error', true, end, setEndValue);
            setFieldInState('errorMsg', 'End date must be after start date', end, setEndValue);
        } else {
            setFieldInState('error', false, end, setEndValue);
            setFieldInState('errorMsg', '', end, setEndValue);
        }
    };    

    const handleDescription = (e) => {
        setDescription(e.target.value);
    }

    const handleNewAdmin = (e) => {
        setNewAdmin(e.target.value); 
    }

    const addAdmin = (e) => {
        e.stopPropagation();
        e.nativeEvent.stopImmediatePropagation();
        const adminList_t = [...adminList]; 
        adminList_t.push({admin: newAdmin});
        setAdminList(adminList_t);
    }

    const removeAdmin = (index) => {
        const admin_list = [...adminList]; 
        admin_list.splice(index, 1);
        setAdminList(admin_list);
    }

    const addSection = (e) => {
        setSeatingList([...seatingList, { sectionName: "", sectionCapacity: 0 }])
        console.log(seatingList)
    }

    const removeSeating = (index) => {
        const list = [...seatingList]
        list.splice(index, 1)
        setSeatingList(list)
    }

    const handleSectionChange = (e, index) => {
        const list = [...seatingList]
        list[index].sectionName = e.target.value 
        setSeatingList(list)
        console.log(seatingList)
    }

    const handleCapacityChange = (e, index) => {
        const {name, value} = e.target
        const list = [...seatingList]
        list[index][name] = value 
        setSeatingList(list)
        console.log(seatingList)
    }

    return (
        <div>
            <BackdropNoBG>
            <Header/>
            <H3 sx={{fontSize: '30px'}}>
                Create Event
            </H3>
            <div>
            <EventForm>
                <Grid container spacing={2} sx={{marginLeft: 5, marginRight: 5, maxWidth: '1200px', width: '100%'}}>
                    <Grid item xs={12}>
                        Event Cover photo
                    </Grid>
                    <Grid item xs={6}>
                        <Grid container spacing={2}>
                            <Grid item xs={12}>
                                <OutlinedInput placeholder="Event name" variant="outlined" sx={{paddingLeft: '15px', borderRadius: 2}} fullWidth={true} onChange={handleEventNameChange}/>
                            </Grid>
                            <Grid item xs={12}>
                            <OutlinedInput placeholder="Address" variant="outlined" sx={{paddingLeft: '15px', borderRadius: 2}} fullWidth={true} onChange={handleAddressChange}/>
                            </Grid>
                            <Grid item xs={3}>
                            <OutlinedInput placeholder="Postcode" variant="outlined" sx={{paddingLeft: '15px', borderRadius: 2}} fullWidth={true} onChange={handlePostcodeChange}/>
                            </Grid>
                            <Grid item xs={4}>
                            <OutlinedInput placeholder="State" variant="outlined" sx={{paddingLeft: '15px', borderRadius: 2}} fullWidth={true} onChange={handleStateChange}/>
                            </Grid>
                            <Grid item xs={5}>
                            <OutlinedInput placeholder="Country" variant="outlined" sx={{paddingLeft: '15px', borderRadius: 2}} fullWidth={true} onChange={handleCountryChange}/>
                            </Grid>

                            <LocalizationProvider dateAdapter={AdapterMoment}>
                                <Grid item xs={6}>
                                    
                                        <FormControl fullWidth={false}>
                                            <DateTimePicker
                                                label="Event Start"
                                                value={start}
                                                onChange={handleStartChange}
                                                inputFormat="DD/MM/YYYY HH:mm"
                                                renderInput={(params) => <TextField {...params} />}
                                                />
                                        </FormControl>
                                </Grid>
                                <Grid item xs={6}>
                                    <FormControl fullWidth={false}>
                                            <DateTimePicker
                                            label="Event End"
                                            value={end.end}
                                            onChange={handleEndChange}
                                            inputFormat="DD/MM/YYYY HH:mm"
                                            renderInput={(params) => <TextField {...params} />}
                                            />
                                            <FormHelperText>{end.errorMsg}</FormHelperText>
                                        </FormControl>
                                    
                                </Grid>
                            </LocalizationProvider>
                            

                            <Grid item xs={5}>
                                <OutlinedInput placeholder="New admin" variant="outlined" onChange={handleNewAdmin} sx={{paddingLeft: '15px', borderRadius: 2}} fullWidth={true}/>
                            </Grid>
                            <Grid item xs={7}>
                            <TkrButton variant="contained" onClick={addAdmin}>
                                Add Admin
                            </TkrButton>
                            </Grid>
                            <Grid item xs={12}>
                            
                            Admin List:
                                {adminList.map((value, key) => {                                    
                                    // return (<div key={key}>
                                    //     {value.admin}
                                    // </div>)
                                    return (
                                    <div key={key}>
                                        <Grid container spacing={1}>
                                            <Grid item xs={7}>
                                                <ListItemText
                                                    primary={value.admin}
                                                />  
                                            </Grid>
                                            <Grid item xs={5}>
                                                { adminList.length != 0 ? <IconButton edge="end" aria-label="delete" onClick={() => removeAdmin(key)}>
                                                    <DeleteIcon />
                                                </IconButton> : <></>}
                                            </Grid>
                                        </Grid>
                                        
                                    </div>
                                    )
                                })}
                            </Grid>


                            <Grid item xs={12}>
                            <TextField
                            id="standard-multiline-static"
                            label="Event Description"
                            multiline
                            rows={4}
                            defaultValue=""
                            variant="filled"
                            onChange={handleDescription}
                            fullWidth
                            />
                            </Grid>
                        </Grid>
                    </Grid>
                    <Grid item xs={1}></Grid>
                    <Grid item xs={5}>
                        <h3> Ticket Allocations </h3>
                        {/* <OutlinedInput placeholder="Country" variant="outlined" sx={{paddingLeft: '15px', borderRadius: 2}} fullWidth={true}/> */}
                        {seatingList.map((value, index) => {
                            return (
                            <div key={index}>
                                <Grid container spacing={1}>
                                    <Grid item xs={7}>
                                        <OutlinedInput 
                                            placeholder="Section Name" 
                                            variant="outlined" 
                                            sx={{paddingLeft: '15px', borderRadius: 2}} 
                                            fullWidth={true}
                                            onChange= {(e) => handleSectionChange(e, index)}  
                                        />
                                    </Grid>
                                    <Grid item xs={3}>
                                    <OutlinedInput placeholder="Spots" variant="outlined" sx={{paddingLeft: '15px', borderRadius: 2}} fullWidth={true}/>
                                    </Grid>
                                    <Grid item xs={2}>
                                        
                                        <IconButton edge="end" aria-label="delete" onClick={() => removeSeating(index)}>
                                            <DeleteIcon />
                                        </IconButton>
                                    </Grid>
                                </Grid>
                                
                                {(seatingList.length - 1 === index) && 
                                (
                                    <TkrButton variant="contained" onClick={addSection}>
                                        Add Section
                                    </TkrButton>
                                )}
                            </div>                                
                            )
                        })}
                        {seatingList.length === 0 ? <TkrButton variant="contained" onClick={addSection}>
                                        Add Section
                        </TkrButton> : <></>}
                    </Grid>
                    <TkrButton variant="contained">
                        Create Event
                    </TkrButton>
                </Grid>

                {/* <Grid container spacing={2} sx={{paddingLeft: 5, paddingRight: 5}}>
                    <Grid xs={5}>
                        <OutlinedInput placeholder="Event name" variant="outlined" sx={{paddingLeft: '15px', borderRadius: 2}} fullWidth={true}/>
                        <br/>
                        <OutlinedInput placeholder="Address" variant="outlined" sx={{paddingLeft: '15px', borderRadius: 2}} fullWidth={true}/>
                        <OutlinedInput placeholder="State" variant="outlined" sx={{paddingLeft: '15px', borderRadius: 2}} fullWidth={false}/>
                        <OutlinedInput placeholder="Postcode" variant="outlined" sx={{paddingLeft: '15px', borderRadius: 2}} fullWidth={false}/>
                        <OutlinedInput placeholder="Country" variant="outlined" sx={{paddingLeft: '15px', borderRadius: 2}} fullWidth={false}/>
                    </Grid>

                         <Grid xs={7}>
                        <OutlinedInput placeholder="State" variant="outlined" sx={{paddingLeft: '15px', borderRadius: 2}} fullWidth={true}/>
                    </Grid> }

                    <Grid xs={6}>
                        <LocalizationProvider dateAdapter={AdapterMoment}>
                            <FormControl fullWidth={false}>
                                <DateTimePicker
                                    label="Event Start"
                                    value={start}
                                    onChange={handleStartChange}
                                    inputFormat="DD/MM/YYYY HH:mm"
                                    renderInput={(params) => <TextField {...params} />}
                                    />
                            </FormControl>
                            

                            <FormControl fullWidth={false}>
                                <DateTimePicker
                                label="Event End"
                                value={end.end}
                                onChange={handleEndChange}
                                inputFormat="DD/MM/YYYY HH:mm"
                                renderInput={(params) => <TextField {...params} />}
                                />
                                <FormHelperText>{end.errorMsg}</FormHelperText>
                            </FormControl>
                        </LocalizationProvider>
                    </Grid>
                    
                    <Grid xs={6}>
                    <OutlinedInput placeholder="New Admin" variant="outlined" sx={{paddingLeft: '15px', borderRadius: 2}} fullWidth={false}/>
                    <TkrButton
                        variant="contained"
                        
                        >
                        Add Admin
                    </TkrButton>
                    </Grid>

                    <Grid xs={5}>
                        <TextField
                        id="standard-multiline-static"
                        label="Event Description"
                        multiline
                        rows={3}
                        defaultValue=""
                        variant="filled"
                        onChange={handleDescription}
                        fullWidth
                        />
                    </Grid>
                

                
                </Grid> */}
            </EventForm>
            </div>
            
            </BackdropNoBG>
        </div>
    )

}
