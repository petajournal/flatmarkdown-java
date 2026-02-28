# flatmarkdown-java

Java bindings for [flatmarkdown](https://crates.io/crates/flatmarkdown) via Java FFM (Foreign Function & Memory) API.

The native library is bundled inside the JAR and loaded automatically at runtime.

## Requirements

- Java 25+
- Rust toolchain (for building the native library)

## API

```java
import com.sosuisha.flatmarkdown.FlatmarkdownJava;

String html = FlatmarkdownJava.markdownToHtml("# Hello");
// => "<h1>Hello</h1>\n"

String ast = FlatmarkdownJava.markdownToAst("# Hello");
// => {"type":"document","children":[{"type":"heading","level":1,...}]}
```

## Build

```sh
# Build native library
cargo build --release

# Copy DLL to Maven resources
mkdir -p src/main/resources/win32-x86-64
cp target/release/flatmarkdown_java.dll src/main/resources/win32-x86-64/

# Build and install JAR to local Maven repository
mvn install
```

Or simply run `bash build.sh`.

## Maven dependency

```xml
<dependency>
    <groupId>com.sosuisha</groupId>
    <artifactId>flatmarkdown</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

The consuming application must pass `--enable-native-access=ALL-UNNAMED` as a JVM argument.
