import React from 'react';

import Typography from '@mui/material/Typography';
import Tooltip from '@mui/material/Tooltip';


// Component that shows a binary value.
const BinaryValue = ({ hexString, value }) => {
  return (
    <Tooltip disableFocusListener title={value ?? ''}>
      <Typography className='binary-value'>{hexString}</Typography>
    </Tooltip>
  );
};

const Register = ({ register }) => {
  return (
    <>
      <td className="registerName">{register.name}</td>
      <td className="registerName">
        {register.alias ? `(${register.alias})` : ''}
      </td>
      <td>
        <BinaryValue hexString={register.hexString} value={register.value} />
      </td>
    </>
  );
};

const Registers = ({ gpr, fpu, special }) => {
  return (
    <div>
      <table id="registers">
        <tbody>
          {
            // Ugly way of using a single table to show both GPR and FPU registers.
            gpr.map((register, i) => (
              <tr key={i}>
                <Register register={register} />
                <Register register={fpu[i]} />
              </tr>
            ))
          }
          {special
            .filter((r) => r.name != 'FCSR')
            .map((register) => (
              <tr key={register.name}>
                <Register register={register} />
              </tr>
            ))}
        </tbody>
      </table>
    </div>
  );
};

export default Registers;
