import React from 'react';
import { Typography } from '@mui/material';

const Memory = (props) => {
  return (
      <textarea readOnly value={props.memory} id="memory-view"/>
  );
};

export default Memory;
