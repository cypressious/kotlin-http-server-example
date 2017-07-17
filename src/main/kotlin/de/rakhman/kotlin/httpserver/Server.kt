package de.rakhman.kotlin.httpserver

import com.google.gson.Gson
import com.google.gson.JsonElement
import fi.iki.elonen.NanoHTTPD
import fi.iki.elonen.NanoHTTPD.Method.POST
import fi.iki.elonen.NanoHTTPD.Response.Status.NOT_FOUND
import fi.iki.elonen.NanoHTTPD.Response.Status.OK

class Server : NanoHTTPD(8080) {

    val gson = Gson()
    val routes: MutableMap<Pair<Method, String>, Context.() -> Any> = mutableMapOf()

    fun register(method: Method, path: String, route: Context.() -> Any) {
        routes.put(method to path) {
            route()
        }
    }

    inline fun <reified T> register(path: String, crossinline route: Context.(T) -> Any) {
        routes.put(POST to path) {
            val body = gson.fromJson<T>(session.getBody(), T::class.java)
            route(body)
        }

    }

    private fun notFound() = newFixedLengthResponse(NOT_FOUND, "text/plain", "Not found")

    override fun serve(session: IHTTPSession): Response {
        val method = session.method
        val path = session.uri

        val key = method to path
        val route = routes[key] ?: return notFound()

        val context = Context(session)
        val result = context.route()

        val response = when (result) {
            is Response -> result
            is JsonElement -> newFixedLengthResponse(OK, "application/json", result.toString())
            else -> newFixedLengthResponse(OK, "application/json", gson.toJson(result))
        }

        for ((name, value) in context.responseHeaders) {
            response.addHeader(name, value)
        }

        return response
    }
}

class Context(
        val session: NanoHTTPD.IHTTPSession,
        val responseHeaders: MutableMap<String, String> = mutableMapOf()
)
