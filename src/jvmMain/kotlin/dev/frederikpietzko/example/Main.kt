package dev.frederikpietzko.example

import dev.frederikpietzko.example.rpc.plumbing.EnableRpc
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableRpc
class Application

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}