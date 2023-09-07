package com.danielmaile.simplemessageapi

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class SimpleMessageApiApplication

fun main(args: Array<String>) {
    runApplication<SimpleMessageApiApplication>(*args)
}
