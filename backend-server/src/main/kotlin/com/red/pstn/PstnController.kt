package com.red.pstn

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/pstn")
class PstnController(private val pstn: PstnService) {

  @PostMapping("/dial")
  fun dial(@RequestParam number: String) = pstn.dial(number)

  @GetMapping("/sim")
  fun sim() = pstn.simStatus()
}
