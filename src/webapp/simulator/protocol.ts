/**
 * protocol.ts — TypeScript types for the EduMIPS64 Web Worker boundary.
 *
 * These types are derived from the Java @JsType classes in
 * src/main/java/org/edumips64/client/ and the worker helper methods
 * wired up in src/webapp/index.js.  Each type is annotated with its
 * Java source of truth so that future changes to the Java side can be
 * audited against this file.
 *
 * The wire format is produced by GWT's JsInterop layer.  Primitive
 * fields (boolean, int, String) map directly to JS boolean/number/string.
 * JsArray<T> fields arrive as plain JS arrays after parseResult() calls
 * JSON.parse() on the string-serialised register/memory/statistics fields.
 */

// ---------------------------------------------------------------------------
// Instruction (Java source: Instruction.java)
// ---------------------------------------------------------------------------

/**
 * A single MIPS instruction as it appears in the pipeline or the parsed
 * instruction list.
 *
 * Java source of truth: org.edumips64.client.Instruction
 */
export interface PipelineInstruction {
  /** Instruction mnemonic, e.g. "DADD". Bubble placeholder is " ". */
  Name: string;
  /** Full assembly representation including operands, e.g. "DADD R1,R2,R3". */
  Code: string;
  /** Assembly-source comment for this instruction. */
  Comment: string;
  /** Globally unique serial number assigned when the instruction was issued. */
  SerialNumber: number;
  /** Memory address (byte offset from 0) where the instruction is stored. */
  Address: number;
  /** Source-file line number (1-based). */
  Line: number;
  /** 32-bit binary representation as a 32-character string of '0'/'1'. */
  BinaryRepresentation: string;
  /** First 6 bits of BinaryRepresentation — the opcode field. */
  OpCode: string;
  /**
   * CycleState tag for the most recently completed CPU cycle, or null when the
   * CycleBuilder has not seen this instruction yet.  String values correspond
   * to CycleState.name(): normal stages ("IF","ID","EX","MEM","WB",
   * "A1".."A4","M1".."M7","DIV") or stall tags ("RAW","WAW","StDiv",
   * "StEx","StFun","Str","StAdd","StMul").  When Stage === "DIV_COUNT",
   * DivCount carries the per-cycle divider counter.
   *
   * Java source: Instruction.Stage / ResultFactory.wrap()
   */
  Stage: string | null;
  /**
   * FP-divider per-cycle counter when Stage === "DIV_COUNT", otherwise -1.
   * Java source: Instruction.DivCount
   */
  DivCount: number;
}

// ---------------------------------------------------------------------------
// Pipeline (Java source: Pipeline.java)
// ---------------------------------------------------------------------------

/**
 * Snapshot of all pipeline slots for one CPU cycle.
 *
 * Integer stages (IF/ID/EX/MEM/WB) are always present (though the slot may
 * hold a null/bubble when the stage is empty).  FPU slots are null when
 * empty.
 *
 * Java source of truth: org.edumips64.client.Pipeline
 */
export interface Pipeline {
  // Integer pipeline
  IF: PipelineInstruction | null;
  ID: PipelineInstruction | null;
  EX: PipelineInstruction | null;
  MEM: PipelineInstruction | null;
  WB: PipelineInstruction | null;
  // FP Adder stages (4 stages)
  FPAdder1: PipelineInstruction | null;
  FPAdder2: PipelineInstruction | null;
  FPAdder3: PipelineInstruction | null;
  FPAdder4: PipelineInstruction | null;
  // FP Multiplier stages (7 stages)
  FPMultiplier1: PipelineInstruction | null;
  FPMultiplier2: PipelineInstruction | null;
  FPMultiplier3: PipelineInstruction | null;
  FPMultiplier4: PipelineInstruction | null;
  FPMultiplier5: PipelineInstruction | null;
  FPMultiplier6: PipelineInstruction | null;
  FPMultiplier7: PipelineInstruction | null;
  // FP Divider (single stage)
  FPDivider: PipelineInstruction | null;
}

// ---------------------------------------------------------------------------
// ParsingError (Java source: ParserError.java)
// ---------------------------------------------------------------------------

