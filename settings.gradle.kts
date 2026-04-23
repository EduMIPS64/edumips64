rootProject.name = "edumips64"

/*
 * EduMIPS64 is intentionally a single-project Gradle build.
 *
 * Splitting into sub-projects (e.g. shared / docs / war / jar) to enable
 * cross-project parallel execution was proposed and assessed. The conclusion
 * was that the cost (large refactor of build.gradle.kts, CI workflows, the
 * out/ artifact layout, dev container, Snap, Electron and MSI pipelines)
 * outweighs the benefit: most heavy tasks (compileJava, gwtCompile, copyHelp,
 * jar) are on a serial critical path, and the main parallelizable chunk —
 * the Sphinx doc tasks for en/it/zh × html/pdf — all share the same managed
 * Python virtualenv and don't split cleanly across subprojects. CI is also
 * already parallelized at the workflow-job level.
 *
 * If this is ever revisited, `org.gradle.parallel=true` is already set in
 * gradle.properties and will take effect automatically once multiple
 * projects exist.
 */
