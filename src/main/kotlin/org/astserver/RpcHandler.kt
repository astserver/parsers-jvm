package org.astserver

import com.google.gson.JsonObject
import java.io.PrintStream

/**
 * Simple interface for a JSON-RPC method handler that writes to a stream.
 */
interface RpcHandler {
  /**
   * JSON-RPC invokation handler method. It is responsible for either:
   * - printing "result":(result json element) to the stream
   * - printing "error":(error json elememnt) to the stream
   * - throwing an exception before it has printed anything to the stream
   * @param params The JSON object of params for the given RPC method
   * @param output The stream to write the JSON result or error of the invokation
   */
  fun printResultOrError(params: JsonObject, output: PrintStream)
}
