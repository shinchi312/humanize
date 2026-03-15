# IntelliJ Setup on Hardened System (No sudo)

Use this when `javac` is unavailable in your shell but you can still work inside user space.

## 1) Use a user-local JDK

1. Download JDK 21 archive (`.tar.gz`) from a vendor you trust.
2. Extract into a folder you own, for example:
`~/jdks/jdk-21`

## 2) Configure IntelliJ Project SDK

1. Open: `File -> Project Structure -> SDKs`.
2. Click `+` and add JDK path:
`/home/<your-user>/jdks/jdk-21`
3. Open: `Project -> Project SDK` and choose that JDK.
4. Open: `Modules` and ensure module SDK uses `Project SDK`.

## 3) Configure Maven Runner in IntelliJ

1. Open: `Settings -> Build, Execution, Deployment -> Build Tools -> Maven`.
2. In `Runner`, set `JRE` to `Project SDK`.
3. In `Maven home path`, keep `Bundled` or your preferred Maven.
4. Import/reload Maven project.

## 4) Build using wrapper from terminal (optional)

```bash
cd backend
./scripts/with-local-jdk.sh -DskipTests clean compile
```

## 5) Quick validation

Run in IntelliJ Terminal:

```bash
cd backend
./scripts/with-local-jdk.sh -v
```

Expected:
1. Java version `21.x`
2. Maven wrapper is used from `./mvnw`

