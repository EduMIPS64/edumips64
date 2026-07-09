import React from 'react';

import Code from './Code';
import Cycles from './Cycles';
import Memory from './Memory';
import Pipeline from './Pipeline';
import Registers from './Registers';
import Statistics from './Statistics';
import Header from './Header';
import RunControlsToolbar from './RunControlsToolbar';
import WorkspaceLayout from './WorkspaceLayout';
import Box from '@mui/material/Box';
import SortableDashboardCard from './SortableDashboardCard';
import IssuesCard from './IssuesCard';
import StdOut from './StdOut';
import InputDialog from './InputDialog';
import RuntimeErrorDialog from './RuntimeErrorDialog';

import {
  DndContext,
  type DragEndEvent,
  type DragStartEvent,
  DragOverlay,
  type DropAnimation,
  KeyboardSensor,
  PointerSensor,
  closestCenter,
  defaultDropAnimationSideEffects,
  useSensor,
  useSensors,
} from '@dnd-kit/core';
import {
  SortableContext,
  arrayMove,
  sortableKeyboardCoordinates,
  verticalListSortingStrategy,
} from '@dnd-kit/sortable';
import DashboardCard from './DashboardCard';

import AccountTreeOutlinedIcon from '@mui/icons-material/AccountTreeOutlined';
import DnsOutlinedIcon from '@mui/icons-material/DnsOutlined';
import DragIndicatorIcon from '@mui/icons-material/DragIndicator';
import InsightsOutlinedIcon from '@mui/icons-material/InsightsOutlined';
import StorageOutlinedIcon from '@mui/icons-material/StorageOutlined';
import TerminalOutlinedIcon from '@mui/icons-material/TerminalOutlined';

import useMediaQuery from '@mui/material/useMediaQuery';
import { ThemeProvider } from '@mui/material/styles';
import CssBaseline from '@mui/material/CssBaseline';
import { buildTheme } from '../theme';

import debounce from 'lodash/debounce';
import isEqual from 'lodash/isEqual';
import { useSetting } from '../settings/useSetting';
import { SettingKey } from '../settings/SettingKey';
import SampleProgram from '../data/SampleProgram';
import type {
  CacheConfig as CacheConfigType,
  DashboardWidgetId,
  ExpandedAccordions,
} from '../settings/schema';

import { useSimulatorData } from '../hooks/useSimulatorData';
import { useExecutionController } from '../hooks/useExecutionController';
import { useKeyboardShortcuts } from '../hooks/useKeyboardShortcuts';
import type { SimulatorResult, SimulatorWorker } from '../simulator/protocol';
import type { ITelemetryClient } from '../telemetry';
import type * as monacoEditor from 'monaco-editor';

interface SimulatorProps {
  worker: SimulatorWorker;
  initialState: SimulatorResult;
  appInsights: ITelemetryClient;
}

