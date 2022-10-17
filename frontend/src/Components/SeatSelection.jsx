import { Checkbox, FormControl, Grid, MenuItem, Select, Tooltip, Typography } from '@mui/material'
import React from 'react'
import { CentredBox } from '../Styles/HelperStyles'
import Collapse from '@mui/material/Collapse';
import { styled } from '@mui/material/styles';
import IconButton from '@mui/material/IconButton';
import ExpandMoreIcon from '@mui/icons-material/ExpandMore';
import PersonIcon from '@mui/icons-material/Person';
import PersonOutlineIcon from '@mui/icons-material/PersonOutline';
import { Box } from '@mui/system';
import { setFieldInState } from '../Helpers';

const ExpandMore = styled((props) => {
  const { expand, ...other } = props;
  return <IconButton {...other} />;
})(({ theme, expand }) => ({
  transform: !expand ? 'rotate(0deg)' : 'rotate(180deg)',
  marginLeft: 'auto',
  transition: theme.transitions.create('transform', {
    duration: theme.transitions.duration.shortest,
  }),
}));

// Vary this variable to make there be less per row or more
const numPerRow = 10

export default function SeatSelection ({section, index, sectionDetails, setSectionDetails}) {
  const [expanded, setExpanded] = React.useState(false)

  // Contain all seats
  const [seats, setSeats] = React.useState([])

  const [rowsList, setRowsList] = React.useState([])

  const [disableSeats, setDisableSeats] = React.useState(false)

  React.useEffect(() => {
    var seats_t = []
    for (var i = 1; i <= section.capacity; i++) {
      seats_t.push(section.section[0]+i)
    }
    setSeats(seats_t)
    const numRows = seats_t.length/10 + 1
    const rowsList_a = []
    for (var m = 0; m < numRows; m++) {
      const rowsList_t = seats_t.splice(0, 10)
      rowsList_a.push(rowsList_t)
    }
    setRowsList(rowsList_a)
  },[])

  const handleExpandClick = () => {
    setExpanded(!expanded);
  };

  const handleQuantityChange = (key, qty) => {
    if (qty > 0) {
      setExpanded(true)
    } else if (qty === 0) {
      setExpanded(false)
    }
    const new_sections = sectionDetails.map((value, key_m) => {
      if (key_m === key) {
        if (qty < value.seatsSelected.length) {
          var newSeatsSelected = value.seatsSelected.splice(0, qty)
          return {...value, quantity: qty, seatsSelected: newSeatsSelected}
        } else if (qty > value.seatsSelected.length) {
          setDisableSeats(false)
          return {...value, quantity: qty}
        } else {
          return {...value, quantity: qty, }
        }
      }
      return (value)
    })
    setSectionDetails(new_sections)
  }

  const handleSelectSeat = (checked, seatVal) => {
    const currentlySelected = section.seatsSelected
    // If seat is checked, then add to selected list
    if (checked) {
      currentlySelected.push(seatVal)
      currentlySelected.sort()
    } else {
      const index = currentlySelected.indexOf(seatVal)
      currentlySelected.pop(index)
    }
    section.seatsSelected = currentlySelected
    const newState = sectionDetails.map(obj => {
      if (obj === section) {
        return {...obj, seatsSelected: currentlySelected};
      }
      return obj
    })
    setSectionDetails(newState)
    console.log(currentlySelected)
    if (currentlySelected.length === section.quantity) {
      setDisableSeats(true)
    } else {
      setDisableSeats(false)
    }
  }
  
  function SeatRow ({row}) {
    return (
      <div>
        {row.map((value, key) => {
          return (
            <Tooltip key={key} title={value}>
              <Checkbox
                disabled={disableSeats && !section.seatsSelected.includes(value)}
                icon={<PersonOutlineIcon/>}
                checkedIcon={<PersonIcon/>}
                onClick={(e) => handleSelectSeat(e.target.checked, value)}
                checked={section.seatsSelected.includes(value)}
              />
            </Tooltip>
          )
        })}
      </div>
    )
  }

  return (
    <CentredBox sx={{pl: 10, pr: 10, flexDirection: 'column'}}>
      <Grid container  spacing={2} >
        <Grid item xs={10}>
          <Typography sx={{fontSize: 30, fontWeight: 'bold'}}>
            {section.section} - ${section.ticket_price}
          </Typography>
        </Grid>
        <Grid item xs={2}>
          <FormControl fullWidth>
            <Select
              value={section.quantity}
              onChange={(e) => {handleQuantityChange(index, e.target.value)}}
            >

              {section.seats.map((value, key) => {
                return (
                  <MenuItem key={key} value={value}>
                    {value}
                  </MenuItem>
                )
              })}
            </Select>
          </FormControl>
        </Grid>
        {(section.selectable && section.quantity > 0)
          ? <>
              <Grid item xs={8}>

              </Grid>
              <Grid item xs={4}>
                <Box sx={{display: 'flex', justifyContent: 'flex-end'}}>
                  <Typography
                    sx={{
                      p: '8px'
                    }}
                  >
                    Choose Seats
                  </Typography>
                  <ExpandMore
                    sx={{
                      m:0
                    }}
                    expand={expanded}
                    onClick={handleExpandClick}
                    aria-expanded={expanded}
                    aria-label="show more"
                  >
                    <ExpandMoreIcon />
                  </ExpandMore>
                </Box>
                
              </Grid>
            </>
          : <></>

        }
      </Grid>
      <Collapse in={expanded && section.selectable}>
        <CentredBox>
          {rowsList.map((value, key) => {
            return (
              <SeatRow key={key} row={value}>

              </SeatRow>
            )
          })}
        </CentredBox>
      </Collapse>
      <br/>
    </CentredBox>
  )
}