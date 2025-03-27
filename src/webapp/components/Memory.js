import React from 'react';

import Typography from '@mui/material/Typography';
import Tooltip from '@mui/material/Tooltip';


// Component that shows a binary value.
const BinaryValue = ({ hexString, value }) => {
  return (
      <Tooltip disableFocusListener title={value ?? ''}>
        <Typography sx={{
          fontFamily: "Menlo, Monaco, 'Courier New', monospace",
          padding: '0.1em 10px',
          textAlign: 'right',
        }}>
          {hexString}
        </Typography>
      </Tooltip>
  );
};


const MemoryElement = ({memoryelement}) => {
  return (
      <>
          <td className="elementName">{memoryelement.address}</td>
      </>
  );
};

const Memory = ({ cells = [], instructions =[]}) => {
    return (
        <div>
            <table id="memory">
                <tbody>
                {
                    cells.map((memoryelement, i) => (
                        <tr key={i}>
                            <MemoryElement memoryelement={memoryelement} />
                            <MemoryElement memoryelement={cells[i]} />
                        </tr>
                    ))
                }
                {
                    instructions.map((memoryelement, i) => (
                        <tr key={i}>
                            <MemoryElement memoryelement={memoryelement} />
                            <MemoryElement memoryelement={instructions[i]} />
                        </tr>
                    ))
                }
                </tbody>
            </table>
        </div>
    );
};

export default Memory;
