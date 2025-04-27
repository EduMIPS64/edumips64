import React from 'react';

const Row = ({ label, value }) => {
    return (
    <tr>
    <td style={{ fontSize: '0.9rem' }}>{label}</td>
    <td style={{ 
        fontFamily: "Menlo, Monaco, 'Courier New', monospace",
        width: '20%',
        fontSize: '0.75rem',
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
  memoryStalls, L1I_reads, L1I_misses,L1D_reads,L1D_reads_misses,L1D_writes,L1D_writes_misses,
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
        <table style={tableStyle}>
            <tbody>
            <tr>
                <th colSpan="2" style={{textAlign: 'left', fontSize: '1rem', padding: '0.5rem 0'}}>
                    Execution
                </th>
            </tr>
            <PluralRow value={cycles} label="Cycle"/>
            <PluralRow value={instructions} label="Instruction"/>
            <Row value={instructions == 0 ? 0 : (cycles / instructions).toFixed(2)}
                 label="Cycles per Instructions (CPI)"/>
            </tbody>
        </table>
      </div>

        <div>
            <table style={tableStyle}>
                <tbody>
                <tr>
                    <th colSpan="2" style={{textAlign: 'left', fontSize: '1rem', padding: '0.5rem 0'}}>
                        Stalls
                    </th>
                </tr>
                <PluralRow value={rawStalls} label="RAW Stall"/>
                <PluralRow value={wawStalls} label="WAW Stall"/>
                <PluralRow value={memoryStalls} label="Structural Stall"/>
                <tr>
                    <th colSpan="2" style={{textAlign: 'left', fontSize: '1rem', padding: '0.5rem 0'}}>
                        Cache Memory Statistics
                    </th>
                </tr>
                <Row value={L1I_reads} label="L1 Instruction Reads"/>
                <Row value={L1I_misses} label="L1 Instruction Misses"/>
                <Row value={L1D_reads} label="L1 Data Reads"/>
                <Row value={L1D_reads_misses} label="L1 Data Read Misses"/>
                <Row value={L1D_writes} label="L1 Data Writes"/>
                <Row value={L1D_writes_misses} label="L1 Data Write Misses"/>
                </tbody>
            </table>
        </div>
    </div>
  );
};

export default Statistics;
