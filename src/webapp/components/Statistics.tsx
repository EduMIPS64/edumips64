import React from 'react';

import Box from '@mui/material/Box';
import Tooltip from '@mui/material/Tooltip';
import Typography from '@mui/material/Typography';

import TimerOutlinedIcon from '@mui/icons-material/TimerOutlined';
import FormatListNumberedOutlinedIcon from '@mui/icons-material/FormatListNumberedOutlined';
import SpeedOutlinedIcon from '@mui/icons-material/SpeedOutlined';
import SyncProblemOutlinedIcon from '@mui/icons-material/SyncProblemOutlined';
import PauseCircleOutlinedIcon from '@mui/icons-material/PauseCircleOutlined';
import BlockOutlinedIcon from '@mui/icons-material/BlockOutlined';
import MenuBookOutlinedIcon from '@mui/icons-material/MenuBookOutlined';
import ErrorOutlineOutlinedIcon from '@mui/icons-material/ErrorOutlineOutlined';
import Inventory2OutlinedIcon from '@mui/icons-material/Inventory2Outlined';
import EditNoteOutlinedIcon from '@mui/icons-material/EditNoteOutlined';
import ReportProblemOutlinedIcon from '@mui/icons-material/ReportProblemOutlined';
import DataObjectOutlinedIcon from '@mui/icons-material/DataObjectOutlined';

import type { Statistics } from '../simulator/protocol';

interface StatTileProps {
  icon: React.ReactNode;
  label: string;
  /** Optional shorter caption shown under the value; `label` remains the
   * tooltip / aria-label. */
  shortLabel?: string;
  value: React.ReactNode;
  valueId?: string;
}

// A single compact stat: icon + prominent value, with the full label
// available as a tooltip/aria-label and a small caption underneath so the
// meaning is still visible without hovering.
const StatTile = ({ icon, label, shortLabel, value, valueId }: StatTileProps) => {
  return (
    <Tooltip title={label} arrow enterDelay={400}>
      <Box
        role="group"
        aria-label={label}
        sx={{
          display: 'flex',
          alignItems: 'center',
          gap: 0.75,
          minWidth: '7.5rem',
          flex: '1 1 7.5rem',
          px: 1,
          py: 0.5,
          borderRadius: 1.5,
          bgcolor: 'action.hover',
        }}
      >
        <Box
          sx={{
            display: 'flex',
            color: 'primary.main',
            flexShrink: 0,
          }}
        >
          {icon}
        </Box>
        <Box sx={{ minWidth: 0 }}>
          <Typography
            id={valueId}
            component="div"
            sx={{
              fontFamily: "Menlo, Monaco, 'Courier New', monospace",
              fontWeight: 700,
              fontSize: '0.95rem',
              lineHeight: 1.2,
              whiteSpace: 'nowrap',
              overflow: 'hidden',
              textOverflow: 'ellipsis',
            }}
          >
            {value}
          </Typography>
          <Typography
            component="div"
            sx={{
              fontSize: '0.65rem',
              lineHeight: 1.2,
              color: 'text.secondary',
              whiteSpace: 'nowrap',
              overflow: 'hidden',
              textOverflow: 'ellipsis',
            }}
          >
            {shortLabel ?? label}
          </Typography>
        </Box>
      </Box>
    </Tooltip>
  );
};

interface StatGroupProps {
  title: string;
  children: React.ReactNode;
}

// A lightweight section grouping: a small caption-style label followed by a
// wrapping row of tiles, without heavy headers/tables.
const StatGroup = ({ title, children }: StatGroupProps) => {
  return (
    <Box sx={{ '&:not(:first-of-type)': { mt: 1 } }}>
      <Typography
        component="div"
        sx={{
          fontSize: '0.7rem',
          fontWeight: 700,
          letterSpacing: '0.04em',
          textTransform: 'uppercase',
          color: 'text.secondary',
          mb: 0.5,
        }}
      >
        {title}
      </Typography>
      <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 0.75 }}>
        {children}
      </Box>
    </Box>
  );
};

// Props match the Statistics protocol type (spread from `stats` in Simulator).
const StatisticsPanel = ({
  cycles,
  instructions,
  rawStalls,
  wawStalls,
  memoryStalls,
  dividerStalls,
  exStalls,
  funcUnitStalls,
  L1I_reads,
  L1I_misses,
  L1D_reads,
  L1D_reads_misses,
  L1D_writes,
  L1D_writes_misses,
  codeSizeBytes,
}: Statistics) => {
  const cpi = instructions === 0 ? 0 : (cycles / instructions).toFixed(2);
  const structuralStalls =
    (dividerStalls || 0) + (memoryStalls || 0) + (exStalls || 0) + (funcUnitStalls || 0);

  // TODO: FCSR.
  return (
    <div id="statistics">
      <StatGroup title="Execution">
        <StatTile
          icon={<TimerOutlinedIcon fontSize="small" />}
          label="Cycles"
          value={cycles}
          valueId="stat-cycles"
        />
        <StatTile
          icon={<FormatListNumberedOutlinedIcon fontSize="small" />}
          label="Instructions"
          value={instructions}
          valueId="stat-instructions"
        />
        <StatTile
          icon={<SpeedOutlinedIcon fontSize="small" />}
          label="Cycles per instruction (CPI)"
          shortLabel="CPI"
          value={cpi}
        />
        <StatTile
          icon={<DataObjectOutlinedIcon fontSize="small" />}
          label="Code size (bytes)"
          shortLabel="Code size (B)"
          value={codeSizeBytes}
          valueId="stat-code-size"
        />
      </StatGroup>

      <StatGroup title="Stalls">
        <StatTile
          icon={<SyncProblemOutlinedIcon fontSize="small" />}
          label="RAW stalls"
          value={rawStalls}
          valueId="stat-raw-stalls"
        />
        <StatTile
          icon={<PauseCircleOutlinedIcon fontSize="small" />}
          label="WAW stalls"
          value={wawStalls}
          valueId="stat-waw-stalls"
        />
        <StatTile
          icon={<BlockOutlinedIcon fontSize="small" />}
          label="Structural stalls"
          value={structuralStalls}
          valueId="stat-structural-stalls"
        />
      </StatGroup>

      <StatGroup title="Cache memory">
        <StatTile
          icon={<MenuBookOutlinedIcon fontSize="small" />}
          label="L1 instruction reads"
          value={L1I_reads}
          valueId="stat-l1i-reads"
        />
        <StatTile
          icon={<ErrorOutlineOutlinedIcon fontSize="small" />}
          label="L1 instruction misses"
          value={L1I_misses}
          valueId="stat-l1i-misses"
        />
        <StatTile
          icon={<Inventory2OutlinedIcon fontSize="small" />}
          label="L1 data reads"
          value={L1D_reads}
          valueId="stat-l1d-reads"
        />
        <StatTile
          icon={<ReportProblemOutlinedIcon fontSize="small" />}
          label="L1 data read misses"
          value={L1D_reads_misses}
          valueId="stat-l1d-read-misses"
        />
        <StatTile
          icon={<EditNoteOutlinedIcon fontSize="small" />}
          label="L1 data writes"
          value={L1D_writes}
          valueId="stat-l1d-writes"
        />
        <StatTile
          icon={<ReportProblemOutlinedIcon fontSize="small" />}
          label="L1 data write misses"
          value={L1D_writes_misses}
          valueId="stat-l1d-write-misses"
        />
      </StatGroup>
    </div>
  );
};

export default React.memo(StatisticsPanel);
