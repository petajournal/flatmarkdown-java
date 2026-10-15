#!/usr/bin/env bash
set -euo pipefail

# Pick the native library name and resource directory for the current platform.
# These must match the lookup in FlatmarkdownJava.loadNativeLibrary().
case "$(uname -s)-$(uname -m)" in
  Darwin-arm64)
    lib_file="libflatmarkdown_java.dylib"
    resource_dir="darwin-aarch64"
    ;;
  Linux-x86_64)
    lib_file="libflatmarkdown_java.so"
    resource_dir="linux-x86-64"
    ;;
  MINGW*-x86_64|MSYS*-x86_64|CYGWIN*-x86_64)
    lib_file="flatmarkdown_java.dll"
    resource_dir="win32-x86-64"
    ;;
  *)
    echo "Unsupported platform: $(uname -s)/$(uname -m)" >&2
    exit 1
    ;;
esac

# Build the Rust cdylib
echo "Building Rust cdylib..."
cargo build --release

# Copy the native library to Maven resources
echo "Copying ${lib_file} to src/main/resources/${resource_dir}/..."
mkdir -p "src/main/resources/${resource_dir}"
cp "target/release/${lib_file}" "src/main/resources/${resource_dir}/"

# Build Maven JAR
echo "Building Maven JAR..."
./mvnw clean package

echo "Done! JAR is at target/flatmarkdown-0.4.0-SNAPSHOT.jar"
