package(default_visibility = ["//visibility:public"])

java_library(
    name = "swing",
    srcs = glob(["*.java"]),
    deps = [
        "//src/main/java/org/edumips64/core:converter",
        "//src/main/java/org/edumips64/core:cpu",
        "//src/main/java/org/edumips64/core:dinero",
        "//src/main/java/org/edumips64/core:exceptions",
        "//src/main/java/org/edumips64/core:memory",
        "//src/main/java/org/edumips64/core:pipeline",
        "//src/main/java/org/edumips64/core:registers",
        "//src/main/java/org/edumips64/core/fpu:bitsetfp",
        "//src/main/java/org/edumips64/core/fpu:registerfp",
        "//src/main/java/org/edumips64/core/is:exceptions",
        "//src/main/java/org/edumips64/core/is:instructioninterface",
        "//src/main/java/org/edumips64/core/parser",
        "//src/main/java/org/edumips64/ui/swing/img",
        "//src/main/java/org/edumips64/utils:config",
        "//src/main/java/org/edumips64/utils:currentlocale",
        "//src/main/java/org/edumips64/utils:cyclebuilder",
        "//src/main/java/org/edumips64/utils/io",
        "@javax_help_javahelp//jar",
    ],
)
