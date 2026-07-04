import React, { useEffect, useRef, useState } from 'react';
import { initVimMode, type VimAdapterInstance } from 'monaco-vim';

import { Editor } from '@monaco-editor/react';
import type { OnMount } from '@monaco-editor/react';
import useMediaQuery from '@mui/material/useMediaQuery';
import * as monacoEditor from 'monaco-editor/esm/vs/editor/editor.api';
import { DEFAULT_PIPELINE_COLORS } from '../settings/schema';
import type { Pipeline, ParsingError, PipelineInstruction } from '../simulator/protocol';
import type { PipelineColors } from '../settings/schema';

// Resolve the Monaco editor theme name from the MUI palette mode passed in
// by the parent (which already accounts for the user's THEME_MODE setting).
// When the parent does not provide a palette mode (e.g. older callers or
// tests), fall back to the OS-level `prefers-color-scheme` preference so
// that behaviour does not regress.
const resolveMonacoTheme = (
  paletteMode: 'light' | 'dark' | undefined,
  prefersDarkMode: boolean,
): string => {
  if (paletteMode === 'dark') return 'vs-dark';
  if (paletteMode === 'light') return 'vs-light';
  return prefersDarkMode ? 'vs-dark' : 'vs-light';
};

const withAlpha = (hexColor: string, alphaHex: string): string =>
  `${hexColor}${alphaHex}`;

const pipelineHighlightStyle = (
  pipelineColors: PipelineColors | undefined,
): Record<string, string> => {
  const colors = { ...DEFAULT_PIPELINE_COLORS, ...(pipelineColors || {}) };
  return {
    '--pipeline-stage-if': withAlpha(colors.IF, '80'),
    '--pipeline-stage-id': withAlpha(colors.ID, '80'),
    '--pipeline-stage-ex': withAlpha(colors.EX, '80'),
    '--pipeline-stage-mem': withAlpha(colors.MEM, '80'),
    '--pipeline-stage-wb': withAlpha(colors.WB, '80'),
    '--pipeline-stage-fp-adder': withAlpha(colors.FPAdder, '80'),
    '--pipeline-stage-fp-multiplier': withAlpha(colors.FPMultiplier, '80'),
    '--pipeline-stage-fp-divider': withAlpha(colors.FPDivider, '80'),
  };
};

// Global MIPS language definition for the Monaco editor.
monacoEditor.languages.register({ id: 'mips' });

interface CodeProps {
  code: string;
  onChangeValue: (value: string) => void;
  parsingErrors: ParsingError[] | null | undefined;
  parsedInstructions: PipelineInstruction[] | null | undefined;
  pipeline: Pipeline;
  running: boolean;
  viMode: boolean;
  fontSize: number;
  validInstructions?: string;
  paletteMode?: 'light' | 'dark';
  pipelineColors?: PipelineColors;
  onEditorReady?: (editor: monacoEditor.editor.IStandaloneCodeEditor) => void;
}

