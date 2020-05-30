import React, { useEffect, useState } from "react";

import MonacoEditor from 'react-monaco-editor';

const Code = (props) => {
    const [monaco, setMonaco] = useState(null);
    const [editor, setEditor] = useState(null);

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
        lineNumbersMinChars: 3,

        // Note: the documentation mentions this might have a negative performance
        // impact.
        automaticLayout: true,
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
        props.onChangeValue(newValue);
    }

    return (
        <MonacoEditor
            language="mips"
            value={props.code}
            options={options}
            onChange={onChange}
            theme="vs-light"
            editorDidMount={editorDidMount}
        />
    )
}

export default Code;