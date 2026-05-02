import React from 'react';

import Code from './Code';
import Memory from './Memory';
import Pipeline from './Pipeline';
import Registers from './Registers';
import Statistics from './Statistics';
import ErrorList from './ErrorList';
import StdOut from './StdOut';
import InputDialog from './InputDialog';
import HelpDialog from './HelpDialog';
import CpuStatusDisplay from './CpuStatusDisplay';

import Button from '@mui/material/Button';
import IconButton from '@mui/material/IconButton';
import Tooltip from '@mui/material/Tooltip';
import Drawer from '@mui/material/Drawer';

import useMediaQuery from '@mui/material/useMediaQuery';
import { createTheme, ThemeProvider } from '@mui/material/styles';
import CssBaseline from '@mui/material/CssBaseline';

import PlayArrowIcon from '@mui/icons-material/PlayArrow';
import FastForwardIcon from '@mui/icons-material/FastForward';
import PlayCircleIcon from '@mui/icons-material/PlayCircle';
import PauseCircleIcon from '@mui/icons-material/PauseCircle';
import StopCircleIcon from '@mui/icons-material/StopCircle';
import UploadIcon from '@mui/icons-material/Upload';
import DownloadIcon from '@mui/icons-material/Download';
import DeleteForeverIcon from '@mui/icons-material/DeleteForever';
import HelpIcon from '@mui/icons-material/Help';
import TuneIcon from '@mui/icons-material/Tune';

import logoBright from '../static/logo.png';
import logoDark from '../static/logo-dark.png';

import SampleProgram from '../data/SampleProgram';

import { debounce, isEqual } from 'lodash';
import Settings from './Settings';
import CacheConfig from "./CacheConfig";
import { useSetting } from '../settings/useSetting';
import { SettingKey } from '../settings/SettingKey';

import '../css/dashboard-grid.css';

