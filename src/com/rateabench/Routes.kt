package com.rateabench

import com.google.gson.Gson
import com.rateabench.db.Bench
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.StatusPages
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.get
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.sessions.get
import io.ktor.sessions.sessions
import io.ktor.sessions.set

/**
 * Created by Jonathan Schurmann on 5/16/19.
 */

class Constants {
    companion object {
        const val VERSION = "/v1"
    }
}

const val V = Constants.VERSION

@Location("$V/benches/{id}")
class BenchRoute(val id: Long)


@KtorExperimentalLocationsAPI
fun Routing.api() {

    get("$V/benches") {
        call.respondText(
            Gson().toJson(Bench.getAll()), ContentType.Application.Json
        )
    }
    post("$V/benches") {
        val bench = call.receive<PostBench>()
        var statusCode = HttpStatusCode.Created
        if (!bench.isValid()) statusCode = HttpStatusCode.BadRequest
        if (!Bench.insert(bench)) statusCode = HttpStatusCode.InternalServerError
        call.respond(statusCode)
    }

    get<BenchRoute> { benchRoute ->
        val bench = Bench.getByPK(benchRoute.id)
        if (bench == null) {
            call.respond(HttpStatusCode.NotFound)
        }
        call.respondText(
            Gson().toJson(bench),
            ContentType.Application.Json
        )

    }

    get("/session/increment") {
        val session = call.sessions.get<MySession>() ?: MySession()
        call.sessions.set(session.copy(count = session.count + 1))
        call.respondText("Counter is ${session.count}. Refresh to increment.")
    }

    install(StatusPages) {
        exception<AuthenticationException> { cause ->
            call.respond(HttpStatusCode.Unauthorized)
        }
        exception<AuthorizationException> { cause ->
            call.respond(HttpStatusCode.Forbidden)
        }

    }

}
