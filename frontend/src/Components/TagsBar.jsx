import React from "react"
import AddIcon from '@mui/icons-material/Add';
import { Box } from "@mui/system";
import { CentredBox } from "../Styles/HelperStyles";
import ShadowInput from "./ShadowInput";
import Chip from '@mui/material/Chip';
import { borderRadius, styled, alpha } from '@mui/system';
import { ContrastInput, ContrastInputWrapper } from "../Styles/InputStyles";
import { setFieldInState, stringToColor } from "../Helpers";
import { Typography } from "@mui/material";
import TagIcon from '@mui/icons-material/Tag';

export default function TagsBar({tags, setTags=null, editable=false}) {
  const [newTag, setNewTag] = React.useState('')

  const addTag = () => {
    const tags_t = [...tags]
    tags_t.push(newTag);
    setTags(tags_t);
    setNewTag('')
  }

  const handleDelete = (tagToDelete, index) => () => {
    const tags_list = [...tags];
    tags_list.splice(index, 1);
    setTags(tags_list);
  };


  return (
    <Box>
      {editable
        ? <>
            <CentredBox sx={{width: 300, alignItems: 'center', gap: '10px'}}>
              <ContrastInputWrapper>
                <ContrastInput
                  value={newTag}
                  placeholder="Tag"
                  sx={{height: 40}}
                  onChange={(e) => setNewTag(e.target.value)}
                  startAdornment={<CentredBox sx={{pr: 1}}><TagIcon sx={{color: "rgba(0,0,0,0.45)"}}/></CentredBox>}
                />
              </ContrastInputWrapper>
              <Chip label="Add Tag" disabled={(newTag.length > 0) ? false : true} onClick={addTag} icon={<AddIcon/>}/>
            </CentredBox>
            <br/>
          </>
        : <></>
      }
      <Box 
        sx={{
          backgroundColor: editable ? alpha('#6A7B8A', 0.3) : "rgba(0,0,0,0)",
          padding: '10px',
          borderRadius: 3,
          display: 'flex',
          flexWrap: 'wrap',
          gap: '10px'
        }}
      >
        {editable
          ? <>
              {tags.map((data, key) => {
                return (
                  <Chip
                    key={key}
                    icon={<CentredBox ><TagIcon sx={{color: "#FFFFFF"}}/></CentredBox>}
                    label={data}
                    onDelete={handleDelete(data, key)}
                    sx={{
                      backgroundColor: stringToColor(data),
                      color: "#FFFFFF"
                    }}
                  />
                );
              })}
            </>
          : <>
              {tags.map((data, key) => {
                return (
                  <Chip
                    key={key}
                    icon={<CentredBox ><TagIcon sx={{color: "#FFFFFF"}}/></CentredBox>}
                    label={data}
                    sx={{
                      backgroundColor: stringToColor(data),
                      color: "#FFFFFF"
                    }}
                  />
                );
              })}
            </>
        }
        
      </Box>
    </Box>
  )
}