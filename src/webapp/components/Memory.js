import React from 'react';

const Memory = (props) => {
  return (
      <textarea readOnly value={props.memory} id="memory-view"/>
  );
};

export default Memory;
