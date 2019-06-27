package com.rateabench

import com.rateabench.db.framework.BenchRepository
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.post
import org.apache.commons.codec.binary.Base64
import java.util.*


/**
 * Created by Jonathan Schurmann on 5/18/19.
 */
fun Routing.upload() {
    post("$V/images") {
        var status = HttpStatusCode.Created
        val postImage = call.receive<PostImage>()
        if (!postImage.isValid()) {
            call.respond(HttpStatusCode.BadRequest)
            return@post
        }
        val bytes: ByteArray = Base64.decodeBase64(postImage.image)
        val uuid = UUID.nameUUIDFromBytes(bytes)
        val benchId = postImage.benchId
        val url = SpacesAPI.uploadFile("$uuid-$benchId", bytes)
        //Insert/replace url to database for benchId
        val success = BenchRepository.updateImageURL(benchId, url)
        if (!success) status = HttpStatusCode.BadRequest
        call.respond(status)
    }
}
