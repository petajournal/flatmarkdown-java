package com.sosuisha.flatmarkdown;

import java.io.IOException;
import java.io.InputStream;
import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SymbolLookup;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

/**
 * Java bindings for the
 * <a href="https://github.com/petajournal/flatmarkdown">Flatmarkdown</a>
 * native library, providing Markdown-to-HTML and Markdown-to-AST conversion
 * via the Foreign Function &amp; Memory API.
 *
 * @see <a href="https://github.com/petajournal/flatmarkdown">Flatmarkdown on
 *      GitHub</a>
 */
public class FlatmarkdownJava {

    private static final MethodHandle MARKDOWN_TO_HTML;
    private static final MethodHandle MARKDOWN_TO_AST;
    private static final MethodHandle FREE_STRING;

    static {
        loadNativeLibrary();
        SymbolLookup lookup = SymbolLookup.loaderLookup();
        Linker linker = Linker.nativeLinker();

        // const char* -> char* (returns heap-allocated string)
        FunctionDescriptor strToStr = FunctionDescriptor.of(
                ValueLayout.ADDRESS, ValueLayout.ADDRESS);

        // char* -> void (frees heap-allocated string)
        FunctionDescriptor freeStr = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS);

        MARKDOWN_TO_HTML = linker.downcallHandle(
                lookup.find("markdown_to_html").orElseThrow(), strToStr);
        MARKDOWN_TO_AST = linker.downcallHandle(
                lookup.find("markdown_to_ast").orElseThrow(), strToStr);
        FREE_STRING = linker.downcallHandle(
                lookup.find("free_string").orElseThrow(), freeStr);
    }

    /**
     * Converts a Markdown string to HTML.
     *
     * @param input the Markdown text to convert; must not be {@code null}
     * @return the resulting HTML string
     * @throws RuntimeException     if the native call fails
     * @throws NullPointerException if {@code input} is {@code null}
     */
    public static String markdownToHtml(String input) {
        Objects.requireNonNull(input);

        try (Arena arena = Arena.ofConfined()) {
            MemorySegment inputSeg = arena.allocateFrom(input);
            MemorySegment resultPtr = (MemorySegment) MARKDOWN_TO_HTML.invokeExact(inputSeg);
            String result = resultPtr.reinterpret(Long.MAX_VALUE).getString(0);
            FREE_STRING.invokeExact(resultPtr);
            return result;
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Converts a Markdown string to an AST (Abstract Syntax Tree) in JSON format.
     *
     * <p>
     * For details on the AST specification, see the
     * <a href="https://github.com/petajournal/flatmarkdown">Flatmarkdown</a>
     * documentation.
     * </p>
     *
     * @param input the Markdown text to parse; must not be {@code null}
     * @return the AST as a JSON string
     * @throws RuntimeException     if the native call fails
     * @throws NullPointerException if {@code input} is {@code null}
     */
    public static String markdownToAst(String input) {
        Objects.requireNonNull(input);

        try (Arena arena = Arena.ofConfined()) {
            MemorySegment inputSeg = arena.allocateFrom(input);
            MemorySegment resultPtr = (MemorySegment) MARKDOWN_TO_AST.invokeExact(inputSeg);
            String result = resultPtr.reinterpret(Long.MAX_VALUE).getString(0);
            FREE_STRING.invokeExact(resultPtr);
            return result;
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    private static void loadNativeLibrary() {
        String os = System.getProperty("os.name", "").toLowerCase();
        String arch = System.getProperty("os.arch", "").toLowerCase();

        String resourceDir;
        String libName;
        if (os.contains("win") && arch.contains("amd64")) {
            resourceDir = "win32-x86-64";
            libName = "flatmarkdown_java.dll";
        } else if (os.contains("linux") && arch.contains("amd64")) {
            resourceDir = "linux-x86-64";
            libName = "libflatmarkdown_java.so";
        } else if (os.contains("mac") && arch.contains("aarch64")) {
            resourceDir = "darwin-aarch64";
            libName = "libflatmarkdown_java.dylib";
        } else {
            throw new UnsupportedOperationException(
                    "Unsupported platform: " + os + "/" + arch);
        }

        String resourcePath = "/" + resourceDir + "/" + libName;
        try (InputStream in = FlatmarkdownJava.class.getResourceAsStream(resourcePath)) {
            if (in == null) {
                throw new RuntimeException("Native library not found in JAR: " + resourcePath);
            }
            Path tempFile = Files.createTempFile("flatmarkdown_java", libName);
            tempFile.toFile().deleteOnExit();
            Files.copy(in, tempFile, StandardCopyOption.REPLACE_EXISTING);
            System.load(tempFile.toAbsolutePath().toString());
        } catch (IOException e) {
            throw new RuntimeException("Failed to load native library", e);
        }
    }
}
