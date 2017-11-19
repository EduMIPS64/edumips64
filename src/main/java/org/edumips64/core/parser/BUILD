package(default_visibility = ["//visibility:public"])

java_library(
    name = "parser",
    srcs = glob(["*.java"]),
    deps = [
        "//src/main/java/org/edumips64/core:converter",
        "//src/main/java/org/edumips64/core:exceptions",
        "//src/main/java/org/edumips64/core:fcsr",
        "//src/main/java/org/edumips64/core:memory",
        "//src/main/java/org/edumips64/core:symboltable",
        "//src/main/java/org/edumips64/core/fpu:exceptions",
        "//src/main/java/org/edumips64/core/fpu:fpinstructionutils",
        "//src/main/java/org/edumips64/core/is:instruction",
        "//src/main/java/org/edumips64/core/is:instructionbuilder",
        "//src/main/java/org/edumips64/utils:currentlocale",
        "//src/main/java/org/edumips64/utils/io",
    ],
)
