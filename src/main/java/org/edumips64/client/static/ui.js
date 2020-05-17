'use strict';

// The amount of steps to run in multi-step executions.
const INTERNAL_STEPS_STRIDE = 50;

// Number of steps to run with the multi-step button.
const STEP_STRIDE = 500;

const Registers = ({gpr, fpu, special}) => {
    return (
        <div>
            <table id="registers">
                <tbody>
                {// Ugly way of using a single table to show both GPR and FPU registers.
                gpr.map((register, i) => (
                    <tr key={register.name}>
                        <td className="registerName">{register.name}</td>
                        <td className="registerValue">{register.value}</td>
                        <td className="registerName">{fpu[i].name}</td>
                        <td className="registerValue">{fpu[i].value}</td>
                    </tr>
                    ))
                }
                {special.filter(r => r.name != "FCSR").map(register => (
                    <tr key={register.name}>
                        <td className="registerName">{register.name}</td>
                        <td className="registerValue">{register.value}</td>
                        <td /><td />
                    </tr>
                    ))
                }
                </tbody>
            </table>
        </div>
    ); 
}

// A toy component that appends a final "s" to the label if
// the given value is != 1. Of course this is not proper
// pluralization, just me playing around with React.
const PluralLabel = ({label, value}) => {
    const pluralize = value => value != 1 ? "s" : "";
    return <div>{value} {label}{pluralize(value)}</div>
}

const Statistics = ({cycles, instructions, rawStalls, wawStalls, memoryStalls, codeSizeBytes, fcsr}) => {
    // TODO: FCSR.
    return (
        <div id="statistics">
            <div>
                <b>Execution</b>
                <PluralLabel value={cycles} label="Cycle" />
                <PluralLabel value={instructions} label="Instruction" />
                <div>{instructions == 0 ? 0 : (cycles / instructions).toFixed(2)} Cycles per Instructions (CPI)</div>
            </div><br/>
            <div>
                <b>Stalls</b>
                <PluralLabel value={rawStalls} label="RAW Stall" />
                <PluralLabel value={wawStalls} label="WAW Stall" />
                <PluralLabel value={memoryStalls} label="Memory Stall" />
            </div><br />
            <div>
                <b>Code size</b>
                <div>{codeSizeBytes} Bytes</div>
            </div>
        </div>
    );
}
const Memory = (props) => {
    return (
        <div id="memory">
            <textarea readOnly value={props.memory} />
        </div>
    );
}

const Code = (props) => {
    return (
        <div id="code">
            <textarea 
                value={props.code}
                onChange={(event) => {props.onChangeValue(event.target.value);}}
                />
            <div id="controls">
                <input id="load-button" type="button" value="Load/Reset" onClick={() => {props.onLoadClick()}} disabled={!props.loadEnabled} />
                <input id="step-button" type="button" value="Single Step" onClick={() => {props.onStepClick(1)}} disabled={!props.stepEnabled} />
                <input id="multi-step-button" type="button" value="Multi Step" onClick={() => {props.onStepClick(STEP_STRIDE)}} disabled={!props.stepEnabled} />
                <input id="run-button" type="button" value="Run All" onClick={() => {props.onRunClick()}} disabled={!props.runEnabled} />
                <input id="stop-button" type="button" value="Stop" onClick={() => {props.onStopClick()}} disabled={!props.stopEnabled} />
            </div>
        </div>
    );
}

const Pipeline = ({pipeline}) => {
    return (
        <div id="pipeline">
            <table>
                <tbody>
                    <tr><td>IF</td><td>{pipeline.IF?.Code}</td></tr>
                    <tr><td>ID</td><td>{pipeline.ID?.Code}</td></tr>
                    <tr><td>EX</td><td>{pipeline.EX?.Code}</td></tr>
                    <tr><td>MEM</td><td>{pipeline.MEM?.Code}</td></tr>
                    <tr><td>WB</td><td>{pipeline.WB?.Code}</td></tr>
                </tbody>
            </table>
        </div>
    )
}

const parseResult = (result) => {
    result.registers = JSON.parse(result.registers);
    result.statistics = JSON.parse(result.statistics);
    return result;
}

