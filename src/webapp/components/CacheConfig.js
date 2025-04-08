import React, { useState, useEffect } from 'react';
import Typography from '@mui/material/Typography';

const CacheConfig = ({ enableCache, onChange }) => {
    const [l1d, setL1D] = useState({ size: 1024, blockSize: 16, associativity: 1, penalty: 0 });
    const [l1i, setL1I] = useState({ size: 1024, blockSize: 16, associativity: 1, penalty: 40 });

    useEffect(() => {
        if (onChange) {
            onChange({ l1d, l1i });
        }
    }, [l1d, l1i]);

    return (
        <div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '1rem', marginBottom: '1rem' }}>
                <Typography sx={{ fontWeight: 'bold', fontSize: '0.75rem', minWidth: '130px' }}>L1 Data Cache</Typography>
                <div>
                    <Typography sx={{ fontSize: '0.75rem' }}>Size</Typography>
                    <input type="number" size="4" value={l1d.size} onChange={e => setL1D({ ...l1d, size: parseInt(e.target.value) })} />
                </div>
                <div>
                    <Typography sx={{ fontSize: '0.75rem' }}>Block Size</Typography>
                    <input type="number" size="4" value={l1d.blockSize} onChange={e => setL1D({ ...l1d, blockSize: parseInt(e.target.value) })} />
                </div>
                <div>
                    <Typography sx={{ fontSize: '0.75rem' }}>Associativity</Typography>
                    <input type="number" size="4" value={l1d.associativity} onChange={e => setL1D({ ...l1d, associativity: parseInt(e.target.value) })} />
                </div>
                <div>
                    <Typography sx={{ fontSize: '0.75rem' }}>Penalty</Typography>
                    <input type="number" size="4" value={l1d.penalty} onChange={e => setL1D({ ...l1d, penalty: parseInt(e.target.value) })} />
                </div>
            </div>

            <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
                <Typography sx={{ fontWeight: 'bold', fontSize: '0.75rem', minWidth: '130px' }}>L1 Instruction Cache</Typography>
                <div>
                    <Typography sx={{ fontSize: '0.75rem' }}>Size</Typography>
                    <input type="number" size="4" value={l1i.size} onChange={e => setL1I({ ...l1i, size: parseInt(e.target.value) })} />
                </div>
                <div>
                    <Typography sx={{ fontSize: '0.75rem' }}>Block Size</Typography>
                    <input type="number" size="4" value={l1i.blockSize} onChange={e => setL1I({ ...l1i, blockSize: parseInt(e.target.value) })} />
                </div>
                <div>
                    <Typography sx={{ fontSize: '0.75rem' }}>Associativity</Typography>
                    <input type="number" size="4" value={l1i.associativity} onChange={e => setL1I({ ...l1i, associativity: parseInt(e.target.value) })} />
                </div>
                <div>
                    <Typography sx={{ fontSize: '0.75rem' }}>Penalty</Typography>
                    <input type="number" size="4" value={l1i.penalty} onChange={e => setL1I({ ...l1i, penalty: parseInt(e.target.value) })} />
                </div>
            </div>
        </div>
    );
};

export default CacheConfig;