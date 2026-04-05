# GWT Bug: `gwt-user:2.12.2` + `gwt-dev:2.13.0` version mismatch causes `InternalCompilerException`

## Summary

When `gwt-user` resolves to version **2.12.2** but `gwt-dev` is at version **2.13.0**, all
`GWTTestCase`-based tests fail with an `InternalCompilerException` during GWT's
Java-to-JavaScript compilation phase.

## Root Cause

`gwt-dev:2.13.0` removed the class:
```
com.google.gwt.dev.util.log.speedtracer.SpeedTracerLogger$EventType
```
but `gwt-user:2.12.2`'s JUnit / `GWTTestCase` infrastructure still references it.  This
causes a `NoClassDefFoundError` at test-compilation time, which is then wrapped in an
`InternalCompilerException`.

### How EduMIPS64 triggers this

The EduMIPS64 project uses the `us.ascendtech.gwt.classic` Gradle plugin (version 0.13.0),
which pulls in `gwt-servlet:2.12.2` as a transitive dependency.  When Renovate upgraded
only `gwt-dev` to 2.13.0, Gradle's conflict-resolution strategy kept `gwt-user` at 2.12.2
(forced by the lower `gwt-servlet:2.12.2`), creating the mismatch silently.

## Reproducing

```bash
cd gwt-bug-repro
./gradlew clean test --tests "SimpleGWTTest"
```

This should fail with:
```
[ERROR] An internal compiler exception occurred
    com.google.gwt.dev.jjs.InternalCompilerException: Unexpected error during visit.
        ...
    Caused by: java.lang.NoClassDefFoundError:
        com/google/gwt/dev/util/log/speedtracer/SpeedTracerLogger$EventType
```

### To verify the fix

Edit `build.gradle.kts` and change **both** versions to 2.12.2 (or both to 2.13.0):

```bash
# Match both versions → tests pass
sed -i 's/val gwtUserVersion = "2.12.2"/val gwtUserVersion = "2.13.0"/' build.gradle.kts
./gradlew clean test --tests "SimpleGWTTest"
```

## Environment

- **gwt-user:** 2.12.2
- **gwt-dev:** 2.13.0
- **Java:** 17+
- **Build tool:** Gradle (wrapper included)

## Error Log (abbreviated)

```
[ERROR] An internal compiler exception occurred
    com.google.gwt.dev.jjs.InternalCompilerException: Unexpected error during visit.
      at com.google.gwt.dev.jjs.ast.JVisitor.translateException(JVisitor.java:111)
      at com.google.gwt.dev.jjs.ast.JModVisitor.accept(JModVisitor.java:276)
      at com.google.gwt.dev.jjs.ast.JCastOperation.traverse(JCastOperation.java:76)
      ...
    Caused by: java.lang.NoClassDefFoundError:
      com/google/gwt/dev/util/log/speedtracer/SpeedTracerLogger$EventType
```

## Suggested Fix

`gwt-dev:2.13.0` should not remove classes that are still part of `gwt-user:2.12.x`'s
public API surface (or `gwt-user:2.13.0` should be required to match), so that mixed
version combinations produce a clear error rather than an internal compiler crash.
