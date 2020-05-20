import React from "react";

import Code from "./Code";
import Memory from "./Memory";
import Pipeline from "./Pipeline";
import Registers from "./Registers";
import Statistics from "./Statistics";

import SampleProgram from "../data/SampleProgram";

const Simulator = ({sim, initialState}) => {
    // The amount of steps to run in multi-step executions.
    const INTERNAL_STEPS_STRIDE = 50;

    const [registers, setRegisters] = React.useState(initialState.registers);
    const [memory, setMemory] = React.useState(initialState.memory);
    const [stats, setStats] = React.useState(initialState.statistics);
    const [code, setCode] = React.useState(SampleProgram);
    const [status, setStatus] = React.useState(initialState.status);
    const [pipeline, setPipeline] = React.useState(initialState.pipeline);
    const [parsingErrors, setParsingErrors] = React.useState(initialState.parsingErrors);

    // Number of steps left to run. Used to keep track of execution.
    // If set to -1, runs until the execution ends.
    const [stepsToRun, setStepsToRun] = React.useState(0);

    // Signals that the simulation must stop.
    const [mustStop, setMustStop] = React.useState(false);

    // Tracks whether the worker is currently running code.
    const [executing, setExecuting] = React.useState(false);

    // Tracks whether the simulation is running in "run all" mode (run until finished).
    const [runAll, setRunAll] = React.useState(false);

    const simulatorRunning = status == "RUNNING";

    sim.onmessage = (e) => {
        const result = sim.parseResult(e.data);
        console.log("Got message from worker.", result);
        updateState(result);
    }

    const updateState = (result) => {
        console.log("Updating state.");

        setExecuting(false);
        setRegisters(result.registers);
        setMemory(result.memory);
        setStats(result.statistics);
        setStatus(result.status);
        setPipeline(result.pipeline);
        setParsingErrors(result.parsingErrors);

        if (!result.success) {
            alert(result.errorMessage);
        } 

        if (result.status !== "RUNNING" || mustStop || result.encounteredBreak) {
            setStepsToRun(0);
            setMustStop(false);
            setRunAll(false);
        } else if (stepsToRun > 0) {
            console.log("Steps left: " + stepsToRun)
            stepCode(stepsToRun);
        } else if (runAll) {
            stepCode(INTERNAL_STEPS_STRIDE);
        }
    }

    const loadCode = () => {
        console.log("Executing loadCode");
        sim.load(code);
    }

    const stepCode = (n) => {
        console.log("Executing steps: " + n);
        const toRun = Math.min(n, INTERNAL_STEPS_STRIDE);
        setStepsToRun(n - toRun);
        setExecuting(true);
        sim.step(toRun);
    }
    
    const runCode = () => {
        console.log("Executing runCode");
        setRunAll(true);
        stepCode(INTERNAL_STEPS_STRIDE);
    }

    return (
        <div id="widgetGrid">
            <Code 
                onRunClick={runCode} runEnabled={simulatorRunning && !executing}
                onStepClick={stepCode} stepEnabled={simulatorRunning && !executing}
                onLoadClick={loadCode} loadEnabled={true}
                onStopClick={() => {setMustStop(true)}} stopEnabled={executing}
                onChangeValue={(text) => setCode(text)} 
                code={code}
                parsingErrors={parsingErrors}
            />
            <Registers {...registers}/>
            <Memory memory={memory}/>
            <Statistics {...stats}/>
            <Pipeline pipeline={pipeline}/>
        </div>
    );
}

export default Simulator;