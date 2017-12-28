package org.astserver

import java.util.HashMap
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.Lexer
import org.antlr.v4.runtime.Parser
import org.antlr.v4.runtime.TokenStream
import org.antlr.v4.runtime.CharStream
import org.antlr.v4.runtime.CharStreams

/**
 * This is a cache and instantiator for ANTLR lexers/parsers that are loaded
 * dynamically by grammar.
 * @param packagePrefix The package prefix for ANTLR lexers/parser classes, an
 *   empty string if they are in the default package, or their package name plus
 *   a trailing dot of they are in a different package.
 * @param rootRulesByGrammar A map from grammar names e.g. "C" to the names for
 *   the root source file parse rule for that grammar e.g. "compilationUnit".
 */
class ParserStore(private val classLoader: ClassLoader,
    private val packagePrefix: String,
    private val rootRulesByGrammar: Map<String, String>) {
  private val parsersByGrammar = HashMap<String, LexerAndParser>()

  /**
   * Retrieves a LexerAndParser for the given grammar name, or throws an
   * exception if there is no such ANTLR parser compiled for that grammar.
   */
  @Suppress("UnsafeCast")
  fun getLexerAndParser(grammarName: String) =
      parsersByGrammar.computeIfAbsent(grammarName, {
        val rootRuleMethodName = rootRulesByGrammar.get(grammarName)
        if (rootRuleMethodName == null) {
          throw IllegalArgumentException("No source file rule for $grammarName")
        }
        val parserClass = classLoader.loadClass("$packagePrefix${it}Parser")
        val rootRuleMethod = parserClass.getMethod(rootRuleMethodName)
        val lexerClass = classLoader.loadClass("$packagePrefix${it}Lexer")
        val lexer = lexerClass.getConstructor(CharStream::class.java)
            .newInstance(CharStreams.fromString("")) as Lexer
        val parser = parserClass.getConstructor(TokenStream::class.java)
            .newInstance(CommonTokenStream(lexer)) as Parser
        parser.removeErrorListeners()
        LexerAndParser(lexer, parser, rootRuleMethod)
      })
}
