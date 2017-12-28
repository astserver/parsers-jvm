package org.astserver

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSyntaxException
import java.io.PrintStream

private const val ERROR_PREFIX = "\"error\":"
private const val RESPONSE_PREFIX = "{\"jsonrpc\":\"2.0\",\"id\":"
private const val ERROR_RESPONSE_PREFIX =
    "${RESPONSE_PREFIX}null,$ERROR_PREFIX"
private const val INVALID_JSON_ERROR =
    "${ERROR_RESPONSE_PREFIX}\"Invalid JSON\"}"
private const val NOT_JSON_OBJECT_ERROR =
    "${ERROR_RESPONSE_PREFIX}\"Command not an object\"}"
private const val MISSING_ID_ERROR = "${ERROR_RESPONSE_PREFIX}\"Missing id\"}"
private const val MISSING_METHOD_ERROR = "Missing method"
private const val METHOD_NOT_STRING_ERROR = "Method not a string"
private const val INVALID_METHOD_ERROR = "Invalid method"
private const val MISSING_PARAMS = "Missing params"
private const val PARAMS_NOT_OBJECT_ERROR = "Params not an object"

/**
 * JSON-RPC 2.0 dispatcher that parses calls and delegates to RpcHandler
 * instances.
 * @param handlerMap Map from JSON-RPC methods to handler instances.
 * @param output The PrintStream to write JSON call responses to.
 */
class RpcDispatcher(private val handlerMap: Map<String, RpcHandler>,
    private val output: PrintStream) {
  private val gson = Gson()
  private val jsonParser = JsonParser()

  /**
   * Parses a JSON-RPC 2.0 command message (printing errors if invalid in any
   * way), then dispatches it to the appropriate handler instance. The responses
   * are written to the PrintStream configured in the dispatcher constructor.
   * @param jsonCommand String of the JSON-RPC command to be run.
   */
  @Suppress("TooGenericExceptionCaught")
  fun handleRpc(jsonCommand: String) {
    val command = parseCommandOrPrintError(jsonCommand)
    if (command == null) {
      return
    }

    val commandId = command.get("id")
    if (commandId == null) {
      output.print(MISSING_ID_ERROR)
      return
    }

    val method = getMethodOrPrintError(command, commandId)
    if (method == null) {
      return
    }

    val handler = handlerMap.get(method)
    if (handler == null) {
      printErrorWithId(commandId, INVALID_METHOD_ERROR)
      return
    }

    val params = getParamsOrPrintError(command, commandId)
    if (params == null) {
      return
    }

    output.print(RESPONSE_PREFIX)
    gson.toJson(commandId, output)
    output.print(",")
    try {
      handler.printResultOrError(params, output)
    } catch (exception: Exception) {
      output.print(ERROR_PREFIX)
      gson.toJson(JsonPrimitive(exception.toString()), output)
    }
    output.print("}")
  }

  private fun parseCommandOrPrintError(jsonCommand: String): JsonObject? {
    var commandElement: JsonElement?
    try {
      commandElement = jsonParser.parse(jsonCommand)
    } catch (e: JsonSyntaxException) {
      output.print(INVALID_JSON_ERROR)
      return null
    }

    if (!commandElement.isJsonObject) {
      output.print(NOT_JSON_OBJECT_ERROR)
      return null
    }
    return commandElement.asJsonObject
  }

  private fun getMethodOrPrintError(command: JsonObject,
      id: JsonElement): String? {
    val methodElement = command.get("method")
    if (methodElement == null) {
      printErrorWithId(id, MISSING_METHOD_ERROR)
      return null
    }
    if (!methodElement.isJsonPrimitive ||
        !methodElement.asJsonPrimitive.isString) {
      printErrorWithId(id, METHOD_NOT_STRING_ERROR)
      return null
    }
    return methodElement.asString
  }

  private fun getParamsOrPrintError(command: JsonObject,
      id: JsonElement): JsonObject? {
    val paramsElement = command.get("params")
    if (paramsElement == null) {
      printErrorWithId(id, MISSING_PARAMS)
      return null
    }
    if (!paramsElement.isJsonObject) {
      printErrorWithId(id, PARAMS_NOT_OBJECT_ERROR)
      return null
    }
    return paramsElement.asJsonObject
  }

  private fun printErrorWithId(id: JsonElement, error: String) {
    output.print("{\"jsonrpc\":\"2.0\",\"id\":")
    gson.toJson(id, output)
    output.print(",$ERROR_PREFIX")
    gson.toJson(JsonPrimitive(error), output)
    output.print("}")
  }
}
