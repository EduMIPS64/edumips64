'use strict';

const Registers = (props) => {
    return (
        <div className="pure-u-1 pure-u-lg-1-4">
            <table>
                {props.registers.map(register => (
                    <tr key={register.name}>
                        <td className="registerName">{register.name}</td>
                        <td className="registerValue">{register.value}</td>
                    </tr>
                ))
                }

            </table>
        </div>
    ); 
}

const Statistics = (props) => {
    return (
        <div className="pure-u-1 pure-u-lg-1-4">
            <textarea readOnly value={props.stats} />
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
            <input id="run-button" type="button" value="Run" onClick={() => {props.onClick()}} />
        </div>
    );
}

const Simulator = (props) => {
    const regs = [...Array(32).keys()];
    const defaultRegisters = regs.map(r => {
        return {name: `R${r}`, value: "0000000000000000"};
    })

    const [registers, setRegisters] = React.useState(defaultRegisters);
    const [memory, setMemory] = React.useState("Memory will appear here.");
    const [stats, setStats] = React.useState("Statistics will appear here");
    const [code, setCode] = React.useState(`; Example program. Loads the value 10 (A) into R1.
.data
    .word64 10

.code
    lw r1, 0(r0)
    SYSCALL 0
`);

    const runCode = () => {
        console.log("Executing runCode");
        const simulator = new jsedumips64.WebUi();
        simulator.init();
        const result = simulator.runProgram(code);
        if (result.length != 0) {
            alert(result);
        } else {
            setRegisters(JSON.parse(simulator.getRegisters()));
            setMemory(simulator.getMemory());
            setStats(simulator.getStatistics());
        }
    }

    return (
        <React.Fragment>
            <Code onClick={runCode} onChangeValue ={(text) => setCode(text)} code={code}/>
            <Registers registers={registers}/>
            <Memory memory={memory}/>
            <Statistics stats={stats}/>
        </React.Fragment>
    );
}

ReactDOM.render(
    <Simulator />,
    document.getElementById('simulator')
);