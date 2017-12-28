package org.astserver

import java.lang.reflect.Method
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.Lexer
import org.antlr.v4.runtime.Parser
import org.antlr.v4.runtime.RuleContext

/**
 * Container for an ANTLR lexer, parser and root parser rule for parsing a
 * source file of the type of the lexer and parser. For instance, for C, lexer
 * would be an instance of CLexer, parser a CParser and rootRuleMethod would be
 * the compilationUnit method of CParser.
 */
class LexerAndParser(val lexer: Lexer, val parser: Parser,
      val rootRuleMethod: Method) {
  /**
   * Lexes and parses the given source text based on the root source file rule
   * of the grammar for the lexer/parser and returns a RuleContext for the
   * parse. If parsing fails, this may throw an exception.
   */
  @Suppress("UnsafeCast")
  fun parseRootRule(sourceText: String): RuleContext {
    lexer.setInputStream(CharStreams.fromString(sourceText))
    parser.setInputStream(CommonTokenStream(lexer))
    return rootRuleMethod.invoke(parser) as RuleContext
  }
}