const Simulator = ({worker, initialState, appInsights}) => {
  // The amount of steps to run in multi-step executions.
  const INTERNAL_STEPS_STRIDE = 50;

  const [registers, setRegisters] = React.useState(initialState.registers);
  const [memory, setMemory] = React.useState(initialState.memory);
  const [stats, setStats] = React.useState(initialState.statistics);
  const [code, setCode] = React.useState(SampleProgram);
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
  const [stepStride, setStepStride] = useSetting(SettingKey.STEP_STRIDE);
  const [executionDelayMs, setExecutionDelayMs] = useSetting(
    SettingKey.EXECUTION_DELAY_MS,
  );

  // `executionDelayMs` is read inside async callbacks that were captured when
  // a step batch started (potentially many batches ago). Mirror the latest
  // value in a ref so the delay applied between batches always reflects the
  // *current* setting, not the one that was active when "Run All" was
  // pressed. This lets the user tweak the delay live, mid-run.
  const executionDelayRef = React.useRef(executionDelayMs);
  React.useEffect(() => {
    executionDelayRef.current = executionDelayMs;
  }, [executionDelayMs]);

  // Keep the simulator worker's forwarding flag in sync with the persisted
  // setting. Runs once on mount (so a value restored from localStorage is
  // pushed to the worker) and whenever the user toggles the switch.
  React.useEffect(() => {
    worker.setForwarding(forwarding);
  }, [forwarding]);

  // Track expanded state for each accordion
  const [expandedAccordions, setExpandedAccordions] = useSetting(SettingKey.EXPANDED_ACCORDIONS);

  // Track if data has changed while accordion was collapsed
  const [accordionChanges, setAccordionChanges] = React.useState({
    stats: false,
    pipeline: false,
    registers: false,
    memory: false,
    stdout: false,
  });

  // Refs to track previous values for change detection
  const prevStats = React.useRef(stats);
  const prevPipeline = React.useRef(pipeline);
  const prevRegisters = React.useRef(registers);
  const prevMemory = React.useRef(memory);
  const prevStdout = React.useRef(stdout);

  // Ref to track if we are resetting the simulator (clearing code)
  const isResetting = React.useRef(false);

  // Detect changes in accordion data when collapsed
  React.useEffect(() => {
    if (!accordionAlerts) {
      prevStats.current = stats;
      prevPipeline.current = pipeline;
      prevRegisters.current = registers;
      prevMemory.current = memory;
      prevStdout.current = stdout;
      return;
    }

    // If we are resetting, update refs but don't trigger changes
    if (isResetting.current) {
      prevStats.current = stats;
      prevPipeline.current = pipeline;
      prevRegisters.current = registers;
      prevMemory.current = memory;
      prevStdout.current = stdout;
      isResetting.current = false;
      return;
    }
    
    const statsChanged = !isEqual(stats, prevStats.current);
    const pipelineChanged = !isEqual(pipeline, prevPipeline.current);
    const registersChanged = !isEqual(registers, prevRegisters.current);
    const memoryChanged = !isEqual(memory, prevMemory.current);
    const stdoutChanged = stdout !== prevStdout.current;
    
    // Update refs first
    prevStats.current = stats;
    prevPipeline.current = pipeline;
    prevRegisters.current = registers;
    prevMemory.current = memory;
    prevStdout.current = stdout;
    
    // Only update state if there are actual changes for collapsed accordions
    if ((!expandedAccordions.stats && statsChanged) ||
        (!expandedAccordions.pipeline && pipelineChanged) ||
        (!expandedAccordions.registers && registersChanged) ||
        (!expandedAccordions.memory && memoryChanged) ||
        (!expandedAccordions.stdout && stdoutChanged)) {
      setAccordionChanges(prev => ({
        ...prev,
        stats: prev.stats || (!expandedAccordions.stats && statsChanged),
        pipeline: prev.pipeline || (!expandedAccordions.pipeline && pipelineChanged),
        registers: prev.registers || (!expandedAccordions.registers && registersChanged),
        memory: prev.memory || (!expandedAccordions.memory && memoryChanged),
        stdout: prev.stdout || (!expandedAccordions.stdout && stdoutChanged),
      }));
    }
  }, [stats, pipeline, registers, memory, stdout, accordionAlerts, expandedAccordions]);

  // Handle accordion expansion change
  const handleAccordionChange = (panel) => (event, isExpanded) => {
    setExpandedAccordions(prev => ({
      ...prev,
      [panel]: isExpanded,
    }));
    
    // Clear change indicator when accordion is opened
    if (isExpanded) {
      setAccordionChanges(prev => ({
        ...prev,
        [panel]: false,
      }));
    }
  };

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
    isResetting.current = true;
    setInputRequest(null);
    if (hadPendingBatch) {
      setExecuting(false);
    }
    worker.reset();
    // Clear accordion change markers
    setAccordionChanges({
      stats: false,
      pipeline: false,
      registers: false,
      memory: false,
      stdout: false,
    });
  }


  const setCacheConfig = (config) => {
    worker.setCacheConfig(config);
  };

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

  // A debounced version of syntaxCheck. Needed to not run props.onChange too often.
  const debouncedSyntaxCheck = debounce((code) => worker.checkSyntax(code), 500);

  // Run a syntax check on the initial code once on mount so that warnings
  // (e.g. deprecated instructions in the sample program) are surfaced
  // immediately, without requiring the user to edit the code first.
  React.useEffect(() => {
    worker.checkSyntax(code);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const onCodeChange = (code) => {
    setCode(code);
    debouncedSyntaxCheck(code);
  };

    // ---------------------------------------------------------------------------
  // Dashboard Grid UI
  // ---------------------------------------------------------------------------
  // Every panel is always visible. CSS Grid layout: editor + side panel +
  // bottom inspector with sticky tabs. Cache + general settings live in a
  // slide-in Preferences drawer reached from the topbar.

  const prefersDarkMode = useMediaQuery('(prefers-color-scheme: dark)');

  const theme = React.useMemo(
    () => createTheme({
      palette: {
        mode: prefersDarkMode ? 'dark' : 'light',
        primary: { main: '#4f46e5' },
      },
    }),
    [prefersDarkMode],
  );

  const [sideTab, setSideTab] = React.useState('pipeline');
  const [bottomTab, setBottomTab] = React.useState('memory');
  const [prefsOpen, setPrefsOpen] = React.useState(false);
  const [helpOpen, setHelpOpen] = React.useState(false);

  const fileInputRef = React.useRef(null);
  const onPickFile = (e) => {
    const f = e.target.files && e.target.files[0];
    if (!f) return;
    const reader = new FileReader();
    reader.onload = (ev) => setCode(ev.target.result);
    reader.readAsText(f);
    e.target.value = '';
  };

  const tabBtn = (current, setCurrent, id, label, alert) => (
    <button
      key={id}
      className={current === id ? 'active' : ''}
      onClick={() => setCurrent(id)}
    >
      {label}{alert ? ' \u2022' : ''}
    </button>
  );

  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <InputDialog request={inputRequest} onSubmit={submitInput} onCancel={cancelInput} />
      <HelpDialog open={helpOpen} handleClose={() => setHelpOpen(false)} ver={worker.version} />

      <input
        ref={fileInputRef}
        type="file"
        accept=".asm,.txt,.s"
        style={{ display: 'none' }}
        onChange={onPickFile}
      />

      <div className="dg-root">
        <div className="dg-topbar">
          <span className="dg-brand">
            <img src={prefersDarkMode ? logoDark : logoBright} alt="EduMIPS64" />
            EduMIPS64
            <span style={{ fontWeight: 400, fontSize: 11, color: 'var(--dg-text-dim)', marginLeft: 4 }}>
              \u00b7 Dashboard
            </span>
          </span>

          <div className="dg-runs">
            <Tooltip title="Load code into the simulator" arrow>
              <span>
                <Button
                  size="small"
                  variant="contained"
                  className="dg-load-btn"
                  startIcon={<UploadIcon />}
                  disabled={!isValidProgram() || status === 'RUNNING'}
                  onClick={clickLoad}
                >
                  Load
                </Button>
              </span>
            </Tooltip>
            <Tooltip title="Single step" arrow>
              <span>
                <Button size="small" startIcon={<PlayArrowIcon />}
                  disabled={!simulatorRunning || executing}
                  onClick={() => clickStep(1)}>Step</Button>
              </span>
            </Tooltip>
            <Tooltip title={`Run ${stepStride} steps`} arrow>
              <span>
                <Button size="small" startIcon={<FastForwardIcon />}
                  disabled={!simulatorRunning || executing}
                  onClick={() => clickStep(stepStride)}>Multi</Button>
              </span>
            </Tooltip>
            <Tooltip title="Run until finished" arrow>
              <span>
                <Button size="small" startIcon={<PlayCircleIcon />}
                  disabled={!simulatorRunning || executing}
                  onClick={clickRun}>Run</Button>
              </span>
            </Tooltip>
            <Tooltip title="Pause" arrow>
              <span>
                <IconButton size="small" disabled={!executing}
                  onClick={() => { appInsights.trackEvent({ name: 'pause' }); setMustPause(true); }}>
                  <PauseCircleIcon fontSize="small" />
                </IconButton>
              </span>
            </Tooltip>
            <Tooltip title="Stop and reset CPU" arrow>
              <span>
                <IconButton size="small" disabled={!simulatorRunning || executing} onClick={clickStop}>
                  <StopCircleIcon fontSize="small" />
                </IconButton>
              </span>
            </Tooltip>
          </div>

          <div className="dg-tail">
            <span className="dg-status-chip"><CpuStatusDisplay status={status} /></span>
            <Tooltip title="Open code from file" arrow>
              <IconButton size="small" disabled={status === 'RUNNING'} onClick={() => fileInputRef.current?.click()}>
                <UploadIcon fontSize="small" />
              </IconButton>
            </Tooltip>
            <Tooltip title="Save code" arrow>
              <IconButton size="small" disabled={status === 'RUNNING'} onClick={saveCode}>
                <DownloadIcon fontSize="small" />
              </IconButton>
            </Tooltip>
            <Tooltip title="Clear editor" arrow>
              <IconButton size="small" disabled={status === 'RUNNING'} onClick={clearCode}>
                <DeleteForeverIcon fontSize="small" />
              </IconButton>
            </Tooltip>
            <Tooltip title="Preferences" arrow>
              <IconButton size="small" onClick={() => setPrefsOpen(true)}>
                <TuneIcon fontSize="small" />
              </IconButton>
            </Tooltip>
            <Tooltip title="Help" arrow>
              <IconButton size="small" onClick={() => setHelpOpen(true)}>
                <HelpIcon fontSize="small" />
              </IconButton>
            </Tooltip>
          </div>
        </div>

        <div className="dg-card dg-editor">
          <div className="dg-card-head">
            <span>Code</span>
            <span className="dg-tabs">
              <span style={{fontSize:11, color:'var(--dg-text-dim)', textTransform:'none', letterSpacing:0, padding:'3px 6px'}}>
                {parsingErrors && parsingErrors.length > 0 ? `${parsingErrors.length} issue(s)` : 'no issues'}
              </span>
            </span>
          </div>
          <div className="dg-card-body">
            <ErrorList parsingErrors={parsingErrors} />
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
            />
          </div>
        </div>

        <div className="dg-card dg-side">
          <div className="dg-card-head">
            <span>CPU State</span>
            <div className="dg-tabs">
              {tabBtn(sideTab, setSideTab, 'pipeline', 'Pipeline', accordionAlerts && accordionChanges.pipeline)}
              {tabBtn(sideTab, setSideTab, 'registers', 'Registers', accordionAlerts && accordionChanges.registers)}
            </div>
          </div>
          <div className="dg-card-body">
            {sideTab === 'pipeline' && <Pipeline pipeline={pipeline} />}
            {sideTab === 'registers' && <Registers {...registers} />}
          </div>
        </div>

        <div className="dg-card dg-bottom">
          <div className="dg-card-head">
            <span>Inspect</span>
            <div className="dg-tabs">
              {tabBtn(bottomTab, setBottomTab, 'memory', 'Memory', accordionAlerts && accordionChanges.memory)}
              {tabBtn(bottomTab, setBottomTab, 'stats', 'Stats', accordionAlerts && accordionChanges.stats)}
              {tabBtn(bottomTab, setBottomTab, 'stdout', 'Output', accordionAlerts && accordionChanges.stdout)}
            </div>
          </div>
          <div className="dg-card-body">
            {bottomTab === 'memory' && <Memory memory={memory} />}
            {bottomTab === 'stats' && <Statistics {...stats} />}
            {bottomTab === 'stdout' && <StdOut stdout={stdout} />}
          </div>
        </div>
      </div>

      <Drawer anchor="right" open={prefsOpen} onClose={() => setPrefsOpen(false)}>
        <div className="dg-drawer-content">
          <h2 style={{ marginTop: 0 }}>Preferences</h2>
          <div className="dg-drawer-section">
            <h4>Cache configuration</h4>
            <CacheConfig showTitle={false} onChange={setCacheConfig} status={status} />
          </div>
          <div className="dg-drawer-section">
            <h4>Simulator settings</h4>
            <Settings
              viMode={viMode}
              setViMode={setViMode}
              fontSize={fontSize}
              setFontSize={setFontSize}
              accordionAlerts={accordionAlerts}
              setAccordionAlerts={setAccordionAlerts}
              forwarding={forwarding}
              setForwarding={setForwarding}
              stepStride={stepStride}
              setStepStride={setStepStride}
              executionDelayMs={executionDelayMs}
              setExecutionDelayMs={setExecutionDelayMs}
              status={status}
              showTitle={false}
            />
          </div>
        </div>
      </Drawer>
    </ThemeProvider>
  );
};

export default Simulator;
