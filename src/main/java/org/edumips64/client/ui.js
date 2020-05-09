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
                <tr><td>IF</td><td>{pipeline.IF?.Code}</td></tr>
                <tr><td>ID</td><td>{pipeline.ID?.Code}</td></tr>
                <tr><td>EX</td><td>{pipeline.EX?.Code}</td></tr>
                <tr><td>MEM</td><td>{pipeline.MEM?.Code}</td></tr>
                <tr><td>WB</td><td>{pipeline.WB?.Code}</td></tr>
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

const Simulator = (props) => {
    const [simulator, setSimulator] = React.useState(props.simulator);
    const [registers, setRegisters] = React.useState(JSON.parse(props.simulator.getRegisters()));
    const [memory, setMemory] = React.useState(props.simulator.getMemory());
    const [stats, setStats] = React.useState(JSON.parse(props.simulator.getStatistics()));
    const [code, setCode] = React.useState(sampleProgram);
    const [status, setStatus] = React.useState(jsedumips64.Status.READY);
    const [pipeline, setPipeline] = React.useState(props.simulator.getPipeline());

    const simulatorRunning = status == jsedumips64.Status.RUNNING;

    const updateState = (result) => {
        setRegisters(JSON.parse(simulator.getRegisters()));
        setMemory(simulator.getMemory());
        setStats(JSON.parse(simulator.getStatistics()));
        setStatus(result.status);
        setPipeline(props.simulator.getPipeline());
        console.log(pipeline);
    }

    const loadCode = () => {
        console.log("Executing loadCode");
        simulator.reset();
        const result = simulator.loadProgram(code);
        updateState(result);
        console.log(result);

        if (!result.success) {
            alert(result.errorMessage);
        } 

        // Upon loading, run one step.
        stepCode();
    }

    const stepCode = () => {
        console.log("Executing step");
        const result = simulator.step();
        updateState(result);
        console.log(result);

        if (!result.success) {
            alert(result.errorMessage);
        } 

    }
    
    const runCode = () => {
        console.log("Executing runCode - " + simulator);
        const result = simulator.runAll();
        updateState(result);
        console.log(result);

        if (!result.success) {
            alert(result.errorMessage);
        } 
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

// This method is called by WebUI.onModuleLoad, a callback invoked by GWT when the module is loaded.
// It's necessary to be called by GWT because before the module is loaded the GWT objects cannot be used.
const onGwtReady = () => {
    let sim = new jsedumips64.WebUi();
    sim.init();

    ReactDOM.render(
        <Simulator simulator={sim} />,
        document.getElementById('simulator')
    )

}