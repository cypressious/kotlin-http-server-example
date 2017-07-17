package de.rakhman.kotlin.httpserver

import com.google.gson.JsonObject
import fi.iki.elonen.NanoHTTPD.Method.GET

fun main(args: Array<String>) {
    val server = Server()

    server.register(GET, "/hello") {
        val json = JsonObject()
        val name = this.session.parms["name"]
        json.addProperty("text", "Hello, $name")
        json
    }

    server.register<Person>("/register") { person ->
        responseHeaders.put("X-Foo", "Bar")

        RegistrationResult(person, registered = true)
    }

    server.start()
    readLine()
}

class Person(
        val name: String,
        val age: Int
)

class RegistrationResult(
        val person: Person,
        val registered: Boolean
)