const Code = ({
  code,
  onChangeValue,
  parsingErrors,
  parsedInstructions,
  pipeline,
  running,
  viMode,
  fontSize,
  validInstructions,
  paletteMode,
  pipelineColors,
  onEditorReady,
}: CodeProps) => {
  const [monaco, setMonaco] = useState<typeof monacoEditor | null>(null);
  const [editor, setEditor] = useState<monacoEditor.editor.IStandaloneCodeEditor | null>(null);
  const vimInstanceRef = useRef<VimAdapterInstance | null>(null);
  const hoverDisposableRef = useRef<monacoEditor.IDisposable | null>(null);

  // Decorations (used for CPU stage indication).
  const decorationsRef = useRef<string[]>([]);

  // Maps line of code to CPU stage.
  const [stageMap, setStageMap] = useState<Map<number, string>>(() => new Map());

  // Install our MIPS syntax highlighting provider.
  //
  // Background and pitfalls
  // -----------------------
  //
  // monaco-editor ships its own stock `mips` Monarch grammar via
  // `basic-languages/mips/mips.contribution.js`. That contribution registers
  // a `TokensProviderFactory` whose `create()` is async — Monaco only awaits
  // it the first time a `mips` model needs tokens, and on resolution it
  // calls `TokenizationRegistry.register('mips', stockSupport)`, which
  // overwrites whatever was installed synchronously by
  // `setMonarchTokensProvider`. So a plain `setMonarchTokensProvider` call
  // loses a race to the stock grammar a few hundred ms after the editor
  // mounts.
  //
  // The previous version of this file masked the race by reinstalling our
  // provider in the component render body, which fires many times per
  // second while a program runs (each pipeline state update re-renders the
  // Simulator). That kept our provider on top, but every reinstall
  // invalidates Monaco's tokenization state and triggers an async retoken-
  // ization pass during which lines render with the default `mtk1` token
  // class — i.e. plain black text. That was the run-time "flicker" bug
  // reported in #1723.
  //
  // The robust fix is to *replace* the stock factory rather than fight it.
  // `monaco.languages.registerTokensProviderFactory` disposes any previous
  // factory for the language and installs ours, so the basic-languages
  // grammar can never reach the editor. We additionally call
  // `setMonarchTokensProvider` synchronously so the very first paint uses
  // our tokenizer (before the factory's `create()` is awaited).
  //
  // Note: monacoSetup.ts imports editor.main (not editor.api) to activate all
  // editor features (hover, find, etc.), which also loads Monaco's built-in
  // language contributions — including the stock mips grammar.  The
  // registerTokensProviderFactory + setMonarchTokensProvider pattern below
  // overrides the stock grammar at registration time, not just at runtime.
  //
  // The effect re-runs whenever `validInstructions` changes so newly added
  // instruction mnemonics are picked up by the keyword regex.
  useEffect(() => {
    if (!monaco) return undefined;
    const instructionRegex = new RegExp(`\\b(${validInstructions ?? ''})\\b`);
    // Each rule is a [regex, tokenClass] pair — exactly what Monarch expects
    // for IShortMonarchLanguageRule1. TypeScript needs a cast here because
    // the inferred array element type (string | RegExp)[] is less specific
    // than the required IMonarchLanguageRule tuple type.
    // TODO(ts): Consider using explicit as-const tuple syntax when the
    // tokenizer array grows stable.
    const tokenizer = {
      tokenizer: {
        root: [
          [/^[ \t]*[a-zA-Z_][\w]*:/, 'type.identifier'] as [RegExp, string], // label
          [instructionRegex, 'keyword'] as [RegExp, string],
          [/\.[a-zA-Z_][\w]*/, 'strong'] as [RegExp, string], // directives
          [/[#,]/, 'delimiter'] as [RegExp, string],
          [/\br(?:\d{1,2})\b/, 'string'] as [RegExp, string],
          [/\d+/, 'number'] as [RegExp, string],
          [/".*?"/, 'regexp'] as [RegExp, string],
          [/;.*/, 'comment'] as [RegExp, string],
          [/[a-zA-Z_][\w]*/, 'identifier'] as [RegExp, string],
        ],
      },
    } as monacoEditor.languages.IMonarchLanguage;
    // Replace any previously-registered tokens-provider factory for `mips`.
    // monaco-editor ships its own `mips` Monarch grammar via
    // `basic-languages/mips/mips.contribution.js`, registered as an *async*
    // factory. If we only call `setMonarchTokensProvider` (which performs
    // a synchronous `register`), the stock factory's later async resolution
    // overwrites our provider. `registerTokensProviderFactory` disposes any
    // existing factory and installs ours, so the stock grammar can never
    // reach the editor.
    const factoryDisposable = monaco.languages.registerTokensProviderFactory(
      'mips',
      { create: () => tokenizer },
    );
    // Also install synchronously so the very first paint already uses our
    // tokenizer before the factory's `create()` is awaited.
    monaco.languages.setMonarchTokensProvider('mips', tokenizer);
    return () => {
      if (
        factoryDisposable &&
        typeof factoryDisposable.dispose === 'function'
      ) {
        factoryDisposable.dispose();
      }
    };
  }, [monaco, validInstructions]);

  useEffect(() => {
    if (!monaco || !editor) {
      return;
    }
    if (!running) {
      decorationsRef.current = editor.deltaDecorations(
        decorationsRef.current,
        [],
      );
      setStageMap(new Map());
      return;
    }

    const newStageMap = new Map<number, string>();
    const newDecorations: monacoEditor.editor.IModelDeltaDecoration[] = [];
    const createDecoration = (
      instr: PipelineInstruction,
      className: string,
    ): monacoEditor.editor.IModelDeltaDecoration => ({
      range: new monaco.Range(instr.Line, 1, instr.Line, 1),
      options: { isWholeLine: true, className },
    });
    if (pipeline.IF && pipeline.IF.Line) {
      newDecorations.push(createDecoration(pipeline.IF, 'stageIf'));
      newStageMap.set(pipeline.IF.Line, 'Instruction Fetch (IF)');
    }
    if (pipeline.ID && pipeline.ID.Line) {
      newDecorations.push(createDecoration(pipeline.ID, 'stageId'));
      newStageMap.set(pipeline.ID.Line, 'Instruction Decode (ID)');
    }
    if (pipeline.EX && pipeline.EX.Line) {
      newDecorations.push(createDecoration(pipeline.EX, 'stageEx'));
      newStageMap.set(pipeline.EX.Line, 'Execute (EX)');
    }
    if (pipeline.MEM && pipeline.MEM.Line) {
      newDecorations.push(createDecoration(pipeline.MEM, 'stageMem'));
      newStageMap.set(pipeline.MEM.Line, 'Memory Access (MEM)');
    }
    if (pipeline.WB && pipeline.WB.Line) {
      newDecorations.push(createDecoration(pipeline.WB, 'stageWb'));
      newStageMap.set(pipeline.WB.Line, 'Write Back (WB)');
    }
    if (pipeline.FPDivider && pipeline.FPDivider.Line) {
      newDecorations.push(
        createDecoration(pipeline.FPDivider, 'stageFPDivider'),
      );
      newStageMap.set(pipeline.FPDivider.Line, 'FPU Divider');
    }
    if (pipeline.FPAdder1 && pipeline.FPAdder1.Line) {
      newDecorations.push(
        createDecoration(pipeline.FPAdder1, 'stageFPAdder'),
      );
      newStageMap.set(pipeline.FPAdder1.Line, 'FPU Adder (1)');
    }
    if (pipeline.FPAdder2 && pipeline.FPAdder2.Line) {
      newDecorations.push(
        createDecoration(pipeline.FPAdder2, 'stageFPAdder'),
      );
      newStageMap.set(pipeline.FPAdder2.Line, 'FPU Adder (2)');
    }
    if (pipeline.FPAdder3 && pipeline.FPAdder3.Line) {
      newDecorations.push(
        createDecoration(pipeline.FPAdder3, 'stageFPAdder'),
      );
      newStageMap.set(pipeline.FPAdder3.Line, 'FPU Adder (3)');
    }
    if (pipeline.FPAdder4 && pipeline.FPAdder4.Line) {
      newDecorations.push(
        createDecoration(pipeline.FPAdder4, 'stageFPAdder'),
      );
      newStageMap.set(pipeline.FPAdder4.Line, 'FPU Adder (4)');
    }
    if (pipeline.FPMultiplier1 && pipeline.FPMultiplier1.Line) {
      newDecorations.push(
        createDecoration(pipeline.FPMultiplier1, 'stageFPMultiplier'),
      );
      newStageMap.set(pipeline.FPMultiplier1.Line, 'FPU Muliplier (1)');
    }
    if (pipeline.FPMultiplier2 && pipeline.FPMultiplier2.Line) {
      newDecorations.push(
        createDecoration(pipeline.FPMultiplier2, 'stageFPMultiplier'),
      );
      newStageMap.set(pipeline.FPMultiplier2.Line, 'FPU Muliplier (2)');
    }
    if (pipeline.FPMultiplier3 && pipeline.FPMultiplier3.Line) {
      newDecorations.push(
        createDecoration(pipeline.FPMultiplier3, 'stageFPMultiplier'),
      );
      newStageMap.set(pipeline.FPMultiplier3.Line, 'FPU Muliplier (3)');
    }
    if (pipeline.FPMultiplier4 && pipeline.FPMultiplier4.Line) {
      newDecorations.push(
        createDecoration(pipeline.FPMultiplier4, 'stageFPMultiplier'),
      );
      newStageMap.set(pipeline.FPMultiplier4.Line, 'FPU Muliplier (4)');
    }
    if (pipeline.FPMultiplier5 && pipeline.FPMultiplier5.Line) {
      newDecorations.push(
        createDecoration(pipeline.FPMultiplier5, 'stageFPMultiplier'),
      );
      newStageMap.set(pipeline.FPMultiplier5.Line, 'FPU Muliplier (5)');
    }
    if (pipeline.FPMultiplier6 && pipeline.FPMultiplier6.Line) {
      newDecorations.push(
        createDecoration(pipeline.FPMultiplier6, 'stageFPMultiplier'),
      );
      newStageMap.set(pipeline.FPMultiplier6.Line, 'FPU Muliplier (6)');
    }
    if (pipeline.FPMultiplier7 && pipeline.FPMultiplier7.Line) {
      newDecorations.push(
        createDecoration(pipeline.FPMultiplier7, 'stageFPMultiplier'),
      );
      newStageMap.set(pipeline.FPMultiplier7.Line, 'FPU Muliplier (7)');
    }
    setStageMap(newStageMap);
    decorationsRef.current = editor.deltaDecorations(
      decorationsRef.current,
      newDecorations,
    );
  }, [pipeline, running, monaco, editor]);

  // Hook to update the map of source line to instruction.
  useEffect(() => {
    if (!monaco) {
      return;
    }

    const map = new Map<number, PipelineInstruction>();
    if (parsedInstructions) {
      parsedInstructions
        .filter((instruction): instruction is PipelineInstruction => Boolean(instruction))
        .forEach((instruction) => map.set(instruction.Line, instruction));
    }

    if (hoverDisposableRef.current) {
      hoverDisposableRef.current.dispose();
    }

    const disposable = monaco.languages.registerHoverProvider('mips', {
      provideHover: (
        model: monacoEditor.editor.ITextModel,
        position: monacoEditor.Position,
      ) => {
        if (map.size === 0) {
          return;
        }

        const line = position.lineNumber;
        if (!map.has(line)) {
          return;
        }

        const instruction = map.get(line)!;
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
    hoverDisposableRef.current = disposable;
    return () => {
      if (hoverDisposableRef.current === disposable) {
        hoverDisposableRef.current.dispose();
        hoverDisposableRef.current = null;
      }
    };
  }, [parsedInstructions, stageMap, monaco]);

  // @monaco-editor/react disposes the editor's model on unmount when
  // keepCurrentModel is false (the default).  This replaces the manual
  // editorWillUnmount + model.dispose() pattern from react-monaco-editor.
  //
  // Under React.StrictMode's dev-only double-mount sequence the lifecycle
  // is: mount → cleanup (unmount + model disposal) → remount.  With
  // keepCurrentModel=false the second mount starts with a fresh model,
  // so monaco.editor.getModels() never accumulates stale orphans.

  const onMount: OnMount = (
    editorInstance,
    monacoInstance,
  ) => {
    // Expose monaco and editor to window for testing purposes.
    // These properties are declared on Window in vendor.d.ts.
    window.monaco = monacoInstance;
    window.editor = editorInstance;

    setMonaco(monacoInstance);
    setEditor(editorInstance);

    // Allow parents to receive the editor instance (e.g. so the Issues
    // panel can jump the cursor to a specific line/column on click).
    if (typeof onEditorReady === 'function') {
      onEditorReady(editorInstance);
    }

    // Enable Vi mode if viMode prop is true
    if (viMode) {
      vimInstanceRef.current = initVimMode(editorInstance);
    }

    // Ensure the required command is registered
    editorInstance.addAction({
      id: 'editor.action.insertLineAfter',
      label: 'Insert Line After',
      keybindings: [monacoInstance.KeyMod.CtrlCmd | monacoInstance.KeyCode.Enter],
      run: (ed) => {
        ed.trigger('keyboard', 'type', { text: '\n' });
      },
    });
  };

  // Hook to dynamically toggle Vi mode when viMode prop changes
  useEffect(() => {
    if (!editor) return;
    if (viMode && !vimInstanceRef.current) {
      vimInstanceRef.current = initVimMode(editor);
    } else if (!viMode && vimInstanceRef.current) {
      vimInstanceRef.current.dispose();
      vimInstanceRef.current = null;
    }
    return () => {
      if (vimInstanceRef.current) {
        vimInstanceRef.current.dispose();
        vimInstanceRef.current = null;
      }
    };
  }, [editor, viMode]);

  const options: monacoEditor.editor.IStandaloneEditorConstructionOptions = {
    selectOnLineNumbers: true,
    roundedSelection: false,
    readOnly: running,
    cursorStyle: 'line' as const,
    codeLens: false,
    minimap: { enabled: false },
    lineNumbersMinChars: 3,
    automaticLayout: true,
    fontSize,
  };

  // Hook to compute and set markers for warnings and errors.
  useEffect(() => {
    if (!monaco || !editor) {
      return;
    }

    const model = editor.getModel();
    if (!model) return;
    monaco.editor.setModelMarkers(model, 'EduMIPS64', []);

    if (!parsingErrors) {
      return;
    }

    const lines = editor.getValue().split('\n');
    const markers = parsingErrors
      .filter((err) => {
        // Skip errors for lines that no longer exist in the editor
        // (can happen due to race conditions during rapid editing)
        return err.row >= 1 && err.row <= lines.length;
      })
      .map((err): monacoEditor.editor.IMarkerData => {
        const line = lines[err.row - 1];
        // First non-space character (or column 1 if line is empty/whitespace)
        const startColumn = Math.max(line.search(/\S/) + 1, 1);
        return {
          startLineNumber: err.row,
          endLineNumber: err.row,
          startColumn: startColumn,
          endColumn: line.length + 1,
          message: `${err.description}`,
          severity: err.isWarning
            ? monacoEditor.MarkerSeverity.Warning
            : monacoEditor.MarkerSeverity.Error,
          source: 'EduMIPS64',
        };
      });

    monaco.editor.setModelMarkers(model, 'EduMIPS64', markers);
  }, [parsingErrors, editor, monaco]);

  // Resolve the editor theme: prefer the user-selected palette mode passed
  // in from the parent (driven by the THEME_MODE setting), and fall back to
  // the OS preference when the parent leaves the choice on `auto`.
  const prefersDarkMode = useMediaQuery('(prefers-color-scheme: dark)');
  const editorTheme = resolveMonacoTheme(paletteMode, prefersDarkMode);
  const highlightStyle: React.CSSProperties & Record<string, string> = {
    height: '100%',
    ...pipelineHighlightStyle(pipelineColors),
  };

  return (
    <div style={highlightStyle} data-testid="code-editor">
      <Editor
        language="mips"
        value={code}
        options={options}
        onChange={(value) => {
          // @monaco-editor/react's onChange passes `string | undefined`; guard
          // against undefined (fires when the model is being disposed).
          if (value !== undefined) {
            onChangeValue(value);
          }
        }}
        theme={editorTheme}
        onMount={onMount}
        // keepCurrentModel=false (the default) disposes the model on unmount.
        // This prevents orphan models from accumulating under StrictMode's
        // dev double-mount (see #1918).
        keepCurrentModel={false}
        height="100%"
      />
    </div>
  );
};

export default Code;