/**
 * A single parser diagnostic (error or warning) from the assembler.
 *
 * Java source of truth: org.edumips64.client.ParserError
 */
export interface ParsingError {
  /** 1-based source-file row where the error was detected. */
  row: number;
  /** 1-based column offset. */
  column: number;
  /** true = warning only; false = hard error that blocks execution. */
  isWarning: boolean;
  /** Human-readable description of the problem. */
  description: string;
}

// ---------------------------------------------------------------------------
// Register types (serialised by ResultFactory.getRegisters())
// ---------------------------------------------------------------------------

/**
 * General-purpose or special register as returned by ResultFactory.getRegisters().
 *
 * Java source of truth: ResultFactory.getRegisters() in ResultFactory.java.
 * Fields are built from org.edumips64.core.Register / RegisterFP.
 */
export interface Register {
  /** Register canonical name, e.g. "R0", "F0", "LO". */
  name: string;
  /** ABI alias for GPRs (e.g. "zero", "at"); absent for FPU/special registers. */
  alias?: string;
  /** 64-bit value as a zero-padded hex string, e.g. "0000000000000000". */
  hexString: string;
  /** Numeric value as a decimal string (signed for GPR/FPU). */
  value: string;
}

/**
 * The parsed registers object (after worker.parseResult() calls JSON.parse).
 * Java source: ResultFactory.getRegisters()
 */
export interface Registers {
  /** 32 general-purpose integer registers (R0..R31). */
  gpr: Register[];
  /** 32 floating-point registers (F0..F31). */
  fpu: Register[];
  /** Special registers: LO, HI, FCSR. */
  special: Register[];
}

// ---------------------------------------------------------------------------
// Memory (serialised by ResultFactory.getMemory())
// ---------------------------------------------------------------------------

/**
 * A single data memory cell.
 * Java source: ResultFactory.getMemory() in ResultFactory.java.
 */
export interface MemoryCell {
  /** Address as a hex string, e.g. "0x0000000000000000". */
  address_hex: string;
  /** Address as a decimal number. */
  address: number;
  /** Cell value as a decimal string. */
  value: string;
  /** Cell value as a hex string. */
  value_hex: string;
  /** Assembly label at this address (empty string if none). */
  label: string;
  /** Assembly code at this address (empty string if data-only). */
  code: string;
  /** Assembly comment (empty string if none). */
  comment: string;
}

/**
 * The parsed memory object (after worker.parseResult() calls JSON.parse).
 * Java source: ResultFactory.getMemory()
 */
export interface Memory {
  cells: MemoryCell[];
}

// ---------------------------------------------------------------------------
// Statistics (serialised by ResultFactory.getStatistics())
// ---------------------------------------------------------------------------

/**
 * CPU execution counters and cache statistics.
 * Java source: ResultFactory.getStatistics() in ResultFactory.java.
 */
export interface Statistics {
  // Execution
  cycles: number;
  instructions: number;
  // Stalls
  rawStalls: number;
  wawStalls: number;
  dividerStalls: number;
  memoryStalls: number;
  exStalls: number;
  funcUnitStalls: number;
  // Cache
  L1I_reads: number;
  L1I_misses: number;
  L1D_reads: number;
  L1D_reads_misses: number;
  L1D_writes: number;
  L1D_writes_misses: number;
  // Code size
  codeSizeBytes: number;
  // FPU Control Status Register value as a binary string
  fcsr: string;
}

// ---------------------------------------------------------------------------
// CpuStatus
// ---------------------------------------------------------------------------

/**
 * The three observable CPU states.
 *
 * Java source: ResultFactory.FromCpuStatus() in ResultFactory.java.
 *   READY   → CPU is initialised but no program is loaded.
 *   RUNNING → A program is loaded and steps can be executed.
 *   STOPPED → The program has halted (SYSCALL 0 or end-of-instructions).
 */
export type CpuStatus = 'READY' | 'RUNNING' | 'STOPPED';

// ---------------------------------------------------------------------------
// SimulatorResult — the parsed worker message
// ---------------------------------------------------------------------------

