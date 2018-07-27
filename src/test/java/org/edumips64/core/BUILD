package(default_visibility = ["//visibility:public"])

java_test(
    name = "CPUTest",
    size = "small",
    srcs = ["CPUTest.java"],
    deps = [
        "//src/main/java/org/edumips64/core:cpu",
        "//src/main/java/org/edumips64/core:exceptions",
        "//src/main/java/org/edumips64/core:memory",
        "//src/main/java/org/edumips64/core/is",
        "//src/test/java/org/edumips64:basetest",
    ],
)

java_test(
    name = "FixedBitSetTest",
    size = "small",
    srcs = ["FixedBitSetTest.java"],
    deps = ["//src/main/java/org/edumips64/core:bitsets"],
)

java_test(
    name = "MemoryTest",
    size = "small",
    srcs = ["MemoryTest.java"],
    deps = [
        "//src/main/java/org/edumips64/core:cpu",
        "//src/main/java/org/edumips64/core:dinero",
        "//src/main/java/org/edumips64/core:iomanager",
        "//src/main/java/org/edumips64/core:memory",
        "//src/main/java/org/edumips64/core/is",
        "//src/main/java/org/edumips64/core/is:instructionbuilder",
        "//src/main/java/org/edumips64/utils/io",
        "//src/test/java/org/edumips64:basetest",
    ],
)

java_test(
    name = "ParserTest",
    size = "small",
    srcs = ["ParserTest.java"],
    deps = [
        "//src/main/java/org/edumips64/core:cpu",
        "//src/main/java/org/edumips64/core:dinero",
        "//src/main/java/org/edumips64/core:fcsr",
        "//src/main/java/org/edumips64/core:iomanager",
        "//src/main/java/org/edumips64/core:memory",
        "//src/main/java/org/edumips64/core:symboltable",
        "//src/main/java/org/edumips64/core/is",
        "//src/main/java/org/edumips64/core/is:instructionbuilder",
        "//src/main/java/org/edumips64/core/parser",
        "//src/main/java/org/edumips64/utils/io",
        "//src/test/java/org/edumips64:basetest",
    ],
)

java_test(
    name = "PipelineTest",
    size = "small",
    srcs = ["PipelineTest.java"],
    deps = [
        "//src/main/java/org/edumips64/core:pipeline",
        "//src/main/java/org/edumips64/core/is",
        "//src/test/java/org/edumips64:basewithbuildertest",
    ],
)
