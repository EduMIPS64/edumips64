
import React, { useEffect, useState } from 'react';
import { initVimMode } from 'monaco-vim';

// new
import MonacoEditor from 'react-monaco-editor';
import useMediaQuery from '@mui/material/useMediaQuery';


const Code = (props) => {

  const [monaco, setMonaco] = useState(null);
  const [editor, setEditor] = useState(null);
  // IDisposable to clean up the hover provider.
  const [hoverDisposable, setHoverCleanup] = useState(null);

  // Decorations (used for CPU stage indication).
  const [decorations, setDecorations] = useState([]);

  // Maps line of code to CPU stage.
  const [stageMap, setStageMap] = useState(new Map());

  useEffect(() => {
    if (!monaco) {
      return;
    }
    if (!props.running && decorations) {
      const newDecorations = editor.deltaDecorations(decorations, []);
      setDecorations(decorations);
      return;
    }

    const newStageMap = new Map();
    const newDecorations = [];
    const createDecoration = (instr, className) => {
      return {
        range: new monaco.Range(instr.Line, 1, instr.Line, 1),
        options: { isWholeLine: true, className },
      };
    };
    if (props.pipeline.IF && props.pipeline.IF.Line) {
      newDecorations.push(createDecoration(props.pipeline.IF, 'stageIf'));
      newStageMap.set(props.pipeline.IF.Line, 'Instruction Fetch (IF)');
    }
    if (props.pipeline.ID && props.pipeline.ID.Line) {
      newDecorations.push(createDecoration(props.pipeline.ID, 'stageId'));
      newStageMap.set(props.pipeline.ID.Line, 'Instruction Decode (ID)');
    }
    if (props.pipeline.EX && props.pipeline.EX.Line) {
      newDecorations.push(createDecoration(props.pipeline.EX, 'stageEx'));
      newStageMap.set(props.pipeline.EX.Line, 'Execute (EX)');
    }
    if (props.pipeline.MEM && props.pipeline.MEM.Line) {
      newDecorations.push(createDecoration(props.pipeline.MEM, 'stageMem'));
      newStageMap.set(props.pipeline.MEM.Line, 'Memory Access (MEM)');
    }
    if (props.pipeline.WB && props.pipeline.WB.Line) {
      newDecorations.push(createDecoration(props.pipeline.WB, 'stageWb'));
      newStageMap.set(props.pipeline.WB.Line, 'Write Back (WB)');
    }
    if (props.pipeline.FPDivider && props.pipeline.FPDivider.Line) {
      newDecorations.push(
        createDecoration(props.pipeline.FPDivider, 'stageFPDivider'),
      );
      newStageMap.set(props.pipeline.FPDivider.Line, 'FPU Divider');
    }
    if (props.pipeline.FPAdder1 && props.pipeline.FPAdder1.Line) {
      newDecorations.push(
        createDecoration(props.pipeline.FPAdder1, 'stageFPAdder'),
      );
      newStageMap.set(props.pipeline.FPAdder1.Line, 'FPU Adder (1)');
    }
    if (props.pipeline.FPAdder2 && props.pipeline.FPAdder2.Line) {
      newDecorations.push(
        createDecoration(props.pipeline.FPAdder2, 'stageFPAdder'),
      );
      newStageMap.set(props.pipeline.FPAdder2.Line, 'FPU Adder (2)');
    }
    if (props.pipeline.FPAdder3 && props.pipeline.FPAdder3.Line) {
      newDecorations.push(
        createDecoration(props.pipeline.FPAdder3, 'stageFPAdder'),
      );
      newStageMap.set(props.pipeline.FPAdder3.Line, 'FPU Adder (3)');
    }
    if (props.pipeline.FPAdder4 && props.pipeline.FPAdder4.Line) {
      newDecorations.push(
        createDecoration(props.pipeline.FPAdder4, 'stageFPAdder'),
      );
      newStageMap.set(props.pipeline.FPAdder4.Line, 'FPU Adder (4)');
    }
    if (props.pipeline.FPMultiplier1 && props.pipeline.FPMultiplier1.Line) {
      newDecorations.push(
        createDecoration(props.pipeline.FPMultiplier1, 'stageFPMultiplier'),
      );
      newStageMap.set(props.pipeline.FPMultiplier1.Line, 'FPU Muliplier (1)');
    }
    if (props.pipeline.FPMultiplier2 && props.pipeline.FPMultiplier2.Line) {
      newDecorations.push(
        createDecoration(props.pipeline.FPMultiplier2, 'stageFPMultiplier'),
      );
      newStageMap.set(props.pipeline.FPMultiplier2.Line, 'FPU Muliplier (2)');
    }
    if (props.pipeline.FPMultiplier3 && props.pipeline.FPMultiplier3.Line) {
      newDecorations.push(
        createDecoration(props.pipeline.FPMultiplier3, 'stageFPMultiplier'),
      );
      newStageMap.set(props.pipeline.FPMultiplier3.Line, 'FPU Muliplier (3)');
    }
    if (props.pipeline.FPMultiplier4 && props.pipeline.FPMultiplier4.Line) {
      newDecorations.push(
        createDecoration(props.pipeline.FPMultiplier4, 'stageFPMultiplier'),
      );
      newStageMap.set(props.pipeline.FPMultiplier4.Line, 'FPU Muliplier (4)');
    }
    if (props.pipeline.FPMultiplier5 && props.pipeline.FPMultiplier5.Line) {
      newDecorations.push(
        createDecoration(props.pipeline.FPMultiplier5, 'stageFPMultiplier'),
      );
      newStageMap.set(props.pipeline.FPMultiplier5.Line, 'FPU Muliplier (5)');
    }
    if (props.pipeline.FPMultiplier6 && props.pipeline.FPMultiplier6.Line) {
      newDecorations.push(
        createDecoration(props.pipeline.FPMultiplier6, 'stageFPMultiplier'),
      );
      newStageMap.set(props.pipeline.FPMultiplier6.Line, 'FPU Muliplier (6)');
    }
    if (props.pipeline.FPMultiplier7 && props.pipeline.FPMultiplier7.Line) {
      newDecorations.push(
        createDecoration(props.pipeline.FPMultiplier7, 'stageFPMultiplier'),
      );
      newStageMap.set(props.pipeline.FPMultiplier7.Line, 'FPU Muliplier (7)');
    }
    setStageMap(newStageMap);
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
        const contents = [
          { value: `*Address*: \`${instruction.Address}\`` },
          { value: `*OpCode*: \`${instruction.OpCode}\`` },
          { value: `*Binary*: \`${instruction.BinaryRepresentation}\`` },
          {
            value: `*Hex*: \`${parseInt(instruction.BinaryRepresentation, 2)
              .toString(16)
              .toUpperCase()
              .padStart(8, '0')}\``,
          },
        ];
        if (stageMap.has(line)) {
          contents.push({ value: `*CPU Stage* \`${stageMap.get(line)}\`` });
        }
        return {
          range: new monaco.Range(line, 0, line, model.getLineMaxColumn(line)),
          contents,
        };
      },
    });
    setHoverCleanup(disposable);
  }, [props.parsedInstructions, stageMap, monaco]); // eslint-disable-line react-hooks/exhaustive-deps

  /*
  const editorDidMount = (editor, monaco) => {
    setMonaco(monaco);
    setEditor(editor);
  };

   */


  const editorDidMount = (editor, monaco) => {
    setMonaco(monaco);
    setEditor(editor);

    // Initialize Vim Mode
    const vimMode = initVimMode(editor);
    console.log("Vim Mode enabled:", vimMode);
  };



  const options = {
    selectOnLineNumbers: true,
    roundedSelection: false,
    readOnly: props.running,
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

  // Set the dark theme if necessary
  const prefersDarkMode = useMediaQuery('(prefers-color-scheme: dark)');

  return (
    <MonacoEditor
      language="mips"
      value={props.code}
      options={options}
      onChange={props.onChangeValue}
      theme={prefersDarkMode ? 'vs-dark' : 'vs-light'}
      editorDidMount={editorDidMount}
    />
  );
};

export default Code;
