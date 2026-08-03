package com.red.storage

import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/media")
class StorageController(private val storage: StorageService) {

  @PostMapping("/upload")
  fun upload(@RequestParam("file") file: MultipartFile): ResponseEntity<Map<String, String>> {
    require(!file.isEmpty) { "Empty file" }
    val objectName = storage.upload(file)
    return ResponseEntity.ok(
      mapOf(
        "objectName" to objectName,
        "downloadUrl" to "/api/media/$objectName",
        "contentType" to (file.contentType ?: "application/octet-stream"),
        "size" to file.size.toString()
      )
    )
  }

  @GetMapping("/{objectName}", produces = [MediaType.APPLICATION_OCTET_STREAM_VALUE])
  fun download(@PathVariable objectName: String): ResponseEntity<ByteArray> {
    val bytes = storage.download(objectName).use { it.readAllBytes() }
    return ResponseEntity.ok()
      .contentType(MediaType.APPLICATION_OCTET_STREAM)
      .body(bytes)
  }
}