/**
 * The fully-parsed result of a worker message, after worker.parseResult()
 * has JSON-parsed the register/memory/statistics string fields.
 *
 * Java source of truth: org.edumips64.client.Result
 * (with registers/memory/statistics expanded from JSON strings to objects
 * by worker.parseResult() in index.js).
 */
export interface SimulatorResult {
  /** Whether the operation succeeded without errors. */
  success: boolean;
  /** Human-readable error description; empty string on success. */
  errorMessage: string;
  /** CPU state after the operation. */
  status: CpuStatus;
  /** Parsed register snapshot (after JSON.parse in parseResult). */
  registers: Registers;
  /** Parsed memory snapshot (after JSON.parse in parseResult). */
  memory: Memory;
  /** Parsed execution statistics (after JSON.parse in parseResult). */
  statistics: Statistics;
  /** Pipeline snapshot (direct JsType object, not JSON-serialised). */
  pipeline: Pipeline;
  /** true when the CPU hit a BREAK instruction during the last step batch. */
  encounteredBreak: boolean;
  /** Parser diagnostics from the last load/checksyntax call; null until populated. */
  parsingErrors: ParsingError[] | null;
  /**
   * Instructions currently loaded in memory; null when parsingErrors contains
   * at least one non-warning error.
   */
  parsedInstructions: PipelineInstruction[] | null;
  /**
   * Accumulated standard-output text from the running program.
   * Empty string when nothing has been written.
   */
  stdout: string;
  /**
   * The method that produced this result: "step", "load", "reset",
   * "checksyntax", etc.  Used by the worker handler to dispatch results to
   * the correct update path.
   *
   * Java source: Result.method
   */
  method: string;
  // Input-request fields — populated when inputRequested=true.
  /** true when the CPU is blocked waiting for stdin input. */
  inputRequested: boolean;
  /** Maximum accepted input length (0 = unlimited). */
  inputMaxLength: number;
  /** Number of steps still pending when input was requested. */
  inputResumeSteps: number;
  /** Title for the InputDialog. */
  inputDialogTitle: string;
  /** Prompt text for the InputDialog. */
  inputPromptMessage: string;
  /** Message shown when the user's input exceeds inputMaxLength. */
  inputTooLongMessage: string;
  // Synchronous-exception fields — populated when a SynchronousException was raised.
  /** Exception code name (e.g. "INT_OVERFLOW"); empty string when none. */
  errorCode: string;
  /** Name of the faulting instruction; empty string when none. */
  errorInstruction: string;
  /** Pipeline stage where the exception occurred; empty string when none. */
  errorStage: string;
}

// ---------------------------------------------------------------------------
// SimulatorWorker — the augmented Worker instance
// ---------------------------------------------------------------------------

/**
 * The Web Worker proxy created in index.js, augmented with convenience
 * methods that wrap postMessage calls and a parseResult helper.
 *
 * The worker runs the GWT-compiled EduMIPS64 core (worker.js) and
 * communicates exclusively via postMessage/onmessage.
 */
export interface SimulatorWorker extends Worker {
  /** Send a "reset" message — re-initialises the CPU to the READY state. */
  reset(): void;
  /**
   * Send a "step" message — execute up to `n` CPU cycles.
   * The result arrives as a 'message' event with a SimulatorResult payload.
   */
  step(n: number): void;
  /** Send a "load" message — assemble and load `code` into the simulator. */
  load(code: string): void;
  /** Send a "checksyntax" message — syntax-check `code` without loading it. */
  checkSyntax(code: string): void;
  /** Send a "setCacheConfig" message — reconfigure the L1 cache parameters. */
  setCacheConfig(config: object): void;
  /** Send a "setForwarding" message — enable/disable pipeline forwarding. */
  setForwarding(enabled: boolean): void;
  /** Send a "setDelaySlot" message — enable/disable branch delay slot. */
  setDelaySlot(enabled: boolean): void;
  /** Send a "provideInput" message — supply stdin text while blocked on input. */
  provideInput(input: string): void;
  /**
   * Parse a raw worker message data object into a SimulatorResult.
   * JSON-decodes the registers/memory/statistics string fields in place.
   */
  parseResult(result: Record<string, unknown>): SimulatorResult;
  /** The git-describe version string, baked in by webpack DefinePlugin. */
  version: string;
}
