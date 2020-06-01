import React from 'react';

const Stage = ({ name, stage, alwaysDisplay = false }) => {
  if (!stage && !alwaysDisplay) {
    return <React.Fragment />;
  }
  return (
    <tr>
      <td>{name}</td>
      <td>{stage?.Code}</td>
    </tr>
  );
};

const Pipeline = ({ pipeline }) => {
  return (
    <div id="pipeline">
      <table>
        <tbody>
          <Stage name="IF" stage={pipeline.IF} alwaysDisplay={true} />
          <Stage name="ID" stage={pipeline.ID} alwaysDisplay={true} />
          <Stage name="EX" stage={pipeline.EX} alwaysDisplay={true} />
          <Stage name="MEM" stage={pipeline.MEM} alwaysDisplay={true} />
          <Stage name="WB" stage={pipeline.WB} alwaysDisplay={true} />
          <Stage name="FPU Divider" stage={pipeline.FPDivider} />
          <Stage name="FPU Adder 1" stage={pipeline.FPAdder1} />
          <Stage name="FPU Adder 2" stage={pipeline.FPAdder2} />
          <Stage name="FPU Adder 3" stage={pipeline.FPAdder3} />
          <Stage name="FPU Adder 4" stage={pipeline.FPAdder4} />
          <Stage name="FPU Multiplier 1" stage={pipeline.FPMultiplier1} />
          <Stage name="FPU Multiplier 2" stage={pipeline.FPMultiplier2} />
          <Stage name="FPU Multiplier 3" stage={pipeline.FPMultiplier3} />
          <Stage name="FPU Multiplier 4" stage={pipeline.FPMultiplier4} />
          <Stage name="FPU Multiplier 5" stage={pipeline.FPMultiplier5} />
          <Stage name="FPU Multiplier 6" stage={pipeline.FPMultiplier6} />
        </tbody>
      </table>
    </div>
  );
};

export default Pipeline;
