plugins {
    java
}

repositories {
    mavenCentral()
}

dependencies {
    // GWT dependencies - change version here to test
    testImplementation("org.gwtproject:gwt-user:2.13.0")
    testImplementation("org.gwtproject:gwt-dev:2.13.0")
    
    // JUnit 4 for GWTTestCase
    testImplementation("junit:junit:4.13.2")
}

tasks.withType<Test> {
    // GWTTestCase requires JUnit 4, not JUnit Platform
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
