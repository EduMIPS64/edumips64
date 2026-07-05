import React from 'react';

import Code from './Code';
import Cycles from './Cycles';
import Memory from './Memory';
import Pipeline from './Pipeline';
import Registers from './Registers';
import Statistics from './Statistics';
import Header from './Header';
import RunControlsToolbar from './RunControlsToolbar';
import Accordion from '@mui/material/Accordion';
import AccordionDetails from '@mui/material/AccordionDetails';
import MuiAccordionSummary, {
  type AccordionSummaryProps,
} from '@mui/material/AccordionSummary';
import Grid from '@mui/material/Grid';
import ErrorList from './ErrorList';
import StdOut from './StdOut';
import InputDialog from './InputDialog';
import RuntimeErrorDialog from './RuntimeErrorDialog';

import ArrowForwardIosSharpIcon from '@mui/icons-material/ArrowForwardIosSharp';

import { styled } from '@mui/material/styles';

import useMediaQuery from '@mui/material/useMediaQuery';
import { ThemeProvider } from '@mui/material/styles';
import CssBaseline from '@mui/material/CssBaseline';
import { buildTheme } from '../theme';

import ExpandMoreIcon from '@mui/icons-material/ExpandMore';
import Typography from '@mui/material/Typography';

import debounce from 'lodash/debounce';
import isEqual from 'lodash/isEqual';
import { useSetting } from '../settings/useSetting';
import { SettingKey } from '../settings/SettingKey';
import SampleProgram from '../data/SampleProgram';
import type { CacheConfig as CacheConfigType } from '../settings/schema';

import { useSimulatorData } from '../hooks/useSimulatorData';
import { useExecutionController } from '../hooks/useExecutionController';
import { useKeyboardShortcuts } from '../hooks/useKeyboardShortcuts';
import type { SimulatorResult, SimulatorWorker } from '../simulator/protocol';
import type { ITelemetryClient } from '../telemetry';
import type * as monacoEditor from 'monaco-editor/esm/vs/editor/editor.api';
import type { ExpandedAccordions } from '../settings/schema';

// Styled accordion header shared by all right-panel widgets. Defined at
// module scope on purpose: defining a styled() component inside the
// Simulator render body would create a new component *type* on every
// render, making React unmount and remount every accordion header each
// time a worker message updates state.
//
// Explicitly type the wrapper so `styled()` knows that all AccordionSummary
// props (children, expandIcon, etc.) are valid on the resulting component.
const AccordionSummary = styled(({ ...props }: AccordionSummaryProps) => (
  <MuiAccordionSummary
    expandIcon={<ArrowForwardIosSharpIcon sx={{ fontSize: '0.8rem' }} />}
    {...props}
  />
))(({ theme }) => ({
  backgroundColor:
    theme.palette.mode === 'dark'
      ? 'rgba(141, 166, 255, 0.08)'
      : 'rgba(53, 87, 212, 0.06)',
  flexDirection: 'row-reverse',
  '& .MuiAccordionSummary-expandIconWrapper.Mui-expanded': {
    transform: 'rotate(180deg)',
  },
  '& .MuiAccordionSummary-content': {
    marginLeft: theme.spacing(1),
  },
}));

// Key names for the right-panel accordion panels.
type AccordionPanel = keyof ExpandedAccordions;

