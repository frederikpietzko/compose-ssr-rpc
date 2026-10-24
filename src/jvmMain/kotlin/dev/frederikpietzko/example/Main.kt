package dev.frederikpietzko.example

import dev.frederikpietzko.example.rpc.plumbing.EnableRpc
import dev.frederikpietzko.example.spring.plumbing.EnablePages
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableRpc
@EnablePages
class Application

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}