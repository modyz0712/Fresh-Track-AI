# FreshTrack AI Build Toolchain

This repository is expected to stay aligned with the GitHub `dev` branch:

- Remote branch: `origin/dev`
- Android Gradle Plugin: `9.0.1`
- Gradle wrapper: `9.1.0`
- Kotlin Compose plugin: `2.0.21`
- KSP plugin: `2.3.4`
- Compile SDK: `36`
- Target SDK: `36`
- Min SDK: `26`
- Java language level for source/target compatibility: `11`
- Required Gradle runtime JDK: `17+`

## Verified Local Rule

Gradle must be launched with JDK `17+`.

On this workspace, the verified working runtime is Android Studio's embedded JBR:

`C:\Program Files\Android\Android Studio\jbr`

If Gradle is launched from a shell that defaults to Java 8, the build fails before project evaluation because AGP `9.0.1` requires JDK `17+`.

## Practical Guidance

- Android Studio: set Gradle JDK to the embedded JBR or another JDK `17+`.
- CLI on Windows PowerShell:

```powershell
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
.\gradlew.bat :app:assembleDebug
```

## Version Control Rule

Before changing toolchain versions, verify against GitHub `dev`:

```powershell
git fetch origin dev
git rev-parse HEAD
git rev-parse origin/dev
git status -sb
```

Only update AGP, Gradle, Kotlin, or KSP together when there is a deliberate branch-wide upgrade plan.
