import React from 'react';

import Typography from '@mui/material/Typography';

import type { MemoryCell, Memory } from '../simulator/protocol';
import BinaryValue from './BinaryValue';

interface MemoryElementProps {
  memoryelement: MemoryCell;
}

const MemoryElement = ({ memoryelement }: MemoryElementProps) => {
  return (
    <>
      <td>
        <BinaryValue
          hexString={memoryelement.address_hex}
          value={memoryelement.address}
        />
      </td>
      <td>
        <BinaryValue
          hexString={memoryelement.value_hex}
          value={memoryelement.value}
        />
      </td>
      <td className="elementLabel">{memoryelement.label}</td>
      <td className="elementCode">{memoryelement.code}</td>
      <td className="elementComment">{memoryelement.comment}</td>
    </>
  );
};

interface MemoryProps {
  memory: Memory;
}

const MemoryPanel = ({ memory }: MemoryProps) => {
  const { cells = [] } = memory;
  return (
    <div>
      {cells.length === 0 ? (
        <Typography>No program loaded</Typography>
      ) : (
        <table id="memory">
          <thead>
            <tr>
              <th style={{ textAlign: 'left' }}>Address</th>
              <th style={{ textAlign: 'left' }}>Value</th>
              <th style={{ textAlign: 'left' }}>Label</th>
              <th style={{ textAlign: 'left' }}>Code</th>
              <th style={{ textAlign: 'left' }}>Comment</th>
            </tr>
          </thead>
          <tbody>
            {cells.map((_memoryelement, i) => (
              <tr key={i}>
                <MemoryElement memoryelement={cells[i]} />
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
};

export default React.memo(MemoryPanel);
