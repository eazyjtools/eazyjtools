package io.github.eazyjtools.parse.number;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Optional;
import java.util.function.Function;

/**
 * Utility class for safe parsing of numeric values from {@link String} inputs.
 * <p>
 * The parser methods are designed for "best-effort" conversion without throwing parsing exceptions to the caller.
 * Instead, they return {@code null} (for {@code OrNull} variants) or a caller-provided default value
 * (for {@code OrDefault} variants).
 * </p>
 *
 * <h2>Common rules</h2>
 * <ul>
 *   <li>If {@code input == null}, then {@code OrNull} methods return {@code null} and {@code OrDefault} methods
 *       return {@code defaultValue}.</li>
 *   <li>If {@code trimInput == true}, the input is trimmed using {@link String#trim()} prior to parsing.</li>
 *   <li>If the effective string becomes empty ({@code ""}), then {@code OrNull} methods return {@code null}
 *       and {@code OrDefault} methods return {@code defaultValue}.</li>
 *   <li>If parsing fails with {@link NumberFormatException}, then {@code OrNull} methods return {@code null}
 *       and {@code OrDefault} methods return {@code defaultValue}.</li>
 * </ul>
 *
 * <h2>Notes</h2>
 * <ul>
 *   <li>{@code Double} parsing additionally rejects non-finite values:
 *       {@code NaN}, {@code Infinity}, {@code -Infinity}.</li>
 *   <li>{@link BigDecimal} and {@link BigInteger} constructors use standard Java syntax.
 *       For {@link BigDecimal}, decimal separator must be a dot (e.g. {@code "12.34"}). Inputs like {@code "12,34"}
 *       are not supported by these methods and will be treated as invalid.</li>
 * </ul>
 *
 * @author romanzdev
 * @since 1.0.0
 */
public final class NumberParser {

