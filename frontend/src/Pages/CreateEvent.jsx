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

    const [description, setDescription] = React.useState(""); 

    const [newHost, setNewHost] = React.useState("");

    const [hostList, setHostList] = React.useState([{ host: "" }]);

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
        console.log(description);
    }

    const handleNewHost = (e) => {
        setNewHost(e.target.value); 
    }

    const addHost = (e) => {
        e.stopPropagation();
        e.nativeEvent.stopImmediatePropagation();
        const hostList_t = [...hostList]; 
        hostList_t.push({host: newHost});
        setHostList(hostList_t);
        console.log(hostList);
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
                    <Grid item xs={7}>
                        <Grid container spacing={2}>
                            <Grid item xs={12}>
                                <OutlinedInput placeholder="Event name" variant="outlined" sx={{paddingLeft: '15px', borderRadius: 2}} fullWidth={true}/>
                            </Grid>
                            <Grid item xs={12}>
                            <OutlinedInput placeholder="Address" variant="outlined" sx={{paddingLeft: '15px', borderRadius: 2}} fullWidth={true}/>
                            </Grid>
                            <Grid item xs={3}>
                            <OutlinedInput placeholder="Postcode" variant="outlined" sx={{paddingLeft: '15px', borderRadius: 2}} fullWidth={true}/>
                            </Grid>
                            <Grid item xs={4}>
                            <OutlinedInput placeholder="State" variant="outlined" sx={{paddingLeft: '15px', borderRadius: 2}} fullWidth={true}/>
                            </Grid>
                            <Grid item xs={5}>
                            <OutlinedInput placeholder="Country" variant="outlined" sx={{paddingLeft: '15px', borderRadius: 2}} fullWidth={true}/>
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
                                <OutlinedInput placeholder="New host" variant="outlined" onChange={handleNewHost} sx={{paddingLeft: '15px', borderRadius: 2}} fullWidth={true}/>
                            </Grid>
                            <Grid item xs={7}>
                                
                            </Grid>
                            <Grid item xs={3}>
                            <TkrButton variant="contained" onClick={addHost}>
                                Add Host
                            </TkrButton>
                                {hostList.map((value, key) => {                                    
                                    return (<div key={key}>
                                        {value.host}
                                    </div>)
                                })}
                            </Grid>
                            <Grid item xs={9}></Grid>


                            <Grid item xs={8}>
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
                        </Grid>
                    </Grid>
                    <Grid item xs={5}>

                    </Grid>
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
                    <OutlinedInput placeholder="New Host" variant="outlined" sx={{paddingLeft: '15px', borderRadius: 2}} fullWidth={false}/>
                    <TkrButton
                        variant="contained"
                        
                        >
                        Add Host
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
