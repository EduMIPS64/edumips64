import React, { useState, useEffect } from 'react';
import Typography from '@mui/material/Typography';

const CacheConfig = ({ onChange, status }) => {
    const [l1d, setL1D] = useState({ size: 1024, blockSize: 16, associativity: 1, penalty: 40 });
    const [l1i, setL1I] = useState({ size: 1024, blockSize: 16, associativity: 1, penalty: 40 });

    const labelStyle = {
        fontSize: '0.75rem',
        color: status === 'RUNNING' ? 'gray' : 'inherit'
    };

    useEffect(() => {
        if (onChange) {
            onChange({ l1d, l1i });
        }
    }, [l1d, l1i]);

    return (
        <div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '1rem', marginBottom: '1rem' }}>
                <Typography sx={{ ...labelStyle, fontWeight: 'bold', minWidth: '130px' }}>L1 Data Cache</Typography>
                <div>
                    <Typography sx={labelStyle}>Size</Typography>
                    <input type="number" size="4" value={l1d.size} onChange={e => setL1D({ ...l1d, size: parseInt(e.target.value) })} disabled={status === 'RUNNING'} />
                </div>
                <div>
                    <Typography sx={labelStyle}>Block Size</Typography>
                    <input type="number" size="4" value={l1d.blockSize} onChange={e => setL1D({ ...l1d, blockSize: parseInt(e.target.value) })} disabled={status === 'RUNNING'} />
                </div>
                <div>
                    <Typography sx={labelStyle}>Associativity</Typography>
                    <input type="number" size="4" value={l1d.associativity} onChange={e => setL1D({ ...l1d, associativity: parseInt(e.target.value) })} disabled={status === 'RUNNING'} />
                </div>
                <div>
                    <Typography sx={labelStyle}>Penalty</Typography>
                    <input type="number" size="4" value={l1d.penalty} onChange={e => setL1D({ ...l1d, penalty: parseInt(e.target.value) })} disabled={status === 'RUNNING'} />
                </div>
            </div>

            <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
                <Typography sx={{ ...labelStyle, fontWeight: 'bold', minWidth: '130px' }}>L1 Instruction Cache</Typography>
                <div>
                    <Typography sx={labelStyle}>Size</Typography>
                    <input type="number" size="4" value={l1i.size} onChange={e => setL1I({ ...l1i, size: parseInt(e.target.value) })} disabled={status === 'RUNNING'} />
                </div>
                <div>
                    <Typography sx={labelStyle}>Block Size</Typography>
                    <input type="number" size="4" value={l1i.blockSize} onChange={e => setL1I({ ...l1i, blockSize: parseInt(e.target.value) })} disabled={status === 'RUNNING'} />
                </div>
                <div>
                    <Typography sx={labelStyle}>Associativity</Typography>
                    <input type="number" size="4" value={l1i.associativity} onChange={e => setL1I({ ...l1i, associativity: parseInt(e.target.value) })} disabled={status === 'RUNNING'} />
                </div>
                <div>
                    <Typography sx={labelStyle}>Penalty</Typography>
                    <input type="number" size="4" value={l1i.penalty} onChange={e => setL1I({ ...l1i, penalty: parseInt(e.target.value) })} disabled={status === 'RUNNING'} />
                </div>
            </div>
        </div>
    );
};

export default CacheConfig;