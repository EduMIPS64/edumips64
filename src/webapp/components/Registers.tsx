import React from 'react';

import type { Register, Registers } from '../simulator/protocol';
import BinaryValue from './BinaryValue';

interface RegisterRowProps {
  register: Register;
}

const RegisterRow = ({ register }: RegisterRowProps) => {
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

// Props match the Registers protocol type (spread from `registers` in Simulator).
const RegistersPanel = ({ gpr, fpu, special }: Registers) => {
  return (
    <div>
      <table id="registers">
        <tbody>
          {
            // Ugly way of using a single table to show both GPR and FPU registers.
            gpr.map((register, i) => (
              <tr key={i}>
                <RegisterRow register={register} />
                <RegisterRow register={fpu[i]} />
              </tr>
            ))
          }
          {special
            .filter((r) => r.name !== 'FCSR')
            .map((register) => (
              <tr key={register.name}>
                <RegisterRow register={register} />
              </tr>
            ))}
        </tbody>
      </table>
    </div>
  );
};

export default React.memo(RegistersPanel);
