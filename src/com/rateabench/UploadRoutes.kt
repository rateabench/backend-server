package com.rateabench

import com.google.gson.Gson
import com.rateabench.db.Bench
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.http.content.streamProvider
import io.ktor.request.receiveMultipart
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.post
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import kotlinx.io.ByteArrayOutputStream
import kotlinx.io.InputStream
import kotlinx.io.OutputStream
import java.util.*

/**
 * Created by Jonathan Schurmann on 5/18/19.
 */
fun Routing.upload() {
    post("$V/images") {
        var status = HttpStatusCode.Created
        var postImage: PostImage? = null
        var bytes: ByteArray = ByteArray(0)

        val multipart = call.receiveMultipart()
        multipart.forEachPart { part ->
            when (part) {
                is PartData.FormItem -> {
                    val postData = Gson().fromJson(part.value, PostImage::class.java)
                    if (!postData.isValid()) status = HttpStatusCode.BadRequest
                    postImage = postData
                }
                is PartData.FileItem -> {
                    part.streamProvider().use { its ->
                        bytes = its.copyToSuspend(ByteArrayOutputStream())
                    }
                }

            }
            part.dispose()
        }
        if (status == HttpStatusCode.BadRequest) {
            call.respond(status)
            return@post
        }
        //Post to spaces
        val uuid = UUID.nameUUIDFromBytes(bytes)
        val benchId = postImage!!.benchId
        val url = SpacesAPI.uploadFile("$uuid-$benchId", bytes)
        //Insert/replace url to database for benchId
        val success = Bench.updateImageURL(benchId, url)
        if (!success) status = HttpStatusCode.BadRequest
        call.respond(status)

    }
}

suspend fun InputStream.copyToSuspend(
    out: OutputStream,
    bufferSize: Int = DEFAULT_BUFFER_SIZE,
    yieldSize: Int = 4 * 1024 * 1024,
    dispatcher: CoroutineDispatcher = Dispatchers.IO
): ByteArray {
    return withContext(dispatcher) {
        val buffer = ByteArray(bufferSize)
        var bytesCopied = 0
        var bytesAfterYield = 0
        while (true) {
            val bytes = read(buffer).takeIf { it >= 0 } ?: break
            out.write(buffer, 0, bytes)
            if (bytesAfterYield >= yieldSize) {
                yield()
                bytesAfterYield %= yieldSize
            }
            bytesCopied += bytes
            bytesAfterYield += bytes
        }
        val bytes = ByteArray(bytesCopied)
        out.write(bytes)
        return@withContext bytes
    }
}

