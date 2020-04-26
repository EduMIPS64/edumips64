'use strict';

const Registers = ({gpr, fpu, special}) => {
    return (
        <div className="pure-u-1 pure-u-lg-1-4">
            <table className="registers">
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
    return <p>{value} {label}{pluralize(value)}</p>
}

const Statistics = ({cycles, instructions, rawStalls, wawStalls, memoryStalls, codeSizeBytes, fcsr}) => {
    // TODO: FCSR.
    return (
        <div className="pure-u-1 pure-u-lg-1-4">
            <div className="widget">
                <p>
                <b>Execution</b>
                <PluralLabel value={cycles} label="Cycle" />
                <PluralLabel value={instructions} label="Instruction" />
                <p>{instructions == 0 ? 0 : (cycles / instructions).toFixed(2)} Cycles per Instructions (CPI)</p>
                </p>
                <p>
                <b>Stalls</b>
                <PluralLabel value={rawStalls} label="RAW Stall" />
                <PluralLabel value={wawStalls} label="WAW Stall" />
                <PluralLabel value={memoryStalls} label="Memory Stall" />
                <b>Code size</b>
                <p>{codeSizeBytes} Bytes</p>
                </p>
            </div>
        </div>
    );
}
const Memory = (props) => {
    return (
        <div className="pure-u-1 pure-u-lg-1-4">
            <textarea readOnly value={props.memory} />
        </div>
    );
}

const Code = (props) => {
    return (
        <div className="pure-u-1 pure-u-lg-1-4">
            <textarea 
                value={props.code}
                onChange={(event) => {props.onChangeValue(event.target.value);}}
                />
            <br />
            <input id="run-button" type="button" value="Run" onClick={() => {props.onClick()}} enabled={props.enabled} />
        </div>
    );
}

const Simulator = (props) => {
    // TODO: should the default state be fetched directly from the CPU?
    // the current model creates the CPU only when something is executed,
    // but in reality we should keep the CPU instance as state, and we could
    // fetch default values from there.
    const emptyReg = "0000000000000000";

    // Generates the default, empty registers as returned by the CPU getRegisters() call.
    const generateDefaultRegisters = () => {
        const regs = [...Array(32).keys()];

        const defaultRegisters = regs.map(r => {
            return {name: `R${r}`, value: emptyReg}
        });
        const fpuRegisters = regs.map(r => {
            return {name: `F${r}`, value: emptyReg}
        })
        const specialRegisters = ["LO", "HI", "FCSR"].map(r => {return {name: r, value: emptyReg}});
        return {"gpr": defaultRegisters, "fpu": fpuRegisters, "special": specialRegisters}
    }

    const defaultStats = {
        "cycles": 0,
        "instructions": 0,
        "rawStalls": 0,
        "wawStalls": 0,
        "dividerStalls": 0,
        "memoryStalls": 0,
        "codeSizeBytes": 0,
        "fcsr": emptyReg
    }

    const [simulator, setSimulator] = React.useState(props.simulator);
    const [registers, setRegisters] = React.useState(generateDefaultRegisters());
    const [memory, setMemory] = React.useState("Memory will appear here.");
    const [stats, setStats] = React.useState(defaultStats);
    const [code, setCode] = React.useState(`; Example program. Loads the value 10 (A) into R1.
.data
    .word64 10

.code
    lw r1, 0(r0)
    SYSCALL 0
`);
    const updateState = () => {
        setRegisters(JSON.parse(simulator.getRegisters()));
        setMemory(simulator.getMemory());
        setStats(JSON.parse(simulator.getStatistics()));
    }
    
    const runCode = () => {
        console.log("Executing runCode - " + simulator);
        const result = JSON.parse(simulator.runProgram(code));
        updateState();
        console.log(result);

        if (!result.success) {
            alert(result.errorMessage);
        } 
    }

    return (
        <React.Fragment>
            <Code onClick={runCode} onChangeValue ={(text) => setCode(text)} code={code} />
            <Registers {...registers}/>
            <Memory memory={memory}/>
            <Statistics {...stats}/>
        </React.Fragment>
    );
}

// Ugly hack to wait for GWT initialization to finish.
// If initialization is not done, jsedumips64 will be undefined.
// I tried a couple of different ways of doing this with sleeps,
// this is a compromise between simplicity and user experience.
//
// The better way to handle this would be to somehow get a call
// when GWT is initialized, which could be done by overriding
// the WebUi.onModuleLoad() function.
console.log("Waiting 500ms for GWT to initialize");
setTimeout(() => {
    let sim = new jsedumips64.WebUi();
    sim.init();

    ReactDOM.render(
        <Simulator simulator={sim} />,
        document.getElementById('simulator')
    )
}, 500);