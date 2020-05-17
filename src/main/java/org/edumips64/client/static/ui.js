'use strict';

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
                <input id="step-button" type="button" value="Single Step" onClick={() => {props.onStepClick()}} disabled={!props.stepEnabled} />
                <input id="run-button" type="button" value="Run All" onClick={() => {props.onRunClick()}} disabled={!props.runEnabled} />
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

const sampleProgram =`; Example program. Loads the value 10 (A) into R1.
.data
    .word64 10

.code
    lw r1, 0(r0)
    SYSCALL 0
`;

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

    const simulatorRunning = status == "RUNNING";

    sim.onmessage = (e) => {
        console.log("Got message from worker");
        const result = parseResult(e.data);
        console.log(result);
        updateState(result);
    }

    const updateState = (result) => {
        console.log("Updating state.");
        console.log(result);
        setRegisters(result.registers);
        setMemory(result.memory);
        setStats(result.statistics);
        setStatus(result.status);
        setPipeline(result.pipeline);

        if (!result.success) {
            alert(result.errorMessage);
        } 
    }

    const loadCode = () => {
        console.log("Executing loadCode");
        sim.load(code);
    }

    const stepCode = () => {
        console.log("Executing step");
        sim.step();
    }
    
    const runCode = () => {
        console.log("Executing runCode");
        sim.runAll();
    }

    return (
        <div id="widgetGrid">
            <Code 
                onRunClick={runCode} runEnabled={simulatorRunning}
                onStepClick={stepCode} stepEnabled={simulatorRunning}
                onLoadClick={loadCode} loadEnabled={true}
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
simulator.step = () => {
    simulator.postMessage({"method": "step"});
}
simulator.load = (code) => {
    simulator.postMessage({"method": "load", "code": code});
}
simulator.runAll = () => {
    simulator.postMessage({"method": "runAll"});
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