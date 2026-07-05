// @vitest-environment jsdom
/**
 * Unit tests for components/Cycles.tsx — the temporal instruction-vs-cycle
 * diagram (roadmap item U.7).
 *
 * Covers:
 *   - the empty state (no cycles executed yet);
 *   - grid geometry: one row per instruction, cells at the right cycle
 *     columns (via the data-cycle / data-state attributes);
 *   - the state → color mapping, including the dedicated Stall color and
 *     the FP functional-unit patterns (A1..A4, M1..M7, DIV, D00..D24);
 *   - the truncation notice for very long executions.
 */

import { describe, it, expect, afterEach } from 'vitest';
import { render, screen, cleanup } from '@testing-library/react';

// Vitest does not expose a global afterEach, so React Testing Library's
// automatic cleanup does not kick in; unmount explicitly between tests.
afterEach(cleanup);
import Cycles, { colorKeyForState } from '../../webapp/components/Cycles';
import { DEFAULT_PIPELINE_COLORS } from '../../webapp/settings/schema';
import type { CyclesDiagram } from '../../webapp/simulator/protocol';

function makeDiagram(overrides: Partial<CyclesDiagram> = {}): CyclesDiagram {
  return {
    time: 5,
    elements: [
      {
        name: 'DADDI R1,R0,42',
        serialNumber: 1,
        startTime: 1,
        states: ['IF', 'ID', 'EX', 'MEM', 'WB'],
      },
      {
        name: 'DADD R3,R1,R2',
        serialNumber: 2,
        startTime: 2,
        states: ['IF', 'RAW', 'ID', 'EX'],
      },
    ],
    ...overrides,
  };
}

describe('colorKeyForState', () => {
  it('maps the integer stages to their own palette entries', () => {
    expect(colorKeyForState('IF')).toBe('IF');
    expect(colorKeyForState('ID')).toBe('ID');
    expect(colorKeyForState('EX')).toBe('EX');
    expect(colorKeyForState('MEM')).toBe('MEM');
    expect(colorKeyForState('WB')).toBe('WB');
  });

  it('maps a squashed fetch (" ") to the IF color, like Swing', () => {
    expect(colorKeyForState(' ')).toBe('IF');
  });

  it('maps every stall tag to the dedicated Stall color', () => {
    for (const tag of ['RAW', 'WAW', 'StDiv', 'StEx', 'StFun', 'Str', 'StAdd', 'StMul']) {
      expect(colorKeyForState(tag)).toBe('Stall');
    }
  });

  it('maps FP functional-unit stages by pattern', () => {
    expect(colorKeyForState('A1')).toBe('FPAdder');
    expect(colorKeyForState('A4')).toBe('FPAdder');
    expect(colorKeyForState('M1')).toBe('FPMultiplier');
    expect(colorKeyForState('M7')).toBe('FPMultiplier');
    expect(colorKeyForState('DIV')).toBe('FPDivider');
    expect(colorKeyForState('D00')).toBe('FPDivider');
    expect(colorKeyForState('D24')).toBe('FPDivider');
  });

  it('returns null for tags outside the vocabulary', () => {
    expect(colorKeyForState('A5')).toBeNull();
    expect(colorKeyForState('M8')).toBeNull();
    expect(colorKeyForState('D2')).toBeNull();
    expect(colorKeyForState('bogus')).toBeNull();
  });
});

describe('Cycles — empty state', () => {
  it('renders an empty diagram (no rows) when no cycle has been executed yet', () => {
    const { container } = render(<Cycles cycles={{ time: 0, elements: [] }} />);
    // The widget still renders — just with no instruction rows — rather than
    // a placeholder message, so the bottom region always shows the grid frame.
    expect(screen.getByTestId('cycles-widget').getAttribute('data-time')).toBe(
      '0',
    );
    expect(
      container.querySelectorAll('[data-testid="cycles-row"]'),
    ).toHaveLength(0);
  });
});

