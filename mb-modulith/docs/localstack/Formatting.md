# Formatting

## Google Java Format

Formatting follows the [Google Java Style](https://google.github.io/styleguide/javaguide.html), supported by
[google-java-format](https://github.com/google/google-java-format)

For full IDE support in IntelliJ please install the `google-java-format` plugin and configure according to the
[installation instructions](https://github.com/google/google-java-format#intellij-android-studio-and-other-jetbrains-ides)

Note: This also includes downloading the
[Google Java Format IntelliJ Syle File](https://raw.githubusercontent.com/google/styleguide/gh-pages/intellij-java-google-style.xml)
and using it as Java Formatter (Preferences -> Editor -> Code Style -> Java -> Wheel besides Scheme -> Import)

## Spotless

[Spotless Gradle Plugin](https://github.com/diffplug/spotless/tree/main/plugin-gradle) is used
to enforce a consistent code style. It is executed as part of the `check` gradle task.
To manually run it use:

```shell script
./gradlew :spotlessCheck
```

If Spotless finds violations they can be fixed with

```shell script
./gradlew :spotlessApply
```
