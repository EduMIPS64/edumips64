import React, { useEffect, useState } from 'react';
import { initVimMode } from 'monaco-vim';

// new
import MonacoEditor from 'react-monaco-editor';
import useMediaQuery from '@mui/material/useMediaQuery';
import * as monacoEditor from 'monaco-editor';

// Lightweight editor used as a mobile fallback: a <textarea> with a
// syntax-highlighted overlay rendered by Prism. Works natively with
// mobile soft keyboards and touch selection.
import SimpleCodeEditor from 'react-simple-code-editor';
import Prism from 'prismjs/components/prism-core';

// Global MIPS language definition for the Monaco editor.
monacoEditor.languages.register({ id: 'mips' });

const Code = (props) => {

  const [monaco, setMonaco] = useState(null);
  const [editor, setEditor] = useState(null);
  const [vimInstance, setVimInstance] = useState(null); // new state for Vim instance
  // IDisposable to clean up the hover provider.
  const [hoverDisposable, setHoverCleanup] = useState(null);

  // Decorations (used for CPU stage indication).
  const [decorations, setDecorations] = useState([]);

  // Maps line of code to CPU stage.
  const [stageMap, setStageMap] = useState(new Map());

  // Dynamically build the syntax highlighting regex for instructions.
  var instructionRegex = new RegExp(`\\b(${props.validInstructions})\\b`)
  monacoEditor.languages.setMonarchTokensProvider('mips', {
    tokenizer: {
      root: [
        [/^[ \t]*[a-zA-Z_][\w]*:/, 'type.identifier'], // label
        [instructionRegex, 'keyword'],
        [/\.[a-zA-Z_][\w]*/, 'strong'], // directives
        [/[#,]/, 'delimiter'],
        [/\br(?:\d{1,2})\b/, 'string'],
        [/\d+/, 'number'],
        [/".*?"/, 'regexp'],
        [/;.*/, 'comment'],
        [/[a-zA-Z_][\w]*/, 'identifier'],
      ]
    }
  });

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
  }, [props.parsedInstructions, stageMap, monaco]); // eslint-disable-line @eslint-react/exhaustive-deps

  const saveCodeToFile = () => {
    const blob = new Blob([props.code], { type: 'text/plain' });
    const a = document.createElement('a');
    a.href = URL.createObjectURL(blob);
    a.download = 'code.s';
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
  };

  const loadCodeFromFile = (event) => {
    const file = event.target.files[0];
    if (!file) return;

    const reader = new FileReader();
    reader.onload = (e) => {
      props.onChangeValue(e.target.result);
    };
    reader.readAsText(file);
  };

  const editorDidMount = (editor, monaco) => {
    // Expose monaco and editor to window for testing purposes
    window.monaco = monaco;
    window.editor = editor;
    
    setMonaco(monaco);
    setEditor(editor);

    // Enable Vi mode if viMode prop is true
    if (props.viMode) {
      const vim = initVimMode(editor);
      setVimInstance(vim);
    }

    // Ensure the required command is registered
    editor.addAction({
      id: "editor.action.insertLineAfter",
      label: "Insert Line After",
      keybindings: [monaco.KeyMod.CtrlCmd | monaco.KeyCode.Enter],
      run: function (ed) {
        ed.trigger("keyboard", "type", { text: "\n" });
      },
    });
  };

  // Hook to dynamically toggle Vi mode when viMode prop changes
  useEffect(() => {
    if (!editor) return;
    if (props.viMode) {
      const vim = initVimMode(editor);
      setVimInstance(vim);
    } else if (vimInstance) {
      vimInstance.dispose();
      setVimInstance(null);
    }
  }, [props.viMode]);

  const options = {
    selectOnLineNumbers: true,
    roundedSelection: false,
    readOnly: props.running,
    cursorStyle: 'line',
    codelens: false,
    minimap: { enabled: false },
    tabsize: 4,
    lineNumbersMinChars: 3,
    automaticLayout: true,
    fontSize: props.fontSize,  // Set font size from props
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
    const markers = props.parsingErrors
      .filter((err) => {
        // Skip errors for lines that no longer exist in the editor
        // (can happen due to race conditions during rapid editing)
        return err.row >= 1 && err.row <= lines.length;
      })
      .map((err) => {
        const line = lines[err.row - 1];
        // First non-space character (or column 1 if line is empty/whitespace)
        const startColumn = Math.max(line.search(/\S/) + 1, 1);
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

  // Detect devices where the primary pointer is coarse (touch screens).
  // Monaco Editor does not work reliably with soft keyboards and touch
  // selection on such devices, so we fall back to a plain <textarea>
  // which provides a native, reliable editing experience. Pipeline
  // stages and parsing errors are still shown in the surrounding panels.
  const useCoarsePointerFallback = useMediaQuery('(pointer: coarse)');

  if (useCoarsePointerFallback) {
    return (
      <MobileCodeEditor
        code={props.code}
        onChange={props.onChangeValue}
        running={props.running}
        fontSize={props.fontSize}
        prefersDarkMode={prefersDarkMode}
        validInstructions={props.validInstructions}
      />
    );
  }

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

// Build a Prism grammar for MIPS that mirrors the Monarch tokenizer used
// by Monaco. Token names map to the `token.<name>` CSS classes we style
// further down.
const buildMipsGrammar = (validInstructions) => ({
  comment: /;.*/,
  string: /"(?:\\.|[^"\\])*"/,
  label: {
    pattern: /^[ \t]*[a-zA-Z_]\w*:/m,
    alias: 'function',
  },
  directive: {
    pattern: /\.[a-zA-Z_]\w*/,
    alias: 'attr-name',
  },
  keyword: new RegExp(`\\b(?:${validInstructions})\\b`, 'i'),
  register: {
    pattern: /\br\d{1,2}\b/i,
    alias: 'variable',
  },
  number: /\b\d+\b/,
  punctuation: /[#,()]/,
});

// MobileCodeEditor is a lightweight code editor used on touch-first
// devices, where Monaco Editor is known to have serious usability issues
// (see https://github.com/microsoft/monaco-editor/issues/246). It is
// built on a native <textarea> with a Prism-highlighted overlay, so the
// device's native soft keyboard, touch selection and accessibility all
// Just Work while users still get syntax highlighting. Pipeline state,
// parsing errors and instruction info are shown in the surrounding
// panels (Pipeline, ErrorList, Registers, Memory).
const MobileCodeEditor = ({
  code,
  onChange,
  running,
  fontSize,
  prefersDarkMode,
  validInstructions,
}) => {
  const containerRef = React.useRef(null);

  const grammar = React.useMemo(
    () => buildMipsGrammar(validInstructions || ''),
    [validInstructions],
  );

  const highlight = React.useCallback(
    (source) => Prism.highlight(source, grammar, 'mips'),
    [grammar],
  );

  // Expose a minimal Monaco-compatible API on window.editor so existing
  // test helpers and integrations continue to work.
  React.useEffect(() => {
    const getTextarea = () =>
      containerRef.current && containerRef.current.querySelector('textarea');
    const api = {
      getValue: () => {
        const t = getTextarea();
        return t ? t.value : code;
      },
      setValue: (v) => {
        onChange && onChange(v);
      },
      focus: () => {
        const t = getTextarea();
        if (t) t.focus();
      },
    };
    window.editor = api;
    return () => {
      // Only clear the global if it still points at our API, to avoid
      // clobbering another editor instance that may have mounted.
      if (window.editor === api) {
        delete window.editor;
      }
    };
  }, [onChange, code]);

  const bg = prefersDarkMode ? '#1e1e1e' : '#ffffff';
  const fg = prefersDarkMode ? '#d4d4d4' : '#000000';

  // Prism token colors. Two palettes so light and dark themes both work.
  const tokenColors = prefersDarkMode
    ? {
        comment: '#6a9955',
        string: '#ce9178',
        keyword: '#569cd6',
        directive: '#c586c0',
        label: '#dcdcaa',
        register: '#9cdcfe',
        number: '#b5cea8',
        punctuation: '#d4d4d4',
      }
    : {
        comment: '#008000',
        string: '#a31515',
        keyword: '#0000ff',
        directive: '#af00db',
        label: '#795e26',
        register: '#267f99',
        number: '#098658',
        punctuation: '#000000',
      };

  const containerStyle = {
    width: '100%',
    height: '100%',
    boxSizing: 'border-box',
    overflow: 'auto',
    backgroundColor: bg,
    color: fg,
    fontFamily: "Menlo, Monaco, 'Courier New', monospace",
    fontSize: `${fontSize || 14}px`,
    lineHeight: 1.5,
    // Scoped Prism-like token colors — defined here rather than in the
    // global stylesheet to keep the change self-contained.
    ['--tok-comment']: tokenColors.comment,
    ['--tok-string']: tokenColors.string,
    ['--tok-keyword']: tokenColors.keyword,
    ['--tok-directive']: tokenColors.directive,
    ['--tok-label']: tokenColors.label,
    ['--tok-register']: tokenColors.register,
    ['--tok-number']: tokenColors.number,
    ['--tok-punctuation']: tokenColors.punctuation,
  };

  const editorStyle = {
    fontFamily: "Menlo, Monaco, 'Courier New', monospace",
    fontSize: `${fontSize || 14}px`,
    minHeight: '100%',
    outline: 'none',
  };

  const textareaClassName = 'mobile-code-editor-textarea';

  return (
    <div
      ref={containerRef}
      className="mobile-code-editor"
      style={containerStyle}
    >
      <style>{`
        .mobile-code-editor .token.comment     { color: var(--tok-comment); font-style: italic; }
        .mobile-code-editor .token.string      { color: var(--tok-string); }
        .mobile-code-editor .token.keyword     { color: var(--tok-keyword); font-weight: bold; }
        .mobile-code-editor .token.directive   { color: var(--tok-directive); font-weight: bold; }
        .mobile-code-editor .token.label       { color: var(--tok-label); font-weight: bold; }
        .mobile-code-editor .token.register    { color: var(--tok-register); }
        .mobile-code-editor .token.number      { color: var(--tok-number); }
        .mobile-code-editor .token.punctuation { color: var(--tok-punctuation); }
        .mobile-code-editor .${textareaClassName} {
          outline: none !important;
        }
      `}</style>
      <SimpleCodeEditor
        value={code}
        onValueChange={(v) => onChange && onChange(v)}
        highlight={highlight}
        padding={8}
        tabSize={4}
        insertSpaces={true}
        disabled={running}
        textareaClassName={textareaClassName}
        textareaId="mobile-code-editor-textarea"
        aria-label="MIPS64 assembly code editor"
        spellCheck={false}
        autoCapitalize="off"
        autoCorrect="off"
        autoComplete="off"
        style={editorStyle}
      />
    </div>
  );
};

export default Code;
