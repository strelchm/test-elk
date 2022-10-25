package ru.strelchm.testelk

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import lombok.extern.slf4j.Slf4j
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import java.util.concurrent.TimeUnit

@RestController("/test")
@Slf4j
class TestController(@Autowired private var meterRegistry: MeterRegistry) {
    private val testCounter: Counter = meterRegistry.counter("test_counter");
    private val testTimer: Timer = meterRegistry.timer("test_timer");
    private val testTimerBuilder: Timer.Builder = Timer.builder("time.test")
        .publishPercentiles(0.5, 0.7, 0.9, 0.95, 0.99)
        .description("test query with percentiles");
    private var callTime: Long = System.currentTimeMillis()

    companion object {
        val LOG = LoggerFactory.getLogger(TestController::class.java.name)!!
    }

    @GetMapping("/{message}")
    fun test(@PathVariable message: String): String {
        LOG.info(message)
        testCounter.increment()
        testTimer.record(System.currentTimeMillis() - callTime, TimeUnit.MILLISECONDS)
        callTime = System.currentTimeMillis()

        testTimerBuilder.tags("tag", "tag_val_1")
            .register(meterRegistry)
            .record(System.currentTimeMillis() - callTime, TimeUnit.MILLISECONDS)

        testTimerBuilder.tags("tag", "tag_val_2")
            .register(meterRegistry)
            .record(System.currentTimeMillis(), TimeUnit.MILLISECONDS)

        return "OK... $message"
    }
}