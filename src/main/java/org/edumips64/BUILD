# Build variables.
codename = "Nicolosi"

splash_img = "images/splash.png"

version = "1.2.4"

manifest_nosplash_lines = [
    "Codename: {}".format(codename),
    "Signature-Version: {}".format(version),

    # Missing variables due to https://github.com/bazelbuild/bazel/issues/2009. Must be fixed before relying only
    # on Bazel.
    #"Build-Date: {}".format(CURRENT_TIME),
    #"Git-Revision: {}".format(git_revision),
]

manifest_lines = manifest_nosplash_lines + [
    "SplashScreen-Image: {}".format(splash_img),
]

# Main binary rules.
genrule(
    name = "standalone-jar",
    srcs = [":edumips64_deploy.jar"],
    outs = ["edumips64-" + version + ".jar"],
    cmd = "cp $(location :edumips64_deploy.jar) $@",
)

# TODO: this genrule is not cross-platform. Should be tested on Win/Mac and adapted.
genrule(
    name = "slim-jar",
    srcs = [":edumips64_deploy.jar"],
    outs = ["edumips64-" + version + "-nodeps.jar"],
    cmd = "\n".join([
        "cp $(location :edumips64_deploy.jar) $@;",
        "chmod u+w $(OUTS);",
        "zip -q -d $(OUTS) 'com/sun/java/help/*' 'javax/help/*'",
        "chmod u-w $(OUTS);",
    ]),
)

genrule(
    name = "cli-jar",
    srcs = [":edumips64-cli_deploy.jar"],
    outs = ["edumips64-" + version + "-cli.jar"],
    cmd = "cp $(location :edumips64-cli_deploy.jar) $@",
)

java_binary(
    name = "edumips64",
    deploy_manifest_lines = manifest_lines,
    main_class = "org.edumips64.Main",
    resources = [
        "//docs/user/en:html",
        "//docs/user/en:images",
        "//docs/user/en:javahelp",
        "//docs/user/it:html",
        "//docs/user/it:images",
        "//docs/user/it:javahelp",
    ],
    stamp = 1,
    runtime_deps = [
        "main-lib",
    ],
)

java_binary(
    name = "edumips64-cli",
    deploy_manifest_lines = manifest_nosplash_lines,
    main_class = "org.edumips64.MainCLI",
    runtime_deps = [
        "main-lib",
    ],
)

# TODO: split out the GUI and the non-GUI code once parseArgsOrExit is out of Main.
java_library(
    name = "main-lib",
    srcs = [
        "Main.java",
        "MainCLI.java",
    ],
    deps = [
        "//src/main/java/org/edumips64/core:cpu",
        "//src/main/java/org/edumips64/core:dinero",
        "//src/main/java/org/edumips64/core:iomanager",
        "//src/main/java/org/edumips64/core:memory",
        "//src/main/java/org/edumips64/core:symboltable",
        "//src/main/java/org/edumips64/core/is",
        "//src/main/java/org/edumips64/core/is:exceptions",
        "//src/main/java/org/edumips64/core/is:instructionbuilder",
        "//src/main/java/org/edumips64/core/parser",
        "//src/main/java/org/edumips64/ui/swing",
        "//src/main/java/org/edumips64/ui/swing/img",
        "//src/main/java/org/edumips64/utils:config",
        "//src/main/java/org/edumips64/utils:currentlocale",
        "//src/main/java/org/edumips64/utils:cyclebuilder",
        "//src/main/java/org/edumips64/utils:javaprefsconfigstore",
        "//src/main/java/org/edumips64/utils:metainfo",
        "//src/main/java/org/edumips64/utils/io",
    ],
)
