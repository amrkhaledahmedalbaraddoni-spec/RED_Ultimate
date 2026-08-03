package com.red.storage

import com.red.config.StorageProperties
import io.minio.BucketExistsArgs
import io.minio.MakeBucketArgs
import io.minio.MinioClient
import io.minio.PutObjectArgs
import io.minio.GetObjectArgs
import jakarta.annotation.PostConstruct
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.InputStream
import java.util.UUID

/**
 * Local object storage via MinIO (S3-compatible). Files are stored under a private bucket and
 * served back through the backend (which can apply auth) — never exposed publicly.
 */
@Service
class StorageService(props: StorageProperties) {

  private val client: MinioClient = MinioClient.builder()
    .endpoint(props.endpoint)
    .credentials(props.accessKey, props.secretKey)
    .build()

  private val bucket: String = props.bucket

  @PostConstruct
  fun ensureBucket() {
    val exists = runCatching {
      client.bucketExists(BucketExistsArgs.builder().bucket(bucket).build())
    }.getOrDefault(false)
    if (!exists) {
      runCatching { client.makeBucket(MakeBucketArgs.builder().bucket(bucket).build()) }
    }
  }

  fun upload(file: MultipartFile): String {
    val ext = file.originalFilename?.substringAfterLast('.', "")?.let { ".$it" } ?: ""
    val objectName = "${UUID.randomUUID()}$ext"
    client.putObject(
      PutObjectArgs.builder()
        .bucket(bucket)
        .`object`(objectName)
        .stream(file.inputStream, file.size, -1)
        .contentType(file.contentType ?: "application/octet-stream")
        .build()
    )
    return objectName
  }

  fun download(objectName: String): InputStream =
    client.getObject(GetObjectArgs.builder().bucket(bucket).`object`(objectName).build())
}
