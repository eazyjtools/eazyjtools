package io.github.eazyjtools.parse.number;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Test suite for {@link NumberParser}.
 * <p>
 * These tests verify the public API contract of the parser methods for:
 * </p>
 * <ul>
 *   <li>{@code Integer}, {@code Long}, {@code Double}, {@link BigInteger}, {@link BigDecimal}</li>
 *   <li>{@code OrNull} variants (return {@code null} on invalid input)</li>
 *   <li>{@code OrDefault} variants (return provided default value on invalid input)</li>
 *   <li>Default trimming behavior (overloads without {@code trimInput} use {@code trimInput=true})</li>
 *   <li>Explicit {@code trimInput} flag behavior</li>
 *   <li>Invalid inputs: {@code null}, empty strings, whitespace-only strings, malformed numbers, overflows</li>
 *   <li>{@code Double} special rule: non-finite values ({@code NaN}, {@code Infinity}, {@code -Infinity})
 *       are treated as invalid</li>
 * </ul>
 * <p>
 * The utility-class constructor is also tested via reflection to ensure instantiation is prohibited.
 * </p>
 *
 * @author romanzdev
 * @since 1.0.0
 */
@DisplayName("NumberParser: safe numeric parsing (OrNull/OrDefault, trim, finiteness)")
public class NumberParserTests {

    /* ------------------------------------------------------------------------------------ */
    /* constructor                                                                          */
    /* ------------------------------------------------------------------------------------ */

    @Test
    @DisplayName("Constructor: utility class cannot be instantiated")
    void givenPrivateConstructor_whenInvokedViaReflection_thenThrowsUnsupportedOperationException() throws Exception {
        Constructor<NumberParser> ctor = NumberParser.class.getDeclaredConstructor();
        ctor.setAccessible(true);
        InvocationTargetException ex = assertThrows(InvocationTargetException.class, () -> {
            ctor.newInstance();
        });
        assertNotNull(ex.getCause());
        assertEquals(UnsupportedOperationException.class, ex.getCause().getClass());
        assertEquals("Utility class cannot be instantiated!", ex.getCause().getMessage());
    }

    /* ------------------------------------------------------------------------------------ */
    /* parseInt*                                                                            */
    /* ------------------------------------------------------------------------------------ */
    
    @Nested
    @DisplayName("parseInt(String)")
    class ParseIntOptionalTests {

        @ParameterizedTest(name = "[{index}] {0}: \"{1}\" -> present: {2}")
        @MethodSource("cases")
        @DisplayName("Int parsing (Optional): trim enabled by default; Optional.empty on invalid")
        void givenString_whenParseInt_thenOptionalMatchesExpectation(
                String caseName,
                String input,
                boolean expectedPresent,
                Integer expectedValue) {
            var result = NumberParser.parseInt(input);
            assertEquals(expectedPresent, result.isPresent());
            if (expectedPresent) {
                assertEquals(expectedValue, result.orElseThrow());
            } else {
                assertTrue(result.isEmpty());
            }
        }

        static Stream<Arguments> cases() {
            return Stream.of(
                    // null/blank -> empty
                    Arguments.of("null", null, false, null),
                    Arguments.of("empty", "", false, null),
                    Arguments.of("blank spaces", "   ", false, null),
                    Arguments.of("blank tabs/newlines", "\t\n\r", false, null),

                    // valid (trim enabled)
                    Arguments.of("zero", "0", true, 0),
                    Arguments.of("negative", "-42", true, -42),
                    Arguments.of("positive sign", "+42", true, 42),
                    Arguments.of("leading zeros", "00042", true, 42),
                    Arguments.of("int max", "2147483647", true, 2147483647),
                    Arguments.of("int min", "-2147483648", true, -2147483648),
                    Arguments.of("leading space (trim -> ok)", " 1", true, 1),
                    Arguments.of("trailing space (trim -> ok)", "1 ", true, 1),
                    Arguments.of("surrounded spaces (trim -> ok)", " 1 ", true, 1),

                    // invalid -> empty
                    Arguments.of("internal space", "1 2", false, null),
                    Arguments.of("decimal dot", "1.0", false, null),
                    Arguments.of("scientific notation", "1e3", false, null),
                    Arguments.of("underscore", "1_000", false, null),
                    Arguments.of("letters", "abc", false, null),
                    Arguments.of("hex prefix", "0x10", false, null),
                    Arguments.of("double sign", "--1", false, null),
                    Arguments.of("sign only plus", "+", false, null),
                    Arguments.of("sign only minus", "-", false, null),

                    // overflow -> empty
                    Arguments.of("overflow above max", "2147483648", false, null),
                    Arguments.of("overflow below min", "-2147483649", false, null),

                    // trim() does not remove NBSP -> should be invalid
                    Arguments.of("nbsp around number (trim does not remove)", "\u00A01\u00A0", false, null)
            );
        }
    }

    @Nested
    @DisplayName("parseIntOrNull(String)")
    class ParseIntOrNullDefaultTrimTests {

        @ParameterizedTest(name = "[{index}] {0}: \"{1}\" -> expected: {2}")
        @MethodSource("cases")
        @DisplayName("Int parsing (nullable): trim enabled by default")
        void givenString_whenParseIntOrNull_thenReturnsExpected(String caseName, String input, Integer expected) {
            Integer result = NumberParser.parseIntOrNull(input);
            assertEquals(expected, result);
        }

