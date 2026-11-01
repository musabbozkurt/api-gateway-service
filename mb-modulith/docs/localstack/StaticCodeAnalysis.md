# Static Code Analysis

## Error Prone

[Error Prone](https://errorprone.info/index) is used to prevent a class of possible bugs which can
be detected at compile time. It is integrated into the build process via
[Gradle Plugin](https://github.com/tbroyer/gradle-errorprone-plugin)

### IntelliJ Plugin

To add the plugin, start the IDE and find the Plugins dialog. Browse Repositories, choose Category:
Build, and find the Error-prone plugin. Right-click and choose “Download and install”. The IDE will
restart after you’ve exited these dialogs.

To enable Error Prone, choose `Settings | Compiler | Java Compiler | Use compiler`: Javac with error-prone
and also make sure `Settings | Compiler | Use external build` is NOT selected.

## PMD

[PMD](https://pmd.github.io/latest/) is a static code analysis tool focussed on setting a certain
code quality standard in projects.
It is integrated into the build with a [Gradle Plugin](https://docs.gradle.org/current/userguide/pmd_plugin.html).
The checks are executed on `check` stage of the Gradle build.

The rules are configured in [pmd.xml](../pmd.xml)

Full rules reference available on the [PMD Website](https://pmd.github.io/pmd-6.31.0/pmd_rules_java.html)

### IntelliJ

Install the plugin [pmd-idea](https://plugins.jetbrains.com/plugin/15412-pmd-idea) and point to [pmd.xml](../pmd.xml)
`CMD + Shift + A -> PMD` allows scanning the project after setting up.
