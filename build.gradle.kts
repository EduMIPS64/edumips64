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
    id ("com.dorongold.task-tree") version "1.5"
    id ("us.ascendtech.gwt.classic") version "0.5.1"
}

repositories {
    jcenter()
}

dependencies {
    compileOnly("com.google.gwt:gwt-user:2.9.0")
    compileOnly("com.google.gwt:gwt-dev:2.9.0")
    compileOnly("com.google.elemental2:elemental2-dom:1.1.0")
    implementation("javax.help:javahelp:2.0.05")
    implementation("com.vertispan.rpc:workers:1.0-alpha-5")
    implementation("info.picocli:picocli:4.5.1")
    testImplementation("junit:junit:4.13")
}

application {
  mainClassName = "org.edumips64.Main"  
}
val codename: String by project
val version: String by project

// Specify Java source/target version.
tasks.compileJava {
    sourceCompatibility = "11"
    targetCompatibility = "11"
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
        exclude("**/src/**", "**/design/**", "**/*.py",  "**/*.pyc", 
            "**/*.md", "**/.buildinfo", "**/objects.inv", "**/*.txt", "**/__pycache__/**")
    }
    into ("${docsDir}")
    dependsOn("copyHelpEn")
    dependsOn("copyHelpIt")
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
    dependsOn(configurations.runtimeClasspath)
    from(sourceSets.main.get().output)
    from({
        configurations.runtimeClasspath.get().filter { it.name.contains("javahelp") && it.name.endsWith("jar") }.map { zipTree(it) }
        configurations.runtimeClasspath.get().filter { it.name.contains("picocli") && it.name.endsWith("jar") }.map { zipTree(it) }
    })
    manifest {
        attributes["Main-Class"] = application.mainClassName
        from(sharedManifest)
    }
    dependsOn("copyHelp")
}

tasks.assemble{
    dependsOn("jar") 
}

// NoHelp JAR
tasks.create<Jar>("noHelpJar"){
    classifier = "nohelp"
    dependsOn(configurations.runtimeClasspath)
    from(sourceSets.main.get().output)
    from({
        configurations.runtimeClasspath.get().filter { it.name.contains("picocli") && it.name.endsWith("jar") }.map { zipTree(it) }
    })
    manifest {
        attributes["Main-Class"] = application.mainClassName
        from(sharedManifest)
    }
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

tasks.register("release") {
    group = "Release"
    description = "Creates all artifacts for a given EduMIPS64 release"
    dependsOn("allDocs")
    dependsOn("jar")

    doFirst {
        println("Creating artifacts for version $version")
    }
}

tasks.create<Exec>("exeCreator"){
    group = "Distribution"
    description = "Creates an installable exe file"
    workingDir = File("${projectDir}")
    dependsOn("jar")

    doFirst{
    	//check if user is on Windows
        var os = System.getProperty("os.name") as String;
        if(!("Windows" in os)){
            throw GradleException("This tasks shall run on Windows")
        }

        println("Creating edumips64-"+version+".jar File");
        
        //check if jdk 14+ is installed
        if(System.getProperty("java.version").toInt()<14) {
            throw GradleException("JDK version installed must be 14+")
        }
        else{
            println("Correct Jdk version Installed!")
        }
        
        //check if Wix is installed
        if(System.getenv("WIX")==null){
            throw GradleException("Please Install Wix")
        }
        else{
            println("Wix Installed!")
        }

	//create script file that creates the executable
        var script = File("./scripts/exeCreator.bat")
        script.createNewFile()
        var version_arr = version.split('.').toMutableList();
        if(version_arr.size>3 && version_arr[3].toInt()>0) {
            version_arr[2] = (version_arr[2].toInt()+ 1).toString()
        }
        var jpackage_version = version_arr[0]+'.'+version_arr[1]+'.'+version_arr[2]
        println("Running JPackage!")
        script.writeText("jpackage.exe --main-jar edumips64-"+version+".jar --input ./build/libs/ --app-version "+jpackage_version+" --name EduMIPS64 --description \"Educational MIPS64 CPU Simulator\" --vendor \"EduMIPS64 Development Team\" --copyright \"Copyright "+LocalDateTime.now().year+", EduMIPS64 development Team\" --license-file ./LICENSE --win-shortcut --win-dir-chooser --win-menu --type msi --icon ./src/main/resources/images/ico.ico --win-per-user-install")
        commandLine("./scripts/exeCreator.bat");
    }
}

/*
 * GWT tasks
 */
gwt {
    modules.add("org.edumips64.webclient") 
    sourceLevel = "1.11"
}
