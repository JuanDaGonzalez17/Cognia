package com.cognia.api.domain.model;
import java.util.Locale;
import java.text.Normalizer;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
public record ShortAnswerCriterion(String pattern, boolean regularExpression) {
	public ShortAnswerCriterion { if (pattern == null || pattern.isBlank() || pattern.length() > 255) throw new IllegalArgumentException("Criterion must contain 1 to 255 characters"); if (regularExpression) try { Pattern.compile(normalize(pattern)); } catch (PatternSyntaxException ex) { throw new IllegalArgumentException("Invalid regular expression", ex); } }
	public boolean matches(String answer) { var normalized = normalize(answer); return regularExpression ? Pattern.compile(normalize(pattern)).matcher(normalized).matches() : normalized.equals(normalize(pattern)); }
	public static String normalize(String value) { return Normalizer.normalize(value.trim().toLowerCase(Locale.ROOT), Normalizer.Form.NFD).replaceAll("\\p{M}+", ""); }
}
