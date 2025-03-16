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
    id ("com.dorongold.task-tree") version "4.0.0"
    id ("us.ascendtech.gwt.classic") version "0.9.2"
    id ("ru.vyarus.use-python") version "4.0.0"
}

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("org.gwtproject:gwt-user:2.12.2")
    compileOnly("org.gwtproject:gwt-dev:2.12.2")
    compileOnly("com.google.elemental2:elemental2-dom:1.2.3")
    compileOnly("com.vertispan.rpc:workers:1.0-alpha-8")
    
    implementation("com.formdev:flatlaf:3.5.4")
    implementation("javax.help:javahelp:2.0.05")
    implementation("info.picocli:picocli:4.7.6")

    testImplementation(platform("org.junit:junit-bom:5.12.1"))
	testImplementation("org.junit.jupiter:junit-jupiter")

    // To run JUnit 4 tests.
    testImplementation("junit:junit:4.13.2")    
    testRuntimeOnly("org.junit.vintage:junit-vintage-engine")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

python {
    scope = VIRTUALENV
    requirements.file = "docs/requirements.txt"
    minPythonVersion = "3.8"
}

application {
  mainClass.set("org.edumips64.Main")
}
val codename: String by project
val version: String by project




// Specify Java source/target version and source encoding.
tasks.compileJava {
    sourceCompatibility = "17"
    targetCompatibility = "17"
    options.encoding = "UTF-8"
}

/* 
 * Documentation tasks. To avoid dependency on GNU Make, these tasks duplicate the commands run by the Sphinx makefiles.
 */
fun buildDocsCmd(language: String, type: String) : String {
    val baseDir = "${buildDir}/docs/${language}"
    return "-m sphinx -N -a -E . ${baseDir}/${type} -b ${type} -d ${baseDir}/doctrees"
}

tasks.register<PythonTask>("htmlDocsEn") {
    workDir = "${projectDir}/docs/user/en/src"
    command = buildDocsCmd("en", "html")
}

tasks.register<PythonTask>("htmlDocsIt") {
    workDir = "${projectDir}/docs/user/it/src"
    command = buildDocsCmd("it", "html")
}
tasks.register<PythonTask>("htmlDocsZh") {
    workDir = "${projectDir}/docs/user/zh/src"
    command = buildDocsCmd("zh", "html")
}

tasks.register<PythonTask>("pdfDocsEn") {
    workDir = "${projectDir}/docs/user/en/src"
    command = buildDocsCmd("en", "pdf")
}

tasks.register<PythonTask>("pdfDocsIt") {
    workDir = "${projectDir}/docs/user/it/src"
    command = buildDocsCmd("it", "pdf")
}

tasks.register<PythonTask>("pdfDocsZh") {
    workDir = "${projectDir}/docs/user/zh/src"
    command = buildDocsCmd("zh", "pdf")
}


// Catch-all task for documentation
tasks.create<GradleBuild>("allDocs") {
    tasks = listOf("htmlDocsIt","htmlDocsZh", "htmlDocsEn", "pdfDocsEn", "pdfDocsZh", "pdfDocsIt")
    description = "Run all documentation tasks"
}

/*
 * Jar tasks
 */
val docsDir = "build/classes/java/main/docs"
// Include the docs folder at the root of the jar, for JavaHelp
tasks.create<Copy>("copyHelpEn") {
    from("${buildDir}/docs/en") {
        include("html/**")
        exclude("**/_sources/**")
    }
    into ("${docsDir}/user/en")
    dependsOn("htmlDocsEn")
}

tasks.create<Copy>("copyHelpIt") {
    from("${buildDir}/docs/it") {
        include("html/**")
        exclude("**/_sources/**")
    }
    into ("${docsDir}/user/it")
    dependsOn("htmlDocsIt")
}
tasks.create<Copy>("copyHelpZh") {
    from("${buildDir}/docs/zh") {
        include("html/**")
        exclude("**/_sources/**")
    }
    into ("${docsDir}/user/zh")
    dependsOn("htmlDocsZh")
}

tasks.create<Copy>("copyHelp") {
    from("docs/") {
        exclude("**/src/**", "**/design/**", "**/*.py",  "**/*.pyc", 
            "**/*.md", "**/.buildinfo", "**/objects.inv", "**/*.txt", "**/__pycache__/**")
    }
    into ("${docsDir}")
    dependsOn("copyHelpEn")
    dependsOn("copyHelpIt")
    dependsOn("copyHelpZh")
}

