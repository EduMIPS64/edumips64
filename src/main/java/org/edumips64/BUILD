# Build variables.
codename = "Nicolosi"

splash_img = "images/splash.png"

version = "1.2.4"

manifest_nosplash_lines = [
    "Codename: {}".format(codename),
    "Signature-Version: {}".format(version),
]

manifest_lines = manifest_nosplash_lines + [
    "SplashScreen-Image: {}".format(splash_img),
]

# Main binary rules.
# TODO: those genrules are likely Linux-only. Should be tested on other platforms.
genrule(
    name = "standalone-jar",
    srcs = [":edumips64_deploy.jar"],
    outs = ["edumips64-" + version + ".jar"],
    # This genrule does the following things:
    # - renames the edumips64_deploy.jar file to edumips64-${version}.jar;
    # - adds to the manifest the build date and the git revision (by first extracting it, changing it and adding it
    #   back to the JAR file);
    # The main reason for this hack is adding build-time variables to the Manifest, which is not directly
    # supported in java_binary (see https://github.com/bazelbuild/bazel/issues/2009).
    # Note that the JAR file is write-protected, so the genrule needs to first add write permissions and then remove
    # them at the end.
    cmd = "\n".join([
        "ls $(GENDIR)/src/main/java/org/edumips64",
        "cp $(location :edumips64_deploy.jar) $@",
        # The jar is write-protected, need to un-protect it before fiddling with it.
        "chmod u+w $(OUTS);",
        "unzip -q $(OUTS) META-INF/MANIFEST.MF",
        "sed -i '$$ d' META-INF/MANIFEST.MF",
        "echo \"Build-Date: $$(date)\" >> META-INF/MANIFEST.MF",
        "GIT_REV=$$(grep STABLE_GIT_COMMIT bazel-out/stable-status.txt | cut -d' ' -f2)",
        "echo \"Git-Revision: $$GIT_REV\" >> META-INF/MANIFEST.MF",
        "zip -q -r -ll $(OUTS) META-INF/MANIFEST.MF",
        "chmod u-w $(OUTS);",
    ]),
    stamp = 1,
)

genrule(
    name = "slim-jar",
    srcs = [":edumips64_deploy.jar"],
    outs = ["edumips64-" + version + "-nodeps.jar"],
    # This rule behaves much like standalone-jar, but in addition also removes the class files from javahelp.
    #
    # This is necessary because there is no easy way to do so via Bazel dependencies. There is an open feature
    # request to make this possible in Bazel at https://github.com/bazelbuild/bazel/issues/5663.
    cmd = "\n".join([
        "cp $(location :edumips64_deploy.jar) $@;",
        "chmod u+w $(OUTS);",
        "unzip -q $(OUTS) META-INF/MANIFEST.MF",
        "sed -i '$$ d' META-INF/MANIFEST.MF",
        "echo \"Build-Date: $$(date)\" >> META-INF/MANIFEST.MF",
        "GIT_REV=$$(grep STABLE_GIT_COMMIT bazel-out/stable-status.txt | cut -d' ' -f2)",
        "echo \"Git-Revision: $$GIT_REV\" >> META-INF/MANIFEST.MF",
        "zip -q -r -ll $(OUTS) META-INF/MANIFEST.MF",
        # Remove the javahelp classes.
        "zip -q -d $(OUTS) 'com/sun/java/help/*' 'javax/help/*'",
        "chmod u-w $(OUTS);",
    ]),
    stamp = 1,
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
