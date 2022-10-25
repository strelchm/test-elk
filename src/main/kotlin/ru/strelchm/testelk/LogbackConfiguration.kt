package ru.strelchm.testelk

import ch.qos.logback.classic.AsyncAppender
import ch.qos.logback.classic.LoggerContext
import net.logstash.logback.appender.LogstashTcpSocketAppender
import net.logstash.logback.encoder.LogstashEncoder
import net.logstash.logback.stacktrace.ShortenedThrowableConverter
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import java.net.InetSocketAddress

@Configuration
class LogbackConfiguration(
    @param:Value("\${spring.application.name}") private val appName: String,
    @param:Value("\${logstash.host}") private val logstashHost: String,
    @param:Value("\${logstash.port}") private val logstashPort: Int,
    @param:Value("\${logstash.queue-size}") private val logstashQueueSize: Int
) {
    private val LOG = LoggerFactory.getLogger(LogbackConfiguration::class.java)
    private val CONTEXT = LoggerFactory.getILoggerFactory() as LoggerContext

    private fun addLogstashAppender(context: LoggerContext) {
        LOG.info("Initializing Logstash logging")
        val logstashAppender = LogstashTcpSocketAppender()
        logstashAppender.name = LOGSTASH_APPENDER_NAME
        logstashAppender.context = context
        val customFields = "{\"servicename\":\"" + appName + "\"}"
        // More documentation is available at: https://github.com/logstash/logstash-logback-encoder
        val logstashEncoder = LogstashEncoder() // Set the Logstash appender config
        logstashEncoder.customFields = customFields
        logstashAppender.addDestinations(InetSocketAddress(logstashHost, logstashPort))
        val throwableConverter = ShortenedThrowableConverter()
        throwableConverter.isRootCauseFirst = true
        logstashEncoder.throwableConverter = throwableConverter
        logstashEncoder.customFields = customFields
        logstashAppender.encoder = logstashEncoder
        logstashAppender.start()
        // Wrap the appender in an Async appender for performance
        val asyncLogstashAppender = AsyncAppender()
        asyncLogstashAppender.context = context
        asyncLogstashAppender.name = ASYNC_LOGSTASH_APPENDER_NAME
        asyncLogstashAppender.queueSize = logstashQueueSize
        asyncLogstashAppender.addAppender(logstashAppender)
        asyncLogstashAppender.start()
        context.getLogger("ROOT").addAppender(asyncLogstashAppender)
    }

    companion object {
        private const val LOGSTASH_APPENDER_NAME = "LOGSTASH"
        private const val ASYNC_LOGSTASH_APPENDER_NAME = "ASYNC_LOGSTASH"
    }

    init {
        addLogstashAppender(CONTEXT)
    }
}