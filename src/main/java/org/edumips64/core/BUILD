package(default_visibility = ["//visibility:public"])

java_library(
    name = "cpu",
    srcs = [
        "CPU.java",
    ],
    deps = [
        ":exceptions",
        ":fcsr",
        ":memory",
        ":pipeline",
        ":registers",
        "//src/main/java/org/edumips64/core/fpu:exceptions",
        "//src/main/java/org/edumips64/core/fpu:fppipeline",
        "//src/main/java/org/edumips64/core/fpu:registerfp",
        "//src/main/java/org/edumips64/core/is:exceptions",
        "//src/main/java/org/edumips64/core/is:instructioninterface",
        "//src/main/java/org/edumips64/utils:config",
    ],
)

java_library(
    name = "iomanager",
    srcs = [
        "IOManager.java",
    ],
    deps = [
        ":exceptions",
        ":memory",
        "//src/main/java/org/edumips64/utils/io",
    ],
)

java_library(
    name = "pipeline",
    srcs = [
        "Pipeline.java",
    ],
    deps = [
        "//src/main/java/org/edumips64/core/is:instructioninterface",
    ],
)

java_library(
    name = "symboltable",
    srcs = [
        "SymbolTable.java",
    ],
    deps = [
        ":exceptions",
        ":memory",
        "//src/main/java/org/edumips64/core/is:instructioninterface",
    ],
)

java_library(
    name = "memory",
    srcs = [
        "Memory.java",
        "MemoryElement.java",
    ],
    deps = [
        ":bitsets",
        ":converter",
        ":exceptions",
        "//src/main/java/org/edumips64/core/is:instructioninterface",
    ],
)

java_library(
    name = "converter",
    srcs = [
        "Converter.java",
    ],
    deps = [
        ":exceptions",
    ],
)

java_library(
    name = "bitsets",
    srcs = [
        "BitSet32.java",
        "BitSet64.java",
        "FixedBitSet.java",
        "IrregularStringOfBitsException.java",
        "IrregularWriteOperationException.java",
        "NotAlignException.java",
    ],
    deps = [
        ":converter",
    ],
)

java_library(
    name = "dinero",
    srcs = [
        "Dinero.java",
    ],
    deps = [
        ":converter",
        ":exceptions",
        "//src/main/java/org/edumips64/utils/io",
    ],
)

java_library(
    name = "registers",
    srcs = [
        "AlreadyLockedRegisterException.java",
        "Register.java",
    ],
    deps = [
        ":bitsets",
        ":converter",
        ":exceptions",
    ],
)

java_library(
    name = "fcsr",
    srcs = [
        "FCSRRegister.java",
    ],
    deps = [
        ":bitsets",
        ":exceptions",
        "//src/main/java/org/edumips64/core/fpu:exceptions",
    ],
)

java_library(
    name = "exceptions",
    srcs = glob(["*Exception.java"]),
)