        static Stream<Arguments> cases() {
            return Stream.of(
                    // null/blank -> null
                    Arguments.of("null", null, null),
                    Arguments.of("empty", "", null),
                    Arguments.of("blank spaces", "   ", null),
                    Arguments.of("blank tabs/newlines", "\t\n\r", null),

                    // valid (trim enabled)
                    Arguments.of("zero", "0", 0),
                    Arguments.of("negative", "-42", -42),
                    Arguments.of("positive sign", "+42", 42),
                    Arguments.of("leading zeros", "00042", 42),
                    Arguments.of("int max", "2147483647", 2147483647),
                    Arguments.of("int min", "-2147483648", -2147483648),
                    Arguments.of("leading space (trim -> ok)", " 1", 1),
                    Arguments.of("trailing space (trim -> ok)", "1 ", 1),
                    Arguments.of("surrounded spaces (trim -> ok)", " 1 ", 1),

                    // invalid format -> null
                    Arguments.of("internal space", "1 2", null),
                    Arguments.of("decimal dot", "1.0", null),
                    Arguments.of("scientific notation", "1e3", null),
                    Arguments.of("underscore", "1_000", null),
                    Arguments.of("letters", "abc", null),
                    Arguments.of("hex prefix", "0x10", null),
                    Arguments.of("double sign", "--1", null),
                    Arguments.of("sign only plus", "+", null),
                    Arguments.of("sign only minus", "-", null),

                    // overflow -> null
                    Arguments.of("overflow above max", "2147483648", null),
                    Arguments.of("overflow below min", "-2147483649", null),

                    // trim() does not remove NBSP -> should still be invalid
                    Arguments.of("nbsp around number (trim does not remove)", "\u00A01\u00A0", null)
            );
        }
    }

    @Nested
    @DisplayName("parseIntOrNull(String, boolean)")
    class ParseIntOrNullTrimFlagTests {

        @ParameterizedTest(name = "[{index}] {0}: input=\"{1}\", trim={2} -> expected: {3}")
        @MethodSource("cases")
        @DisplayName("Int parsing (nullable): respects trimInput flag")
        void givenStringAndTrimFlag_whenParseIntOrNull_thenReturnsExpected(
                String caseName,
                String input,
                boolean trimInput,
                Integer expected) {
            Integer result = NumberParser.parseIntOrNull(input, trimInput);
            assertEquals(expected, result);
        }

        static Stream<Arguments> cases() {
            return Stream.of(
                    // null/empty
                    Arguments.of("null (trim=true)", null, true, null),
                    Arguments.of("null (trim=false)", null, false, null),
                    Arguments.of("empty (trim=true)", "", true, null),
                    Arguments.of("empty (trim=false)", "", false, null),
                    Arguments.of("blank spaces (trim=true)", "   ", true, null),
                    Arguments.of("blank spaces (trim=false)", "   ", false, null),

                    // whitespace around number
                    Arguments.of("leading space (trim=true)", " 7", true, 7),
                    Arguments.of("leading space (trim=false)", " 7", false, null),
                    Arguments.of("trailing space (trim=true)", "7 ", true, 7),
                    Arguments.of("trailing space (trim=false)", "7 ", false, null),
                    Arguments.of("surrounded spaces (trim=true)", " 7 ", true, 7),
                    Arguments.of("surrounded spaces (trim=false)", " 7 ", false, null),

                    // valid
                    Arguments.of("plus sign", "+9", true, 9),
                    Arguments.of("minus sign", "-9", true, -9),
                    Arguments.of("leading zeros", "0009", true, 9),

                    // invalid
                    Arguments.of("decimal", "7.0", true, null),
                    Arguments.of("internal space", "7 0", true, null),
                    Arguments.of("overflow", "2147483648", true, null)
            );
        }
    }

    @Nested
    @DisplayName("parseIntOrDefault(String, int)")
    class ParseIntOrDefaultDefaultTrimTests {

        @ParameterizedTest(name = "[{index}] {0}: \"{1}\" default={2} -> expected: {3}")
        @MethodSource("cases")
        @DisplayName("Int parsing (default): trim enabled by default")
        void givenString_whenParseIntOrDefault_thenReturnsExpected(
                String caseName,
                String input,
                int defaultValue,
                int expected) {
            int result = NumberParser.parseIntOrDefault(input, defaultValue);
            assertEquals(expected, result);
        }

        static Stream<Arguments> cases() {
            return Stream.of(
                    // null/blank -> default
                    Arguments.of("null", null, 99, 99),
                    Arguments.of("empty", "", 99, 99),
                    Arguments.of("blank", "   ", 99, 99),
                    Arguments.of("blank tabs/newlines", "\t\n\r", -5, -5),

                    // valid
                    Arguments.of("simple", "12", 99, 12),
                    Arguments.of("trimmed leading space", " 12", 99, 12),
                    Arguments.of("trimmed trailing space", "12 ", 99, 12),
                    Arguments.of("trimmed surrounded spaces", " 12 ", 99, 12),
                    Arguments.of("negative", "-12", 99, -12),
                    Arguments.of("plus sign", "+12", 99, 12),
                    Arguments.of("max", "2147483647", 99, 2147483647),
                    Arguments.of("min", "-2147483648", 99, -2147483648),

                    // invalid -> default
                    Arguments.of("overflow above max", "2147483648", 99, 99),
                    Arguments.of("decimal", "12.0", 99, 99),
                    Arguments.of("letters", "abc", 99, 99),
                    Arguments.of("internal space", "1 2", 99, 99)
            );
        }
    }

    @Nested
    @DisplayName("parseIntOrDefault(String, boolean, int)")
    class ParseIntOrDefaultTrimFlagTests {

        @ParameterizedTest(name = "[{index}] {0}: input=\"{1}\", trim={2}, default={3} -> expected: {4}")
        @MethodSource("cases")
        @DisplayName("Int parsing (default): respects trimInput flag")
        void givenStringAndTrimFlag_whenParseIntOrDefault_thenReturnsExpected(
                String caseName,
                String input,
                boolean trimInput,
                int defaultValue,
                int expected) {
            int result = NumberParser.parseIntOrDefault(input, trimInput, defaultValue);
            assertEquals(expected, result);
        }

