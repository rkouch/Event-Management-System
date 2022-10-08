import React from  'react'
import { setFieldInState } from '../Helpers'
import { ContrastInput, ContrastInputWrapper } from '../Styles/InputStyles'

export default function ShadowInput({backgroundColor='#FFFFFF', state, field, setState, sx={}, defaultValue=''}) {

  const onChange = (e) => {
    setFieldInState(field, e.target.value, state, setState)
  }

  return (
    <ContrastInputWrapper>
      <ContrastInput fullWidth sx={sx} defaultValue={defaultValue} onChange={onChange}>
      </ContrastInput>
    </ContrastInputWrapper>
  )
}