const Simulator = ({ worker, initialState, appInsights }: SimulatorProps) => {
  // ---------------------------------------------------------------------------
  // Settings (persisted in localStorage)
  // ---------------------------------------------------------------------------
  const [storedCode, setStoredCode, resetStoredCode] = useSetting(
    SettingKey.EDITOR_CODE,
  );
  const [viMode, setViMode] = useSetting(SettingKey.VI_MODE);
  const [fontSize, setFontSize] = useSetting(SettingKey.FONT_SIZE);
  const [accordionAlerts, setAccordionAlerts] = useSetting(
    SettingKey.ACCORDION_ALERTS,
  );
  const [expandedAccordions, setExpandedAccordions] = useSetting(
    SettingKey.EXPANDED_ACCORDIONS,
  );
  const [forwarding, setForwarding] = useSetting(SettingKey.FORWARDING);
  const [delaySlot, setDelaySlot] = useSetting(SettingKey.DELAY_SLOT);
  const [stepStride, setStepStride] = useSetting(SettingKey.STEP_STRIDE);
  const [executionDelayMs, setExecutionDelayMs] = useSetting(
    SettingKey.EXECUTION_DELAY_MS,
  );
  const [pipelineColors, setPipelineColors] = useSetting(
    SettingKey.PIPELINE_COLORS,
  );
  const [themeMode, setThemeMode] = useSetting(SettingKey.THEME_MODE);
  const [workspaceLayout, setWorkspaceLayout] = useSetting(
    SettingKey.WORKSPACE_LAYOUT,
  );
  const [widgetOrder, setWidgetOrder] = useSetting(SettingKey.WIDGET_ORDER);

  // ---------------------------------------------------------------------------
  // Editor code persistence
  // ---------------------------------------------------------------------------
  // - `storedCode` is what useLocalStorage actually holds ('' = "never edited by user").
  // - `code` is the live value the Monaco editor shows ('' maps to SampleProgram).
  // - Writes to localStorage are debounced so individual keystrokes don't block.
  const [code, _setCode] = React.useState(() =>
    storedCode === '' ? SampleProgram : storedCode,
  );

  // Debounce localStorage writes to 500 ms so each keystroke doesn't trigger a
  // synchronous `setItem` call.  The useMemo ensures a single stable debounced
  // function instance exists across re-renders (the instance carries the timer
  // state; recreating it on every render would orphan pending timers).
  const debouncedPersistCode = React.useMemo(
    () => debounce((v: string) => setStoredCode(v), 500),
    // setStoredCode comes from useLocalStorage and is stable across renders.
    [setStoredCode],
  );

  // Public setter: updates the editor display immediately while deferring the
  // localStorage write.
  const setCode = React.useCallback(
    (newCode: string) => {
      _setCode(newCode);
      debouncedPersistCode(newCode);
    },
    [debouncedPersistCode],
  );

  // ---------------------------------------------------------------------------
  // Simulator data state (registers, memory, stats, pipeline, etc.)
  // ---------------------------------------------------------------------------
  const {
    registers,
    memory,
    stats,
    status,
    pipeline,
    cycles,
    parsingErrors,
    parsedInstructions,
    stdout,
    inputRequest,
    setStdout,
    setParsingErrors,
    setInputRequest,
    applyResultState,
    applyChecksyntaxResult,
  } = useSimulatorData(initialState);

  // Message shown in the RuntimeErrorDialog; null = dialog closed. Set by the
  // execution controller when a runtime error result arrives (the controller
  // stops the CPU immediately; the dialog is purely informational).
  const [runtimeErrorMessage, setRuntimeErrorMessage] = React.useState<
    string | null
  >(null);

  // ---------------------------------------------------------------------------
  // Execution controller (reducer, batch scheduling, worker subscription)
  // ---------------------------------------------------------------------------
  const { executing, dispatch, runCode, stepCode, stopCode, notifyReset } =
    useExecutionController({
      worker,
      applyResultState,
      applyChecksyntaxResult,
      setInputRequest,
      executionDelayMs,
      onRuntimeError: (message: string) => setRuntimeErrorMessage(message),
    });

  const simulatorRunning = status === 'RUNNING';

  // Tracks if the program has no syntax errors and can be loaded.
  // TODO: Allow code execution w/ warnings in the worker, then uncomment the line below
  const isValidProgram = () => {
    if (!parsingErrors) {
      return true;
    } else {
      return parsingErrors.filter((e) => !e.isWarning).length === 0;
    }
  };

  // ---------------------------------------------------------------------------
  // Worker settings sync
  // ---------------------------------------------------------------------------

  // Keep the simulator worker's forwarding flag in sync with the persisted
  // setting. Runs once on mount (so a value restored from localStorage is
  // pushed to the worker) and whenever the user toggles the switch.
  // biome-ignore lint/correctness/useExhaustiveDependencies: worker is a stable module singleton; re-run only when the setting changes
  React.useEffect(() => {
    worker.setForwarding(forwarding);
  }, [forwarding]);

  // Same pattern for the branch delay slot setting.
  // biome-ignore lint/correctness/useExhaustiveDependencies: worker is a stable module singleton; re-run only when the setting changes
  React.useEffect(() => {
    worker.setDelaySlot(delaySlot);
  }, [delaySlot]);

  // ---------------------------------------------------------------------------
  // Editor ref (for Issues panel click-to-navigate)
  // ---------------------------------------------------------------------------
  const editorRef =
    React.useRef<monacoEditor.editor.IStandaloneCodeEditor | null>(null);
  const handleEditorReady = React.useCallback(
    (editor: monacoEditor.editor.IStandaloneCodeEditor) => {
      editorRef.current = editor;
    },
    [],
  );
  const handleIssueClick = React.useCallback((row: number, column: number) => {
    const editor = editorRef.current;
    if (!editor) {
      return;
    }
    // Reveal the offending line in the centre of the viewport, place the
    // cursor at the reported column and focus the editor so the user can
    // start fixing the problem immediately.
    editor.revealLineInCenter(row);
    const safeColumn = Math.max(1, column || 1);
    editor.setPosition({ lineNumber: row, column: safeColumn });
    editor.focus();
  }, []);

  // Toggles a single dashboard card's expanded/collapsed state, persisting
  // the whole `expandedAccordions` map via `useSetting` so a returning user
  // sees the same layout they left (mirrors the pre-DashboardCard Accordion
  // behavior — same schema key, same storage shape).
  const toggleAccordion = React.useCallback(
    (panel: keyof ExpandedAccordions) => {
      setExpandedAccordions((prev) => ({
        ...prev,
        [panel]: !prev[panel],
      }));
    },
    [setExpandedAccordions],
  );

  // ---------------------------------------------------------------------------
  // Dashboard card drag-and-drop reordering
  // ---------------------------------------------------------------------------

  // PointerSensor uses a small activation distance so a stray click on the
  // drag handle (e.g. while aiming for a nearby control) doesn't start a
  // drag; KeyboardSensor lets the handle be operated with Space/Enter to
  // lift, arrow keys to move, and Space/Enter to drop, using dnd-kit's
  // built-in sortable coordinate getter.
  const dashboardSensors = useSensors(
    useSensor(PointerSensor, { activationConstraint: { distance: 4 } }),
    useSensor(KeyboardSensor, {
      coordinateGetter: sortableKeyboardCoordinates,
    }),
  );

  // The currently-dragged widget id (both for pointer and keyboard-driven
  // drags), and the on-screen width of its card at the moment the drag
  // started. `DragOverlay` renders its child outside the dashboard's grid
  // (in a portal-like fixed-position layer), so nothing constrains its size
  // to the current hover slot the way an in-place sortable item would be —
  // that's what let the dragged card visually assume the aspect ratio of
  // whatever slot it was hovering. Pinning the overlay to the width the card
  // had *before* the drag started (grid layout only varies card width, since
  // every card is `fullWidth`) keeps its whole footprint stable for the
  // entire drag, and the in-grid original renders as a plain dimmed
  // placeholder (see `SortableDashboardCard`) instead of following the
  // pointer itself.
  const [activeWidgetId, setActiveWidgetId] =
    React.useState<DashboardWidgetId | null>(null);
  const [activeWidgetWidth, setActiveWidgetWidth] = React.useState<
    number | undefined
  >(undefined);

  const handleDashboardDragStart = React.useCallback(
    (event: DragStartEvent) => {
      setActiveWidgetId(event.active.id as DashboardWidgetId);
      setActiveWidgetWidth(event.active.rect.current.initial?.width);
    },
    [],
  );

  const handleDashboardDragEnd = React.useCallback(
    (event: DragEndEvent) => {
      setActiveWidgetId(null);
      setActiveWidgetWidth(undefined);
      const { active, over } = event;
      if (!over || active.id === over.id) {
        return;
      }
      setWidgetOrder((prev) => {
        const oldIndex = prev.indexOf(active.id as DashboardWidgetId);
        const newIndex = prev.indexOf(over.id as DashboardWidgetId);
        if (oldIndex === -1 || newIndex === -1) {
          return prev;
        }
        return arrayMove(prev, oldIndex, newIndex);
      });
    },
    [setWidgetOrder],
  );

  const handleDashboardDragCancel = React.useCallback(() => {
    setActiveWidgetId(null);
    setActiveWidgetWidth(undefined);
  }, []);

  // A subtle, close-to-default drop animation: the overlay eases from
  // wherever it was released back into the dropped slot, with a gentle fade
  // (no bouncy scale/shadow pop) so it reads as a "settle", not an effect.
  const dashboardDropAnimation: DropAnimation = React.useMemo(
    () => ({
      sideEffects: defaultDropAnimationSideEffects({
        styles: { active: { opacity: '0.4' } },
      }),
    }),
    [],
  );

  // ---------------------------------------------------------------------------
  // Syntax checking (stable debounced instance)
  // ---------------------------------------------------------------------------

  // A stable debounced wrapper around worker.checkSyntax.  Creating this with
  // useMemo ensures the same debounced instance (with its internal timer state)
  // is reused across re-renders.  A plain `const debouncedSyntaxCheck = debounce(...)`
  // at the top of the function body would create a fresh function on every
  // render, orphaning any pending timer from the previous render.
  // Declared before business operations so restoreDefaultSample can call .cancel().
  const debouncedSyntaxCheck = React.useMemo(
    () => debounce((c: string) => worker.checkSyntax(c), 500),
    // worker is stable across renders (passed as a prop reference).
    [worker],
  );

  // ---------------------------------------------------------------------------
  // Business operations
  // ---------------------------------------------------------------------------
  // All of these are declared before useKeyboardShortcuts so that the hook
  // can receive stable function references without TDZ issues.

  const loadCode = () => {
    setStdout('');
    worker.load(code);
  };

  const clearCode = () => {
    notifyReset(); // cancels batch, dispatches RESET, worker.reset(), clears inputRequest
    setCode('.data\n\n.code\n  SYSCALL 0\n');
  };

  const restoreDefaultSample = () => {
    notifyReset(); // cancels batch, dispatches RESET, worker.reset(), clears inputRequest
    // Cancel any pending debounced writes / syntax checks so stale results
    // from the previous code don't arrive after the restore.
    debouncedPersistCode.cancel();
    debouncedSyntaxCheck.cancel();
    // Update local state immediately so the editor switches to the sample at
    // once, without waiting for the debounced write round-trip.
    _setCode(SampleProgram);
    // Reset the persisted value to the sentinel so future reloads also show
    // the sample (and benefit from any future sample update).
    resetStoredCode();
    // Clear stale parsing errors so the issues panel doesn't show diagnostics
    // for the code that was just replaced.
    setParsingErrors([]);
    // Run a fresh syntax check on the restored sample so any warnings in the
    // sample are surfaced immediately.
    worker.checkSyntax(SampleProgram);
  };

  const setCacheConfig = React.useCallback(
    (config: { l1d: CacheConfigType; l1i: CacheConfigType }) => {
      worker.setCacheConfig(config);
    },
    [worker],
  );

  const openCode = () => {
    const fileInput = document.createElement('input');
    fileInput.type = 'file';
    fileInput.accept = '.asm,.txt,.s';
    fileInput.onchange = (event: Event) => {
      const target = event.target as HTMLInputElement;
      const file = target.files?.[0];
      if (file) {
        const reader = new FileReader();
        reader.onload = (e: ProgressEvent<FileReader>) => {
          const result = e.target?.result;
          if (typeof result === 'string') {
            setCode(result);
          }
        };
        reader.readAsText(file);
      }
    };
    fileInput.click();
  };

  const saveCode = () => {
    const file = new Blob([code], { type: 'text/plain' });
    const fileURL = URL.createObjectURL(file);
    const link = document.createElement('a');
    link.href = fileURL;
    link.download = 'code.s';
    link.click();
    URL.revokeObjectURL(fileURL);
  };

  const submitInput = (input: string) => {
    setInputRequest(null);
    dispatch({ type: 'INPUT_SUBMITTED' });
    worker.provideInput(input);
  };

  const cancelInput = () => {
    submitInput('');
  };

  // ---------------------------------------------------------------------------
  // Click handlers (decouple telemetry from business logic)
  // ---------------------------------------------------------------------------
  const clickRun = () => {
    appInsights.trackEvent({ name: 'click', properties: { action: 'run' } });
    runCode();
  };

  const clickStep = (n: number) => {
    appInsights.trackEvent({ name: 'click', properties: { action: 'step' } });
    stepCode(n);
  };

  const clickStop = () => {
    appInsights.trackEvent({ name: 'click', properties: { action: 'stop' } });
    stopCode();
  };

  // ---------------------------------------------------------------------------
  // Keyboard shortcuts
  // ---------------------------------------------------------------------------
  useKeyboardShortcuts({
    status,
    executing,
    inputRequest,
    isValidProgram,
    loadCode,
    runCode,
    stepCode,
    stopCode,
    pauseCode: () => dispatch({ type: 'PAUSE_REQUESTED' }),
    stepStride,
    appInsights,
  });

  // ---------------------------------------------------------------------------
  // Syntax check on mount
  // ---------------------------------------------------------------------------

  // Run a syntax check on the initial code once on mount so that warnings
  // (e.g. deprecated instructions in the sample program) are surfaced
  // immediately, without requiring the user to edit the code first.
  // biome-ignore lint/correctness/useExhaustiveDependencies: intentionally runs once on mount with the initial code
  React.useEffect(() => {
    worker.checkSyntax(code);
  }, []);

  const onCodeChange = (newCode: string) => {
    setCode(newCode);
    debouncedSyntaxCheck(newCode);
  };

  // ---------------------------------------------------------------------------
  // Theme resolution
  // ---------------------------------------------------------------------------
  const prefersDarkMode = useMediaQuery('(prefers-color-scheme: dark)');

  // Resolve the user-selected theme mode: 'auto' defers to the OS media
  // query, while 'light' / 'dark' force the corresponding palette so the
  // dark theme can be exercised regardless of the OS preference.
  const paletteMode: 'light' | 'dark' =
    themeMode === 'light'
      ? 'light'
      : themeMode === 'dark'
        ? 'dark'
        : prefersDarkMode
          ? 'dark'
          : 'light';

  // Mirror the resolved palette mode onto the root <html> element so that
  // static CSS rules can target `html[data-theme='dark']` instead of the OS
  // `prefers-color-scheme` media query, which desyncs when the user forces
  // a theme override in Settings.
  React.useLayoutEffect(() => {
    document.documentElement.setAttribute('data-theme', paletteMode);
  }, [paletteMode]);

  // The theme (palette, typography, component overrides) is centralized in
  // src/webapp/theme.ts; it already applies `responsiveFontSizes` so text
  // shrinks gracefully on phones and tablets.
  const theme = React.useMemo(() => buildTheme(paletteMode), [paletteMode]);

  // ---------------------------------------------------------------------------
  // Dashboard card definitions
  // ---------------------------------------------------------------------------

  // Static per-widget metadata (title, icon, sizing) and content, keyed by
  // the same stable ids used by the `WIDGET_ORDER` setting. Rebuilt every
  // render (it's cheap: a handful of object literals), but keyed access
  // means the drag-and-drop reorder below only ever changes *sequence*, not
  // identity, so React reconciles moved cards instead of remounting them.
  const dashboardWidgets: Record<
    DashboardWidgetId,
    {
      title: string;
      icon: React.ReactNode;
      maxContentHeight?: string;
      fullWidth?: boolean;
      expanded: boolean;
      onToggle: () => void;
      content: React.ReactNode;
    }
  > = {
    stats: {
      title: 'Stats',
      icon: <InsightsOutlinedIcon fontSize="small" />,
      fullWidth: true,
      expanded: expandedAccordions.stats,
      onToggle: () => toggleAccordion('stats'),
      content: <Statistics {...stats} />,
    },
    pipeline: {
      title: 'Pipeline',
      icon: <AccountTreeOutlinedIcon fontSize="small" />,
      fullWidth: true,
      expanded: expandedAccordions.pipeline,
      onToggle: () => toggleAccordion('pipeline'),
      content: <Pipeline pipeline={pipeline} colors={pipelineColors} />,
    },
    registers: {
      title: 'Registers',
      icon: <DnsOutlinedIcon fontSize="small" />,
      maxContentHeight: '48vh',
      fullWidth: true,
      expanded: expandedAccordions.registers,
      onToggle: () => toggleAccordion('registers'),
      content: <Registers {...registers} />,
    },
    memory: {
      title: 'Memory',
      icon: <StorageOutlinedIcon fontSize="small" />,
      maxContentHeight: '40vh',
      fullWidth: true,
      expanded: expandedAccordions.memory,
      onToggle: () => toggleAccordion('memory'),
      content: <Memory memory={memory} />,
    },
    stdout: {
      title: 'Standard Output',
      icon: <TerminalOutlinedIcon fontSize="small" />,
      fullWidth: true,
      expanded: expandedAccordions.stdout,
      onToggle: () => toggleAccordion('stdout'),
      content: <StdOut stdout={stdout} />,
    },
  };

  // ---------------------------------------------------------------------------
  // Render
  // ---------------------------------------------------------------------------
  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <InputDialog
        request={inputRequest}
        onSubmit={submitInput}
        onCancel={cancelInput}
      />
      <RuntimeErrorDialog
        open={runtimeErrorMessage !== null}
        message={runtimeErrorMessage || ''}
        onClose={() => setRuntimeErrorMessage(null)}
      />
      <Header
        onLoadClick={loadCode}
        loadEnabled={isValidProgram()}
        onClearClick={clearCode}
        onOpenClick={openCode}
        onSaveClick={saveCode}
        onRestoreClick={restoreDefaultSample}
        parsingErrors={parsingErrors}
        version={worker.version}
        status={status}
        executing={executing}
        inputRequest={inputRequest}
        prefersDarkMode={prefersDarkMode}
        onCacheConfigChange={setCacheConfig}
        viMode={viMode}
        setViMode={setViMode}
        fontSize={fontSize}
        setFontSize={setFontSize}
        accordionAlerts={accordionAlerts}
        setAccordionAlerts={setAccordionAlerts}
        pipelineColors={pipelineColors}
        setPipelineColors={setPipelineColors}
        themeMode={themeMode}
        setThemeMode={setThemeMode}
        forwarding={forwarding}
        setForwarding={setForwarding}
        delaySlot={delaySlot}
        setDelaySlot={setDelaySlot}
        stepStride={stepStride}
        setStepStride={setStepStride}
        executionDelayMs={executionDelayMs}
        setExecutionDelayMs={setExecutionDelayMs}
      />
      <RunControlsToolbar
        onStepClick={clickStep}
        onRunClick={clickRun}
        onPauseClick={() => {
          appInsights.trackEvent({ name: 'pause' });
          dispatch({ type: 'PAUSE_REQUESTED' });
        }}
        onStopClick={clickStop}
        status={status}
        executing={executing}
        inputRequest={inputRequest}
        multiStepCount={stepStride}
      />
      <WorkspaceLayout
        layout={workspaceLayout}
        onLayoutChange={setWorkspaceLayout}
        bottomTitle="Cycles"
        left={
          <Code
            onChangeValue={onCodeChange}
            code={code}
            parsingErrors={parsingErrors}
            parsedInstructions={parsedInstructions}
            pipeline={pipeline}
            running={simulatorRunning}
            viMode={viMode}
            fontSize={fontSize}
            validInstructions={initialState.validInstructions}
            paletteMode={paletteMode}
            pipelineColors={pipelineColors}
            onEditorReady={handleEditorReady}
          />
        }
        bottom={<Cycles cycles={cycles} colors={pipelineColors} />}
        right={
          <Box
            id="dashboard"
            sx={{
              display: 'grid',
              gridTemplateColumns: { xs: '1fr', lg: '1fr 1fr' },
              gap: 1.5,
              p: 1.5,
              // Grow with the content; the parent panel (WorkspaceLayout's
              // right column) provides the scrollbar. A definite height
              // here would let the grid compress the card rows instead of
              // overflowing.
              minHeight: '100%',
              alignContent: 'start',
            }}
          >
            <IssuesCard
              parsingErrors={parsingErrors}
              onIssueClick={handleIssueClick}
            />
            <DndContext
              sensors={dashboardSensors}
              collisionDetection={closestCenter}
              onDragStart={handleDashboardDragStart}
              onDragEnd={handleDashboardDragEnd}
              onDragCancel={handleDashboardDragCancel}
            >
              <SortableContext
                items={widgetOrder}
                strategy={verticalListSortingStrategy}
              >
                {widgetOrder.map((widgetId) => {
                  const widget = dashboardWidgets[widgetId];
                  // Guard against a widget id that has no definition (should
                  // not happen: `sanitize` normalizes the stored order to
                  // exactly the known ids), so a future mismatch degrades to
                  // "skip this entry" instead of crashing the dashboard.
                  if (!widget) {
                    return null;
                  }
                  return (
                    <SortableDashboardCard
                      key={widgetId}
                      id={widgetId}
                      htmlId={`${widgetId}-card`}
                      title={widget.title}
                      icon={widget.icon}
                      maxContentHeight={widget.maxContentHeight}
                      fullWidth={widget.fullWidth}
                      expanded={widget.expanded}
                      onToggle={widget.onToggle}
                    >
                      {widget.content}
                    </SortableDashboardCard>
                  );
                })}
              </SortableContext>
              <DragOverlay dropAnimation={dashboardDropAnimation}>
                {activeWidgetId && dashboardWidgets[activeWidgetId] ? (
                  <Box
                    sx={{
                      width: activeWidgetWidth,
                      // A single large-blur, zero-spread shadow so the clone
                      // reads as the unchanged card gently floating above the
                      // page. MUI's numbered elevations (e.g. `boxShadow: 6`)
                      // include tight edge-hugging components that render as
                      // an ugly grey ring around the card — avoid them here.
                      boxShadow: '0 8px 24px rgba(0, 0, 0, 0.15)',
                      borderRadius: 1.25,
                      cursor: 'grabbing',
                    }}
                  >
                    <DashboardCard
                      title={dashboardWidgets[activeWidgetId].title}
                      icon={dashboardWidgets[activeWidgetId].icon}
                      maxContentHeight={
                        dashboardWidgets[activeWidgetId].maxContentHeight
                      }
                      expanded={dashboardWidgets[activeWidgetId].expanded}
                      onToggle={() => {}}
                      dragHandle={
                        <Box
                          sx={{
                            display: 'flex',
                            alignItems: 'center',
                            pl: 0.5,
                            color: 'text.secondary',
                          }}
                        >
                          <DragIndicatorIcon fontSize="small" />
                        </Box>
                      }
                    >
                      {dashboardWidgets[activeWidgetId].content}
                    </DashboardCard>
                  </Box>
                ) : null}
              </DragOverlay>
            </DndContext>
          </Box>
        }
      />
    </ThemeProvider>
  );
};

export default Simulator;