        static Stream<Arguments> cases() {
            return Stream.of(
                    Arguments.of("null", null, true, 7, 7),
                    Arguments.of("empty", "", false, 7, 7),

                    Arguments.of("spaces around (trim=true)", " 8 ", true, 7, 8),
                    Arguments.of("spaces around (trim=false)", " 8 ", false, 7, 7),

                    Arguments.of("valid", "8", false, 7, 8),
                    Arguments.of("invalid", "8.0", true, 7, 7),
                    Arguments.of("overflow", "2147483648", true, 7, 7)
            );
        }
    }

    /* ------------------------------------------------------------------------------------ */
    /* parseLong*                                                                           */
    /* ------------------------------------------------------------------------------------ */
    
    @Nested
    @DisplayName("parseLong(String)")
    class ParseLongOptionalTests {

        @ParameterizedTest(name = "[{index}] {0}: \"{1}\" -> present: {2}")
        @MethodSource("cases")
        @DisplayName("Long parsing (Optional): trim enabled by default; Optional.empty on invalid")
        void givenString_whenParseLong_thenOptionalMatchesExpectation(
                String caseName,
                String input,
                boolean expectedPresent,
                Long expectedValue) {
            var result = NumberParser.parseLong(input);
            assertEquals(expectedPresent, result.isPresent());
            if (expectedPresent) {
                assertEquals(expectedValue, result.orElseThrow());
            } else {
                assertTrue(result.isEmpty());
            }
        }

        static Stream<Arguments> cases() {
            return Stream.of(
                    // null/blank -> empty
                    Arguments.of("null", null, false, null),
                    Arguments.of("empty", "", false, null),
                    Arguments.of("blank", "   ", false, null),
                    Arguments.of("blank tabs/newlines", "\t\n\r", false, null),

                    // valid (trim enabled)
                    Arguments.of("zero", "0", true, 0L),
                    Arguments.of("negative", "-42", true, -42L),
                    Arguments.of("plus sign", "+42", true, 42L),
                    Arguments.of("leading zeros", "00042", true, 42L),
                    Arguments.of("long max", "9223372036854775807", true, 9223372036854775807L),
                    Arguments.of("long min", "-9223372036854775808", true, -9223372036854775808L),
                    Arguments.of("trimmed surrounded", " 10 ", true, 10L),

                    // invalid -> empty
                    Arguments.of("invalid decimal", "1.0", false, null),
                    Arguments.of("invalid scientific", "1e3", false, null),
                    Arguments.of("invalid letters", "abc", false, null),

                    // overflow -> empty
                    Arguments.of("overflow above max", "9223372036854775808", false, null),
                    Arguments.of("overflow below min", "-9223372036854775809", false, null)
            );
        }
    }

    @Nested
    @DisplayName("parseLongOrNull(String)")
    class ParseLongOrNullDefaultTrimTests {

        @ParameterizedTest(name = "[{index}] {0}: \"{1}\" -> expected: {2}")
        @MethodSource("cases")
        @DisplayName("Long parsing (nullable): trim enabled by default")
        void givenString_whenParseLongOrNull_thenReturnsExpected(String caseName, String input, Long expected) {
            Long result = NumberParser.parseLongOrNull(input);
            assertEquals(expected, result);
        }

        static Stream<Arguments> cases() {
            return Stream.of(
                    Arguments.of("null", null, null),
                    Arguments.of("empty", "", null),
                    Arguments.of("blank", "   ", null),

                    Arguments.of("zero", "0", 0L),
                    Arguments.of("negative", "-42", -42L),
                    Arguments.of("plus sign", "+42", 42L),
                    Arguments.of("leading zeros", "00042", 42L),

                    Arguments.of("long max", "9223372036854775807", 9223372036854775807L),
                    Arguments.of("long min", "-9223372036854775808", -9223372036854775808L),

                    Arguments.of("trim leading", " 5", 5L),
                    Arguments.of("trim trailing", "5 ", 5L),
                    Arguments.of("trim surrounded", " 5 ", 5L),

                    Arguments.of("invalid decimal", "1.0", null),
                    Arguments.of("invalid scientific", "1e3", null),
                    Arguments.of("invalid letters", "abc", null),

                    Arguments.of("overflow above max", "9223372036854775808", null),
                    Arguments.of("overflow below min", "-9223372036854775809", null)
            );
        }
    }

    @Nested
    @DisplayName("parseLongOrNull(String, boolean)")
    class ParseLongOrNullTrimFlagTests {

        @ParameterizedTest(name = "[{index}] {0}: input=\"{1}\", trim={2} -> expected: {3}")
        @MethodSource("cases")
        @DisplayName("Long parsing (nullable): respects trimInput flag")
        void givenStringAndTrimFlag_whenParseLongOrNull_thenReturnsExpected(
                String caseName,
                String input,
                boolean trimInput,
                Long expected) {
            Long result = NumberParser.parseLongOrNull(input, trimInput);
            assertEquals(expected, result);
        }

        static Stream<Arguments> cases() {
            return Stream.of(
                    Arguments.of("spaces around (trim=true)", " 10 ", true, 10L),
                    Arguments.of("spaces around (trim=false)", " 10 ", false, null),

                    Arguments.of("valid", "10", false, 10L),
                    Arguments.of("invalid", "10.0", true, null),
                    Arguments.of("overflow", "9223372036854775808", true, null)
            );
        }
    }

    @Nested
    @DisplayName("parseLongOrDefault(String, long)")
    class ParseLongOrDefaultDefaultTrimTests {

