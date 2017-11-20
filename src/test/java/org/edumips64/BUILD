package(default_visibility = ["//visibility:public"])

java_test(
    name = "EndToEndTests",
    size = "small",
    srcs = [
        "EndToEndTests.java",
    ],
    data = [
        "//src/test/resources:testresources",
        "//src/test/resources/utils:testutils",
    ],
    deps = [
        ":basetest",
        ":basewithbuildertest",
        "//src/main/java/org/edumips64/core:cpu",
        "//src/main/java/org/edumips64/core:dinero",
        "//src/main/java/org/edumips64/core:exceptions",
        "//src/main/java/org/edumips64/core:iomanager",
        "//src/main/java/org/edumips64/core:memory",
        "//src/main/java/org/edumips64/core:symboltable",
        "//src/main/java/org/edumips64/core/fpu:registerfp",
        "//src/main/java/org/edumips64/core/is",
        "//src/main/java/org/edumips64/core/is:exceptions",
        "//src/main/java/org/edumips64/core/is:instructionbuilder",
        "//src/main/java/org/edumips64/core/parser",
        "//src/main/java/org/edumips64/utils:config",
        "//src/main/java/org/edumips64/utils:cyclebuilder",
        "//src/main/java/org/edumips64/utils/io",
    ],
)

java_library(
    name = "basetest",
    srcs = [
        "BaseTest.java",
        "InvalidCycleElementTransactionException.java",
    ],
    deps = [
        "//src/main/java/org/edumips64/core:cpu",
        "//src/main/java/org/edumips64/core/fpu:registerfp",
        "//src/main/java/org/edumips64/core/parser",
        "//src/main/java/org/edumips64/utils:config",
        "//src/main/java/org/edumips64/utils:cyclebuilder",
        "@junit_junit//jar",
    ],
)

java_library(
    name = "basewithbuildertest",
    srcs = [
        "BaseWithInstructionBuilderTest.java",
    ],
    deps = [
        ":basetest",
        "//src/main/java/org/edumips64/core:cpu",
        "//src/main/java/org/edumips64/core:dinero",
        "//src/main/java/org/edumips64/core:iomanager",
        "//src/main/java/org/edumips64/core:memory",
        "//src/main/java/org/edumips64/core:symboltable",
        "//src/main/java/org/edumips64/core/fpu:registerfp",
        "//src/main/java/org/edumips64/core/is",
        "//src/main/java/org/edumips64/core/is:instructionbuilder",
        "//src/main/java/org/edumips64/core/parser",
        "//src/main/java/org/edumips64/utils:config",
        "//src/main/java/org/edumips64/utils:cyclebuilder",
        "//src/main/java/org/edumips64/utils/io",
    ],
)
