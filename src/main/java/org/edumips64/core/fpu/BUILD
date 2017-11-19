package(default_visibility = ["//visibility:public"])

java_library(
    name = "fppipeline",
    srcs = [
        "FPPipeline.java",
    ],
    deps = [
        "//src/main/java/org/edumips64/core/is:instructioninterface",
    ],
)

java_library(
    name = "registerfp",
    srcs = [
        "RegisterFP.java",
    ],
    deps = [
        ":bitsetfp",
        "//src/main/java/org/edumips64/core:exceptions",
    ],
)

java_library(
    name = "bitsetfp",
    srcs = [
        "BitSet64FP.java",
    ],
    deps = [
        ":exceptions",
        ":fpinstructionutils",
        "//src/main/java/org/edumips64/core:bitsets",
        "//src/main/java/org/edumips64/core:fcsr",
    ],
)

java_library(
    name = "fpinstructionutils",
    srcs = [
        "FPInstructionUtils.java",
    ],
    deps = [
        ":exceptions",
        "//src/main/java/org/edumips64/core:converter",
        "//src/main/java/org/edumips64/core:exceptions",
        "//src/main/java/org/edumips64/core:fcsr",
    ],
)

java_library(
    name = "exceptions",
    srcs = glob(["*Exception.java"]),
    deps = [
        "//src/main/java/org/edumips64/core:exceptions",
    ],
)
