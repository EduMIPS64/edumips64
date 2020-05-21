import React, { useEffect, useState } from "react";

import MonacoEditor from 'react-monaco-editor';
import { debounce } from 'lodash';

// Number of steps to run with the multi-step button.
const STEP_STRIDE = 500;

const MonacoCode = (props) => {
    const [monaco, setMonaco] = useState(null);
    const [editor, setEditor] = useState(null);

    // A debounced version of onChange. Needed to not run props.onChange too often.
    const debouncedOnChange = debounce(code => props.onChangeValue(code), 500);

    const editorDidMount = (editor, monaco) => {
        setMonaco(monaco);
        setEditor(editor);
    }

    const options = {
        selectOnLineNumbers: true,
        roundedSelection: false,
        readOnly: false,
        cursorStyle: "line",
        automaticLayout: false,
        codelens: false,
        minimap: {enabled: false},
        tabsize: 4,
    };

    const computeMarkers = parsingErrors => {
        // TODO: handle multiple errors in the same line.
        const lines = editor.getValue().split("\n");
        return parsingErrors.map(err => {
            const line = lines[err.row-1];
            // First non-space character.
            const startColumn = line.search(/\S/)+1;
            return {
                startLineNumber: err.row,
                endLineNumber: err.row,
                startColumn: startColumn,
                endColumn: line.length+1,
                message: `${err.description}`,
                severity: err.isWarning ? 4 : 8,
                source: 'EduMIPS64',
            };
        });
    }

    useEffect(() => {
        if (!monaco || !editor) {
            return;
        }

        const model = editor.getModel();
        monaco.editor.setModelMarkers(model, "EduMIPS64", []);

        if(!props.parsingErrors) {
            return;
        }

        console.log("Parsing errors", props.parsingErrors);
        const markers = computeMarkers(props.parsingErrors);
        console.log("Markers", markers);

        monaco.editor.setModelMarkers(model, "EduMIPS64", markers);
    }, [props.parsingErrors]);

    const onChange = (newValue, event) => {
        debouncedOnChange(newValue);
    }

    return (
        <MonacoEditor
            height="400"
            language="mips"
            value={props.code}
            options={options}
            onChange={onChange}
            theme="vs-light"
            editorDidMount={editorDidMount}
        />
    )
}

const Code = (props) => {
    return (
        <div id="code">
            <MonacoCode 
                code={props.code}
                onChangeValue={props.onChangeValue}
                parsingErrors={props.parsingErrors}
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

export default Code;