        @ParameterizedTest(name = "[{index}] {0}: \"{1}\" default={2} -> expected: {3}")
        @MethodSource("cases")
        @DisplayName("Long parsing (default): trim enabled by default")
        void givenString_whenParseLongOrDefault_thenReturnsExpected(
                String caseName,
                String input,
                long defaultValue,
                long expected) {
            long result = NumberParser.parseLongOrDefault(input, defaultValue);
            assertEquals(expected, result);
        }

        static Stream<Arguments> cases() {
            return Stream.of(
                    Arguments.of("null", null, 123L, 123L),
                    Arguments.of("empty", "", 123L, 123L),
                    Arguments.of("blank", "   ", 123L, 123L),

                    Arguments.of("valid", "77", 123L, 77L),
                    Arguments.of("trimmed valid", " 77 ", 123L, 77L),
                    Arguments.of("max", "9223372036854775807", 123L, 9223372036854775807L),
                    Arguments.of("min", "-9223372036854775808", 123L, -9223372036854775808L),

                    Arguments.of("invalid", "77.0", 123L, 123L),
                    Arguments.of("overflow", "9223372036854775808", 123L, 123L)
            );
        }
    }

    @Nested
    @DisplayName("parseLongOrDefault(String, boolean, long)")
    class ParseLongOrDefaultTrimFlagTests {

        @ParameterizedTest(name = "[{index}] {0}: input=\"{1}\", trim={2}, default={3} -> expected: {4}")
        @MethodSource("cases")
        @DisplayName("Long parsing (default): respects trimInput flag")
        void givenStringAndTrimFlag_whenParseLongOrDefault_thenReturnsExpected(
                String caseName,
                String input,
                boolean trimInput,
                long defaultValue,
                long expected) {
            long result = NumberParser.parseLongOrDefault(input, trimInput, defaultValue);
            assertEquals(expected, result);
        }

        static Stream<Arguments> cases() {
            return Stream.of(
                    Arguments.of("spaces around (trim=true)", " 8 ", true, 7L, 8L),
                    Arguments.of("spaces around (trim=false)", " 8 ", false, 7L, 7L),
                    Arguments.of("valid", "8", false, 7L, 8L),
                    Arguments.of("invalid", "8.0", true, 7L, 7L)
            );
        }
    }

    /* ------------------------------------------------------------------------------------ */
    /* parseDouble*                                                                         */
    /* ------------------------------------------------------------------------------------ */
    
    @Nested
    @DisplayName("parseDouble(String)")
    class ParseDoubleOptionalTests {

        @ParameterizedTest(name = "[{index}] {0}: \"{1}\" -> present: {2}")
        @MethodSource("cases")
        @DisplayName("Double parsing (Optional): trim enabled by default; rejects NaN/Infinity")
        void givenString_whenParseDouble_thenOptionalMatchesExpectation(
                String caseName,
                String input,
                boolean expectedPresent,
                Double expectedValue) {
            var result = NumberParser.parseDouble(input);
            assertEquals(expectedPresent, result.isPresent());
            if (expectedPresent) {
                double actual = result.orElseThrow();
                assertEquals(expectedValue.doubleValue(), actual, 1e-12);
            } else {
                assertTrue(result.isEmpty());
            }
        }

        static Stream<Arguments> cases() {
            return Stream.of(
                    // null/blank -> empty
                    Arguments.of("null", null, false, null),
                    Arguments.of("empty", "", false, null),
                    Arguments.of("blank", "   ", false, null),
                    Arguments.of("blank tabs/newlines", "\t\n\r", false, null),

                    // valid (trim enabled)
                    Arguments.of("zero", "0", true, 0.0),
                    Arguments.of("integer string", "42", true, 42.0),
                    Arguments.of("negative", "-42", true, -42.0),
                    Arguments.of("plus sign", "+42", true, 42.0),
                    Arguments.of("decimal", "12.34", true, 12.34),
                    Arguments.of("leading zeros", "00012.50", true, 12.50),
                    Arguments.of("scientific notation", "1e3", true, 1000.0),
                    Arguments.of("scientific notation uppercase", "1E-3", true, 0.001),
                    Arguments.of("trimmed surrounded", " 3.5 ", true, 3.5),

                    // invalid -> empty
                    Arguments.of("internal space", "1 2", false, null),
                    Arguments.of("comma decimal", "12,34", false, null),
                    Arguments.of("underscore", "1_000.0", false, null),
                    Arguments.of("letters", "abc", false, null),

                    // non-finite -> empty
                    Arguments.of("NaN", "NaN", false, null),
                    Arguments.of("Infinity", "Infinity", false, null),
                    Arguments.of("-Infinity", "-Infinity", false, null),
                    Arguments.of("overflow to +Infinity (1e309)", "1e309", false, null),
                    Arguments.of("overflow to -Infinity (-1e309)", "-1e309", false, null)
            );
        }
    }

    @Nested
    @DisplayName("parseDoubleOrNull(String)")
    class ParseDoubleOrNullDefaultTrimTests {

        @ParameterizedTest(name = "[{index}] {0}: \"{1}\" -> expected: {2}")
        @MethodSource("cases")
        @DisplayName("Double parsing (nullable): trim enabled by default; rejects NaN/Infinity")
        void givenString_whenParseDoubleOrNull_thenReturnsExpected(String caseName, String input, Double expected) {
            Double result = NumberParser.parseDoubleOrNull(input);
            if (expected == null) {
                assertNull(result);
                return;
            }
            assertNotNull(result);
            assertEquals(expected.doubleValue(), result.doubleValue(), 1e-12);
        }

