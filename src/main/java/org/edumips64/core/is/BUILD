package(default_visibility = ["//visibility:public"])

java_library(
    name = "is",
    srcs = glob(
        ["*.java"],
        exclude = [
            "Instruction.java",
            "InstructionInterface.java",
            "InstructionBuilder.java",
        ] + glob(["*Exception.java"]),
    ),
    deps = [
        ":exceptions",
        ":instruction",
        "//src/main/java/org/edumips64/core:bitsets",
        "//src/main/java/org/edumips64/core:converter",
        "//src/main/java/org/edumips64/core:cpu",  # HALT
        "//src/main/java/org/edumips64/core:dinero",  # SYSCALL
        "//src/main/java/org/edumips64/core:exceptions",
        "//src/main/java/org/edumips64/core:fcsr",
        "//src/main/java/org/edumips64/core:iomanager",
        "//src/main/java/org/edumips64/core:memory",
        "//src/main/java/org/edumips64/core:registers",
        "//src/main/java/org/edumips64/core/fpu:exceptions",
        "//src/main/java/org/edumips64/core/fpu:fpinstructionutils",
        "//src/main/java/org/edumips64/core/fpu:registerfp",
        "//src/main/java/org/edumips64/utils:currentlocale",
        "//src/main/java/org/edumips64/utils/io",  # SYSCALL
    ],
)

java_library(
    name = "instructionbuilder",
    srcs = ["InstructionBuilder.java"],
    deps = [
        ":instruction",
        ":is",
        "//src/main/java/org/edumips64/core:cpu",
        "//src/main/java/org/edumips64/core:dinero",
        "//src/main/java/org/edumips64/core:iomanager",
        "//src/main/java/org/edumips64/core:memory",
        "//src/main/java/org/edumips64/utils:config",
    ],
)

java_library(
    name = "instruction",
    srcs = ["Instruction.java"],
    deps = [
        ":instructioninterface",
        "//src/main/java/org/edumips64/core:bitsets",
        "//src/main/java/org/edumips64/core:cpu",
        "//src/main/java/org/edumips64/core:dinero",
        "//src/main/java/org/edumips64/core:registers",
        "//src/main/java/org/edumips64/core/fpu:registerfp",
    ],
)

java_library(
    name = "exceptions",
    srcs = glob(["*Exception.java"]),
    deps = [
        "//src/main/java/org/edumips64/core:exceptions",
    ],
)

java_library(
    name = "instructioninterface",
    srcs = ["InstructionInterface.java"],
    deps = [
        ":exceptions",
        "//src/main/java/org/edumips64/core:bitsets",
        "//src/main/java/org/edumips64/core:exceptions",
        "//src/main/java/org/edumips64/core/fpu:exceptions",
    ],
)
