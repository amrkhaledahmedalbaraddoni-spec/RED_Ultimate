package com.red.security

import ch.qos.logback.classic.pattern.MessageConverter
import ch.qos.logback.classic.spi.ILoggingEvent

class ScrubbedMessageConverter : MessageConverter() {
  override fun convert(event: ILoggingEvent): String {
    return LogScrubber.scrub(super.convert(event))
  }
}
