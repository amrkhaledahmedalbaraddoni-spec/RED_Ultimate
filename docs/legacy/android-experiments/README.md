# Legacy Android experiment

This directory is retained as an historical record of the early RED Android sketches. It is not a
Gradle project and is intentionally excluded from the root build because it contains duplicate,
incomplete prototypes (including references to classes that never existed in a buildable module).

The buildable RED features were merged from `app-android` into:

```text
app/src/main/java/com/red
```

The production APK is the single root `:app` module. Nothing in this directory is required to build
or run the application.