describe('Cycles — grid rendering', () => {
  it('renders one row per instruction, labeled with its full name', () => {
    const { container } = render(<Cycles cycles={makeDiagram()} />);
    const rows = container.querySelectorAll('[data-testid="cycles-row"]');
    expect(rows).toHaveLength(2);
    expect(rows[0].getAttribute('data-name')).toBe('DADDI R1,R0,42');
    expect(rows[1].getAttribute('data-name')).toBe('DADD R3,R1,R2');
  });

  it('places each state cell at the cycle it belongs to', () => {
    const { container } = render(<Cycles cycles={makeDiagram()} />);
    const secondRow = container.querySelectorAll('[data-testid="cycles-row"]')[1];
    const cells = secondRow.querySelectorAll('[data-state]');
    // startTime=2, so IF/RAW/ID/EX sit at cycles 2/3/4/5.
    expect([...cells].map((el) => el.getAttribute('data-cycle'))).toEqual([
      '2',
      '3',
      '4',
      '5',
    ]);
    expect([...cells].map((el) => el.getAttribute('data-state'))).toEqual([
      'IF',
      'RAW',
      'ID',
      'EX',
    ]);
  });

  it('fills cells with the per-stage color and stalls with the Stall color', () => {
    const { container } = render(<Cycles cycles={makeDiagram()} />);
    const secondRow = container.querySelectorAll('[data-testid="cycles-row"]')[1];
    const cellColor = (state: string) =>
      (
        secondRow.querySelector(`[data-state="${state}"]`) as HTMLElement
      ).style.backgroundColor;

    // jsdom normalizes hex colors to rgb(); convert the expectations too.
    const toRgb = (hex: string) => {
      const n = parseInt(hex.slice(1), 16);
      return `rgb(${(n >> 16) & 255}, ${(n >> 8) & 255}, ${n & 255})`;
    };

    expect(cellColor('IF')).toBe(toRgb(DEFAULT_PIPELINE_COLORS.IF));
    expect(cellColor('RAW')).toBe(toRgb(DEFAULT_PIPELINE_COLORS.Stall));
    expect(cellColor('EX')).toBe(toRgb(DEFAULT_PIPELINE_COLORS.EX));
  });

  it('honors custom colors passed via the colors prop', () => {
    const colors = { ...DEFAULT_PIPELINE_COLORS, IF: '#123456' };
    const { container } = render(
      <Cycles cycles={makeDiagram()} colors={colors} />,
    );
    const firstCell = container.querySelector(
      '[data-state="IF"]',
    ) as HTMLElement;
    expect(firstCell.style.backgroundColor).toBe('rgb(18, 52, 86)');
  });

  it('exposes the current cycle count on the widget root', () => {
    render(<Cycles cycles={makeDiagram()} />);
    expect(
      screen.getByTestId('cycles-widget').getAttribute('data-time'),
    ).toBe('5');
  });
});

describe('Cycles — long-execution windowing', () => {
  it('shows a truncation notice and drops rows left of the window', () => {
    // One old instruction that finished long before the visible window and
    // one recent instruction inside it.
    const diagram: CyclesDiagram = {
      time: 5000,
      elements: [
        {
          name: 'OLD',
          serialNumber: 1,
          startTime: 1,
          states: ['IF', 'ID', 'EX', 'MEM', 'WB'],
        },
        {
          name: 'RECENT',
          serialNumber: 2,
          startTime: 4999,
          states: ['IF', 'ID'],
        },
      ],
    };
    const { container } = render(<Cycles cycles={diagram} />);
    const rows = container.querySelectorAll('[data-testid="cycles-row"]');
    expect(rows).toHaveLength(1);
    expect(rows[0].getAttribute('data-name')).toBe('RECENT');
    expect(screen.getByText(/showing only the most recent/i)).toBeTruthy();
  });

  it('clips the states of a row that straddles the window edge', () => {
    // 2000-column window over time=3000 → cycles 1001..3000 are visible.
    const diagram: CyclesDiagram = {
      time: 3000,
      elements: [
        {
          name: 'STRADDLER',
          serialNumber: 1,
          startTime: 999,
          states: ['IF', 'ID', 'EX', 'MEM', 'WB'],
        },
      ],
    };
    const { container } = render(<Cycles cycles={diagram} />);
    const cells = container.querySelectorAll('[data-state]');
    // Cycles 999 (IF) and 1000 (ID) fall outside; EX/MEM/WB remain.
    expect([...cells].map((el) => el.getAttribute('data-state'))).toEqual([
      'EX',
      'MEM',
      'WB',
    ]);
    expect(cells[0].getAttribute('data-cycle')).toBe('1001');
  });
});
