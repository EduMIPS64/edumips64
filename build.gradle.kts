/*
 * EduMIPS64 Gradle build configuration
 */
import java.time.LocalDateTime
import ru.vyarus.gradle.plugin.python.task.PythonTask
import ru.vyarus.gradle.plugin.python.PythonExtension.Scope.VIRTUALENV

plugins {
    java
    // The Eclipse plugin adds the "eclipse" task, which generates
    // files needed for Visual Studio Code and other IDEs.
    id ("eclipse")
    id ("application")
    id ("jacoco")
    id ("com.dorongold.task-tree") version "4.0.1"
    id ("us.ascendtech.gwt.classic") version "0.14.0"
    id ("ru.vyarus.use-python") version "4.1.0"
}

// Consolidate all Gradle build outputs under a single "out/" directory in the
// project root, instead of the Gradle default "build/". This keeps generated
// artifacts (JARs, docs, GWT output, MSI, reports, ...) in one discoverable
// location.
layout.buildDirectory.set(layout.projectDirectory.dir("out"))

// The us.ascendtech.gwt.classic plugin hard-codes the resources output to
// "build/classes/java/main" (see GWTLibPlugin). Override it so resources are
// emitted under the custom build directory too, keeping everything in one place.
sourceSets.matching { it.name == "main" }.configureEach {
    output.setResourcesDir(layout.buildDirectory.dir("classes/java/main"))
}

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("org.gwtproject:gwt-user:2.13.0")
    compileOnly("org.gwtproject:gwt-dev:2.13.0")
    compileOnly("com.google.elemental2:elemental2-dom:1.3.2")
    compileOnly("com.vertispan.rpc:workers:1.0-alpha-8")
    
    implementation("com.formdev:flatlaf:3.7.1")
    implementation("javax.help:javahelp:2.0.05")
    implementation("info.picocli:picocli:4.7.7")

    testImplementation(platform("org.junit:junit-bom:6.0.3"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    // To run JUnit 4 tests.
    testImplementation("junit:junit:4.13.2")    
    testRuntimeOnly("org.junit.vintage:junit-vintage-engine")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // GWT Testing
    testImplementation("org.gwtproject:gwt-user:2.13.0")
    testImplementation("org.gwtproject:gwt-dev:2.13.0")
    testImplementation("com.google.elemental2:elemental2-dom:1.3.2")
    testImplementation("com.vertispan.rpc:workers:1.0-alpha-8")
}

python {
    // Allow overriding pythonPath via Gradle project property (e.g., -PpythonPath=... or
    // ORG_GRADLE_PROJECT_pythonPath env var). Used in CI to point to the Python installed
    // by actions/setup-python rather than the system Python.
    val customPythonPath = findProperty("pythonPath") as String?
    if (customPythonPath != null) {
        pythonPath = customPythonPath
    }
    scope = VIRTUALENV
    requirements.file = "docs/requirements.txt"
    minPythonVersion = "3.14"
}

application {
  mainClass.set("org.edumips64.Main")
}
val codename: String by project
val version: String by project

// Use Java toolchain for consistent JDK version across all compilations.
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

// Exclude the GWT super-source directory from the main Java compilation. It provides
// alternative implementations of classes that aren't compatible with GWT's JRE emulation
// (see webclient.gwt.xml <super-source> directive) and would otherwise clash with the
// regular sources as duplicate classes.
sourceSets {
    main {
        java {
            exclude("org/edumips64/supersource/**")
        }
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}

/* 
 * Documentation tasks. To avoid dependency on GNU Make, these tasks duplicate the commands run by the Sphinx makefiles.
 *
 * The user manual is split (via Sphinx `.. only::` directives) in three
 * flavors, all built from the same sources:
 *  - "full"  (no Sphinx tag): includes both UI chapters. Used for the PDF
 *            and matches what Read the Docs publishes.
 *  - "swing" (-t swing): only the desktop (Swing) UI chapter. Used by the
 *            in-application help of the desktop JAR.
 *  - "web"   (-t web): only the web UI chapter. Used by the in-application
 *            help of the web frontend.
 */
fun buildDocsCmd(language: String, type: String, flavor: String = "full") : String {
    val outputSubdir = if (type == "html" && flavor != "full") "${type}-${flavor}" else type
    val baseDir = "${layout.buildDirectory.get()}/docs/${language}"
    val tagFlag = if (flavor == "full") "" else "-t ${flavor} "
    return "-m sphinx -N -a -E ${tagFlag}. ${baseDir}/${outputSubdir} -b ${type} -d ${baseDir}/doctrees-${flavor}"
}

/*
 * Jar tasks
 */
val docsDir = layout.buildDirectory.dir("resources/main/docs")

// Generate documentation tasks for all languages
val languages = listOf("en", "it", "zh")
val docTypes = listOf("html", "pdf")
// HTML flavors that filter the user-interface chapter via Sphinx `.. only::`
// directives. The "full" flavor (no tag) includes both UI chapters and
// matches the PDF and Read the Docs build.
val htmlFlavors = listOf("full", "swing", "web")
val allDocsTaskNames = mutableListOf<String>()
val copyHelpTaskNames = mutableListOf<String>()
val copyWebHelpTaskNames = mutableListOf<String>()
val htmlTaskNames = mutableListOf<String>()

fun langCap(l: String) = l.replaceFirstChar { it.uppercase() }
fun flavorCap(f: String) = f.replaceFirstChar { it.uppercase() }

for (language in languages) {
    // PDF tasks (single flavor: "full"; the PDF always contains everything).
    val pdfTaskName = "pdfDocs${langCap(language)}"
    tasks.register<PythonTask>(pdfTaskName) {
        workDir = "${projectDir}/docs/user/${language}/src"
        command = buildDocsCmd(language, "pdf")
    }
    allDocsTaskNames.add(pdfTaskName)

    // HTML tasks, one per flavor. The "full" flavor keeps the historical
    // task name `htmlDocs<Lang>` so existing dependencies (e.g. on Read the
    // Docs and CI) keep working unchanged.
    for (flavor in htmlFlavors) {
        val taskName = if (flavor == "full") "htmlDocs${langCap(language)}"
                       else "htmlDocs${flavorCap(flavor)}${langCap(language)}"
        tasks.register<PythonTask>(taskName) {
            workDir = "${projectDir}/docs/user/${language}/src"
            command = buildDocsCmd(language, "html", flavor)
        }
        allDocsTaskNames.add(taskName)
        if (flavor == "full") {
            htmlTaskNames.add(taskName)
        }
    }

    // copyHelp<Lang>: copies the Swing-flavored HTML into the JAR resources.
    val copyTaskName = "copyHelp${langCap(language)}"
    tasks.register<Copy>(copyTaskName) {
        from("${layout.buildDirectory.get()}/docs/${language}") {
            // Map the Swing-flavored HTML output to the standard "html/"
            // path used at runtime (Map.jhm + HelpDialog.js both look for
            // docs/<lang>/html/).
            include("html-swing/**")
            exclude("**/_sources/**")
            eachFile { path = path.replaceFirst("html-swing", "html") }
            includeEmptyDirs = false
        }
        into(docsDir.map { it.dir("user/$language") })
        dependsOn("htmlDocsSwing${langCap(language)}")
        mustRunAfter("compileJava")
    }
    copyHelpTaskNames.add(copyTaskName)

    // copyWebHelp<Lang>: copies the Web-flavored HTML into the web bundle.
    val copyWebTaskName = "copyWebHelp${langCap(language)}"
    tasks.register<Copy>(copyWebTaskName) {
        from("${layout.buildDirectory.get()}/docs/${language}") {
            include("html-web/**")
            exclude("**/.buildinfo", "**/objects.inv", "**/_sources/**")
            eachFile { path = path.replaceFirst("html-web", "html") }
            includeEmptyDirs = false
        }
        into(layout.buildDirectory.dir("web/docs/${language}"))
        dependsOn("htmlDocsWeb${langCap(language)}")
    }
    copyWebHelpTaskNames.add(copyWebTaskName)
}

// Catch-all tasks for documentation
tasks.register("allDocs") {
    dependsOn(allDocsTaskNames)
    description = "Run all documentation tasks"
}
tasks.register("htmlDocs") {
    dependsOn(htmlTaskNames)
    description = "Run all HTML documentation tasks"
}

// Include the docs folder at the root of the jar, for JavaHelp

tasks.register<Copy>("copyHelp") {
    from("docs/") {
        exclude("**/src/**", "**/design/**", "**/*.py",  "**/*.pyc", 
            "**/*.md", "**/.buildinfo", "**/objects.inv", "**/*.txt", "**/__pycache__/**")
    }
    into(docsDir)
    copyHelpTaskNames.forEach { dependsOn(it) }
}

/*
 * Source control metadata, using Gradle providers for configuration-cache compatibility.
 * On CI (GitHub Actions), values come from environment variables.
 * Locally, values are obtained by running git commands.
 */
val gitBranch: Provider<String> = providers.environmentVariable("GITHUB_REF")
    .orElse(providers.exec {
        commandLine("git", "rev-parse", "--abbrev-ref", "HEAD")
    }.standardOutput.asText.map { it.trim() })

val gitCommitHash: Provider<String> = providers.environmentVariable("GITHUB_SHA")
    .map { it.substring(0, 7) }
    .orElse(providers.exec {
        commandLine("git", "rev-parse", "--verify", "--short", "HEAD")
    }.standardOutput.asText.map { it.trim() })

val buildQualifier: Provider<String> = providers.environmentVariable("GITHUB_ACTIONS")
    .map { "alpha" }
    .orElse("")

val sharedManifest = Action<Manifest> {
    attributes["Signature-Version"] = version
    attributes["Codename"] = codename
    attributes["Build-Date"] = LocalDateTime.now()
    attributes["Full-Buildstring"] = "${gitBranch.get()}@${gitCommitHash.get()}"
    attributes["Git-Revision"] = gitCommitHash.get()
    attributes["Build-Qualifier"] = buildQualifier.get()
}

// Main JAR — write directly under the build directory (out/) rather than
// the default out/libs/ subfolder, so JARs sit next to other top-level artifacts.
tasks.jar {
    destinationDirectory.set(layout.buildDirectory)
    from(sourceSets.main.get().output)
    from(docsDir) {
        into("docs")
    }
    from({
        configurations.runtimeClasspath.get().filter { (it.name.contains("picocli") || it.name.contains("javahelp") || it.name.contains("flatlaf")) && it.name.endsWith("jar") }.map {  println("Adding dependency " + it.name); zipTree(it) }

    })
    manifest {
        attributes["Main-Class"] = application.mainClass.get()
        sharedManifest.execute(this)
    }
    dependsOn("copyHelp")
}

// NoHelp JAR — same destination as the main JAR (build directory root).
tasks.register<Jar>("noHelpJar"){
    destinationDirectory.set(layout.buildDirectory)
    archiveClassifier.set("nohelp")
    dependsOn(configurations.runtimeClasspath)
    from(sourceSets.main.get().output)
    from({
        configurations.runtimeClasspath.get().filter { it.name.contains("picocli") && it.name.endsWith("jar") || it.name.contains("flatlaf") }.map { println("Adding dependency " + it.name); zipTree(it) }
    })
    manifest {
        attributes["Main-Class"] = application.mainClass.get()
        sharedManifest.execute(this)
    }
}

/*
 * Code coverage report tasks
 */
jacoco {
    toolVersion = "0.8.14"
}
tasks.jacocoTestReport {
    reports {
        xml.required.set(true)
        csv.required.set(false)
        html.required.set(false)
    }
}

// Add logging of all executed tests.
tasks {
    withType<Test> {
        useJUnitPlatform()
        testLogging.events = setOf(
            org.gradle.api.tasks.testing.logging.TestLogEvent.PASSED,
            org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED,
            org.gradle.api.tasks.testing.logging.TestLogEvent.SKIPPED
        )
        
        // GWTTestCase requires source files on the classpath to compile the module
        classpath += files(project.sourceSets.main.get().java.srcDirs)
        classpath += files(project.sourceSets.test.get().java.srcDirs)
        
        // Increase memory for GWT compilation
        maxHeapSize = "1024m"
        
        // Pass system property to allow GWT to find the module
        systemProperty("gwt.args", "-war " + layout.buildDirectory.dir("gwt-tests").get().asFile.absolutePath)
        
        // Ensure UTF-8 encoding for tests
        systemProperty("file.encoding", "UTF-8")
    }
}

tasks.check{
    dependsOn("jacocoTestReport")
}

tasks.register("release") {
    group = "Release"
    description = "Creates all artifacts for a given EduMIPS64 release"
    dependsOn("allDocs")
    dependsOn("jar")
    dependsOn("msi")

    doFirst {
        println("Creating artifacts for version $version.")
    }
}

tasks.register<Exec>("msi"){
    group = "Distribution"
    description = "Creates an installable MSI file"
    workingDir = projectDir

    doFirst{
        var os = System.getProperty("os.name") as String;
        if (!("Windows" in os)){
            throw GradleException("MSI creation must be executed on Windows")
        }
        var majorVersion = System.getProperty("java.version").split(".")[0].toInt()
        
        if (majorVersion < 14) {
            throw GradleException("JDK 14+ is required to create the MSI package.")
        }
        if (!layout.buildDirectory.file("edumips64-${version}.jar").get().asFile.exists()) {
            throw GradleException("Could not find out/edumips64-${version}.jar. Please execute ./gradlew jar before trying to build the MSI.")
        }
        
        if (System.getenv("WIX") == null){
            throw GradleException("Wix is required to create the MSI package.")
        }

        val buildDirName = layout.buildDirectory.get().asFile.name
        // jpackage's --input directory is copied in its entirety into the installer,
        // so we stage just the main JAR in a dedicated folder to avoid shipping the
        // nohelp JAR, WAR, etc. The MSI itself is written to the build directory root.
        val inputStaging = layout.buildDirectory.dir("tmp/msi-input").get().asFile
        inputStaging.deleteRecursively()
        inputStaging.mkdirs()
        layout.buildDirectory.file("edumips64-${version}.jar").get().asFile
            .copyTo(inputStaging.resolve("edumips64-${version}.jar"), overwrite = true)
        val destDir = "./${buildDirName}"
        println("Creating ${destDir}/EduMIPS64-${version}.msi.");
        val cmd = "jpackage.exe --main-jar edumips64-${version}.jar --input ./${buildDirName}/tmp/msi-input/ --dest ${destDir} --app-version ${version} --name EduMIPS64 --description \"Educational MIPS64 CPU Simulator\" --vendor \"EduMIPS64 Development Team\" --copyright \"Copyright ${LocalDateTime.now().year}, EduMIPS64 development Team\" --license-file ./LICENSE --win-shortcut --win-dir-chooser --win-menu --type msi --icon ./src/main/resources/images/ico.ico --win-per-user-install --java-options -Dfile.encoding=utf-8"
        commandLine(cmd.split(" "));
    }
}

/*
 * GWT tasks
 */
gwt {
    modules.add("org.edumips64.webclient")
    sourceLevel = "1.11"
}

// Redirect GWT outputs from the plugin's hard-coded "gwt/war", "gwt/extras" and
// "gwt/codeServer" subdirectories. GWT always writes its output into a
// subfolder named after the module's `rename-to` value (we use "web"), so we
// point it at a scratch staging directory and then copy the produced files
// into out/web/ via the assembleWebApp task below.
tasks.named("gwtCompile") {
    setProperty("outputDir", layout.buildDirectory.dir("tmp/gwt-war").get().asFile)
    setProperty("extraOutputDir", layout.buildDirectory.dir("tmp/gwt-extras").get().asFile)
}
tasks.withType<War>().configureEach {
    destinationDirectory.set(layout.buildDirectory)
}

// Copy the files produced by gwtCompile (scripts, resources) into out/web/,
// which is the canonical location for the assembled web application. Webpack
// also writes its bundle here, and copyWebHelp copies the HTML docs into
// out/web/docs/.
val assembleWebApp by tasks.registering(Copy::class) {
    group = "Web"
    description = "Assembles GWT compiler output into out/web/"
    dependsOn("gwtCompile")
    from(layout.buildDirectory.dir("tmp/gwt-war/web"))
    into(layout.buildDirectory.dir("web"))
}

val npmExecutable = if (System.getProperty("os.name").lowercase().contains("windows")) "npm.cmd" else "npm"

val npmBuild by tasks.registering(Exec::class) {
    group = "Web"
    description = "Builds the EduMIPS64 React frontend"
    workingDir = projectDir
    commandLine(npmExecutable, "run", "build")
    inputs.files(
        "package.json",
        "package-lock.json",
        "webpack.config.js"
    )
    inputs.dir("src/webapp")
    outputs.dir(layout.buildDirectory.dir("web"))
    mustRunAfter("war")
}

val copyWebHelp by tasks.registering {
    group = "Web"
    description = "Copies the Web-flavored HTML help into the web UI bundle"
    // Aggregate per-language copyWebHelp<Lang> tasks; each of them copies the
    // `html-web/` Sphinx build output for one language into out/web/docs/.
    copyWebHelpTaskNames.forEach { dependsOn(it) }
    mustRunAfter("war")
}

tasks.register("webapp") {
    group = "Web"
    description = "Builds the EduMIPS64 web application with bundled documentation"
    dependsOn("war", assembleWebApp, npmBuild, copyWebHelp)

    // Transitional CI compatibility: `pull_request_target` loads reusable
    // workflows (including build-web.yml) from the base branch, so while this
    // PR is open the master workflow is still uploading the web artifact from
    // the legacy `build/gwt/war/edumips64/` path. Mirror the assembled
    // out/web/ tree there so the artifact upload finds the files. Once this
    // PR is merged and master's build-web.yml uploads from `out/web`, this
    // shim can be removed.
    doLast {
        val legacyPath = projectDir.resolve("build/gwt/war/edumips64")
        legacyPath.mkdirs()
        copy {
            from(layout.buildDirectory.dir("web"))
            into(legacyPath)
        }
    }
}
