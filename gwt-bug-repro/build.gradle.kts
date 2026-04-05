plugins {
    java
}

repositories {
    mavenCentral()
}

// The bug is triggered by a version mismatch: gwt-user 2.12.2 + gwt-dev 2.13.0.
//
// In the EduMIPS64 project, this mismatch occurs because the "us.ascendtech.gwt.classic"
// Gradle plugin pulls in gwt-servlet:2.12.2 as a transitive dependency, which forces
// gwt-user down to 2.12.2 via Gradle dependency conflict resolution — even when
// gwt-user:2.13.0 is explicitly requested.
//
// The root cause: gwt-dev 2.13.0 removed the class
//   com.google.gwt.dev.util.log.speedtracer.SpeedTracerLogger$EventType
// but gwt-user 2.12.2 (specifically its JUnit/GWTTestCase infrastructure) still
// references it, causing a NoClassDefFoundError at test compilation time.
//
// To reproduce, we set the versions explicitly to create the mismatch:
val gwtUserVersion = "2.12.2"
val gwtDevVersion = "2.13.0"

dependencies {
    testImplementation("org.gwtproject:gwt-user:$gwtUserVersion")
    testImplementation("org.gwtproject:gwt-dev:$gwtDevVersion")

    // JUnit 4 for GWTTestCase
    testImplementation("junit:junit:4.13.2")
}

tasks.withType<Test> {
    // GWTTestCase extends JUnit 3/4 TestCase
    useJUnit()

    // GWTTestCase requires source files on the classpath
    classpath += files(project.sourceSets.main.get().java.srcDirs)
    classpath += files(project.sourceSets.test.get().java.srcDirs)

    // Increase memory for GWT compilation
    maxHeapSize = "1024m"

    // Pass system property for GWT
    systemProperty("gwt.args", "-war " + project.layout.buildDirectory.dir("gwt-tests").get().asFile.absolutePath)

    testLogging {
        events("passed", "skipped", "failed")
        showStandardStreams = true
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}
