import React from 'react';
import { DEFAULT_PIPELINE_COLORS } from '../settings/schema';

/*
 * Graphical Pipeline widget for the Web UI.
 *
 * The layout mirrors `org.edumips64.ui.swing.GUIPipeline`: IF and ID on the
 * left of the central pipeline axis, MEM and WB on the right, EX above the
 * axis, and the FPU functional units (Multiplier, Adder, Divider) stacked
 * around it. Active stages are filled with their per-stage color (which is
 * persisted and customizable from the Settings panel) and the running
 * instruction's name is rendered inside the block.
 *
 * Stalls (pipeline bubbles, where the in-stage instruction's `Name` is the
 * empty placeholder " ") are rendered with the dedicated `Stall` color and
 * a "stall" label, so the user can tell apart "occupied" from "stalled"
 * functional units at a glance — something the Swing UI does not do.
 */

// SVG canvas dimensions. The component is laid out in this coordinate space
// and then scaled to the available width via `viewBox` + `preserveAspectRatio`.
const VIEW_W = 600;
const VIEW_H = 320;

// Geometry of the five integer stages. `x`/`y` is the top-left corner and
// `w`/`h` are the box dimensions, in viewBox units.
const STAGE_BOXES = {
  IF: { x: 20, y: 140, w: 60, h: 50, label: 'IF' },
  ID: { x: 110, y: 140, w: 60, h: 50, label: 'ID' },
  EX: { x: 270, y: 30, w: 60, h: 50, label: 'EX' },
  MEM: { x: 430, y: 140, w: 60, h: 50, label: 'MEM' },
  WB: { x: 520, y: 140, w: 60, h: 50, label: 'WB' },
};

// FPU functional units, computed below. The Multipliers row sits above the
// integer axis and the Adders row sits below it, with the Divider at the
// bottom — same topology as the Swing widget.
const FPU_LEFT = 195;
const FPU_RIGHT = 405;
const FPU_WIDTH = FPU_RIGHT - FPU_LEFT;

const FP_MULT_COUNT = 7;
const FP_MULT_Y = 95;
const FP_MULT_H = 30;
const FP_MULT_GAP = 4;
const FP_MULT_W =
  (FPU_WIDTH - FP_MULT_GAP * (FP_MULT_COUNT - 1)) / FP_MULT_COUNT;

const FP_ADD_COUNT = 4;
const FP_ADD_Y = 205;
const FP_ADD_H = 30;
const FP_ADD_GAP = 6;
const FP_ADD_W = (FPU_WIDTH - FP_ADD_GAP * (FP_ADD_COUNT - 1)) / FP_ADD_COUNT;

const FP_DIV_BOX = { x: 205, y: 260, w: 190, h: 40 };

const fpMultBox = (i) => ({
  x: FPU_LEFT + i * (FP_MULT_W + FP_MULT_GAP),
  y: FP_MULT_Y,
  w: FP_MULT_W,
  h: FP_MULT_H,
});
const fpAddBox = (i) => ({
  x: FPU_LEFT + i * (FP_ADD_W + FP_ADD_GAP),
  y: FP_ADD_Y,
  w: FP_ADD_W,
  h: FP_ADD_H,
});

// True when the slot is filled by a non-bubble instruction.
const isOccupied = (instr) => !!instr && instr.Name && instr.Name !== ' ';
// True when the slot is filled by a bubble (stall).
const isStall = (instr) => !!instr && instr.Name === ' ';

/**
 * Render a single rectangular pipeline stage.
 */
const StageBox = ({
  box,
  label,
  instr,
  fillColor,
  stallColor,
  outlineColor,
  textColor,
}) => {
  const occupied = isOccupied(instr);
  const stalled = isStall(instr);
  const fill = occupied ? fillColor : stalled ? stallColor : 'transparent';
  const cx = box.x + box.w / 2;
  const labelY = box.y + box.h * 0.38;
  const instrY = box.y + box.h * 0.78;
  const stageLabelOnly = !occupied && !stalled;
  return (
    <g>
      <rect
        x={box.x}
        y={box.y}
        width={box.w}
        height={box.h}
        fill={fill}
        stroke={outlineColor}
        strokeWidth={1.2}
      />
      {stalled && (
        // Diagonal hatching makes stalls visually distinct from active stages
        // even when the user picks a stall color close to a stage color.
        <rect
          x={box.x}
          y={box.y}
          width={box.w}
          height={box.h}
          fill="url(#pipelineStallHatch)"
          pointerEvents="none"
        />
      )}
      <text
        x={cx}
        y={stageLabelOnly ? box.y + box.h / 2 + 4 : labelY}
        textAnchor="middle"
        fontSize="12"
        fontWeight="bold"
        fill={textColor}
      >
        {label}
      </text>
      {occupied && (
        <text
          x={cx}
          y={instrY}
          textAnchor="middle"
          fontSize="11"
          fill={textColor}
        >
          {instr.Name}
        </text>
      )}
      {stalled && (
        <text
          x={cx}
          y={instrY}
          textAnchor="middle"
          fontSize="10"
          fontStyle="italic"
          fill={textColor}
        >
          stall
        </text>
      )}
    </g>
  );
};

