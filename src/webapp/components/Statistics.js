import React from 'react';

const Row = ({ label, value }) => {
    return (
    <tr>
    <td>{label}</td>
    <td style={{ 
        fontFamily: "Menlo, Monaco, 'Courier New', monospace",
        width: '20%',
    }}>{value}</td>
    </tr>
    );
};

// A toy component that appends a final "s" to the label if
// the given value is != 1. Of course this is not proper
// pluralization, just me playing around with React.
const PluralRow = ({ label, value }) => {
  const pluralSuffix = value != 1 ? 's' : '';
  return <Row label={`${label}${pluralSuffix}`} value={value} />;
};

const Statistics = ({
  cycles,
  instructions,
  rawStalls,
  wawStalls,
  memoryStalls,
  codeSizeBytes,
  fcsr,
}) => {
  // Common table style to ensure consistent layout
const tableStyle = {
    border: 'none',
    width: '100%',
    tableLayout: 'fixed', // Forces consistent column widths
};
  // TODO: FCSR.
  return (
    <div id="statistics">
      <div>
        <h3>Execution</h3>
        <table style={tableStyle}>
          <tbody>
            <PluralRow value={cycles} label="Cycle" />
            <PluralRow value={instructions} label="Instruction" />
            <Row value={instructions == 0 ? 0 : (cycles/instructions).toFixed(2)}
                 label="Cycles per Instructions (CPI)" />
          </tbody>
        </table>
      </div>
      
      <div>
        <h3>Stalls</h3>
        <table style={tableStyle}>
          <tbody>
            <PluralRow value={rawStalls} label="RAW Stall" />
            <PluralRow value={wawStalls} label="WAW Stall" />
            <PluralRow value={memoryStalls} label="Memory Stall" />
          </tbody>
        </table>
      </div>
      
      <div>
        <h3>Code size</h3>
        <table style={tableStyle}>
          <tbody>
            <Row value={codeSizeBytes} label="Bytes" />
          </tbody>
        </table>
      </div>
    </div>
  );
};

export default Statistics;
