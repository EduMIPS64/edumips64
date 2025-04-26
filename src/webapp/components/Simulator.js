import React from 'react';

import Code from './Code';
import Memory from './Memory';
import Pipeline from './Pipeline';
import Registers from './Registers';
import Statistics from './Statistics';
import Header from './Header';
import Accordion from '@mui/material/Accordion';
import AccordionDetails from '@mui/material/AccordionDetails';
import MuiAccordionSummary from '@mui/material/AccordionSummary';
import Grid from '@mui/material/Grid';
import ErrorList from './ErrorList';
import StdOut from './StdOut';
import Switch from '@mui/material/Switch';
import Button from '@mui/material/Button';

import ArrowForwardIosSharpIcon from '@mui/icons-material/ArrowForwardIosSharp';

import { styled } from '@mui/material/styles';

import useMediaQuery from '@mui/material/useMediaQuery';
import { createTheme, ThemeProvider } from '@mui/material/styles';
import CssBaseline from '@mui/material/CssBaseline';

import ExpandMoreIcon from '@mui/icons-material/ExpandMore';
import Typography from '@mui/material/Typography';

import SampleProgram from '../data/SampleProgram';

import { debounce } from 'lodash';
import Settings from './Settings';
import CacheConfig from "./CacheConfig";

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

  const [viMode, setViMode] = React.useState(false);
  const [fontSize, setFontSize] = React.useState(14);

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
      return (parsingErrors.filter((e) => !e.isWarning).length == 0);
    }
  };

  worker.onmessage = (e) => {
    const result = worker.parseResult(e.data);
    console.log('Got message from worker.', result);
    updateState(result);
  };

  const updateState = (result) => {
    console.log('Updating state.');

    setExecuting(false);
    setRegisters(result.registers);
    setMemory(result.memory);
    setStats(result.statistics);
    setStatus(result.status);
    setPipeline(result.pipeline);
    setParsingErrors(result.parsingErrors);

    if (result.parsingErrors) {
      setParsedInstructions(null);
    } else {
      setParsedInstructions(result.parsedInstructions);
    }

    if (result.stdout) {
      setStdout(result.stdout);
    }

    // TODO: cleaner handling of error types. Checking the error message is a pretty weak check.
    if (!result.success && result.errorMessage !== 'Parsing errors.') {
      alert(result.errorMessage);
    }

    if (result.status !== 'RUNNING' || mustPause || result.encounteredBreak) {
      setStepsToRun(0);
      setMustPause(false);
      setRunAll(false);
    } else if (stepsToRun > 0) {
      console.log('Steps left: ' + stepsToRun);
      stepCode(stepsToRun);
    } else if (runAll) {
      stepCode(INTERNAL_STEPS_STRIDE);
    }
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
    setMustPause(true);
    setRunAll(false);
    setStepsToRun(0);
    worker.reset();  // Assuming simulator has a reset method
  };

  const clearCode = () => {
    setCode(".data\n\n.code\n  SYSCALL 0\n");
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

  const onCodeChange = (code) => {
    setCode(code);
    debouncedSyntaxCheck(code);
  };

  const AccordionSummary = styled((props) => (
    <MuiAccordionSummary
      expandIcon={<ArrowForwardIosSharpIcon sx={{ fontSize: '0.8rem' }} />}
      {...props}
    />
  ))(({ theme }) => ({
    backgroundColor:
      theme.palette.mode === 'dark'
        ? 'rgba(255, 255, 255, .05)'
        : 'rgba(227, 245, 254, 1)',
    flexDirection: 'row-reverse',
    '& .MuiAccordionSummary-expandIconWrapper.Mui-expanded': {
      transform: 'rotate(180deg)',
    },
    '& .MuiAccordionSummary-content': {
      marginLeft: theme.spacing(1),
    },
  }));

  const prefersDarkMode = useMediaQuery('(prefers-color-scheme: dark)');

  const theme = React.useMemo(
    () =>
      createTheme({
        palette: {
          mode: prefersDarkMode ? 'dark' : 'light',
        },
      }),
    [prefersDarkMode],
  );

  return (
    <>
      <ThemeProvider theme={theme}>
        <CssBaseline />
        <Header
          onRunClick={clickRun}
          runEnabled={simulatorRunning && !executing}
          onStepClick={clickStep}
          stepEnabled={simulatorRunning && !executing}
          onLoadClick={loadCode}
          loadEnabled={isValidProgram()}
          onPauseClick={() => {
            appInsights.trackEvent({name: "pause"})
            setMustPause(true);
          }}
          pauseEnabled={executing}
          onClearClick={clearCode}
          onOpenClick={openCode}
          onSaveClick={saveCode}
          onStopClick={clickStop}
          stopEnabled={simulatorRunning && !executing}
          parsingErrors={parsingErrors}
          version={worker.version}
          status={status}
          prefersDarkMode={prefersDarkMode}
        />
        <Grid container id="main-grid" disableEqualOverflow spacing={0}>
          <Grid id="left-panel" size={8}>
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
          </Grid>
          <Grid size={4} id="right-panel" disableEqualOverflow>
            <ErrorList
              parsingErrors={parsingErrors}
              AccordionSummary={AccordionSummary}
            />
            <Accordion defaultExpanded disableGutters>
              <AccordionSummary expandIcon={<ExpandMoreIcon />}>
                <Typography variant="h7" sx={{ fontWeight: 'bold', color: '#1976d2' }}>
                  Stats
                </Typography>
              </AccordionSummary>
              <AccordionDetails>
                <Statistics {...stats} />
              </AccordionDetails>
            </Accordion>
            <Accordion disableGutters>
              <AccordionSummary expandIcon={<ExpandMoreIcon />}>
                <Typography variant="h7" sx={{ fontWeight: 'bold', color: '#1976d2' }}>
                  Pipeline
                </Typography>
              </AccordionSummary>
              <AccordionDetails>
                <Pipeline pipeline={pipeline} />
              </AccordionDetails>
            </Accordion>
            <Accordion disableGutters>
              <AccordionSummary expandIcon={<ExpandMoreIcon />}>
                <Typography variant="h7" sx={{ fontWeight: 'bold', color: '#1976d2' }}>
                  Registers
                </Typography>
              </AccordionSummary>
              <AccordionDetails>
                <Registers {...registers} />
              </AccordionDetails>
            </Accordion>
            <Accordion disableGutters>
              <AccordionSummary expandIcon={<ExpandMoreIcon />}>
                <Typography variant="h7" sx={{ fontWeight: 'bold', color: '#1976d2' }}>
                  Memory
                </Typography>
              </AccordionSummary>
              <AccordionDetails>
                <Memory memory={memory} />
              </AccordionDetails>
            </Accordion>
            <Accordion disableGutters>
              <AccordionSummary expandIcon={<ExpandMoreIcon />}>
                <Typography variant="h7" sx={{ fontWeight: 'bold', color: '#1976d2' }}>
                  Standard Output
                </Typography>
              </AccordionSummary>
              <AccordionDetails>
                <StdOut stdout={stdout} />
              </AccordionDetails>
            </Accordion>
            <Accordion defaultExpanded disableGutters>
              <AccordionSummary expandIcon={<ExpandMoreIcon />}>
                <Typography variant="h7" sx={{ fontWeight: 'bold', color: status === 'RUNNING' ? 'gray' : '#1976d2' }}>
                  Cache Configuration
                </Typography>
              </AccordionSummary>
              <AccordionDetails>
                <CacheConfig
                  showTitle={false}
                  onChange={setCacheConfig}
                  status={status}
                />
              </AccordionDetails>
            </Accordion>
            <Accordion defaultExpanded disableGutters>
              <AccordionSummary expandIcon={<ExpandMoreIcon />}>
                <Typography variant="h7" sx={{ fontWeight: 'bold', color: '#1976d2' }}>
                  General Settings
                </Typography>
              </AccordionSummary>
              <AccordionDetails>
                <Settings
                  viMode={viMode}
                  setViMode={setViMode}
                  fontSize={fontSize}
                  setFontSize={setFontSize}
                  showTitle={false}
                />
              </AccordionDetails>
            </Accordion>
          </Grid>
        </Grid>
      </ThemeProvider>
    </>
  );
};

export default Simulator;