        static Stream<Arguments> cases() {
            return Stream.of(
                    // null/blank -> null
                    Arguments.of("null", null, null),
                    Arguments.of("empty", "", null),
                    Arguments.of("blank", "   ", null),
                    Arguments.of("blank tabs/newlines", "\t\n\r", null),

                    // valid (trim enabled)
                    Arguments.of("zero", "0", 0.0),
                    Arguments.of("integer string", "42", 42.0),
                    Arguments.of("negative", "-42", -42.0),
                    Arguments.of("plus sign", "+42", 42.0),
                    Arguments.of("decimal", "12.34", 12.34),
                    Arguments.of("leading zeros", "00012.50", 12.50),
                    Arguments.of("scientific notation", "1e3", 1000.0),
                    Arguments.of("scientific notation uppercase", "1E-3", 0.001),
                    Arguments.of("trimmed surrounded", " 3.5 ", 3.5),

                    // invalid -> null
                    Arguments.of("internal space", "1 2", null),
                    Arguments.of("comma decimal", "12,34", null),
                    Arguments.of("underscore", "1_000.0", null),
                    Arguments.of("letters", "abc", null),

                    // non-finite -> null (explicitly rejected)
                    Arguments.of("NaN", "NaN", null),
                    Arguments.of("Infinity", "Infinity", null),
                    Arguments.of("-Infinity", "-Infinity", null),
                    
                    Arguments.of("overflow to +Infinity (1e309)", "1e309", null),
                    Arguments.of("overflow to -Infinity (-1e309)", "-1e309", null)
            );
        }
    }

    @Nested
    @DisplayName("parseDoubleOrNull(String, boolean)")
    class ParseDoubleOrNullTrimFlagTests {

        @ParameterizedTest(name = "[{index}] {0}: input=\"{1}\", trim={2} -> expected: {3}")
        @MethodSource("cases")
        @DisplayName("Double parsing (nullable): respects trimInput flag; rejects NaN/Infinity")
        void givenStringAndTrimFlag_whenParseDoubleOrNull_thenReturnsExpected(
                String caseName,
                String input,
                boolean trimInput,
                Double expected) {
            Double result = NumberParser.parseDoubleOrNull(input, trimInput);
            if (expected == null) {
                assertNull(result);
                return;
            }
            assertNotNull(result);
            assertEquals(expected.doubleValue(), result.doubleValue(), 1e-12);
        }

        static Stream<Arguments> cases() {
            return Stream.of(
                    Arguments.of("spaces around (trim=true)", " 1.25 ", true, 1.25),
                    Arguments.of("spaces around (trim=false)", " 1.25 ", false, null),

                    Arguments.of("valid scientific", "1e3", false, 1000.0),
                    Arguments.of("invalid comma", "1,25", true, null),

                    Arguments.of("NaN rejected", "NaN", true, null),
                    Arguments.of("Infinity rejected", "Infinity", true, null)
            );
        }
    }

    @Nested
    @DisplayName("parseDoubleOrDefault(String, double)")
    class ParseDoubleOrDefaultDefaultTrimTests {

        @ParameterizedTest(name = "[{index}] {0}: \"{1}\" default={2} -> expected: {3}")
        @MethodSource("cases")
        @DisplayName("Double parsing (default): trim enabled by default; rejects NaN/Infinity")
        void givenString_whenParseDoubleOrDefault_thenReturnsExpected(
                String caseName,
                String input,
                double defaultValue,
                double expected) {
            double result = NumberParser.parseDoubleOrDefault(input, defaultValue);
            assertEquals(expected, result, 1e-12);
        }

        static Stream<Arguments> cases() {
            return Stream.of(
                    Arguments.of("null", null, 9.9, 9.9),
                    Arguments.of("empty", "", 9.9, 9.9),
                    Arguments.of("blank", "   ", 9.9, 9.9),

                    Arguments.of("valid", "2.5", 9.9, 2.5),
                    Arguments.of("trimmed", " 2.5 ", 9.9, 2.5),
                    Arguments.of("scientific", "1e3", 9.9, 1000.0),

                    Arguments.of("invalid", "2,5", 9.9, 9.9),
                    Arguments.of("NaN -> default", "NaN", 9.9, 9.9),
                    Arguments.of("Infinity -> default", "Infinity", 9.9, 9.9),
                    Arguments.of("-Infinity -> default", "-Infinity", 9.9, 9.9),
                    
                    Arguments.of("overflow to +Infinity (1e309) -> default", "1e309", 9.9, 9.9),
                    Arguments.of("overflow to -Infinity (-1e309) -> default", "-1e309", 9.9, 9.9)
            );
        }
    }

    @Nested
    @DisplayName("parseDoubleOrDefault(String, boolean, double)")
    class ParseDoubleOrDefaultTrimFlagTests {

        @ParameterizedTest(name = "[{index}] {0}: input=\"{1}\", trim={2}, default={3} -> expected: {4}")
        @MethodSource("cases")
        @DisplayName("Double parsing (default): respects trimInput flag; rejects NaN/Infinity")
        void givenStringAndTrimFlag_whenParseDoubleOrDefault_thenReturnsExpected(
                String caseName,
                String input,
                boolean trimInput,
                double defaultValue,
                double expected) {
            double result = NumberParser.parseDoubleOrDefault(input, trimInput, defaultValue);
            assertEquals(expected, result, 1e-12);
        }

        static Stream<Arguments> cases() {
            return Stream.of(
                    Arguments.of("spaces around (trim=true)", " 8.5 ", true, 7.7, 8.5),
                    Arguments.of("spaces around (trim=false)", " 8.5 \t", false, 7.7, 7.7),

                    Arguments.of("valid", "8.5", false, 7.7, 8.5),
                    Arguments.of("invalid", "8,5", true, 7.7, 7.7),

                    Arguments.of("NaN rejected -> default", "NaN", true, 7.7, 7.7),
                    Arguments.of("Infinity rejected -> default", "Infinity", true, 7.7, 7.7)
            );
        }
    }

