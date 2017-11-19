package(default_visibility = ["//visibility:public"])

java_library(
    name = "cyclebuilder",
    srcs = [
        "CycleBuilder.java",
        "CycleElement.java",
    ],
    deps = [
        "//src/main/java/org/edumips64/core:cpu",
        "//src/main/java/org/edumips64/core:pipeline",
        "//src/main/java/org/edumips64/core/is:instruction",
        "//src/main/java/org/edumips64/core/is:instructioninterface",
    ],
)

java_library(
    name = "config",
    srcs = [
        "ConfigFileNotFoundException.java",
        "ConfigKey.java",
        "ConfigStore.java",
        "ConfigStoreTypeException.java",
        "InMemoryConfigStore.java",
    ],
)

java_library(
    name = "currentlocale",
    srcs = [
        "CurrentLocale.java",
    ],
    deps = [
        ":config",
    ],
)

java_library(
    name = "metainfo",
    srcs = [
        "MetaInfo.java",
    ],
)

java_library(
    name = "javaprefsconfigstore",
    srcs = [
        "JavaPrefsConfigStore.java",
    ],
    deps = [
        ":config",
    ],
)
