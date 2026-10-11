# flatmarkdown-java

Java bindings for [flatmarkdown](https://crates.io/crates/flatmarkdown) via Java FFM (Foreign Function & Memory) API.

The native library is bundled inside the JAR and loaded automatically at runtime.

## Requirements

- Java 25+
- Rust toolchain (for building the native library)

## API

```java
import com.sosuisha.flatmarkdown.FlatmarkdownJava;

// Split a Flat Markdown page into items with metadata
String page = FlatmarkdownJava.pageToAst("<!-- @@ 2026-09-27-15-00-16-W3N8ASY -->\n- [ ] Task1\n\nParagraph");
// => {"type":"page","front_matter":null,"items":[{"level":2,"id":"2026-09-27-15-00-16-W3N8ASY","properties":{},"body":"- [ ] Task1","ast":{...}},{"level":1,"id":null,...}]}

// Convert one item body
String html = FlatmarkdownJava.bodyToHtml("# Hello");
// => "<h1>Hello</h1>\n"

String ast = FlatmarkdownJava.bodyToAst("# Hello");
// => {"type":"document","children":[{"type":"heading","level":1,...}]}
```

`markdownToHtml` and `markdownToAst` still work but are deprecated. Use `bodyToHtml` and `bodyToAst` instead.

The AST format is described in the [flatmarkdown AST specification](https://github.com/petajournal/flatmarkdown/blob/main/SPEC_AST.md).

## Build

```sh
# Build native library
cargo build --release

# Copy DLL to Maven resources
mkdir -p src/main/resources/win32-x86-64
cp target/release/flatmarkdown_java.dll src/main/resources/win32-x86-64/
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

To use a -SNAPSHOT artifact, add the snapshots repository.
```xml
<repositories>
  <repository>
    <name>Central Portal Snapshots</name>
    <id>central-portal-snapshots</id>
    <url>https://central.sonatype.com/repository/maven-snapshots/</url>
    <releases>
      <enabled>false</enabled>
    </releases>
    <snapshots>
      <enabled>true</enabled>
    </snapshots>
  </repository>
</repositories>
```

The consuming application must pass `--enable-native-access=ALL-UNNAMED` as a JVM argument.
