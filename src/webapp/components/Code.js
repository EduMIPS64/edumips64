import React, { useEffect, useState } from 'react';

import MonacoEditor from 'react-monaco-editor';

const Code = (props) => {
  const [monaco, setMonaco] = useState(null);
  const [editor, setEditor] = useState(null);

  // IDisposable to clean up the hover provider.
  const [hoverDisposable, setHoverCleanup] = useState(null);

  // Decorations (used for CPU stage indication).
  const [decorations, setDecorations] = useState([]);

  useEffect(() => {
    if (!monaco) {
      return;
    }
    if (!props.running && decorations) {
      const newDecorations = editor.deltaDecorations(decorations, []);
      setDecorations(decorations);
      return;
    }

    const newDecorations = [];
    const createDecoration = (instr, className) => {
      return {
        range: new monaco.Range(instr.Line, 1, instr.Line, 1),
        options: { isWholeLine: true, className },
      };
    };
    if (props.pipeline.IF) {
      newDecorations.push(createDecoration(props.pipeline.IF, 'stageIf'));
    }
    if (props.pipeline.ID) {
      newDecorations.push(createDecoration(props.pipeline.ID, 'stageId'));
    }
    if (props.pipeline.EX) {
      newDecorations.push(createDecoration(props.pipeline.EX, 'stageEx'));
    }
    if (props.pipeline.MEM) {
      newDecorations.push(createDecoration(props.pipeline.MEM, 'stageMem'));
    }
    if (props.pipeline.WB) {
      newDecorations.push(createDecoration(props.pipeline.WB, 'stageWb'));
    }
    if (props.pipeline.FPDivider) {
      newDecorations.push(
        createDecoration(props.pipeline.FPDivider, 'stageFPDivider'),
      );
    }
    if (props.pipeline.FPAdder1) {
      newDecorations.push(
        createDecoration(props.pipeline.FPAdder1, 'stageFPAdder'),
      );
    }
    if (props.pipeline.FPAdder2) {
      newDecorations.push(
        createDecoration(props.pipeline.FPAdder2, 'stageFPAdder'),
      );
    }
    if (props.pipeline.FPAdder3) {
      newDecorations.push(
        createDecoration(props.pipeline.FPAdder3, 'stageFPAdder'),
      );
    }
    if (props.pipeline.FPAdder4) {
      newDecorations.push(
        createDecoration(props.pipeline.FPAdder4, 'stageFPAdder'),
      );
    }
    if (props.pipeline.FPMultiplier1) {
      newDecorations.push(
        createDecoration(props.pipeline.FPMultiplier1, 'stageFPMultiplier'),
      );
    }
    if (props.pipeline.FPMultiplier2) {
      newDecorations.push(
        createDecoration(props.pipeline.FPMultiplier2, 'stageFPMultiplier'),
      );
    }
    if (props.pipeline.FPMultiplier3) {
      newDecorations.push(
        createDecoration(props.pipeline.FPMultiplier3, 'stageFPMultiplier'),
      );
    }
    if (props.pipeline.FPMultiplier4) {
      newDecorations.push(
        createDecoration(props.pipeline.FPMultiplier4, 'stageFPMultiplier'),
      );
    }
    if (props.pipeline.FPMultiplier5) {
      newDecorations.push(
        createDecoration(props.pipeline.FPMultiplier5, 'stageFPMultiplier'),
      );
    }
    if (props.pipeline.FPMultiplier6) {
      newDecorations.push(
        createDecoration(props.pipeline.FPMultiplier6, 'stageFPMultiplier'),
      );
    }
    if (props.pipeline.FPMultiplier7) {
      newDecorations.push(
        createDecoration(props.pipeline.FPMultiplier7, 'stageFPMultiplier'),
      );
    }
    console.log('decorations');
    console.log(newDecorations);
    const appliedDecorations = editor.deltaDecorations(
      decorations,
      newDecorations,
    );
    setDecorations(appliedDecorations);
  }, [props.pipeline, props.running, monaco]);

  // Hook to update the map of source line to instruction.
  useEffect(() => {
    if (!monaco) {
      return;
    }

    const map = new Map();
    if (props.parsedInstructions) {
      props.parsedInstructions
        .filter((instruction) => instruction)
        .map((instruction) => map.set(instruction.Line, instruction));
    }

    if (hoverDisposable) {
      hoverDisposable.dispose();
    }

    const disposable = monaco.languages.registerHoverProvider('mips', {
      provideHover: (model, position) => {
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
            { value: `*Address*: \`${instruction.Address}\`` },
            { value: `*OpCode*: \`${instruction.OpCode}\`` },
            { value: `*Binary*: \`${instruction.BinaryRepresentation}\`` },
          ],
        };
      },
    });
    setHoverCleanup(disposable);
  }, [props.parsedInstructions, monaco]); // eslint-disable-line react-hooks/exhaustive-deps

  const editorDidMount = (editor, monaco) => {
    setMonaco(monaco);
    setEditor(editor);
  };

  const options = {
    selectOnLineNumbers: true,
    roundedSelection: false,
    readOnly: false,
    cursorStyle: 'line',
    codelens: false,
    minimap: { enabled: false },
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
    monaco.editor.setModelMarkers(model, 'EduMIPS64', []);

    if (!props.parsingErrors) {
      return;
    }

    console.log('Parsing errors', props.parsingErrors);
    const lines = editor.getValue().split('\n');
    const markers = props.parsingErrors.map((err) => {
      const line = lines[err.row - 1];
      // First non-space character.
      const startColumn = line.search(/\S/) + 1;
      return {
        startLineNumber: err.row,
        endLineNumber: err.row,
        startColumn: startColumn,
        endColumn: line.length + 1,
        message: `${err.description}`,
        severity: err.isWarning ? 4 : 8,
        source: 'EduMIPS64',
      };
    });
    console.log('Markers', markers);

    monaco.editor.setModelMarkers(model, 'EduMIPS64', markers);
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
  );
};

export default Code;
