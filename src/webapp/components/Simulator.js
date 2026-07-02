import React from 'react';

import Code from './Code';
import Memory from './Memory';
import Pipeline from './Pipeline';
import Registers from './Registers';
import Statistics from './Statistics';
import Header from './Header';
import RunControlsToolbar from './RunControlsToolbar';
import Box from '@mui/material/Box';
import Grid from '@mui/material/Grid';
import ConfigDialog from './ConfigDialog';
import DashboardCard from './DashboardCard';
import IssuesCard from './IssuesCard';
import StdOut from './StdOut';
import InputDialog from './InputDialog';

import AccountTreeOutlinedIcon from '@mui/icons-material/AccountTreeOutlined';
import DnsOutlinedIcon from '@mui/icons-material/DnsOutlined';
import InsightsOutlinedIcon from '@mui/icons-material/InsightsOutlined';
import StorageOutlinedIcon from '@mui/icons-material/StorageOutlined';
import TerminalOutlinedIcon from '@mui/icons-material/TerminalOutlined';

import useMediaQuery from '@mui/material/useMediaQuery';
import { ThemeProvider } from '@mui/material/styles';
import CssBaseline from '@mui/material/CssBaseline';
import { buildTheme } from '../theme';

import { debounce } from 'lodash';
import { useSetting } from '../settings/useSetting';
import { SettingKey } from '../settings/SettingKey';
import SampleProgram from '../data/SampleProgram';
import { deriveLogicalState } from '../simulatorState';

