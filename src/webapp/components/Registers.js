import React from 'react';

import Typography from '@material-ui/core/Typography';
import Tooltip from '@material-ui/core/Tooltip';
import { makeStyles } from '@material-ui/core/styles';

const useStyles = makeStyles((theme) => ({
  binaryValue: {
    fontFamily: 'Menlo,Monaco,Courier New,monospace',
    fontSize: '0.6rem',
    padding: '0.1em 10px',
    textAlign: 'right',
  },
}));

// Component that shows a binary value.
const BinaryValue = ({ hexString, value }) => {
  const classes = useStyles();
  return (
    <Tooltip disableFocusListener title={value ?? ''}>
      <Typography className={classes.binaryValue}>{hexString}</Typography>
    </Tooltip>
  );
};

const Register = ({ register }) => {
  return (
    <>
      <td className="registerName">{register.name}</td>
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
                <td className="registerName">{register.name}</td>
                <td className="registerValue">{register.value}</td>
                <td />
                <td />
              </tr>
            ))}
        </tbody>
      </table>
    </div>
  );
};

export default Registers;