/**
 * Render a thin connector line between two stages. The lines mirror the
 * arrows drawn by the Swing widget but are kept un-arrowheaded for clarity
 * at small sizes.
 */
const Wire = ({ x1, y1, x2, y2, color }) => (
  <line
    x1={x1}
    y1={y1}
    x2={x2}
    y2={y2}
    stroke={color}
    strokeWidth={1}
    strokeLinecap="round"
  />
);

const Pipeline = ({ pipeline, colors }) => {
  // `colors` is the persisted setting; fall back to the schema defaults so
  // the widget keeps working even if it is rendered before settings are
  // wired in (e.g. in unit tests).
  const c = { ...DEFAULT_PIPELINE_COLORS, ...(colors || {}) };
  const outline = '#444';
  const text = '#111';

  const fpMult = [
    pipeline.FPMultiplier1,
    pipeline.FPMultiplier2,
    pipeline.FPMultiplier3,
    pipeline.FPMultiplier4,
    pipeline.FPMultiplier5,
    pipeline.FPMultiplier6,
    pipeline.FPMultiplier7,
  ];
  const fpAdd = [
    pipeline.FPAdder1,
    pipeline.FPAdder2,
    pipeline.FPAdder3,
    pipeline.FPAdder4,
  ];
  const fpDiv = pipeline.FPDivider;

  // Section labels above the FPU rows (axis y = 165, between IF/ID and
  // MEM/WB). Use the same midpoint as the Swing widget.
  const fpuLabelXMult = (FPU_LEFT + FPU_RIGHT) / 2;
  const fpuLabelXAdd = (FPU_LEFT + FPU_RIGHT) / 2;

  return (
    <div id="pipeline" data-testid="pipeline-widget">
      <svg
        viewBox={`0 0 ${VIEW_W} ${VIEW_H}`}
        preserveAspectRatio="xMidYMid meet"
        style={{ width: '100%', height: 'auto', maxHeight: 360 }}
        role="img"
        aria-label="Pipeline diagram"
      >
        <defs>
          <pattern
            id="pipelineStallHatch"
            patternUnits="userSpaceOnUse"
            width="6"
            height="6"
            patternTransform="rotate(45)"
          >
            <line
              x1="0"
              y1="0"
              x2="0"
              y2="6"
              stroke="rgba(0,0,0,0.35)"
              strokeWidth="2"
            />
          </pattern>
        </defs>

        {/* Connectors (drawn first so the boxes paint on top) */}
        {/* IF -> ID */}
        <Wire x1={80} y1={165} x2={110} y2={165} color={outline} />
        {/* ID -> EX (up) */}
        <Wire x1={170} y1={150} x2={270} y2={70} color={outline} />
        {/* EX -> MEM (down) */}
        <Wire x1={330} y1={70} x2={430} y2={150} color={outline} />
        {/* MEM -> WB */}
        <Wire x1={490} y1={165} x2={520} y2={165} color={outline} />

        {/* ID -> FP Multiplier1, FP Mult7 -> MEM */}
        <Wire
          x1={170}
          y1={158}
          x2={fpMultBox(0).x}
          y2={FP_MULT_Y + FP_MULT_H / 2}
          color={outline}
        />
        <Wire
          x1={fpMultBox(FP_MULT_COUNT - 1).x + FP_MULT_W}
          y1={FP_MULT_Y + FP_MULT_H / 2}
          x2={430}
          y2={158}
          color={outline}
        />
        {/* FP Mult inter-stage wires */}
        {Array.from({ length: FP_MULT_COUNT - 1 }).map((_, i) => {
          const a = fpMultBox(i);
          const b = fpMultBox(i + 1);
          return (
            <Wire
              key={`mw-${i}`}
              x1={a.x + a.w}
              y1={a.y + a.h / 2}
              x2={b.x}
              y2={b.y + b.h / 2}
              color={outline}
            />
          );
        })}

        {/* ID -> FP Adder1, FP Add4 -> MEM */}
        <Wire
          x1={170}
          y1={172}
          x2={fpAddBox(0).x}
          y2={FP_ADD_Y + FP_ADD_H / 2}
          color={outline}
        />
        <Wire
          x1={fpAddBox(FP_ADD_COUNT - 1).x + FP_ADD_W}
          y1={FP_ADD_Y + FP_ADD_H / 2}
          x2={430}
          y2={172}
          color={outline}
        />
        {Array.from({ length: FP_ADD_COUNT - 1 }).map((_, i) => {
          const a = fpAddBox(i);
          const b = fpAddBox(i + 1);
          return (
            <Wire
              key={`aw-${i}`}
              x1={a.x + a.w}
              y1={a.y + a.h / 2}
              x2={b.x}
              y2={b.y + b.h / 2}
              color={outline}
            />
          );
        })}

        {/* ID -> FP Divider, FP Divider -> MEM */}
        <Wire
          x1={170}
          y1={185}
          x2={FP_DIV_BOX.x}
          y2={FP_DIV_BOX.y + FP_DIV_BOX.h / 2}
          color={outline}
        />
        <Wire
          x1={FP_DIV_BOX.x + FP_DIV_BOX.w}
          y1={FP_DIV_BOX.y + FP_DIV_BOX.h / 2}
          x2={430}
          y2={185}
          color={outline}
        />

        {/* Section labels */}
        <text
          x={fpuLabelXMult}
          y={FP_MULT_Y - 5}
          textAnchor="middle"
          fontSize="11"
          fontStyle="italic"
          fill={text}
        >
          FP Multiplier
        </text>
        <text
          x={fpuLabelXAdd}
          y={FP_ADD_Y - 5}
          textAnchor="middle"
          fontSize="11"
          fontStyle="italic"
          fill={text}
        >
          FP Adder
        </text>

        {/* Integer stages */}
        <StageBox
          box={STAGE_BOXES.IF}
          label="IF"
          instr={pipeline.IF}
          fillColor={c.IF}
          stallColor={c.Stall}
          outlineColor={outline}
          textColor={text}
        />
        <StageBox
          box={STAGE_BOXES.ID}
          label="ID"
          instr={pipeline.ID}
          fillColor={c.ID}
          stallColor={c.Stall}
          outlineColor={outline}
          textColor={text}
        />
        <StageBox
          box={STAGE_BOXES.EX}
          label="EX"
          instr={pipeline.EX}
          fillColor={c.EX}
          stallColor={c.Stall}
          outlineColor={outline}
          textColor={text}
        />
        <StageBox
          box={STAGE_BOXES.MEM}
          label="MEM"
          instr={pipeline.MEM}
          fillColor={c.MEM}
          stallColor={c.Stall}
          outlineColor={outline}
          textColor={text}
        />
        <StageBox
          box={STAGE_BOXES.WB}
          label="WB"
          instr={pipeline.WB}
          fillColor={c.WB}
          stallColor={c.Stall}
          outlineColor={outline}
          textColor={text}
        />

        {/* FPU Multipliers */}
        {fpMult.map((instr, i) => (
          <StageBox
            key={`mul-${i}`}
            box={fpMultBox(i)}
            label={`M${i + 1}`}
            instr={instr}
            fillColor={c.FPMultiplier}
            stallColor={c.Stall}
            outlineColor={outline}
            textColor={text}
          />
        ))}

        {/* FPU Adders */}
        {fpAdd.map((instr, i) => (
          <StageBox
            key={`add-${i}`}
            box={fpAddBox(i)}
            label={`A${i + 1}`}
            instr={instr}
            fillColor={c.FPAdder}
            stallColor={c.Stall}
            outlineColor={outline}
            textColor={text}
          />
        ))}

        {/* FPU Divider */}
        <StageBox
          box={FP_DIV_BOX}
          label="FP Divider"
          instr={fpDiv}
          fillColor={c.FPDivider}
          stallColor={c.Stall}
          outlineColor={outline}
          textColor={text}
        />
      </svg>
    </div>
  );
};

export default Pipeline;
