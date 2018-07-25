load("@bazel_tools//tools/build_defs/repo:java.bzl", "java_import_external")

# JavaHelp is imported twice, because we need one target which will make the class
# files bundled within the JAR file (for the stand-alone JAR) and another one with
# the neverlink attribute set to 1, which will NOT include the class files in the
# resulting JAR (for the slim JAR).
java_import_external(
    name = "javax_help_javahelp", # Name obtained by using https://gist.github.com/jart/41bfd977b913c2301627162f1c038e55
    licenses = ["restricted"], # GPL v2 with class path exception
    jar_urls = [
        "http://central.maven.org/maven2/javax/help/javahelp/2.0.05/javahelp-2.0.05.jar",
    ],
    jar_sha256 = "fcf4922d38ff85184f1d2328317bb60826e14da948abd606ee3d5b8c6a70debd",
)

java_import_external(
    name = "javax_help_javahelp-neverlink", # Name obtained by using https://gist.github.com/jart/41bfd977b913c2301627162f1c038e55
    licenses = ["restricted"], # GPL v2 with class path exception
    jar_urls = [
        "http://central.maven.org/maven2/javax/help/javahelp/2.0.05/javahelp-2.0.05.jar",
    ],
    jar_sha256 = "fcf4922d38ff85184f1d2328317bb60826e14da948abd606ee3d5b8c6a70debd",
    neverlink = 1,
)

maven_jar(
  name = "junit_junit",
  artifact = "junit:junit:4.10",
)
