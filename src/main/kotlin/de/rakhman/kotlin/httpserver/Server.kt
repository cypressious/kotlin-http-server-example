package de.rakhman.kotlin.httpserver

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.reflect.TypeToken
import fi.iki.elonen.NanoHTTPD

//todo typialias yes or no?
typealias Route = Context.() -> Any
typealias BodyRoute<T> = Context.(T) -> Any

//enum with properties
enum class MimeTypes(val value: String) {
    TextPlain("text/plain"), ApplicationJson("application/json")
}

class Context(
        val session: NanoHTTPD.IHTTPSession
) {
    val responseHeaders = mutableMapOf<String, String>()
}

abstract class AbstractServer : NanoHTTPD(8080) {
    val gson = Gson()

    //optional explicit types, type inference, extension lambda
    val routes: MutableMap<Pair<Method, String>, Context.() -> Any> = mutableMapOf()

    fun register(method: Method, path: String, route: Route) {
        //trailing lambda
        routes.put(method to path) {
            route()
        }
    }

    //inline, reified
    inline fun <reified T> register(method: Method, path: String, noinline route: BodyRoute<T>) {
        val type = object : TypeToken<T>() {}.type

        //trailing lambda, it
        routes.put(method to path) {
            val body = gson.fromJson<T>(session.getBody(), type)
            route(body)
        }
    }

    fun notFound(): Response = newFixedLengthResponse(Response.Status.NOT_FOUND, MimeTypes.TextPlain.value, "Not found")

    override fun serve(session: IHTTPSession): Response {
        // to infix function, elvis operator
        val route = routes[session.method to session.uri] ?: return notFound()

        val context = Context(session)

        //invoke operator
        val result = context.route()

        //when, smart cast, when as expression
        val response = when (result) {
            is Response -> result
            is JsonElement -> newFixedLengthResponse(Response.Status.OK, MimeTypes.ApplicationJson.value, result.toString())
            else -> newFixedLengthResponse(Response.Status.OK, MimeTypes.ApplicationJson.value, gson.toJson(result))
        }

        //it
        context.responseHeaders.forEach {
            response.addHeader(it.key, it.value)
        }

        return response
    }
}

class Person(
        val name: String,
        val age: Int
)

class PersonResult(
        val person: Person,
        val accepted: Boolean
)

class Server : AbstractServer() {

    //init
    init {
        register(Method.GET, "/hello") {
            this.responseHeaders.put("X-Foo", "Bar")
            "hello world"
        }

        register<Person>(Method.POST, "/post") { person ->
            //named parameter
            PersonResult(person, accepted = true)
        }
    }
}