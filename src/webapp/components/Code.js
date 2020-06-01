import React, { useEffect, useState } from "react";

import MonacoEditor from 'react-monaco-editor';

const Code = (props) => {
    const [monaco, setMonaco] = useState(null);
    const [editor, setEditor] = useState(null);

    // IDisposable to clean up the hover provider.
    const [hoverDisposable, setHoverCleanup] = useState(null);

    // Hook to update the map of source line to instruction.
    useEffect(() => {
        if (!monaco) {
            return;
        }

        let map = new Map();
        if (props.parsedInstructions) {
            props.parsedInstructions
                .filter(instruction => instruction)
                .map(instruction => map.set(instruction.Line, instruction));
        }

        if (hoverDisposable) {
            hoverDisposable.dispose();
        }

        let disposable = monaco.languages.registerHoverProvider("mips", {provideHover:
            (model, position) => {
                if (map.size === 0) {
                    return;
                }

                const line = position.lineNumber;
                if (!map.has(line)) {
                    return;
                }

                const instruction = map.get(line);
                return {
                    range: new monaco.Range(line, 0, line, model.getLineMaxColumn(line)),
                    contents: [
                        { value: `*Address*: \`${instruction.Address}\``},
                        { value: `*OpCode*: \`${instruction.OpCode}\``},
                        { value: `*Binary*: \`${instruction.BinaryRepresentation}\``},
                    ]
                }
            },
        });
        setHoverCleanup(disposable);
    }, [props.parsedInstructions, monaco, hoverDisposable]);


    const editorDidMount = (editor, monaco) => {
        setMonaco(monaco);
        setEditor(editor);
    }

    const options = {
        selectOnLineNumbers: true,
        roundedSelection: false,
        readOnly: false,
        cursorStyle: "line",
        codelens: false,
        minimap: {enabled: false},
        tabsize: 4,
        lineNumbersMinChars: 3,

        // Note: the documentation mentions this might have a negative performance
        // impact.
        automaticLayout: true,
    };

    // Hook to compute and set markers for warnings and errors.
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
        const lines = editor.getValue().split("\n");
        const markers = props.parsingErrors.map(err => {
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
        console.log("Markers", markers);

        monaco.editor.setModelMarkers(model, "EduMIPS64", markers);
    }, [props.parsingErrors, editor, monaco]);

    return (
        <MonacoEditor
            language="mips"
            value={props.code}
            options={options}
            onChange={props.onChangeValue}
            theme="vs-light"
            editorDidMount={editorDidMount}
        />
    )
}

export default Code;