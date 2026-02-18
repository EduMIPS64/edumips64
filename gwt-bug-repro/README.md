# GWT 2.13.0 Bug Reproduction

This is a minimal reproduction case for the GWT 2.13.0 compiler bug that causes `InternalCompilerException` with double-cast operations in GWTTestCase tests.

## Bug Description

GWT 2.13.0 fails to compile GWTTestCase-based tests in certain scenarios with an `InternalCompilerException`. The error occurs in the GWT test infrastructure itself when the compiler generates a double-cast operation in `GWTRunner.java`.

**Note:** The bug manifests differently depending on the test execution mode:
- In **HtmlUnit mode** (default): Tests may pass but the bug exists in the compiler
- In **WebMode** (production mode): The bug causes compilation to fail
- In complex projects with multiple GWT modules: The bug is more likely to appear

This reproduction demonstrates the minimal setup. The bug was discovered in the EduMIPS64 project where it consistently causes `FluentJsonObjectTest` to fail.

## Environment

- **GWT version:** 2.13.0
- **Java version:** 17+
- **Build tool:** Gradle

## Steps to Reproduce

1. Build the project:
   ```bash
   cd gwt-bug-repro
   ./gradlew build
   ```

2. Run the test with GWT 2.13.0:
   ```bash
   ./gradlew test --tests "SimpleGWTTest"
   ```

3. To force WebMode compilation (more likely to trigger the bug):
   ```bash
   ./gradlew test --tests "SimpleGWTTest" -Dgwt.args="-prod"
   ```

## The Actual Bug (from EduMIPS64 project)

In the EduMIPS64 project, this bug consistently causes test failures:

```
[ERROR] com.google.gwt.dev.jjs.InternalCompilerException: Unexpected error during visit.
  at com.google.gwt.dev.jjs.ast.JCastOperation.traverse(JCastOperation.java:76)
```

The root cause is in `GWTRunner.java` line 163:
```java
junitHost = (JUnitHostAsync) (JUnitHostAsync) GWT.create(JUnitHost.class);
```

## Switching Between Versions

To test with GWT 2.12.2 (working version):
```bash
sed -i 's/2.13.0/2.12.2/g' build.gradle.kts
./gradlew clean test --tests "SimpleGWTTest"
```

To test with GWT 2.13.0 (broken version):
```bash
sed -i 's/2.12.2/2.13.0/g' build.gradle.kts
./gradlew clean test --tests "SimpleGWTTest"
```

## Full Reproduction

For the full reproduction that consistently demonstrates the bug, see the EduMIPS64 project:
- Repository: https://github.com/EduMIPS64/edumips64
- Branch: `copilot/sub-pr-1561`
- Test file: `src/test/java/org/edumips64/client/FluentJsonObjectTest.java`

To reproduce the bug there:
1. Clone the repository
2. Edit `build.gradle.kts` to set both `gwt-user` and `gwt-dev` to `2.13.0`
3. Run: `./gradlew test --tests "FluentJsonObjectTest"`
4. Observe the InternalCompilerException

## Related Issues

- GWT Milestone: 2.13.1 (planned fixes)
- Affects all GWTTestCase-based tests in certain configurations
- This is a regression from GWT 2.12.2 which works correctly
