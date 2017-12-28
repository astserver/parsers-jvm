package org.astserver

import java.io.PrintStream
import com.google.gson.JsonObject

private const val ERROR_PREFIX = "\"error\":\""
private const val NO_GRAMMAR_ERROR =
    "${ERROR_PREFIX}No grammar specified\""
private const val GRAMMAR_NOT_STRING_ERROR =
    "${ERROR_PREFIX}grammar not a string\""
private const val PARSER_NOT_FOUND_ERROR =
    "${ERROR_PREFIX}Parser not found for grammar\""

/**
 * Retrieves a LexerAndParser instance from the specified parser store based on
 * the "grammar" property of the given params JsonObject. If the gramamr is not
 * valid, or if no parser is found for the grammar, it writes a JSON-snippet
 * formatted error to the specified output PrintStream.
 */
@Suppress("TooGenericExceptionCaught")
fun getParserOrWriteError(parserStore: ParserStore,
    params: JsonObject, output: PrintStream): LexerAndParser? {
  val grammarElement = params.get("grammar")
  if (grammarElement == null) {
    output.print(NO_GRAMMAR_ERROR)
    return null
  }
  if (!grammarElement.isJsonPrimitive ||
      !grammarElement.asJsonPrimitive.isString) {
    output.print(GRAMMAR_NOT_STRING_ERROR)
    return null
  }
  val grammarName = grammarElement.asString

  try {
    return parserStore.getLexerAndParser(grammarName)
  } catch (e: Exception) {
    output.print(PARSER_NOT_FOUND_ERROR)
    return null
  }
}
