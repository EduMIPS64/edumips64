package(default_visibility = ["//visibility:public"])

# The swing target comes in two ways: one with a JavaHelp dependency which will
# be bundled in the JAR ("swing") and another one with a JavaHelp dependency which
# won't be bundled in the JAR ("swing-no-javahelp").
#
# TODO: clean up code duplication both here and in main-lib / main-lib-no-javahelp.

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

java_library(
    name = "swing-no-javahelp",
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
        "@javax_help_javahelp-neverlink//jar",
    ],
)