    /**
     * Prevents instantiation of this utility class.
     *
     * @throws UnsupportedOperationException always
     */
    private NumberParser() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated!");
    }

    /* ================ Integer parse methods  ================ */
    
    /**
     * Attempts to parse the given string into an {@link Integer} and returns the result as {@link java.util.Optional}.
     * <p>
     * This method uses trimming by default (equivalent to {@code trimInput=true}).
     * It follows the same parsing rules as {@link #parseIntOrNull(String, boolean)}:
     * </p>
     * <ul>
     *   <li>If {@code input} is {@code null}, empty, or becomes empty after {@link String#trim()}, returns {@link java.util.Optional#empty()}.</li>
     *   <li>If parsing fails (invalid format or overflow), returns {@link java.util.Optional#empty()}.</li>
     * </ul>
     *
     * @param input the input string to parse; may be {@code null}
     * @return {@link java.util.Optional} containing the parsed {@link Integer}, or {@link java.util.Optional#empty()}
     *         if input is {@code null}/empty (after trim) or not a valid integer representation (including overflow)
     * @author romanzdev
     * @since 1.0.0
     */
    public static Optional<Integer> parseInt(String input) {
	    return Optional.ofNullable(parseIntOrNull(input, true));
    }

    /**
     * Parses the given string to an {@link Integer} with trimming enabled by default.
     * <p>
     * Equivalent to {@link #parseIntOrNull(String, boolean) parseIntOrNull(input, true)}.
     * </p>
     *
     * @param input the input string to parse; may be {@code null}
     * @return parsed {@link Integer}, or {@code null} if {@code input} is {@code null}, empty (after trim),
     *         or not a valid integer representation
	 * @author romanzdev
	 * @since 1.0.0
     */
    public static Integer parseIntOrNull(String input) {
        return parseIntOrNull(input, true);
    }

    /**
     * Parses the given string to an {@link Integer}.
     * <p>
     * If {@code trimInput} is {@code true}, the input is trimmed before parsing.
     * </p>
     *
     * @param input the input string to parse; may be {@code null}
     * @param trimInput whether to apply {@link String#trim()} before parsing
     * @return parsed {@link Integer}, or {@code null} if {@code input} is {@code null}, empty (after optional trim),
     *         or not a valid integer representation
     * @author romanzdev
     * @since 1.0.0
     */
    public static Integer parseIntOrNull(String input, boolean trimInput) {
        return parse(input, trimInput, Integer::valueOf);
    }

    /**
     * Parses the given string to an {@code int} with trimming enabled by default.
     * <p>
     * Equivalent to {@link #parseIntOrDefault(String, boolean, int) parseIntOrDefault(input, true, defaultValue)}.
     * </p>
     *
     * @param input the input string to parse; may be {@code null}
     * @param defaultValue value to return if parsing fails or the input is blank/empty (after trim)
     * @return parsed {@code int} value, or {@code defaultValue} if {@code input} is {@code null}, empty (after trim),
     *         or not a valid integer representation
     * @author romanzdev
     * @since 1.0.0
     */
    public static int parseIntOrDefault(String input, int defaultValue) {
        return parseIntOrDefault(input, true, defaultValue);
    }

    /**
     * Parses the given string to an {@code int}.
     * <p>
     * If parsing fails or the input is {@code null}/empty (after optional trim), returns {@code defaultValue}.
     * </p>
     *
     * @param input the input string to parse; may be {@code null}
     * @param trimInput whether to apply {@link String#trim()} before parsing
     * @param defaultValue value to return if parsing fails or the input is empty (after optional trim)
     * @return parsed {@code int} value, or {@code defaultValue} if {@code input} is {@code null}, empty (after optional trim),
     *         or not a valid integer representation
	 * @author romanzdev
	 * @since 1.0.0
     */
    public static int parseIntOrDefault(String input, boolean trimInput, int defaultValue) {
        Integer v = parse(input, trimInput, Integer::valueOf);
        return v == null ? defaultValue : v;
    }

    /* ================ Long parse methods  ================ */
    
    /**
     * Attempts to parse the given string into a {@link Long} and returns the result as {@link java.util.Optional}.
     * <p>
     * This method uses trimming by default (equivalent to {@code trimInput=true}).
     * It follows the same parsing rules as {@link #parseLongOrNull(String, boolean)}:
     * </p>
     * <ul>
     *   <li>If {@code input} is {@code null}, empty, or becomes empty after {@link String#trim()}, returns {@link java.util.Optional#empty()}.</li>
     *   <li>If parsing fails (invalid format or overflow), returns {@link java.util.Optional#empty()}.</li>
     * </ul>
     *
     * @param input the input string to parse; may be {@code null}
     * @return {@link java.util.Optional} containing the parsed {@link Long}, or {@link java.util.Optional#empty()}
     *         if input is {@code null}/empty (after trim) or not a valid long representation (including overflow)
     * @author romanzdev
     * @since 1.0.0
     */
    public static Optional<Long> parseLong(String input) {
	    return Optional.ofNullable(parseLongOrNull(input, true));
    }

    /**
     * Parses the given string to a {@link Long} with trimming enabled by default.
     * <p>
     * Equivalent to {@link #parseLongOrNull(String, boolean) parseLongOrNull(input, true)}.
     * </p>
     *
     * @param input the input string to parse; may be {@code null}
     * @return parsed {@link Long}, or {@code null} if {@code input} is {@code null}, empty (after trim),
     *         or not a valid long representation
	 * @author romanzdev
	 * @since 1.0.0
     */
    public static Long parseLongOrNull(String input) {
        return parseLongOrNull(input, true);
    }

    /**
     * Parses the given string to a {@link Long}.
     * <p>
     * If {@code trimInput} is {@code true}, the input is trimmed before parsing.
     * </p>
     *
     * @param input the input string to parse; may be {@code null}
     * @param trimInput whether to apply {@link String#trim()} before parsing
     * @return parsed {@link Long}, or {@code null} if {@code input} is {@code null}, empty (after optional trim),
     *         or not a valid long representation
	 * @author romanzdev
	 * @since 1.0.0
     */
    public static Long parseLongOrNull(String input, boolean trimInput) {
        return parse(input, trimInput, Long::valueOf);
    }

    /**
     * Parses the given string to a {@code long} with trimming enabled by default.
     * <p>
     * Equivalent to {@link #parseLongOrDefault(String, boolean, long) parseLongOrDefault(input, true, defaultValue)}.
     * </p>
     *
     * @param input the input string to parse; may be {@code null}
     * @param defaultValue value to return if parsing fails or the input is empty (after trim)
     * @return parsed {@code long} value, or {@code defaultValue} if {@code input} is {@code null}, empty (after trim),
     *         or not a valid long representation
	 * @author romanzdev
	 * @since 1.0.0
     */
    public static long parseLongOrDefault(String input, long defaultValue) {
        return parseLongOrDefault(input, true, defaultValue);
    }

    /**
     * Parses the given string to a {@code long}.
     * <p>
     * If parsing fails or the input is {@code null}/empty (after optional trim), returns {@code defaultValue}.
     * </p>
     *
     * @param input the input string to parse; may be {@code null}
     * @param trimInput whether to apply {@link String#trim()} before parsing
     * @param defaultValue value to return if parsing fails or the input is empty (after optional trim)
     * @return parsed {@code long} value, or {@code defaultValue} if {@code input} is {@code null}, empty (after optional trim),
     *         or not a valid long representation
	 * @author romanzdev
	 * @since 1.0.0
     */
    public static long parseLongOrDefault(String input, boolean trimInput, long defaultValue) {
        Long v = parse(input, trimInput, Long::valueOf);
        return v == null ? defaultValue : v;
    }

    /* ================ Double parse methods  ================ */
    
    /**
     * Attempts to parse the given string into a finite {@link Double} and returns the result as {@link java.util.Optional}.
     * <p>
     * This method uses trimming by default (equivalent to {@code trimInput=true}).
     * It follows the same parsing rules as {@link #parseDoubleOrNull(String, boolean)}:
     * </p>
     * <ul>
     *   <li>If {@code input} is {@code null}, empty, or becomes empty after {@link String#trim()}, returns {@link java.util.Optional#empty()}.</li>
     *   <li>If parsing fails (invalid format), returns {@link java.util.Optional#empty()}.</li>
     *   <li>Non-finite values ({@code NaN}, {@code Infinity}, {@code -Infinity}) are treated as invalid and produce {@link java.util.Optional#empty()}.</li>
     * </ul>
     *
     * @param input the input string to parse; may be {@code null}
     * @return {@link java.util.Optional} containing the parsed finite {@link Double}, or {@link java.util.Optional#empty()}
     *         if input is {@code null}/empty (after trim), not a valid double representation, or represents a non-finite value
     * @author romanzdev
     * @since 1.0.0
     */
    public static Optional<Double> parseDouble(String input) {
	    return Optional.ofNullable(parseDoubleOrNull(input, true));
    }

    /**
     * Parses the given string to a {@link Double} with trimming enabled by default.
     * <p>
     * Equivalent to {@link #parseDoubleOrNull(String, boolean) parseDoubleOrNull(input, true)}.
     * </p>
     * <p>
     * Non-finite values ({@code NaN}, {@code Infinity}, {@code -Infinity}) are treated as invalid and result in {@code null}.
     * </p>
     *
     * @param input the input string to parse; may be {@code null}
     * @return parsed finite {@link Double}, or {@code null} if {@code input} is {@code null}, empty (after trim),
     *         not a valid double representation, or represents a non-finite value
	 * @author romanzdev
	 * @since 1.0.0
     */
    public static Double parseDoubleOrNull(String input) {
        return parseDoubleOrNull(input, true);
    }

    /**
     * Parses the given string to a {@link Double}.
     * <p>
     * If {@code trimInput} is {@code true}, the input is trimmed before parsing.
     * </p>
     * <p>
     * Non-finite values ({@code NaN}, {@code Infinity}, {@code -Infinity}) are treated as invalid and result in {@code null}.
     * </p>
     *
     * @param input the input string to parse; may be {@code null}
     * @param trimInput whether to apply {@link String#trim()} before parsing
     * @return parsed finite {@link Double}, or {@code null} if {@code input} is {@code null}, empty (after optional trim),
     *         not a valid double representation, or represents a non-finite value
	 * @author romanzdev
	 * @since 1.0.0
     */
    public static Double parseDoubleOrNull(String input, boolean trimInput) {
		if (!trimInput && hasTrimWhitespace(input)) {
			return null;
		}
        Double v = parse(input, trimInput, Double::valueOf);
        return isFinite(v) ? v : null;
    }

    /**
     * Parses the given string to a {@code double} with trimming enabled by default.
     * <p>
     * Equivalent to {@link #parseDoubleOrDefault(String, boolean, double) parseDoubleOrDefault(input, true, defaultValue)}.
     * </p>
     * <p>
     * Non-finite values ({@code NaN}, {@code Infinity}, {@code -Infinity}) are treated as invalid and result in {@code defaultValue}.
     * </p>
     *
     * @param input the input string to parse; may be {@code null}
     * @param defaultValue value to return if parsing fails or yields a non-finite value
     * @return parsed finite {@code double} value, or {@code defaultValue} if {@code input} is {@code null}, empty (after trim),
     *         not a valid double representation, or represents a non-finite value
	 * @author romanzdev
	 * @since 1.0.0
     */
    public static double parseDoubleOrDefault(String input, double defaultValue) {
        return parseDoubleOrDefault(input, true, defaultValue);
    }

    /**
     * Parses the given string to a {@code double}.
     * <p>
     * If parsing fails, the input is {@code null}/empty (after optional trim), or the parsed value is non-finite,
     * returns {@code defaultValue}.
     * </p>
     *
     * @param input the input string to parse; may be {@code null}
     * @param trimInput whether to apply {@link String#trim()} before parsing
     * @param defaultValue value to return if parsing fails, input is empty (after optional trim), or the parsed value is non-finite
     * @return parsed finite {@code double} value, or {@code defaultValue} if {@code input} is {@code null}, empty (after optional trim),
     *         not a valid double representation, or represents a non-finite value
	 * @author romanzdev
	 * @since 1.0.0
     */
    public static double parseDoubleOrDefault(String input, boolean trimInput, double defaultValue) {
		if (!trimInput && hasTrimWhitespace(input)) {
			return defaultValue;
		}
        Double v = parse(input, trimInput, Double::valueOf);
        return isFinite(v) ? v : defaultValue;
    }

    /**
     * Checks whether the provided {@link Double} is finite (not {@code null}, not {@code NaN}, not infinite).
     *
     * @param v the {@link Double} value
     * @return {@code true} if {@code v} is not {@code null}, not {@code NaN}, and not infinite; otherwise {@code false}
	 * @author romanzdev
	 * @since 1.0.0
     */
    private static boolean isFinite(Double v) {
        return v != null && !v.isNaN() && !v.isInfinite();
    }
    
    /**
     * Checks whether the given input contains leading and/or trailing whitespace that would be removed by
     * {@link String#trim()}.
     * <p>
     * This method compares the original string with its {@link String#trim()} result:
     * if they differ, then the input has at least one whitespace character at the beginning and/or end
     * (as defined by {@code trim()}, i.e. characters with code points {@code \u0000} through {@code \u0020}).
     * </p>
     *
     * <h3>Notes</h3>
     * <ul>
     *   <li>If {@code input} is {@code null}, this method returns {@code false}.</li>
     *   <li>This method uses {@link String#trim()}, not {@link String#strip()} — so it does not detect
     *       all Unicode whitespace characters (e.g. NBSP {@code \u00A0}) unless they are removed by {@code trim()}.</li>
     * </ul>
     *
     * @param input the input string to check; may be {@code null}
     * @return {@code true} if {@code input} has leading/trailing whitespace removed by {@link String#trim()},
     *         otherwise {@code false}
     * @author romanzdev
     * @since 1.0.0
     */
    private static boolean hasTrimWhitespace(String input) {
        if (input == null) {
            return false;
        }
        return !input.equals(input.trim());
    }

    /* ================ BigInteger parse methods  ================ */
    
    /**
     * Attempts to parse the given string into a {@link BigInteger} and returns the result as {@link java.util.Optional}.
     * <p>
     * This method uses trimming by default (equivalent to {@code trimInput=true}).
     * It follows the same parsing rules as {@link #parseBigIntegerOrNull(String, boolean)}:
     * </p>
     * <ul>
     *   <li>If {@code input} is {@code null}, empty, or becomes empty after {@link String#trim()}, returns {@link java.util.Optional#empty()}.</li>
     *   <li>If parsing fails (invalid format), returns {@link java.util.Optional#empty()}.</li>
     * </ul>
     *
     * @param input the input string to parse; may be {@code null}
     * @return {@link java.util.Optional} containing the parsed {@link BigInteger}, or {@link java.util.Optional#empty()}
     *         if input is {@code null}/empty (after trim) or not a valid {@link BigInteger} representation
     * @author romanzdev
     * @since 1.0.0
     */
    public static Optional<BigInteger> parseBigInteger(String input) {
	    return Optional.ofNullable(parseBigIntegerOrNull(input, true));
    }

    /**
     * Parses the given string to a {@link BigInteger} with trimming enabled by default.
     * <p>
     * Equivalent to {@link #parseBigIntegerOrNull(String, boolean) parseBigIntegerOrNull(input, true)}.
     * </p>
     *
     * @param input the input string to parse; may be {@code null}
     * @return parsed {@link BigInteger}, or {@code null} if {@code input} is {@code null}, empty (after trim),
     *         or not a valid {@link BigInteger} representation
	 * @author romanzdev
	 * @since 1.0.0
     */
    public static BigInteger parseBigIntegerOrNull(String input) {
        return parseBigIntegerOrNull(input, true);
    }

    /**
     * Parses the given string to a {@link BigInteger}.
     * <p>
     * If {@code trimInput} is {@code true}, the input is trimmed before parsing.
     * </p>
     *
     * @param input the input string to parse; may be {@code null}
     * @param trimInput whether to apply {@link String#trim()} before parsing
     * @return parsed {@link BigInteger}, or {@code null} if {@code input} is {@code null}, empty (after optional trim),
     *         or not a valid {@link BigInteger} representation
	 * @author romanzdev
	 * @since 1.0.0
     */
    public static BigInteger parseBigIntegerOrNull(String input, boolean trimInput) {
        return parse(input, trimInput, BigInteger::new);
    }

    /**
     * Parses the given string to a {@link BigInteger} and returns {@code defaultValue} on failure.
     * <p>
     * Equivalent to {@link #parseBigIntegerOrDefault(String, boolean, BigInteger)
     * parseBigIntegerOrDefault(input, true, defaultValue)}.
     * </p>
     *
     * @param input the input string to parse; may be {@code null}
     * @param defaultValue value to return if parsing fails or input is empty (after trim); must not be {@code null}
     * @return parsed {@link BigInteger}, or {@code defaultValue} if {@code input} is {@code null}, empty (after trim),
     *         or not a valid {@link BigInteger} representation
     * @throws IllegalArgumentException if {@code defaultValue} is {@code null}
	 * @author romanzdev
	 * @since 1.0.0
     */
    public static BigInteger parseBigIntegerOrDefault(String input, BigInteger defaultValue) {
        return parseBigIntegerOrDefault(input, true, defaultValue);
    }

    /**
     * Parses the given string to a {@link BigInteger} and returns {@code defaultValue} on failure.
     *
     * @param input the input string to parse; may be {@code null}
     * @param trimInput whether to apply {@link String#trim()} before parsing
     * @param defaultValue value to return if parsing fails or input is empty (after optional trim); must not be {@code null}
     * @return parsed {@link BigInteger}, or {@code defaultValue} if {@code input} is {@code null}, empty (after optional trim),
     *         or not a valid {@link BigInteger} representation
     * @throws IllegalArgumentException if {@code defaultValue} is {@code null}
	 * @author romanzdev
	 * @since 1.0.0
     */
    public static BigInteger parseBigIntegerOrDefault(String input, boolean trimInput, BigInteger defaultValue) {
        if (defaultValue == null) {
            throw new IllegalArgumentException("defaultValue parameter cannot be null");
        }
        BigInteger v = parse(input, trimInput, BigInteger::new);
        return v == null ? defaultValue : v;
    }

    /* ================ BigDecimal parse methods  ================ */
    
    /**
     * Attempts to parse the given string into a {@link BigDecimal} and returns the result as {@link java.util.Optional}.
     * <p>
     * This method uses trimming by default (equivalent to {@code trimInput=true}).
     * It follows the same parsing rules as {@link #parseBigDecimalOrNull(String, boolean)}:
     * </p>
     * <ul>
     *   <li>If {@code input} is {@code null}, empty, or becomes empty after {@link String#trim()}, returns {@link java.util.Optional#empty()}.</li>
     *   <li>If parsing fails (invalid format), returns {@link java.util.Optional#empty()}.</li>
     * </ul>
     *
     * @param input the input string to parse; may be {@code null}
     * @return {@link java.util.Optional} containing the parsed {@link BigDecimal}, or {@link java.util.Optional#empty()}
     *         if input is {@code null}/empty (after trim) or not a valid {@link BigDecimal} representation
     * @author romanzdev
     * @since 1.0.0
     */
    public static Optional<BigDecimal> parseBigDecimal(String input) {
	    return Optional.ofNullable(parseBigDecimalOrNull(input, true));
    }

    /**
     * Parses the given string to a {@link BigDecimal} with trimming enabled by default.
     * <p>
     * Equivalent to {@link #parseBigDecimalOrNull(String, boolean) parseBigDecimalOrNull(input, true)}.
     * </p>
     *
     * @param input the input string to parse; may be {@code null}
     * @return parsed {@link BigDecimal}, or {@code null} if {@code input} is {@code null}, empty (after trim),
     *         or not a valid {@link BigDecimal} representation
	 * @author romanzdev
	 * @since 1.0.0
     */
    public static BigDecimal parseBigDecimalOrNull(String input) {
        return parseBigDecimalOrNull(input, true);
    }

    /**
     * Parses the given string to a {@link BigDecimal}.
     * <p>
     * If {@code trimInput} is {@code true}, the input is trimmed before parsing.
     * </p>
     *
     * @param input the input string to parse; may be {@code null}
     * @param trimInput whether to apply {@link String#trim()} before parsing
     * @return parsed {@link BigDecimal}, or {@code null} if {@code input} is {@code null}, empty (after optional trim),
     *         or not a valid {@link BigDecimal} representation
	 * @author romanzdev
	 * @since 1.0.0
     */
    public static BigDecimal parseBigDecimalOrNull(String input, boolean trimInput) {
        return parse(input, trimInput, BigDecimal::new);
    }

    /**
     * Parses the given string to a {@link BigDecimal} and returns {@code defaultValue} on failure.
     * <p>
     * Equivalent to {@link #parseBigDecimalOrDefault(String, boolean, BigDecimal)
     * parseBigDecimalOrDefault(input, true, defaultValue)}.
     * </p>
     *
     * @param input the input string to parse; may be {@code null}
     * @param defaultValue value to return if parsing fails or input is empty (after trim); must not be {@code null}
     * @return parsed {@link BigDecimal}, or {@code defaultValue} if {@code input} is {@code null}, empty (after trim),
     *         or not a valid {@link BigDecimal} representation
     * @throws IllegalArgumentException if {@code defaultValue} is {@code null}
     * @author romanzdev
	 * @since 1.0.0
     */
    public static BigDecimal parseBigDecimalOrDefault(String input, BigDecimal defaultValue) {
        return parseBigDecimalOrDefault(input, true, defaultValue);
    }

    /**
     * Parses the given string to a {@link BigDecimal} and returns {@code defaultValue} on failure.
     *
     * @param input the input string to parse; may be {@code null}
     * @param trimInput whether to apply {@link String#trim()} before parsing
     * @param defaultValue value to return if parsing fails or input is empty (after optional trim); must not be {@code null}
     * @return parsed {@link BigDecimal}, or {@code defaultValue} if {@code input} is {@code null}, empty (after optional trim),
     *         or not a valid {@link BigDecimal} representation
     * @throws IllegalArgumentException if {@code defaultValue} is {@code null}
	 * @author romanzdev
	 * @since 1.0.0
     */
    public static BigDecimal parseBigDecimalOrDefault(String input, boolean trimInput, BigDecimal defaultValue) {
        if (defaultValue == null) {
            throw new IllegalArgumentException("defaultValue parameter cannot be null");
        }
        BigDecimal v = parse(input, trimInput, BigDecimal::new);
        return v == null ? defaultValue : v;
    }

    /* ================ Internal  ================ */

    /**
     * Internal parsing helper that applies common normalization and exception handling rules.
     * <p>
     * Normalization rules:
     * </p>
     * <ul>
     *   <li>If {@code input == null}, returns {@code null}.</li>
     *   <li>If {@code trimInput == true}, trims the input via {@link String#trim()}.</li>
     *   <li>If the effective string is empty, returns {@code null}.</li>
     * </ul>
     * <p>
     * Parsing rules:
     * </p>
     * <ul>
     *   <li>Applies the provided {@code parser} to the normalized string.</li>
     *   <li>If the parser throws {@link NumberFormatException}, returns {@code null}.</li>
     * </ul>
     *
     * @param input the input string to parse; may be {@code null}
     * @param trimInput whether to apply {@link String#trim()} before parsing
     * @param parser a function that parses a normalized string into a value; must not be {@code null}
     * @param <T> target type
     * @return parsed value, or {@code null} if input is {@code null}, empty (after optional trim), or parsing fails
     * @throws IllegalArgumentException if {@code parser} is {@code null}
	 * @author romanzdev
	 * @since 1.0.0
     */
    private static <T> T parse(String input, boolean trimInput, Function<String, T> parser) {
        if (parser == null) {
            throw new IllegalArgumentException("parser function parameter cannot be null");
        }
        if (input == null) {
            return null;
        }
        String s = trimInput ? input.trim() : input;
        if (s.isEmpty()) {
            return null;
        }
        try {
            return parser.apply(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}