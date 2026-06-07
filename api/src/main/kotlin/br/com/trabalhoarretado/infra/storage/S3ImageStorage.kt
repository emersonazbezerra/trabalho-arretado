package br.com.trabalhoarretado.infra.storage

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.BucketAlreadyOwnedByYouException
import software.amazon.awssdk.services.s3.model.CreateBucketRequest
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import software.amazon.awssdk.services.s3.model.S3Exception
import java.net.URI
import java.util.concurrent.atomic.AtomicBoolean

data class S3Config(
    val endpoint: String,
    val region: String,
    val accessKey: String,
    val secretKey: String,
    val bucket: String,
    val publicBaseUrl: String,
    val pathStyle: Boolean,
) {
    val defaultAvatarUrl: String
        get() = "${publicBaseUrl.trimEnd('/')}/default-avatar.jpg"
}

class S3ImageStorage(
    private val config: S3Config,
) : ImageStorage {
    private val client: S3Client =
        S3Client
            .builder()
            .endpointOverride(URI.create(config.endpoint))
            .region(Region.of(config.region))
            .credentialsProvider(
                StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(config.accessKey, config.secretKey),
                ),
            ).forcePathStyle(config.pathStyle)
            .httpClient(UrlConnectionHttpClient.create())
            .build()

    private val bucketReady = AtomicBoolean(false)

    override suspend fun upload(
        bytes: ByteArray,
        contentType: String,
        key: String,
    ): String =
        withContext(Dispatchers.IO) {
            ensureBucket()
            client.putObject(
                PutObjectRequest
                    .builder()
                    .bucket(config.bucket)
                    .key(key)
                    .contentType(contentType)
                    .contentLength(bytes.size.toLong())
                    .build(),
                RequestBody.fromBytes(bytes),
            )
            "${config.publicBaseUrl.trimEnd('/')}/$key"
        }

    private fun ensureBucket() {
        if (bucketReady.get()) return
        try {
            client.createBucket(CreateBucketRequest.builder().bucket(config.bucket).build())
        } catch (_: BucketAlreadyOwnedByYouException) {
            // already exists, ignore
        } catch (e: S3Exception) {
            // S3 Ninja / outras impls retornam 409 sem mapear para a exceção tipada
            if (e.statusCode() != 409) throw e
        }
        bucketReady.set(true)
    }
}