// State shape for tracking which accordions have received new data while
// they were collapsed (used to render the change-indicator dot).
type AccordionChanges = Record<AccordionPanel, boolean>;

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
  const [expandedAccordions, setExpandedAccordions] = useSetting(
    SettingKey.EXPANDED_ACCORDIONS,
  );

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
  const [runtimeErrorMessage, setRuntimeErrorMessage] = React.useState<string | null>(null);

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
  React.useEffect(() => {
    worker.setForwarding(forwarding);
  }, [forwarding]);

  // Same pattern for the branch delay slot setting.
  React.useEffect(() => {
    worker.setDelaySlot(delaySlot);
  }, [delaySlot]);

  // ---------------------------------------------------------------------------
  // Editor ref (for Issues panel click-to-navigate)
  // ---------------------------------------------------------------------------
  const editorRef = React.useRef<monacoEditor.editor.IStandaloneCodeEditor | null>(null);
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

  // ---------------------------------------------------------------------------
  // Accordion change-detection state
  // ---------------------------------------------------------------------------

  // Track if data has changed while accordion was collapsed
  const [accordionChanges, setAccordionChanges] = React.useState<AccordionChanges>({
    stats: false,
    pipeline: false,
    cycles: false,
    registers: false,
    memory: false,
    stdout: false,
  });

  // Refs to track previous values for change detection
  const prevStats = React.useRef(stats);
  const prevPipeline = React.useRef(pipeline);
  const prevCycles = React.useRef(cycles);
  const prevRegisters = React.useRef(registers);
  const prevMemory = React.useRef(memory);
  const prevStdout = React.useRef(stdout);

  // Ref to track if we are resetting the simulator (clearing code)
  const isResetting = React.useRef(false);

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
    isResetting.current = true;
    setAccordionChanges({
      stats: false,
      pipeline: false,
      cycles: false,
      registers: false,
      memory: false,
      stdout: false,
    });
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
    isResetting.current = true;
    // Run a fresh syntax check on the restored sample so any warnings in the
    // sample are surfaced immediately.
    worker.checkSyntax(SampleProgram);
    setAccordionChanges({
      stats: false,
      pipeline: false,
      cycles: false,
      registers: false,
      memory: false,
      stdout: false,
    });
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
  // Accordion change-detection effect
  // ---------------------------------------------------------------------------

  // Detect changes in accordion data when collapsed
  React.useEffect(() => {
    if (!accordionAlerts) {
      prevStats.current = stats;
      prevPipeline.current = pipeline;
      prevCycles.current = cycles;
      prevRegisters.current = registers;
      prevMemory.current = memory;
      prevStdout.current = stdout;
      return;
    }

    // If we are resetting, update refs but don't trigger changes
    if (isResetting.current) {
      prevStats.current = stats;
      prevPipeline.current = pipeline;
      prevCycles.current = cycles;
      prevRegisters.current = registers;
      prevMemory.current = memory;
      prevStdout.current = stdout;
      isResetting.current = false;
      return;
    }

    const statsChanged = !isEqual(stats, prevStats.current);
    const pipelineChanged = !isEqual(pipeline, prevPipeline.current);
    // useSimulatorData preserves the cycles reference when the data is
    // deep-equal, so a reference comparison is sufficient here (the object
    // can grow large over long executions, making isEqual costly).
    const cyclesChanged = cycles !== prevCycles.current;
    const registersChanged = !isEqual(registers, prevRegisters.current);
    const memoryChanged = !isEqual(memory, prevMemory.current);
    const stdoutChanged = stdout !== prevStdout.current;

    // Update refs first
    prevStats.current = stats;
    prevPipeline.current = pipeline;
    prevCycles.current = cycles;
    prevRegisters.current = registers;
    prevMemory.current = memory;
    prevStdout.current = stdout;

    // Only update state if there are actual changes for collapsed accordions
    if (
      (!expandedAccordions.stats && statsChanged) ||
      (!expandedAccordions.pipeline && pipelineChanged) ||
      (!expandedAccordions.cycles && cyclesChanged) ||
      (!expandedAccordions.registers && registersChanged) ||
      (!expandedAccordions.memory && memoryChanged) ||
      (!expandedAccordions.stdout && stdoutChanged)
    ) {
      setAccordionChanges((prev) => ({
        ...prev,
        stats: prev.stats || (!expandedAccordions.stats && statsChanged),
        pipeline:
          prev.pipeline || (!expandedAccordions.pipeline && pipelineChanged),
        cycles: prev.cycles || (!expandedAccordions.cycles && cyclesChanged),
        registers:
          prev.registers || (!expandedAccordions.registers && registersChanged),
        memory: prev.memory || (!expandedAccordions.memory && memoryChanged),
        stdout: prev.stdout || (!expandedAccordions.stdout && stdoutChanged),
      }));
    }
  }, [
    stats,
    pipeline,
    cycles,
    registers,
    memory,
    stdout,
    accordionAlerts,
    expandedAccordions,
  ]);

  // Handle accordion expansion change
  const handleAccordionChange =
    (panel: AccordionPanel) => (_event: React.SyntheticEvent, isExpanded: boolean) => {
      setExpandedAccordions((prev) => ({
        ...prev,
        [panel]: isExpanded,
      }));

      // Clear change indicator when accordion is opened
      if (isExpanded) {
        setAccordionChanges((prev) => ({
          ...prev,
          [panel]: false,
        }));
      }
    };

  // ---------------------------------------------------------------------------
  // Syntax check on mount
  // ---------------------------------------------------------------------------

  // Run a syntax check on the initial code once on mount so that warnings
  // (e.g. deprecated instructions in the sample program) are surfaced
  // immediately, without requiring the user to edit the code first.
  React.useEffect(() => {
    worker.checkSyntax(code);
    // eslint-disable-next-line react-hooks/exhaustive-deps
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
  // Render
  // ---------------------------------------------------------------------------
  return (
    <>
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
        <Grid container id="main-grid" spacing={0}>
          <Grid id="left-panel" size={{ xs: 12, md: 8 }}>
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
          </Grid>
          <Grid size={{ xs: 12, md: 4 }} id="right-panel">
            <ErrorList
              parsingErrors={parsingErrors}
              AccordionSummary={AccordionSummary}
              onIssueClick={handleIssueClick}
            />
            <Accordion
              expanded={expandedAccordions.stats}
              onChange={handleAccordionChange('stats')}
              disableGutters
            >
              <AccordionSummary expandIcon={<ExpandMoreIcon />}>
                <Typography
                  variant="subtitle2"
                  sx={{ fontWeight: 600, color: 'primary.main' }}
                >
                  Stats
                  {accordionAlerts && accordionChanges.stats && (
                    <span className="accordion-change-indicator" />
                  )}
                </Typography>
              </AccordionSummary>
              <AccordionDetails>
                <Statistics {...stats} />
              </AccordionDetails>
            </Accordion>
            <Accordion
              expanded={expandedAccordions.pipeline}
              onChange={handleAccordionChange('pipeline')}
              disableGutters
            >
              <AccordionSummary expandIcon={<ExpandMoreIcon />}>
                <Typography
                  variant="subtitle2"
                  sx={{ fontWeight: 600, color: 'primary.main' }}
                >
                  Pipeline
                  {accordionAlerts && accordionChanges.pipeline && (
                    <span className="accordion-change-indicator" />
                  )}
                </Typography>
              </AccordionSummary>
              <AccordionDetails>
                <Pipeline pipeline={pipeline} colors={pipelineColors} />
              </AccordionDetails>
            </Accordion>
            <Accordion
              expanded={expandedAccordions.cycles}
              onChange={handleAccordionChange('cycles')}
              disableGutters
            >
              <AccordionSummary expandIcon={<ExpandMoreIcon />}>
                <Typography
                  variant="subtitle2"
                  sx={{ fontWeight: 600, color: 'primary.main' }}
                >
                  Cycles
                  {accordionAlerts && accordionChanges.cycles && (
                    <span className="accordion-change-indicator" />
                  )}
                </Typography>
              </AccordionSummary>
              <AccordionDetails>
                <Cycles cycles={cycles} colors={pipelineColors} />
              </AccordionDetails>
            </Accordion>
            <Accordion
              expanded={expandedAccordions.registers}
              onChange={handleAccordionChange('registers')}
              disableGutters
            >
              <AccordionSummary expandIcon={<ExpandMoreIcon />}>
                <Typography
                  variant="subtitle2"
                  sx={{ fontWeight: 600, color: 'primary.main' }}
                >
                  Registers
                  {accordionAlerts && accordionChanges.registers && (
                    <span className="accordion-change-indicator" />
                  )}
                </Typography>
              </AccordionSummary>
              <AccordionDetails>
                <Registers {...registers} />
              </AccordionDetails>
            </Accordion>
            <Accordion
              expanded={expandedAccordions.memory}
              onChange={handleAccordionChange('memory')}
              disableGutters
            >
              <AccordionSummary
                expandIcon={<ExpandMoreIcon />}
                id="memory-accordion-summary"
              >
                <Typography
                  variant="subtitle2"
                  sx={{ fontWeight: 600, color: 'primary.main' }}
                >
                  Memory
                  {accordionAlerts && accordionChanges.memory && (
                    <span className="accordion-change-indicator" />
                  )}
                </Typography>
              </AccordionSummary>
              <AccordionDetails>
                <Memory memory={memory} />
              </AccordionDetails>
            </Accordion>
            <Accordion
              expanded={expandedAccordions.stdout}
              onChange={handleAccordionChange('stdout')}
              disableGutters
            >
              <AccordionSummary expandIcon={<ExpandMoreIcon />}>
                <Typography
                  variant="subtitle2"
                  sx={{ fontWeight: 600, color: 'primary.main' }}
                >
                  Standard Output
                  {accordionAlerts && accordionChanges.stdout && (
                    <span className="accordion-change-indicator" />
                  )}
                </Typography>
              </AccordionSummary>
              <AccordionDetails>
                <StdOut stdout={stdout} />
              </AccordionDetails>
            </Accordion>
          </Grid>
        </Grid>
      </ThemeProvider>
    </>
  );
};

export default Simulator;
