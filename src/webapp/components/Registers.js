import React from 'react';

const Registers = ({ gpr, fpu, special }) => {
  return (
    <div>
      <table id="registers">
        <tbody>
          {
            // Ugly way of using a single table to show both GPR and FPU registers.
            gpr.map((register, i) => (
              <tr key={register.name}>
                <td className="registerName">{register.name}</td>
                <td className="registerValue">{register.value}</td>
                <td className="registerName">{fpu[i].name}</td>
                <td className="registerValue">{fpu[i].value}</td>
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
