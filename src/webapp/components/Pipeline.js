import React from 'react';

const Stage = ({ name, instruction, cssClass, alwaysDisplay = false }) => {
  const isBubble = instruction?.Name === " ";
  const shouldDisplay = instruction || alwaysDisplay;
  const shouldHighlight = !!instruction && !isBubble;
  if (!shouldDisplay) {
    return <React.Fragment />;
  }
  return (
    <tr className={shouldHighlight ? cssClass : ""}>
      <td>{name}</td>
      <td>{instruction?.Code}</td>
    </tr>
  );
};

const Pipeline = ({ pipeline }) => {
  return (
    <div id="pipeline">
      <table>
        <tbody>
          <Stage name="IF" instruction={pipeline.IF} alwaysDisplay={true} cssClass="stageIf"/>
          <Stage name="ID" instruction={pipeline.ID} alwaysDisplay={true} cssClass="stageId"/>
          <Stage name="EX" instruction={pipeline.EX} alwaysDisplay={true} cssClass="stageEx"/>
          <Stage name="MEM" instruction={pipeline.MEM} alwaysDisplay={true} cssClass="stageMem"/>
          <Stage name="WB" instruction={pipeline.WB} alwaysDisplay={true} cssClass="stageWb"/>
          <Stage name="FPU Divider" instruction={pipeline.FPDivider} cssClass="stageFPDivider"/>
          <Stage name="FPU Adder 1" instruction={pipeline.FPAdder1} cssClass="stageFPAdder"/>
          <Stage name="FPU Adder 2" instruction={pipeline.FPAdder2} cssClass="stageFPAdder"/>
          <Stage name="FPU Adder 3" instruction={pipeline.FPAdder3} cssClass="stageFPAdder"/>
          <Stage name="FPU Adder 4" instruction={pipeline.FPAdder4} cssClass="stageFPAdder"/>
          <Stage name="FPU Multiplier 1" instruction={pipeline.FPMultiplier1} cssClass="stageFPMultiplier"/>
          <Stage name="FPU Multiplier 2" instruction={pipeline.FPMultiplier2} cssClass="stageFPMultiplier"/>
          <Stage name="FPU Multiplier 3" instruction={pipeline.FPMultiplier3} cssClass="stageFPMultiplier"/>
          <Stage name="FPU Multiplier 4" instruction={pipeline.FPMultiplier4} cssClass="stageFPMultiplier"/>
          <Stage name="FPU Multiplier 5" instruction={pipeline.FPMultiplier5} cssClass="stageFPMultiplier"/>
          <Stage name="FPU Multiplier 6" instruction={pipeline.FPMultiplier6} cssClass="stageFPMultiplier"/>
        </tbody>
      </table>
    </div>
  );
};

export default Pipeline;
