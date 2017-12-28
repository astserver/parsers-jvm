package org.astserver

import java.io.InputStream
import java.io.PrintStream
import java.util.Scanner

// The auto-generated ANTLR parser and lexer classes are in the default package.
private const val PARSER_PACKAGE_PREFIX = ""

/**
 * Runs a simple ANTLR parser JSON-RPC service that listens for input JSON lines
 * from an input stream and writes output to a print stream. It supports
 * "parse" and "getNodeKinds" JSON-RPC methods.
 * @param input The input stream to listen for JSON-RPC call lines from.
 * @param output The print stream that JSON-RPC response lines are written to.
 */
fun runAstServer(input: InputStream, output: PrintStream) {
  val parserStore = ParserStore(ClassLoader.getSystemClassLoader(),
      PARSER_PACKAGE_PREFIX, rootRulesByGrammar)
  val handlerMap = mapOf(
      "parse" to ParseHandler(parserStore),
      "getNodeKinds" to GetNodeKindsHandler(parserStore))
  val rpcDispatcher = RpcDispatcher(handlerMap, output)
  val inputScanner = Scanner(input)

  while (inputScanner.hasNext()) {
    val jsonCommand = inputScanner.nextLine()
    rpcDispatcher.handleRpc(jsonCommand)
    output.append('\n')
  }
}