    /* ------------------------------------------------------------------------------------ */
    /* parseBigInteger*                                                                     */
    /* ------------------------------------------------------------------------------------ */
    
    @Nested
    @DisplayName("parseBigInteger(String)")
    class ParseBigIntegerOptionalTests {

        @ParameterizedTest(name = "[{index}] {0}: \"{1}\" -> present: {2}")
        @MethodSource("cases")
        @DisplayName("BigInteger parsing (Optional): trim enabled by default; Optional.empty on invalid")
        void givenString_whenParseBigInteger_thenOptionalMatchesExpectation(
                String caseName,
                String input,
                boolean expectedPresent,
                BigInteger expectedValue) {
            var result = NumberParser.parseBigInteger(input);
            assertEquals(expectedPresent, result.isPresent());
            if (expectedPresent) {
                assertEquals(expectedValue, result.orElseThrow());
            } else {
                assertTrue(result.isEmpty());
            }
        }

        static Stream<Arguments> cases() {
            return Stream.of(
                    // null/blank -> empty
                    Arguments.of("null", null, false, null),
                    Arguments.of("empty", "", false, null),
                    Arguments.of("blank", "   ", false, null),
                    Arguments.of("blank tabs/newlines", "\t\n\r", false, null),

                    // valid (trim enabled)
                    Arguments.of("zero", "0", true, new BigInteger("0")),
                    Arguments.of("negative", "-42", true, new BigInteger("-42")),
                    Arguments.of("plus sign", "+42", true, new BigInteger("42")),
                    Arguments.of("leading zeros", "00042", true, new BigInteger("42")),
                    Arguments.of("very large", "9999999999999999999999999999999999999999",
                            true, new BigInteger("9999999999999999999999999999999999999999")),
                    Arguments.of("trimmed surrounded", "  123  ", true, new BigInteger("123")),

                    // invalid -> empty
                    Arguments.of("invalid decimal", "1.0", false, null),
                    Arguments.of("invalid scientific", "1e3", false, null),
                    Arguments.of("letters", "abc", false, null),
                    Arguments.of("internal space", "1 2", false, null),
                    Arguments.of("double sign", "--1", false, null)
            );
        }
    }

    @Nested
    @DisplayName("parseBigIntegerOrNull(String)")
    class ParseBigIntegerOrNullDefaultTrimTests {

        @ParameterizedTest(name = "[{index}] {0}: \"{1}\" -> expected: {2}")
        @MethodSource("cases")
        @DisplayName("BigInteger parsing (nullable): trim enabled by default")
        void givenString_whenParseBigIntegerOrNull_thenReturnsExpected(String caseName, String input, BigInteger expected) {
            BigInteger result = NumberParser.parseBigIntegerOrNull(input);
            assertEquals(expected, result);
        }

        static Stream<Arguments> cases() {
            return Stream.of(
                    Arguments.of("null", null, null),
                    Arguments.of("empty", "", null),
                    Arguments.of("blank", "   ", null),

                    Arguments.of("zero", "0", new BigInteger("0")),
                    Arguments.of("negative", "-42", new BigInteger("-42")),
                    Arguments.of("plus sign", "+42", new BigInteger("42")),
                    Arguments.of("leading zeros", "00042", new BigInteger("42")),
                    Arguments.of(
                    		"very large", 
                    		"99999999999999999999999999999999999999999999999999",
                            new BigInteger("99999999999999999999999999999999999999999999999999")),
                    Arguments.of("trimmed surrounded", "  123  ", new BigInteger("123")),

                    Arguments.of("invalid decimal", "1.0", null),
                    Arguments.of("invalid scientific", "1e3", null),
                    Arguments.of("letters", "abc", null),
                    Arguments.of("internal space", "1 2", null),
                    Arguments.of("double sign", "--1", null)
            );
        }
    }

    @Nested
    @DisplayName("parseBigIntegerOrNull(String, boolean)")
    class ParseBigIntegerOrNullTrimFlagTests {

        @ParameterizedTest(name = "[{index}] {0}: input=\"{1}\", trim={2} -> expected: {3}")
        @MethodSource("cases")
        @DisplayName("BigInteger parsing (nullable): respects trimInput flag")
        void givenStringAndTrimFlag_whenParseBigIntegerOrNull_thenReturnsExpected(
                String caseName,
                String input,
                boolean trimInput,
                BigInteger expected) {
            BigInteger result = NumberParser.parseBigIntegerOrNull(input, trimInput);
            assertEquals(expected, result);
        }

        static Stream<Arguments> cases() {
            return Stream.of(
                    Arguments.of("spaces around (trim=true)", " 7 ", true, new BigInteger("7")),
                    Arguments.of("spaces around (trim=false)", " 7 ", false, null),

                    Arguments.of("valid", "7", false, new BigInteger("7")),
                    Arguments.of("invalid", "7.0", true, null)
            );
        }
    }

    @Nested
    @DisplayName("parseBigIntegerOrDefault(String, BigInteger)")
    class ParseBigIntegerOrDefaultDefaultTrimTests {

        @ParameterizedTest(name = "[{index}] {0}: \"{1}\" default={2} -> expected: {3}")
        @MethodSource("cases")
        @DisplayName("BigInteger parsing (default): trim enabled by default")
        void givenString_whenParseBigIntegerOrDefault_thenReturnsExpected(
                String caseName,
                String input,
                BigInteger defaultValue,
                BigInteger expected) {
            BigInteger result = NumberParser.parseBigIntegerOrDefault(input, defaultValue);
            assertEquals(expected, result);
        }

