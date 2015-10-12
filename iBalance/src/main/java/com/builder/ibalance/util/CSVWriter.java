//package com.builder.ibalance.util;
//
//import java.util.List;
//
//public class CSVWriter {
//	public static final int INITIAL_STRING_SIZE = 128;
//	/**
//	 * The character used for escaping quotes.
//	 */
//	public static final char DEFAULT_ESCAPE_CHARACTER = '"';
//	/**
//	 * The default separator to use if none is supplied to the constructor.
//	 */
//	public static final char DEFAULT_SEPARATOR = ',';
//	/**
//	 * The default quote character to use if none is supplied to the
//	 * constructor.
//	 */
//	public static final char DEFAULT_QUOTE_CHARACTER = '"';
//	/**
//	 * The quote constant to use when you wish to suppress all quoting.
//	 */
//	public static final char NO_QUOTE_CHARACTER = '\u0000';
//	/**
//	 * The escape constant to use when you wish to suppress all escaping.
//	 */
//	public static final char NO_ESCAPE_CHARACTER = '\u0000';
//	/**
//	 * Default line terminator uses platform encoding.
//	 */
//	public static final String DEFAULT_LINE_END = "\n";
//	private char separator;
//	private char quotechar;
//	private char escapechar;
//	private String lineEnd;
//
//	/**
//	 * Constructs CSVWriter using a comma for the separator.
//	 *
//	 *
//	 *            the writer to an underlying CSV source.
//	 */
//	public CSVWriter() {
//		this.separator = DEFAULT_SEPARATOR;
//		this.quotechar = DEFAULT_QUOTE_CHARACTER;
//		this.escapechar = DEFAULT_ESCAPE_CHARACTER;
//		this.lineEnd = DEFAULT_LINE_END;
//	}
//
//
//	/**
//	 * Writes the entire list to a CSV file. The list is assumed to be a
//	 * String[]
//	 *
//	 * @param allLines
//	 *            a List of String[], with each String[] representing a line of
//	 *            the file.
//	 * @param applyQuotesToAll
//	 *            true if all values are to be quoted. false if quotes only to
//	 *            be applied to values which contain the separator, escape,
//	 *            quote or new line characters.
//	 */
//	public String writeAll(List<String[]> allLines, boolean applyQuotesToAll) {
//		StringBuilder sb = new StringBuilder();
//		for (String[] line : allLines) {
//			sb.append(writeNext(line, applyQuotesToAll));
//		}
//		return sb.toString();
//	}
//
//	/**
//	 * Writes the entire list to a CSV file. The list is assumed to be a
//	 * String[]
//	 *
//	 * @param allLines
//	 *            a List of String[], with each String[] representing a line of
//	 *            the file.
//	 */
//	public String writeAll(List<String[]> allLines) {
//		StringBuilder sb = new StringBuilder();
//		for (String[] line : allLines) {
//			sb.append(writeNext(line));
//		}
//		return sb.toString();
//	}
//
//	/**
//	 * Writes the column names.
//	 *
//	 * @param rs
//	 *            - ResultSet containing column names.
//	 * @throws SQLException
//	 *             - thrown by ResultSet::getColumnNames
//	 */
//
//	/**
//	 * Writes the entire ResultSet to a CSV file.
//	 *
//	 * The caller is responsible for closing the ResultSet.
//	 *
//	 * @param rs
//	 *            the result set to write
//	 * @param includeColumnNames
//	 *            true if you want column names in the output, false otherwise
//	 * @throws java.io.IOException
//	 *             thrown by getColumnValue
//	 * @throws java.sql.SQLException
//	 *             thrown by getColumnValue
//	 */
//
//	/**
//	 * Writes the entire ResultSet to a CSV file.
//	 *
//	 * The caller is responsible for closing the ResultSet.
//	 *
//	 * @param rs
//	 *            the Result set to write.
//	 * @param includeColumnNames
//	 *            include the column names in the output.
//	 * @param trim
//	 *            remove spaces from the data before writing.
//	 *
//	 * @throws java.io.IOException
//	 *             thrown by getColumnValue
//	 * @throws java.sql.SQLException
//	 *             thrown by getColumnValue
//	 */
//
//	/**
//	 * Writes the next line to the file.
//	 *
//	 * @param nextLine
//	 *            a string array with each comma-separated element as a separate
//	 *            entry.
//	 * @param applyQuotesToAll
//	 *            true if all values are to be quoted. false applies quotes only
//	 *            to values which contain the separator, escape, quote or new
//	 *            line characters.
//	 */
//	public String writeNext(String[] nextLine, boolean applyQuotesToAll) {
//		if (nextLine == null) {
//			return "";
//		}
//		StringBuilder sb = new StringBuilder(INITIAL_STRING_SIZE);
//		for (int i = 0; i < nextLine.length; i++) {
//			if (i != 0) {
//				sb.append(separator);
//			}
//			String nextElement = nextLine[i];
//			if (nextElement == null) {
//				continue;
//			}
//			Boolean stringContainsSpecialCharacters = stringContainsSpecialCharacters(nextElement);
//			if ((applyQuotesToAll || stringContainsSpecialCharacters)
//					&& quotechar != NO_QUOTE_CHARACTER) {
//				sb.append(quotechar);
//			}
//			if (stringContainsSpecialCharacters) {
//				sb.append(processLine(nextElement));
//			} else {
//				sb.append(nextElement);
//			}
//			if ((applyQuotesToAll || stringContainsSpecialCharacters)
//					&& quotechar != NO_QUOTE_CHARACTER) {
//				sb.append(quotechar);
//			}
//		}
//		sb.append(lineEnd);
//		return sb.toString();
//	}
//
//	/**
//	 * Writes the next line to the file.
//	 *
//	 * @param nextLine
//	 *            a string array with each comma-separated element as a separate
//	 *            entry.
//	 */
//	public String writeNext(String[] nextLine) {
//		return writeNext(nextLine, true);
//	}
//
//	/**
//	 * checks to see if the line contains special characters.
//	 *
//	 * @param line
//	 *            - element of data to check for special characters.
//	 * @return true if the line contains the quote, escape, separator, newline
//	 *         or return.
//	 */
//	private boolean stringContainsSpecialCharacters(String line) {
//		return line.indexOf(quotechar) != -1 || line.indexOf(escapechar) != -1
//				|| line.indexOf(separator) != -1
//				|| line.contains(DEFAULT_LINE_END) || line.contains("\r");
//	}
//
//	/**
//	 * Processes all the characters in a line.
//	 *
//	 * @param nextElement
//	 *            - element to process.
//	 * @return a StringBuilder with the elements data.
//	 */
//	protected StringBuilder processLine(String nextElement) {
//		StringBuilder sb = new StringBuilder(INITIAL_STRING_SIZE);
//		for (int j = 0; j < nextElement.length(); j++) {
//			char nextChar = nextElement.charAt(j);
//			processCharacter(sb, nextChar);
//		}
//		return sb;
//	}
//
//	/**
//	 * Appends the character to the StringBuilder adding the escape character if
//	 * needed.
//	 *
//	 * @param sb
//	 *            - StringBuffer holding the processed character.
//	 * @param nextChar
//	 *            - character to process
//	 */
//	private void processCharacter(StringBuilder sb, char nextChar) {
//		if (escapechar != NO_ESCAPE_CHARACTER
//				&& (nextChar == quotechar || nextChar == escapechar)) {
//			sb.append(escapechar).append(nextChar);
//		} else {
//			sb.append(nextChar);
//		}
//	}
//
//
//}
