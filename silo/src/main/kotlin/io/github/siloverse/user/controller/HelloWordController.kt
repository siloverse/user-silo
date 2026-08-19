package io.github.siloverse.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class HelloWordController {

    @GetMapping("/")
    fun helloWorld(): String {
        return "Hello World"
    }

}