        static Stream<Arguments> cases() {
            return Stream.of(
                    Arguments.of("null", null, new BigInteger("9"), new BigInteger("9")),
                    Arguments.of("empty", "", new BigInteger("9"), new BigInteger("9")),
                    Arguments.of("blank", "   ", new BigInteger("9"), new BigInteger("9")),

                    Arguments.of("valid", "123", new BigInteger("9"), new BigInteger("123")),
                    Arguments.of("trimmed", " 123 ", new BigInteger("9"), new BigInteger("123")),
                    Arguments.of("invalid", "12.3", new BigInteger("9"), new BigInteger("9"))
            );
        }
    }

    @Nested
    @DisplayName("parseBigIntegerOrDefault(String, boolean, BigInteger)")
    class ParseBigIntegerOrDefaultTrimFlagTests {

        @ParameterizedTest(name = "[{index}] {0}: input=\"{1}\", trim={2}, default={3} -> expected: {4}")
        @MethodSource("cases")
        @DisplayName("BigInteger parsing (default): respects trimInput flag")
        void givenStringAndTrimFlag_whenParseBigIntegerOrDefault_thenReturnsExpected(
                String caseName,
                String input,
                boolean trimInput,
                BigInteger defaultValue,
                BigInteger expected) {
            BigInteger result = NumberParser.parseBigIntegerOrDefault(input, trimInput, defaultValue);
            assertEquals(expected, result);
        }

        static Stream<Arguments> cases() {
            BigInteger def = new BigInteger("7");
            return Stream.of(
                    Arguments.of("spaces around (trim=true)", " 8 ", true, def, new BigInteger("8")),
                    Arguments.of("spaces around (trim=false)", " 8 ", false, def, def),

                    Arguments.of("valid", "8", false, def, new BigInteger("8")),
                    Arguments.of("invalid", "8.0", true, def, def)
            );
        }
    }

