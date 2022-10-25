package ru.strelchm.testelk

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class TestElkApplication

fun main(args: Array<String>) {
	runApplication<TestElkApplication>(*args)
}
