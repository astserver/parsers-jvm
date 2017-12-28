package org.astserver

import com.google.gson.Gson
import com.google.gson.JsonObject
import java.io.PrintStream
import java.util.ArrayDeque
import java.util.Deque
import org.antlr.v4.runtime.RuleContext

private const val NO_SOURCE_TEXT_ERROR =
    "\"error\":\"No sourceText specified\""
private const val SOURCE_TEXT_NOT_STRING_ERROR =
    "\"error\":\"sourceText not a string\""

/**
 * JSON-RPC handler for the "parse" method. It expects a "grammar" string
 * property in the "params" JSON object that corresponds to an ANTLR parser
 * grammar, and a "sourceFile" string property for the source to parse. It will
 * print the abstract syntax tree (AST) of the source file text, or an error if
 * parsing failed.
 *
 * The printed AST as a JSON array of nested arrays is in the following format:
 * result:[(node kind index), (node start position), (node length), (child 1),
 *      (child2), (child3), ...]
 * The children are printed in the same format. A node with no children has only
 * the basic node kind, node start and node length items in its array.
 * The node kind indices correspond to the indices of the grammar rule names
 * returned by the "getNodeKinds" JSON RPC method.
 *
 * See the repo README.md for more info.
 */
class ParseHandler(private val parserStore: ParserStore) : RpcHandler {
  private val gson = Gson()

  // Special indicator of a closing bracket for pushing on the stack
  private val closeBracketPlaceholder = RuleContext()

  override fun printResultOrError(params: JsonObject, output: PrintStream) {
    val lexerAndParser = getParserOrWriteError(parserStore, params, output)
    if (lexerAndParser == null) {
      return
    }
    val sourceText = getSourceTextOrWriteError(params, output)
    if (sourceText == null) {
      return
    }

    val astRoot = lexerAndParser.parseRootRule(sourceText)
    output.print("\"result\":")
    writeAstJson(astRoot, output)
  }

  private fun getSourceTextOrWriteError(params: JsonObject,
      output: PrintStream): String? {
    val sourceTextElement = params.get("sourceText")
    if (sourceTextElement == null) {
      output.print(NO_SOURCE_TEXT_ERROR)
      return null
    }
    if (!sourceTextElement.isJsonPrimitive ||
        !sourceTextElement.asJsonPrimitive.isString) {
      output.print(SOURCE_TEXT_NOT_STRING_ERROR)
      return null
    }
    return sourceTextElement.asString
  }

  private fun writeAstJson(root: RuleContext, output: PrintStream) {
    val nodeStack = ArrayDeque<RuleContext>()
    printNode(root, nodeStack, output)

    while (!nodeStack.isEmpty()) {
      val node = nodeStack.pop()
      if (node == closeBracketPlaceholder) {
        output.print(']')
      } else {
        output.print(',')
        printNode(node, nodeStack, output)
      }
    }
  }

  private fun printNode(node: RuleContext, nodeStack: Deque<RuleContext>,
                        output: PrintStream) {
    output.print('[')
    output.print(node.ruleIndex)
    output.print(',')
    output.print(node.sourceInterval.a)
    output.print(',')
    output.print(node.sourceInterval.b)

    nodeStack.push(closeBracketPlaceholder)
    var childIndex = node.childCount - 1
    while (childIndex >= 0) {
      val child = node.getChild(childIndex)
      if (child is RuleContext) {
        nodeStack.push(child)
      }
      childIndex--
    }
  }
}
