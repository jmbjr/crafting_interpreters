## install JDK
- JDK LTS 21 Adoptium Temurin: https://adoptium.net/temurin/releases?version=21&os=any&arch=any 
  - or upgrade to latest LTS when they are stable
- OK to modify `PATH` but recommend disabling the other options (associate with .jar, JAVA_HOME, and JavaSoft)
## install gradle'
- gradle website: https://gradle.org/install/
  - link to stable v8.x: https://gradle.org/releases/#8.14.3
  - `curl -O https://services.gradle.org/distributions/gradle-8.14.3-bin.zip gradle-8.14.3-bin.zip`
- `mkdir my-java-template`
- `cd my-java-template`
- `/path/to/gradle/bin/gradle init`
  - Gradle init options:
  - `Type: application`
  - `Language: Java`
  - `Version: 21`
  - `Project Name: <name>`
  - `Application Structure: Single Application`
  - `Build script: Kotlin`
  - `Test framework: JUnit Jupiter (JUnit 5)`
  - `Generate build using new APIs and behavior: no`
## setup gradle wrapper stuff
- `./gradlew tasks`
## run test app
- `./gradlew :app:run`
```
$ ./gradlew :app:run
Calculating task graph as no cached configuration is available for tasks: :app:run

> Task :app:run
Hello World!

BUILD SUCCESSFUL in 3s
2 actionable tasks: 1 executed, 1 up-to-date
Configuration cache entry stored.
```

- `./gradlew :app:test`
```
$ ./gradlew :app:test
Calculating task graph as no cached configuration is available for tasks: :app:test

BUILD SUCCESSFUL in 5s
3 actionable tasks: 2 executed, 1 up-to-date
Configuration cache entry stored.
```

- `./gradlew :app:build`
```
$ ./gradlew :app:build
Calculating task graph as no cached configuration is available for tasks: :app:build

BUILD SUCCESSFUL in 2s
7 actionable tasks: 4 executed, 3 up-to-date
Configuration cache entry stored.
```

- `.gradlew :app:clean`
```
$ ./gradlew :app:clean
Calculating task graph as no cached configuration is available for tasks: :app:clean

BUILD SUCCESSFUL in 1s
1 actionable task: 1 executed
Configuration cache entry stored.
```
## set up vscode java extensions
- Java extension pack
- Gradle for Java
- FWCD Kotlin

## set up java language server if needed.
- ctrl + shift + P and search for "Open User Settings (JSON)"
- add: `"java.jdt.ls.java.home": "C:\\Program Files\\Eclipse Adoptium\\jdk-17.0.16.8-hotspot"    `
- NOTE: you should point to a jdk installed on your system.  seems like you can use 17 for a version 21 project.

## to rebuild Expr.java create `.vscode/launch.json` with the code:
```
{
  "version": "0.2.0",
  "configurations": [
    {
      "type": "java",
      "name": "Debug GenerateAst",
      "request": "launch",
      "mainClass": "com.craftinginterpreters.tool.GenerateAst",
      "args": ["app/src/main/java/com/craftinginterpreters/lox/"],
      "cwd": "${workspaceFolder}"
    }
  ]
}
```