const Simulator = ({worker, initialState, appInsights}) => {
  // The amount of steps to run in multi-step executions.
  const INTERNAL_STEPS_STRIDE = 50;

  const [registers, setRegisters] = React.useState(initialState.registers);
  const [memory, setMemory] = React.useState(initialState.memory);
  const [stats, setStats] = React.useState(initialState.statistics);

  // Editor code persistence:
  // - `storedCode` is what useLocalStorage actually holds ('' = "never edited by user").
  // - `code` is the live value the Monaco editor shows ('' maps to SampleProgram).
  // - Writes to localStorage are debounced so individual keystrokes don't block.
  const [storedCode, setStoredCode, resetStoredCode] = useSetting(SettingKey.EDITOR_CODE);
  const [code, _setCode] = React.useState(() =>
    storedCode === '' ? SampleProgram : storedCode,
  );

  // Debounce localStorage writes to 500 ms so each keystroke doesn't trigger a
  // synchronous `setItem` call.  The useMemo ensures a single stable debounced
  // function instance exists across re-renders (the instance carries the timer
  // state; recreating it on every render would orphan pending timers).
  const debouncedPersistCode = React.useMemo(
    () => debounce((v) => setStoredCode(v), 500),
    // setStoredCode comes from useLocalStorage and is stable across renders.
    [setStoredCode],
  );

  // Public setter: updates the editor display immediately while deferring the
  // localStorage write.
  const setCode = React.useCallback(
    (newCode) => {
      _setCode(newCode);
      debouncedPersistCode(newCode);
    },
    [debouncedPersistCode],
  );
  const [status, setStatus] = React.useState(initialState.status);
  const [pipeline, setPipeline] = React.useState(initialState.pipeline);
  const [parsingErrors, setParsingErrors] = React.useState(
    initialState.parsingErrors,
  );
  const [parsedInstructions, setParsedInstructions] = React.useState(
    initialState.parsedInstructions,
  );
  const [stdout, setStdout] = React.useState('');
  const [inputRequest, setInputRequest] = React.useState(null);

  const [viMode, setViMode] = useSetting(SettingKey.VI_MODE);
  const [fontSize, setFontSize] = useSetting(SettingKey.FONT_SIZE);
  const [accordionAlerts, setAccordionAlerts] = useSetting(SettingKey.ACCORDION_ALERTS);
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

  // `executionDelayMs` is read inside async callbacks that were captured when
  // a step batch started (potentially many batches ago). Mirror the latest
  // value in a ref so the delay applied between batches always reflects the
  // *current* setting, not the one that was active when "Run All" was
  // pressed. This lets the user tweak the delay live, mid-run.
  const executionDelayRef = React.useRef(executionDelayMs);
  React.useEffect(() => {
    executionDelayRef.current = executionDelayMs;
  }, [executionDelayMs]);

  // Reference to the Monaco editor instance, populated by `<Code />` once
  // the editor has mounted. Used by the Issues panel to jump the editor to
  // the line/column of a parsing error or warning when the user clicks it.
  const editorRef = React.useRef(null);
  const handleEditorReady = React.useCallback((editor) => {
    editorRef.current = editor;
  }, []);
  const handleIssueClick = React.useCallback((row, column) => {
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

  // Whether the Configuration dialog (General Settings + Cache) is open.
  // In the dashboard layout the configuration panels live in a modal
  // reachable from the gear button in the header, keeping the dashboard
  // canvas dedicated to live simulator data.
  const [configOpen, setConfigOpen] = React.useState(false);

  // Number of steps left to run. Used to keep track of execution.
  // If set to -1, runs until the execution ends.
  const [stepsToRun, setStepsToRun] = React.useState(0);

  // Signals that the simulation must pause.
  const [mustPause, setMustPause] = React.useState(false);

  // Tracks whether the worker is currently running code.
  const [executing, setExecuting] = React.useState(false);

  // Tracks whether the simulation is running in "run all" mode (run until finished).
  const [runAll, setRunAll] = React.useState(false);

  const simulatorRunning = status == 'RUNNING';

  // Tracks if the program has no syntax errors and can be loaded.
  // TODO: Allow code execution w/ warnings in the worker, then uncomment the line below
  const isValidProgram = () => {
    if (!parsingErrors) { return true; }
    else {
      return (parsingErrors.filter((e) => !e.isWarning).length === 0);
    }
  };

  // Returns true if the given parsingErrors array contains at least one
  // actual error (not just warnings). Used to decide whether it is safe
  // to keep the parsed instructions around (e.g. to power the hover
  // provider in the code editor): programs that only emit warnings are
  // still successfully parsed and loadable, so we should not discard the
  // parsed instructions in that case.
  const hasRealErrors = (parsingErrors) => {
    if (!parsingErrors) return false;
    return parsingErrors.some((e) => !e.isWarning);
  };

  worker.onmessage = (e) => {
    const result = worker.parseResult(e.data);
    console.log('Got message from worker.', result);
    
    // For syntax check responses, only update parsing errors to avoid unnecessary re-renders
    if (result.method === 'checksyntax') {
      setParsingErrors(result.parsingErrors);
      if (hasRealErrors(result.parsingErrors)) {
        setParsedInstructions(null);
      } else {
        setParsedInstructions(result.parsedInstructions);
      }
      return;
    }

    if (result.inputRequested) {
      applyResultState(result);
      setExecuting(false);
      setInputRequest(result);
      return;
    }
    
    updateState(result);
  };

  const applyResultState = (result) => {
    setRegisters(result.registers);
    setMemory(result.memory);
    setStats(result.statistics);
    setStatus(result.status);
    setPipeline(result.pipeline);
    setParsingErrors(result.parsingErrors);

    if (hasRealErrors(result.parsingErrors)) {
      setParsedInstructions(null);
    } else {
      setParsedInstructions(result.parsedInstructions);
    }

    if (result.stdout) {
      setStdout(result.stdout);
    }
  };

  const updateState = (result) => {
    console.log('Updating state.');

    applyResultState(result);

    // TODO: cleaner handling of error types. Checking the error message is a pretty weak check.
    // Runtime errors should not cause multiple alert prompting to avoid webui getting stuck
    if (!result.success && result.errorMessage !== 'Parsing errors.') {
      // Synchronous exceptions carry structured info (errorCode, errorInstruction, errorStage).
      // When present, compose a clearer, multi-line alert message.
      let message = result.errorMessage;
      if (result.errorCode) {
        message = `Synchronous exception: ${result.errorMessage}`;
        if (result.errorInstruction && result.errorStage) {
          message += `\n\nInstruction: ${result.errorInstruction}\nPipeline stage: ${result.errorStage}`;
        }
      }
      alert(message);
      stopCode();
      setExecuting(false);
      // stopCode() queues state updates (setRunAll(false), setMustPause(true))
      // and a worker.reset(), but those don't take effect within this closure.
      // Return early to avoid falling through to the "schedule more steps"
      // branch below, which would otherwise race worker.reset() with a new
      // step() call against an already-reset (READY) CPU and surface a
      // spurious "Cannot run in state READY" alert.
      return;
    }

    // Note: we intentionally keep `executing === true` across inter-batch
    // delays when more steps are queued. Clearing `executing` between
    // batches would toggle the toolbar buttons (Run/Step/Stop becoming
    // enabled, Pause becoming disabled) every stride, which looks like a
    // flash during long runs with a non-zero execution delay. The user
    // should see the same "running" controls whether the worker is busy
    // stepping or we're simply waiting out the inter-batch delay.
    if (result.status !== 'RUNNING' || mustPause || result.encounteredBreak) {
      setStepsToRun(0);
      setMustPause(false);
      setRunAll(false);
      setExecuting(false);
    } else if (stepsToRun > 0) {
      console.log('Steps left: ' + stepsToRun);
      scheduleNextBatch(() => stepCode(stepsToRun));
    } else if (runAll) {
      scheduleNextBatch(() => stepCode(INTERNAL_STEPS_STRIDE));
    } else {
      // No further batches scheduled (e.g. a plain Single Step finishing):
      // we're done executing for now.
      setExecuting(false);
    }
  };

  // Pending timeout id for a delayed follow-up batch, so that stopping the
  // simulation cancels any batch that was sleeping between strides instead
  // of having it fire after `worker.reset()` and surface a spurious error.
  const nextBatchTimeout = React.useRef(null);

  const cancelPendingBatch = () => {
    if (nextBatchTimeout.current !== null) {
      clearTimeout(nextBatchTimeout.current);
      nextBatchTimeout.current = null;
    }
  };

  // Schedule the next internal step batch, inserting the user-configured
  // execution delay so long runs are visually paced. A delay of 0 ms (the
  // default) runs batches back-to-back, matching the pre-existing behavior.
  const scheduleNextBatch = (fn) => {
    cancelPendingBatch();
    const delay = executionDelayRef.current;
    if (delay <= 0) {
      fn();
      return;
    }
    nextBatchTimeout.current = setTimeout(() => {
      nextBatchTimeout.current = null;
      fn();
    }, delay);
  };

  // Click handlers. Decoupled from business logic to place the telemetry hooks in the right place.
  const clickRun = () => {
    appInsights.trackEvent({name: "click", properties: {action: "run"}});
    console.log('Executing runCode');
    runCode()
  }

  const clickStep = (n) => {
    appInsights.trackEvent({name: "click", properties: {action: "step"}});
    console.log('Executing steps: ' + n);
    stepCode(n)
  }

  const clickLoad = () => {
    appInsights.trackEvent({name: "click", properties: {action: "load"}});
    console.log('Executing loadCode');
    loadCode();
  }

  const clickStop = () => {
    appInsights.trackEvent({name: "click", properties: {action: "stop"}});
    console.log('Stopping simulation');
    stopCode();
  }

  // Business logic for click handlers.
  const runCode = () => {
    setRunAll(true);
    stepCode(INTERNAL_STEPS_STRIDE);
  };

  const stepCode = (n) => {
    const toRun = Math.min(n, INTERNAL_STEPS_STRIDE);
    setStepsToRun(n - toRun);
    setExecuting(true);
    worker.step(toRun);
  };

  const stopCode = () => {
    // If a batch was sleeping between strides, cancel it. In that case no
    // worker result is in flight to flip `executing` back off via
    // `updateState`, so clear it here too.
    const hadPendingBatch = nextBatchTimeout.current !== null;
    cancelPendingBatch();
    setMustPause(true);
    setRunAll(false);
    setStepsToRun(0);
    setInputRequest(null);
    if (hadPendingBatch) {
      setExecuting(false);
    }
    worker.reset();  // Assuming simulator has a reset method
  };

  const clearCode = () => {
    const hadPendingBatch = nextBatchTimeout.current !== null;
    cancelPendingBatch();
    setCode(".data\n\n.code\n  SYSCALL 0\n");
    setInputRequest(null);
    if (hadPendingBatch) {
      setExecuting(false);
    }
    worker.reset();
  }

  const restoreDefaultSample = () => {
    const hadPendingBatch = nextBatchTimeout.current !== null;
    cancelPendingBatch();
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
    setInputRequest(null);
    if (hadPendingBatch) {
      setExecuting(false);
    }
    worker.reset();
    // Run a fresh syntax check on the restored sample so any warnings in the
    // sample are surfaced immediately.
    worker.checkSyntax(SampleProgram);
  };


  const setCacheConfig = React.useCallback((config) => {
    worker.setCacheConfig(config);
  }, [worker]);

  const openCode = () => {
    const fileInput = document.createElement('input');
    fileInput.type = 'file';
    fileInput.accept = '.asm,.txt,.s';
    fileInput.onchange = (event) => {
      const file = event.target.files[0];
      if (file) {
        const reader = new FileReader();
        reader.onload = (e) => {
          setCode(e.target.result);
        };
        reader.readAsText(file);
      }
    };
    fileInput.click();
  }

  const loadCode = () => {
    setStdout("");
    worker.load(code);
  };

  const submitInput = (input) => {
    setInputRequest(null);
    setExecuting(true);
    worker.provideInput(input);
  };

  const cancelInput = () => {
    submitInput('');
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

  // A stable debounced wrapper around worker.checkSyntax.  Creating this with
  // useMemo ensures the same debounced instance (with its internal timer state)
  // is reused across re-renders.  A plain `const debouncedSyntaxCheck = debounce(...)`
  // at the top of the function body would create a fresh function on every
  // render, orphaning any pending timer from the previous render.
  const debouncedSyntaxCheck = React.useMemo(
    () => debounce((c) => worker.checkSyntax(c), 500),
    // worker is stable across renders (passed as a prop reference).
    [worker],
  );

  // Run a syntax check on the initial code once on mount so that warnings
  // (e.g. deprecated instructions in the sample program) are surfaced
  // immediately, without requiring the user to edit the code first.
  React.useEffect(() => {
    worker.checkSyntax(code);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  // Global keyboard shortcuts for run-control actions.
  // A ref holds a callback that reads the latest closures on every call, so
  // the window listener itself is registered just once (empty cleanup deps
  // would be wrong; we need status/executing/inputRequest/stepStride for the
  // guard).  We keep the handler in a ref so the listener function identity
  // is stable across re-renders and we only need to re-register when the
  // observable state used for gating actually changes.
  const keyboardHandlerRef = React.useRef(null);
  keyboardHandlerRef.current = (e) => {
    // Don't steal keys while a modal dialog is open (Help, Settings, Input).
    if (document.querySelector('[role="dialog"]')) return;
    const logicalState = deriveLogicalState(status, executing, inputRequest);
    if (logicalState === 'WAITING_FOR_INPUT') return;

    switch (e.key) {
      case 'F2':
        e.preventDefault();
        if (isValidProgram()) {
          appInsights.trackEvent({
            name: 'click',
            properties: { action: 'load', source: 'keyboard' },
          });
          loadCode();
        }
        break;
      case 'F8':
        e.preventDefault();
        if (logicalState === 'READY') {
          appInsights.trackEvent({
            name: 'click',
            properties: { action: 'run', source: 'keyboard' },
          });
          runCode();
        } else if (logicalState === 'EXECUTING') {
          appInsights.trackEvent({ name: 'pause', source: 'keyboard' });
          setMustPause(true);
        }
        break;
      case 'F9':
        e.preventDefault();
        if (logicalState === 'READY') {
          appInsights.trackEvent({
            name: 'click',
            properties: { action: 'step', source: 'keyboard' },
          });
          stepCode(1);
        }
        break;
      case 'F10':
        e.preventDefault();
        if (logicalState === 'READY') {
          appInsights.trackEvent({
            name: 'click',
            properties: { action: 'step', source: 'keyboard' },
          });
          stepCode(stepStride);
        }
        break;
      case 'Escape':
        e.preventDefault();
        if (logicalState === 'READY') {
          appInsights.trackEvent({
            name: 'click',
            properties: { action: 'stop', source: 'keyboard' },
          });
          stopCode();
        }
        break;
      default:
        break;
    }
  };

  React.useEffect(() => {
    const handleKeyDown = (e) => keyboardHandlerRef.current(e);
    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, []);

  const onCodeChange = (code) => {
    setCode(code);
    debouncedSyntaxCheck(code);
  };

  const prefersDarkMode = useMediaQuery('(prefers-color-scheme: dark)');

  // Resolve the user-selected theme mode: 'auto' defers to the OS media
  // query, while 'light' / 'dark' force the corresponding palette so the
  // dark theme can be exercised regardless of the OS preference.
  const paletteMode =
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
  // src/webapp/theme.js; it already applies `responsiveFontSizes` so text
  // shrinks gracefully on phones and tablets.
  const theme = React.useMemo(() => buildTheme(paletteMode), [paletteMode]);

  return (
    <>
      <ThemeProvider theme={theme}>
        <CssBaseline />
        <InputDialog
          request={inputRequest}
          onSubmit={submitInput}
          onCancel={cancelInput}
        />
        <ConfigDialog
          open={configOpen}
          onClose={() => setConfigOpen(false)}
          settingsProps={{
            viMode,
            setViMode,
            fontSize,
            setFontSize,
            accordionAlerts,
            setAccordionAlerts,
            forwarding,
            setForwarding,
            delaySlot,
            setDelaySlot,
            stepStride,
            setStepStride,
            executionDelayMs,
            setExecutionDelayMs,
            pipelineColors,
            setPipelineColors,
            themeMode,
            setThemeMode,
            status,
          }}
          cacheProps={{ onChange: setCacheConfig, status }}
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
          onConfigClick={() => setConfigOpen(true)}
        />
        <RunControlsToolbar
          onStepClick={clickStep}
          onRunClick={clickRun}
          onPauseClick={() => {
            appInsights.trackEvent({ name: 'pause' });
            setMustPause(true);
          }}
          onStopClick={clickStop}
          status={status}
          executing={executing}
          inputRequest={inputRequest}
          multiStepCount={stepStride}
        />
        <Grid container id="main-grid" disableEqualOverflow spacing={0}>
          <Grid id="left-panel" size={{ xs: 12, md: 6 }}>
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
              onEditorReady={handleEditorReady}
            />
          </Grid>
          <Grid size={{ xs: 12, md: 6 }} id="right-panel" disableEqualOverflow>
            <Box
              id="dashboard"
              sx={{
                display: 'grid',
                gridTemplateColumns: { xs: '1fr', lg: '1fr 1fr' },
                gap: 1.5,
                p: 1.5,
                height: '100%',
                overflow: 'auto',
                alignContent: 'start',
              }}
            >
              <IssuesCard
                parsingErrors={parsingErrors}
                onIssueClick={handleIssueClick}
              />
              <DashboardCard
                id="pipeline-card"
                title="Pipeline"
                icon={<AccountTreeOutlinedIcon fontSize="small" />}
              >
                <Pipeline pipeline={pipeline} colors={pipelineColors} />
              </DashboardCard>
              <DashboardCard
                id="stats-card"
                title="Statistics"
                icon={<InsightsOutlinedIcon fontSize="small" />}
                maxContentHeight="42vh"
              >
                <Statistics {...stats} />
              </DashboardCard>
              <DashboardCard
                id="registers-card"
                title="Registers"
                icon={<DnsOutlinedIcon fontSize="small" />}
                maxContentHeight="48vh"
                fullWidth
              >
                <Registers {...registers} />
              </DashboardCard>
              <DashboardCard
                id="memory-card"
                title="Memory"
                icon={<StorageOutlinedIcon fontSize="small" />}
                maxContentHeight="40vh"
                fullWidth
              >
                <Memory memory={memory} />
              </DashboardCard>
              <DashboardCard
                id="stdout-card"
                title="Standard Output"
                icon={<TerminalOutlinedIcon fontSize="small" />}
                fullWidth
              >
                <StdOut stdout={stdout} />
              </DashboardCard>
            </Box>
          </Grid>
        </Grid>
      </ThemeProvider>
    </>
  );
};

export default Simulator;
