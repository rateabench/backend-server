package com.rateabench

import com.google.gson.Gson
import com.rateabench.db.framework.BenchRepository
import com.rateabench.db.framework.RatingRepository
import com.rateabench.db.framework.ReviewRepository
import io.ktor.application.call
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

const val V = Constants.VERSION

@Location("$V/benches/{id}")
class BenchRoute(val id: Long) {
    @Location("/ratings")
    class RatingRoute(val bench: BenchRoute)

    @Location("/reviews")
    class ReviewRoute(val bench: BenchRoute)

}


@KtorExperimentalLocationsAPI
fun Routing.api() {

    get<BenchRoute.ReviewRoute> { reviewRoute ->
        val reviews = ReviewRepository.getReviewsByBenchId(reviewRoute.bench.id)
        call.respondText(
            Gson().toJson(reviews),
            ContentType.Application.Json
        )
    }

    get<BenchRoute.RatingRoute> { ratingRoute ->
        val ratings = RatingRepository.getRatingsByBenchId(ratingRoute.bench.id)
        call.respondText(
            Gson().toJson(ratings),
            ContentType.Application.Json
        )
    }

    post("$V/benches") {
        val bench = call.receive<PostBench>()
        var statusCode = HttpStatusCode.Created
        if (!bench.isValid()) statusCode = HttpStatusCode.BadRequest
        if (!BenchRepository.insert(bench)) statusCode = HttpStatusCode.InternalServerError
        call.respond(statusCode)
    }

    get<BenchRoute> { benchRoute ->
        val bench = BenchRepository.getByPK(benchRoute.id)
        if (bench == null) {
            call.respond(HttpStatusCode.NotFound)
        }
        call.respondText(
            Gson().toJson(bench),
            ContentType.Application.Json
        )
    }

    get("$V/benches") {
        val benches = BenchRepository.getAll()
        call.respondText(
            Gson().toJson(benches),
            ContentType.Application.Json
        )
    }
}
