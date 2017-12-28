package org.astserver

/**
 * Runs a simple JSON-RPC parse service that reads JSON call lines from stdin
 * and writes JSON response lines to stdout.
 * See the repo README.md for details.
 */
fun main(args: Array<String>) {
  runAstServer(System.`in`, System.out)
}
