import React from "react";

// A toy component that appends a final "s" to the label if
// the given value is != 1. Of course this is not proper
// pluralization, just me playing around with React.
const PluralLabel = ({label, value}) => {
    const pluralize = value => value != 1 ? "s" : "";
    return <div>{value} {label}{pluralize(value)}</div>
}

const Statistics = ({cycles, instructions, rawStalls, wawStalls, memoryStalls, codeSizeBytes, fcsr}) => {
    // TODO: FCSR.
    return (
        <div id="statistics">
            <div>
                <b>Execution</b>
                <PluralLabel value={cycles} label="Cycle" />
                <PluralLabel value={instructions} label="Instruction" />
                <div>{instructions == 0 ? 0 : (cycles / instructions).toFixed(2)} Cycles per Instructions (CPI)</div>
            </div><br/>
            <div>
                <b>Stalls</b>
                <PluralLabel value={rawStalls} label="RAW Stall" />
                <PluralLabel value={wawStalls} label="WAW Stall" />
                <PluralLabel value={memoryStalls} label="Memory Stall" />
            </div><br />
            <div>
                <b>Code size</b>
                <div>{codeSizeBytes} Bytes</div>
            </div>
        </div>
    );
}

export default Statistics;