    @Test
    @DisplayName("parseBigIntegerOrDefault(...): defaultValue must not be null")
    void givenNullDefaultValue_whenParseBigIntegerOrDefault_thenThrowsIllegalArgumentException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            NumberParser.parseBigIntegerOrDefault("1", (BigInteger) null);
        });
        assertTrue(ex.getMessage().contains("defaultValue"));
    }

    /* ------------------------------------------------------------------------------------ */
    /* parseBigDecimal*                                                                     */
    /* ------------------------------------------------------------------------------------ */
    
    @Nested
    @DisplayName("parseBigDecimal(String)")
    class ParseBigDecimalOptionalTests {

        @ParameterizedTest(name = "[{index}] {0}: \"{1}\" -> present: {2}")
        @MethodSource("cases")
        @DisplayName("BigDecimal parsing (Optional): trim enabled by default; Optional.empty on invalid")
        void givenString_whenParseBigDecimal_thenOptionalMatchesExpectation(
                String caseName,
                String input,
                boolean expectedPresent,
                BigDecimal expectedValue) {
            var result = NumberParser.parseBigDecimal(input);
            assertEquals(expectedPresent, result.isPresent());
            if (expectedPresent) {
                assertBigDecimalEquals(expectedValue, result.orElseThrow());
            } else {
                assertTrue(result.isEmpty());
            }
        }

        static Stream<Arguments> cases() {
            return Stream.of(
                    // null/blank -> empty
                    Arguments.of("null", null, false, null),
                    Arguments.of("empty", "", false, null),
                    Arguments.of("blank", "   ", false, null),
                    Arguments.of("blank tabs/newlines", "\t\n\r", false, null),

                    // valid (trim enabled)
                    Arguments.of("integer", "42", true, new BigDecimal("42")),
                    Arguments.of("negative", "-42.5", true, new BigDecimal("-42.5")),
                    Arguments.of("plus sign", "+42.5", true, new BigDecimal("42.5")),
                    Arguments.of("leading zeros", "00042.500", true, new BigDecimal("42.500")),
                    Arguments.of("decimal", "12.34", true, new BigDecimal("12.34")),
                    Arguments.of("trimmed surrounded", " 12.34 ", true, new BigDecimal("12.34")),

                    // exponent notation supported by BigDecimal(String)
                    Arguments.of("scientific notation", "1e3", true, new BigDecimal("1e3")),
                    Arguments.of("scientific notation uppercase", "1E-3", true, new BigDecimal("1E-3")),

                    // invalid -> empty
                    Arguments.of("invalid comma decimal", "12,34", false, null),
                    Arguments.of("underscore", "1_000.0", false, null),
                    Arguments.of("letters", "abc", false, null),
                    Arguments.of("internal space", "1 2", false, null),

                    // non-finite strings invalid for BigDecimal(String)
                    Arguments.of("NaN (invalid)", "NaN", false, null),
                    Arguments.of("Infinity (invalid)", "Infinity", false, null)
            );
        }
    }

    @Nested
    @DisplayName("parseBigDecimalOrNull(String)")
    class ParseBigDecimalOrNullDefaultTrimTests {

        @ParameterizedTest(name = "[{index}] {0}: \"{1}\" -> expected: {2}")
        @MethodSource("cases")
        @DisplayName("BigDecimal parsing (nullable): trim enabled by default")
        void givenString_whenParseBigDecimalOrNull_thenReturnsExpected(String caseName, String input, BigDecimal expected) {
            BigDecimal result = NumberParser.parseBigDecimalOrNull(input);
            assertBigDecimalEquals(expected, result);
        }

        static Stream<Arguments> cases() {
            return Stream.of(
                    Arguments.of("null", null, null),
                    Arguments.of("empty", "", null),
                    Arguments.of("blank", "   ", null),

                    Arguments.of("integer", "42", new BigDecimal("42")),
                    Arguments.of("negative", "-42.5", new BigDecimal("-42.5")),
                    Arguments.of("plus sign", "+42.5", new BigDecimal("42.5")),
                    Arguments.of("leading zeros", "00042.500", new BigDecimal("42.500")),
                    Arguments.of("decimal", "12.34", new BigDecimal("12.34")),
                    Arguments.of("trimmed", " 12.34 ", new BigDecimal("12.34")),

                    // BigDecimal(String) supports exponent notation in standard Java representation
                    Arguments.of("scientific notation", "1e3", new BigDecimal("1e3")),
                    Arguments.of("scientific notation uppercase", "1E-3", new BigDecimal("1E-3")),

                    Arguments.of("invalid comma decimal", "12,34", null),
                    Arguments.of("underscore", "1_000.0", null),
                    Arguments.of("letters", "abc", null),
                    Arguments.of("internal space", "1 2", null),

                    // non-finite strings are invalid for BigDecimal(String)
                    Arguments.of("NaN (invalid)", "NaN", null),
                    Arguments.of("Infinity (invalid)", "Infinity", null)
            );
        }
    }

    @Nested
    @DisplayName("parseBigDecimalOrNull(String, boolean)")
    class ParseBigDecimalOrNullTrimFlagTests {

        @ParameterizedTest(name = "[{index}] {0}: input=\"{1}\", trim={2} -> expected: {3}")
        @MethodSource("cases")
        @DisplayName("BigDecimal parsing (nullable): respects trimInput flag")
        void givenStringAndTrimFlag_whenParseBigDecimalOrNull_thenReturnsExpected(
                String caseName,
                String input,
                boolean trimInput,
                BigDecimal expected) {
            BigDecimal result = NumberParser.parseBigDecimalOrNull(input, trimInput);
            assertBigDecimalEquals(expected, result);
        }

        static Stream<Arguments> cases() {
            return Stream.of(
                    Arguments.of("spaces around (trim=true)", " 7.5 ", true, new BigDecimal("7.5")),
                    Arguments.of("spaces around (trim=false)", " 7.5 ", false, null),

                    Arguments.of("valid", "7.5", false, new BigDecimal("7.5")),
                    Arguments.of("invalid comma", "7,5", true, null)
            );
        }
    }

    @Nested
    @DisplayName("parseBigDecimalOrDefault(String, BigDecimal)")
    class ParseBigDecimalOrDefaultDefaultTrimTests {

        @ParameterizedTest(name = "[{index}] {0}: \"{1}\" default={2} -> expected: {3}")
        @MethodSource("cases")
        @DisplayName("BigDecimal parsing (default): trim enabled by default")
        void givenString_whenParseBigDecimalOrDefault_thenReturnsExpected(
                String caseName,
                String input,
                BigDecimal defaultValue,
                BigDecimal expected) {
            BigDecimal result = NumberParser.parseBigDecimalOrDefault(input, defaultValue);
            assertBigDecimalEquals(expected, result);
        }

        static Stream<Arguments> cases() {
            BigDecimal def = new BigDecimal("9.9");
            return Stream.of(
                    Arguments.of("null", null, def, def),
                    Arguments.of("empty", "", def, def),
                    Arguments.of("blank", "   ", def, def),

                    Arguments.of("valid", "123.45", def, new BigDecimal("123.45")),
                    Arguments.of("trimmed", " 123.45 ", def, new BigDecimal("123.45")),
                    Arguments.of("scientific", "1e3", def, new BigDecimal("1e3")),

                    Arguments.of("invalid", "12,3", def, def),
                    Arguments.of("letters", "abc", def, def)
            );
        }
    }

    @Nested
    @DisplayName("parseBigDecimalOrDefault(String, boolean, BigDecimal)")
    class ParseBigDecimalOrDefaultTrimFlagTests {
    	
        @ParameterizedTest(name = "[{index}] {0}: input=\"{1}\", trim={2}, default={3} -> expected: {4}")
        @MethodSource("cases")
        @DisplayName("BigDecimal parsing (default): respects trimInput flag")
        void givenStringAndTrimFlag_whenParseBigDecimalOrDefault_thenReturnsExpected(
                String caseName,
                String input,
                boolean trimInput,
                BigDecimal defaultValue,
                BigDecimal expected) {
            BigDecimal result = NumberParser.parseBigDecimalOrDefault(input, trimInput, defaultValue);
            assertBigDecimalEquals(expected, result);
        }

        static Stream<Arguments> cases() {
            BigDecimal def = new BigDecimal("7.7");
            return Stream.of(
                    Arguments.of("spaces around (trim=true)", " 8.8 ", true, def, new BigDecimal("8.8")),
                    Arguments.of("spaces around (trim=false)", " 8.8 ", false, def, def),

                    Arguments.of("valid", "8.8", false, def, new BigDecimal("8.8")),
                    Arguments.of("invalid comma", "8,8", true, def, def)
            );
        }
    }

    @Test
    @DisplayName("parseBigDecimalOrDefault(...): defaultValue must not be null")
    void givenNullDefaultValue_whenParseBigDecimalOrDefault_thenThrowsIllegalArgumentException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            NumberParser.parseBigDecimalOrDefault("1.0", (BigDecimal) null);
        });
        assertTrue(ex.getMessage().equals("defaultValue parameter cannot be null"));
    }
    
    @Test
    @DisplayName("BigDecimal: parsing preserves scale for string constructor")
    void givenStringWithTrailingZeros_whenParseBigDecimal_thenScaleIsPreserved() {
        BigDecimal v = NumberParser.parseBigDecimalOrNull("00042.500", true);
        assertNotNull(v);
        assertEquals(new BigDecimal("42.500"), v);
        assertEquals(3, v.scale());
    }

    /* ------------------------------------------------------------------------------------ */
    /* helpers                                                                              */
    /* ------------------------------------------------------------------------------------ */

    private static void assertBigDecimalEquals(BigDecimal expected, BigDecimal actual) {
        if (expected == null) {
            assertNull(actual);
            return;
        }
        assertNotNull(actual);
        assertEquals(expected, actual);
    }
}