/*
 * Helper function to execute a command and return its output.
 */
fun String.runCommand(workingDir: File = file("./")): String {
    val parts = this.split("\\s".toRegex())
    val proc = ProcessBuilder(*parts.toTypedArray())
            .directory(workingDir)
            .redirectOutput(ProcessBuilder.Redirect.PIPE)
            .redirectError(ProcessBuilder.Redirect.PIPE)
            .start()

    proc.waitFor(1, TimeUnit.MINUTES)
    return proc.inputStream.bufferedReader().readText().trim()
}

fun getSourceControlMetadata() : Triple<String, String, String> {
    val branch: String
    val commitHash: String
    val qualifier: String
    if(System.getenv("GITHUB_ACTIONS").isNullOrEmpty()) {
        println("Running locally")
        branch = "git rev-parse --abbrev-ref HEAD".runCommand()
        commitHash = "git rev-parse --verify --short HEAD".runCommand()
        qualifier = ""
    } else {
        println("Running under GitHub Actions")
        branch = System.getenv("GITHUB_REF")
        commitHash = System.getenv("GITHUB_SHA").substring(0, 7)
        qualifier = "alpha"
    }
    return Triple(branch, commitHash, qualifier)
}

val sharedManifest = the<JavaPluginConvention>().manifest {
    attributes["Signature-Version"] = version
    attributes["Codename"] = codename
    attributes["Build-Date"] = LocalDateTime.now()

    val (branch, gitRevision, qualifier) = getSourceControlMetadata()
    attributes["Full-Buildstring"] = "$branch@$gitRevision"
    attributes["Git-Revision"] = gitRevision
    attributes["Build-Qualifier"] = qualifier
}

// Main JAR
tasks.jar {
    from(sourceSets.main.get().output)
    from({
        configurations.runtimeClasspath.get().filter { (it.name.contains("picocli") || it.name.contains("javahelp") || it.name.contains("flatlaf")) && it.name.endsWith("jar") }.map {  println("Adding dependency " + it.name); zipTree(it) }

    })
    manifest {
        attributes["Main-Class"] = application.mainClass.get()
        from(sharedManifest)
    }
    dependsOn("copyHelp")
}

tasks.assemble{
    dependsOn("jar") 
}

// NoHelp JAR
tasks.create<Jar>("noHelpJar"){
    archiveClassifier.set("nohelp")
    dependsOn(configurations.runtimeClasspath)
    from(sourceSets.main.get().output)
    from({
        configurations.runtimeClasspath.get().filter { it.name.contains("picocli") && it.name.endsWith("jar") || it.name.contains("flatlaf") }.map { println("Adding dependency " + it.name); zipTree(it) }
    })
    manifest {
        attributes["Main-Class"] = application.mainClass.get()
        from(sharedManifest)
    }
}

/*
 * Code coverage report tasks
 */
jacoco {
    toolVersion = "0.8.12"
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

tasks.create<Exec>("msi"){
    group = "Distribution"
    description = "Creates an installable MSI file"
    workingDir = File("${projectDir}")

    doFirst{
        var os = System.getProperty("os.name") as String;
        if (!("Windows" in os)){
            throw GradleException("MSI creation must be executed on Windows")
        }
        var majorVersion = System.getProperty("java.version").split(".")[0].toInt()
        
        if (majorVersion < 14) {
            throw GradleException("JDK 14+ is required to create the MSI package.")
        }
        if (!File("build/libs/edumips64-${version}.jar").exists()) {
            throw GradleException("Could not find build/libs/edumips64-${version}.jar. Please execute ./gradlew jar before trying to build the MSI.")
        }
        
        if (System.getenv("WIX") == null){
            throw GradleException("Wix is required to create the MSI package.")
        }

        println("Creating EduMIPS64-${version}.msi.");
        val cmd = "jpackage.exe --main-jar edumips64-${version}.jar --input ./build/libs/ --app-version ${version} --name EduMIPS64 --description \"Educational MIPS64 CPU Simulator\" --vendor \"EduMIPS64 Development Team\" --copyright \"Copyright ${LocalDateTime.now().year}, EduMIPS64 development Team\" --license-file ./LICENSE --win-shortcut --win-dir-chooser --win-menu --type msi --icon ./src/main/resources/images/ico.ico --win-per-user-install --java-options -Dfile.encoding=utf-8"
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
