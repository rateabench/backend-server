package com.rateabench

import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.core.sync.ResponseTransformer
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import java.io.InputStream
import java.net.URI


/**
 * Created by Jonathan Schurmann on 5/19/19.
 */
class SpacesAPI {
    companion object {
        private const val bucketName = "imagesbucket"
        private val client: S3Client =
            S3Client.builder()
                .endpointOverride(
                    URI.create(Constants.SPACES_URL)
                ).build()

        fun uploadFile(key: String, bytes: ByteArray): String {
            client.putObject(
                PutObjectRequest.builder().bucket(bucketName).key(key).build(),
                RequestBody.fromBytes(bytes)
            )
            return "${Constants.SPACES_URL}$bucketName/$key"
        }

        fun getFile(key: String): InputStream {
            return client.getObject(
                GetObjectRequest.builder().bucket(bucketName).key(key).build(),
                ResponseTransformer.toBytes()
            ).asInputStream()
        }

        fun deleteFile(key: String): String {
            client.deleteObject(DeleteObjectRequest.builder().bucket(bucketName).key(key).build())
            return "${Constants.SPACES_URL}$bucketName/$key"
        }
    }


}