const Simulator = ({sim, initialState}) => {
    const [registers, setRegisters] = React.useState(initialState.registers);
    const [memory, setMemory] = React.useState(initialState.memory);
    const [stats, setStats] = React.useState(initialState.statistics);
    const [code, setCode] = React.useState(sampleProgram);
    const [status, setStatus] = React.useState(initialState.status);
    const [pipeline, setPipeline] = React.useState(initialState.pipeline);

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
        console.log("Got message from worker");
        const result = parseResult(e.data);
        console.log(result);
        updateState(result);
    }

    const updateState = (result) => {
        console.log("Updating state.");
        setExecuting(false);
        console.log(result);
        setRegisters(result.registers);
        setMemory(result.memory);
        setStats(result.statistics);
        setStatus(result.status);
        setPipeline(result.pipeline);

        if (!result.success) {
            alert(result.errorMessage);
        } 

        if (result.status !== "RUNNING" || mustStop) {
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
            />
            <Registers {...registers}/>
            <Memory memory={memory}/>
            <Statistics {...stats}/>
            <Pipeline pipeline={pipeline}/>
        </div>
    );
}

// Worker that runs the EduMIPS64 core.
// Contains some syntactical sugar methods to make working with the
// Web Worker API a bit easier.
let simulator = new Worker("worker.js");
simulator.reset = () => {
    simulator.postMessage({"method": "reset"});
}
simulator.step = (n) => {
    simulator.postMessage({"method": "step", "steps": n});
}
simulator.load = (code) => {
    simulator.postMessage({"method": "load", "code": code});
}

simulator.reset();
var initializer = (evt) => {
    console.log("Running the initializer callback");

    // Run this callback only once, to initialize the Simulator
    // React component which will then handle all subsequent messages.
    simulator.removeEventListener("message", initializer);
    var initState = parseResult(evt.data);

    ReactDOM.render(
        <Simulator sim={simulator} initialState={initState} />,
        document.getElementById('simulator')
    )
}
simulator.addEventListener("message", initializer);

// Sample MIPS64 program to display.
const sampleProgram =`; An EduMIPS64 test program kindly donated by Gerardo Puga in Issue #132.
; https://github.com/lupino3/edumips64/issues/132
; Adapted by Andrea Spadaccini by removing all compilation warnings.
;
; Will run for 9765 CPU cycles.

; ARQUITECTURA DE COMPUTADORES II, 2014
; TP 02: Segmented Architectures
;
; Calculates the maximum value reached by the Hailstone sequences of the firsts 30 numbers.
; https://en.wikipedia.org/wiki/Collatz_conjecture

; Start of the data segment
; ---

	.data

	; Results table
result: .word32 0

	; -----------------

	; Start of the program segment
	; ---

	.text
	
	; Initialize the main loop index
	daddi R5,R0,1

	; Load the start address of the results table on R4
	daddi R4,R0,result

numloop: 
        ; Initialize R2 with the first number of the sequence
	dadd R2,R0,R5

	; ...this first number is currently also the current maximum value. This maximum is stored in R3.
	dadd R3,R0,R2

	; -----------------

	; Start of the loop that calculates the Hailstone sequence
hailloop: 

	; Is the current number even or odd?
	andi R1,R2,1
	bne  R1,r0,odd   ; if odd, then go to "odd"

	; -----------------
	
	; Even numbers
even:
	; Divide by two
	dsrl R2,R2,1

	; Skip the code for odd numbers
	j anynumber

	; -----------------

	; Odd numbers
odd:
	; Multiply by three and add one
	dsll R1,R2,1
	dadd R2,R1,R2
	daddi R2,R2,1

	; -----------------
anynumber:
	
	; If the new number is higher than the maximum, this is the new maximum
	dsub R1,R3,R2      ; Calculate the difference between the current sequence number and the max, put it in R1
	dsrl R1,R1,31      ; Remove all bits, but the one that contains the sign of the result
	beq R1,r0,skipnewmax ; If the sign bit is zero, the current sequence number is not higher than the current maximum

	dadd R3,R0,R2	   ; Replace the current maximum with the current sequence number
skipnewmax:

	; If the current sequence number is 1, then we reached the end of the sequence
	daddi R1,R2,-1
	bne R1,r0,hailloop

	; Store the final maximum on the results table
	sw R3,0(R4)

	; Increment the table index
	daddi R4,R4,4 

	; Increment the current main loop index
	daddi R5,R5,1

	; check if we have already covered the first 30 natural numbers
	daddi R1,R5,-30
    bne R1,r0,numloop
    
    syscall 0
`;