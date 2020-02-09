/*
 * EduMIPS64 Gradle build configuration
 */
import java.time.LocalDateTime

plugins {
    java
    // The Eclipse plugin adds the "eclipse" task, which generates
    // files needed for Visual Studio Code and other IDEs.
    id ("eclipse")
    id ("application")
    id ("jacoco")
    id ("com.dorongold.task-tree") version "1.3.1"
    id ("us.ascendtech.gwt.classic") version "0.4.20"
}

repositories {
    jcenter()
}

dependencies {
    compileOnly("com.google.gwt:gwt-user:2.8.2")
    implementation("javax.help:javahelp:2.0.05")
    testImplementation("junit:junit:4.12")
}

application {
  mainClassName = "org.edumips64.Main"  
}
val codename = "Phlegmatic"
rootProject.version = "1.2.6"

// Specify Java source/target version.
tasks.compileJava {
    sourceCompatibility = "1.8"
    targetCompatibility = "1.8"
}

/* 
 * Documentation tasks
 */
tasks.create<Exec>("htmlDocsEn"){
    workingDir = File("${projectDir}/docs/user/en/src")
    commandLine("make", "html", "BUILDDIR=${buildDir}/docs/en", "SPHINXOPTS=-N -a -E")
}

tasks.create<Exec>("htmlDocsIt") {
    workingDir = File("${projectDir}/docs/user/it/src")
    commandLine("make", "html", "BUILDDIR=${buildDir}/docs/it", "SPHINXOPTS=-N -a -E")
}

tasks.create<Exec>("pdfDocsEn") {
    workingDir = File("${projectDir}/docs/user/en/src")
    commandLine("make", "pdf", "BUILDDIR=${buildDir}/docs/en", "SPHINXOPTS=-N -a -E")
}

tasks.create<Exec>("pdfDocsIt") {
    workingDir = File("${projectDir}/docs/user/it/src")
    commandLine("make", "pdf", "BUILDDIR=${buildDir}/docs/it", "SPHINXOPTS=-N -a -E")
}

// Catch-all task for documentation
tasks.create<GradleBuild>("allDocs") {
    tasks = listOf("htmlDocsIt", "htmlDocsEn", "pdfDocsEn", "pdfDocsIt")
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

tasks.create<Copy>("copyHelp") {
    from("docs/") {
        exclude("**/src/**", "**/design/**", "**/*.py", "**/*.md", "**/__pycache__/**")
    }
    into ("${docsDir}")
    dependsOn("copyHelpEn")
    dependsOn("copyHelpIt")
}

/*
    Helper function to execute a command and return its output.
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

fun getGitRevisionId() : String {
  val branch = "git rev-parse --abbrev-ref HEAD".runCommand()
  val commitHash = "git rev-parse --verify --short HEAD".runCommand()
  return "edumips64:${branch}:${commitHash}"
}

val sharedManifest = the<JavaPluginConvention>().manifest {
    attributes["Signature-Version"] = version
    attributes["Codename"] = codename
    attributes["Build-Date"] = LocalDateTime.now()
    attributes["Git-Revision"] = getGitRevisionId()
}

// "Slim / nodeps" jar
tasks.jar {
    //classifier = "nodeps"
    manifest {
        attributes["Main-Class"] = application.mainClassName
        attributes["SplashScreen-Image"] = "images/splash.png"
        from(sharedManifest)       
    }
}

// Cli jar
tasks.create<Jar>("cliJar"){
    dependsOn("jar")
    classifier = "cli"
    from(sourceSets.main.get().output)
    manifest {
        attributes["Main-Class"] = "org.edumips64.MainCLI"
        from(sharedManifest)
    }
}

// "Fat" / standalone jar (includes all dependencies)
tasks.create<Jar>("standaloneJar"){
    dependsOn(configurations.runtimeClasspath)
    classifier = "standalone"
    from(sourceSets.main.get().output)
    from({
        configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) }
    })
    manifest {
        attributes["Main-Class"] = application.mainClassName
        from(sharedManifest)   
    }
    dependsOn("copyHelp")
}

tasks.assemble{
    dependsOn("standaloneJar") 
    dependsOn("cliJar") 
}

/*
 * Code coverage report tasks
 */
tasks.jacocoTestReport {
    reports {
        xml.isEnabled = true
        csv.isEnabled = false
        html.isEnabled = false
    }
}

tasks.check{
    dependsOn("jacocoTestReport")
}

/*
 * GWT tasks
 */
gwt {
    modules.add("org.edumips64.webclient") 
}

tasks.create<Copy>("copyStaticFiles") {
    from("src/main/java/org/edumips64/client"){
        include("edumips64.html")
        include("logo.png")
    }
    into("build/gwt/war")
}

tasks.war{
    dependsOn("copyStaticFiles")
}