package org.astserver

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import java.io.PrintStream

/**
 * JSON-RPC handler for the "getNodeKinds" method. It expects a "grammar" string
 * property in the "params" JSON object that corresponds to an ANTLR parser
 * grammar. It will print an array of the rule names for the given grammar such
 * that the indices of the array correspond with the abstract syntax tree node
 * numbers that the parse RPC handler returns.
 */
class GetNodeKindsHandler(private val parserStore: ParserStore) : RpcHandler {
  private val gson = Gson()

  override fun printResultOrError(params: JsonObject, output: PrintStream) {
    val lexerAndParser = getParserOrWriteError(parserStore, params, output)
    if (lexerAndParser == null) {
      return
    }

    val ruleNames = lexerAndParser.parser.getRuleNames()
    val ruleNamesJson = JsonArray(ruleNames.size)
    for (ruleName in ruleNames) {
      ruleNamesJson.add(ruleName)
    }
    output.print("\"result\":")
    gson.toJson(ruleNamesJson, output)
  }
}
