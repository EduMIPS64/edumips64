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

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}

/* 
 * Documentation tasks. To avoid dependency on GNU Make, these tasks duplicate the commands run by the Sphinx makefiles.
 */
fun buildDocsCmd(language: String, type: String) : String {
    val baseDir = "${layout.buildDirectory.get()}/docs/${language}"
    return "-m sphinx -N -a -E . ${baseDir}/${type} -b ${type} -d ${baseDir}/doctrees"
}

/*
 * Jar tasks
 */
val docsDir = layout.buildDirectory.dir("resources/main/docs")

// Generate documentation tasks for all languages
val languages = listOf("en", "it", "zh")
val docTypes = listOf("html", "pdf")
val allDocsTaskNames = mutableListOf<String>()
val copyHelpTaskNames = mutableListOf<String>()
val htmlTaskNames = mutableListOf<String>()

for (language in languages) {
    for (type in docTypes) {
        val taskName = "${type}Docs${language.replaceFirstChar { it.uppercase() }}"
        tasks.register<PythonTask>(taskName) {
            workDir = "${projectDir}/docs/user/${language}/src"
            command = buildDocsCmd(language, type)
        }
        allDocsTaskNames.add(taskName)

        if (type == "html") {
            htmlTaskNames.add(taskName)
        }
    }
    
    // Generate copy tasks for each language
    val copyTaskName = "copyHelp${language.replaceFirstChar { it.uppercase() }}"
    tasks.register<Copy>(copyTaskName) {
        from("${layout.buildDirectory.get()}/docs/${language}") {
            include("html/**")
            exclude("**/_sources/**")
        }
        into(docsDir.map { it.dir("user/$language") })
        dependsOn("htmlDocs${language.replaceFirstChar { it.uppercase() }}")
        mustRunAfter("compileJava")
    }
    copyHelpTaskNames.add(copyTaskName)
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

// Main JAR
tasks.jar {
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

// NoHelp JAR
tasks.register<Jar>("noHelpJar"){
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
        if (!layout.buildDirectory.file("libs/edumips64-${version}.jar").get().asFile.exists()) {
            throw GradleException("Could not find build/libs/edumips64-${version}.jar. Please execute ./gradlew jar before trying to build the MSI.")
        }
        
        if (System.getenv("WIX") == null){
            throw GradleException("Wix is required to create the MSI package.")
        }

        println("Creating EduMIPS64-${version}.msi.");
        val cmd = "jpackage.exe --main-jar edumips64-${version}.jar --input ./${layout.buildDirectory.get().asFile.name}/libs/ --app-version ${version} --name EduMIPS64 --description \"Educational MIPS64 CPU Simulator\" --vendor \"EduMIPS64 Development Team\" --copyright \"Copyright ${LocalDateTime.now().year}, EduMIPS64 development Team\" --license-file ./LICENSE --win-shortcut --win-dir-chooser --win-menu --type msi --icon ./src/main/resources/images/ico.ico --win-per-user-install --java-options -Dfile.encoding=utf-8"
        commandLine(cmd.split(" "));
    }
}

tasks.register<Exec>("dmg"){
    group = "Distribution"
    description = "Creates an installable DMG file for macOS"
    workingDir = projectDir

    doFirst{
        val os = System.getProperty("os.name") as String
        if (!os.contains("Mac", ignoreCase = true)){
            throw GradleException("DMG creation must be executed on macOS")
        }
        val majorVersion = System.getProperty("java.version").split(".")[0].toInt()

        if (majorVersion < 14) {
            throw GradleException("JDK 14+ is required to create the DMG package.")
        }
        if (!layout.buildDirectory.file("libs/edumips64-${version}.jar").get().asFile.exists()) {
            throw GradleException("Could not find build/libs/edumips64-${version}.jar. Please execute ./gradlew jar before trying to build the DMG.")
        }

        println("Creating EduMIPS64-${version}.dmg.")
        commandLine(
            "jpackage",
            "--main-jar", "edumips64-${version}.jar",
            "--input", "${layout.buildDirectory.get().asFile.name}/libs/",
            "--app-version", version,
            "--name", "EduMIPS64",
            "--description", "Educational MIPS64 CPU Simulator",
            "--vendor", "EduMIPS64 Development Team",
            "--copyright", "Copyright ${LocalDateTime.now().year}, EduMIPS64 Development Team",
            "--license-file", "LICENSE",
            "--type", "dmg",
            "--icon", "src/main/resources/images/ico.png",
            "--java-options", "-Dfile.encoding=utf-8"
        )
    }
}

tasks.register<Exec>("deb"){
    group = "Distribution"
    description = "Creates an installable DEB package for Linux"
    workingDir = projectDir

    doFirst{
        val os = System.getProperty("os.name") as String
        if (!os.contains("Linux", ignoreCase = true)){
            throw GradleException("DEB creation must be executed on Linux")
        }
        val majorVersion = System.getProperty("java.version").split(".")[0].toInt()

        if (majorVersion < 14) {
            throw GradleException("JDK 14+ is required to create the DEB package.")
        }
        if (!layout.buildDirectory.file("libs/edumips64-${version}.jar").get().asFile.exists()) {
            throw GradleException("Could not find build/libs/edumips64-${version}.jar. Please execute ./gradlew jar before trying to build the DEB.")
        }

        println("Creating edumips64-${version}.deb.")
        commandLine(
            "jpackage",
            "--main-jar", "edumips64-${version}.jar",
            "--input", "${layout.buildDirectory.get().asFile.name}/libs/",
            "--app-version", version,
            "--name", "EduMIPS64",
            "--description", "Educational MIPS64 CPU Simulator",
            "--vendor", "EduMIPS64 Development Team",
            "--copyright", "Copyright ${LocalDateTime.now().year}, EduMIPS64 Development Team",
            "--license-file", "LICENSE",
            "--type", "deb",
            "--icon", "src/main/resources/images/ico.png",
            "--linux-shortcut",
            "--java-options", "-Dfile.encoding=utf-8"
        )
    }
}

tasks.register<Exec>("rpm"){
    group = "Distribution"
    description = "Creates an installable RPM package for Linux"
    workingDir = projectDir

    doFirst{
        val os = System.getProperty("os.name") as String
        if (!os.contains("Linux", ignoreCase = true)){
            throw GradleException("RPM creation must be executed on Linux")
        }
        val majorVersion = System.getProperty("java.version").split(".")[0].toInt()

        if (majorVersion < 14) {
            throw GradleException("JDK 14+ is required to create the RPM package.")
        }
        if (!layout.buildDirectory.file("libs/edumips64-${version}.jar").get().asFile.exists()) {
            throw GradleException("Could not find build/libs/edumips64-${version}.jar. Please execute ./gradlew jar before trying to build the RPM.")
        }

        println("Creating edumips64-${version}.rpm.")
        commandLine(
            "jpackage",
            "--main-jar", "edumips64-${version}.jar",
            "--input", "${layout.buildDirectory.get().asFile.name}/libs/",
            "--app-version", version,
            "--name", "EduMIPS64",
            "--description", "Educational MIPS64 CPU Simulator",
            "--vendor", "EduMIPS64 Development Team",
            "--copyright", "Copyright ${LocalDateTime.now().year}, EduMIPS64 Development Team",
            "--license-file", "LICENSE",
            "--type", "rpm",
            "--icon", "src/main/resources/images/ico.png",
            "--linux-shortcut",
            "--java-options", "-Dfile.encoding=utf-8"
        )
    }
}

/*
 * GWT tasks
 */
gwt {
    modules.add("org.edumips64.webclient")
    sourceLevel = "1.11"
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
    outputs.dir(layout.buildDirectory.dir("gwt/war/edumips64"))
    mustRunAfter("war")
}

val copyWebHelp by tasks.registering(Copy::class) {
    group = "Web"
    description = "Copies generated HTML help into the web UI bundle"
    dependsOn("htmlDocs")
    from(layout.buildDirectory.dir("docs")) {
        exclude("**/doctrees/**", "**/.buildinfo", "**/objects.inv", "**/_sources/**")
    }
    into(layout.buildDirectory.dir("gwt/war/edumips64/docs"))
    mustRunAfter("war")
}

tasks.register("webapp") {
    group = "Web"
    description = "Builds the EduMIPS64 web application with bundled documentation"
    dependsOn("war", "htmlDocs", npmBuild, copyWebHelp)
}
