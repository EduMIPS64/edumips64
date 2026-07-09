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
import Inventory2OutlinedIcon from '@mui/icons-material/Inventory2Outlined';
import EditNoteOutlinedIcon from '@mui/icons-material/EditNoteOutlined';
import DataObjectOutlinedIcon from '@mui/icons-material/DataObjectOutlined';

import type { Statistics } from '../simulator/protocol';

// Shared look for the small pill tiles: icon + value(s), no caption text.
// The full descriptive label lives in a Tooltip and, for accessibility, as
// an `aria-label` on the focusable tile itself (tabIndex=0), so keyboard and
// screen-reader users get the same information sighted mouse users get from
// hovering (MUI Tooltip also opens on focus by default).
const tileSx = {
  display: 'flex',
  alignItems: 'center',
  gap: 0.5,
  px: 0.75,
  py: 0.25,
  borderRadius: 1.5,
  bgcolor: 'action.hover',
  '&:focus-visible': {
    outline: '2px solid',
    outlineColor: 'primary.main',
    outlineOffset: '1px',
  },
} as const;

const valueSx = {
  fontFamily: "Menlo, Monaco, 'Courier New', monospace",
  fontWeight: 700,
  fontSize: '0.8rem',
  lineHeight: 1.2,
  whiteSpace: 'nowrap',
} as const;

interface StatTileProps {
  icon: React.ReactNode;
  label: string;
  value: React.ReactNode;
  valueId?: string;
}

// A single compact stat: icon + value only. The full label is available as
// a tooltip and as an aria-label on the (focusable) tile.
const StatTile = ({ icon, label, value, valueId }: StatTileProps) => {
  return (
    <Tooltip title={label} arrow enterDelay={400}>
      <Box role="group" aria-label={label} tabIndex={0} sx={tileSx}>
        <Box sx={{ display: 'flex', color: 'primary.main', flexShrink: 0 }}>{icon}</Box>
        <Typography id={valueId} component="div" sx={valueSx}>
          {value}
        </Typography>
      </Box>
    </Tooltip>
  );
};

interface PairStatTileProps {
  icon: React.ReactNode;
  label: string;
  count: React.ReactNode;
  countId?: string;
  misses: React.ReactNode;
  missesId?: string;
}

// A compact "count / misses" stat used for cache metrics: one icon per
// cache-access type, pairing the access count with its miss count so the two
// related numbers read together instead of as separate unlabeled chips.
const PairStatTile = ({ icon, label, count, countId, misses, missesId }: PairStatTileProps) => {
  return (
    <Tooltip title={label} arrow enterDelay={400}>
      <Box role="group" aria-label={label} tabIndex={0} sx={tileSx}>
        <Box sx={{ display: 'flex', color: 'primary.main', flexShrink: 0 }}>{icon}</Box>
        <Typography id={countId} component="div" sx={valueSx}>
          {count}
        </Typography>
        <Typography component="div" sx={{ ...valueSx, fontWeight: 500, color: 'text.disabled' }}>
          /
        </Typography>
        <Typography
          id={missesId}
          component="div"
          sx={{ ...valueSx, color: 'warning.main' }}
        >
          {misses}
        </Typography>
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
    <Box
      sx={{
        display: 'flex',
        alignItems: 'center',
        flexWrap: 'wrap',
        gap: 0.5,
        '&:not(:first-of-type)': { mt: 0.5 },
      }}
    >
      <Typography
        component="div"
        sx={{
          fontSize: '0.6rem',
          fontWeight: 700,
          letterSpacing: '0.03em',
          textTransform: 'uppercase',
          color: 'text.secondary',
          flexShrink: 0,
          mr: 0.25,
        }}
      >
        {title}
      </Typography>
      <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 0.5 }}>{children}</Box>
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
          value={cpi}
        />
        <StatTile
          icon={<DataObjectOutlinedIcon fontSize="small" />}
          label="Code size (bytes)"
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

      <StatGroup title="Cache">
        <PairStatTile
          icon={<MenuBookOutlinedIcon fontSize="small" />}
          label={`L1 instruction cache: ${L1I_reads} reads, ${L1I_misses} miss${L1I_misses === 1 ? '' : 'es'}`}
          count={L1I_reads}
          countId="stat-l1i-reads"
          misses={L1I_misses}
          missesId="stat-l1i-misses"
        />
        <PairStatTile
          icon={<Inventory2OutlinedIcon fontSize="small" />}
          label={`L1 data cache reads: ${L1D_reads} reads, ${L1D_reads_misses} miss${L1D_reads_misses === 1 ? '' : 'es'}`}
          count={L1D_reads}
          countId="stat-l1d-reads"
          misses={L1D_reads_misses}
          missesId="stat-l1d-read-misses"
        />
        <PairStatTile
          icon={<EditNoteOutlinedIcon fontSize="small" />}
          label={`L1 data cache writes: ${L1D_writes} writes, ${L1D_writes_misses} miss${L1D_writes_misses === 1 ? '' : 'es'}`}
          count={L1D_writes}
          countId="stat-l1d-writes"
          misses={L1D_writes_misses}
          missesId="stat-l1d-write-misses"
        />
      </StatGroup>
    </div>
  );
};

export default React.memo(StatisticsPanel);
