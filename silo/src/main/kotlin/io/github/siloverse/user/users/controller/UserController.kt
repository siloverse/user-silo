package io.github.siloverse.user.users.controller

import io.github.siloverse.user.users.error.DuplicateUserException
import io.github.siloverse.user.users.service.UserService
import io.github.siloverse.user.web.request.CreateUserRequest
import io.github.siloverse.user.web.response.CreateUserResponse
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/users")
class UserController(private val userService: UserService) {

    private val logger = LoggerFactory.getLogger(UserController::class.java)

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@Valid @RequestBody request: CreateUserRequest): CreateUserResponse {
        val user = userService.createUser(request)
        logger.info("User[${user.id}] is created and reported.")
        return CreateUserResponse(user.id)
    }

    @ExceptionHandler(DuplicateUserException::class)
    @ResponseStatus(HttpStatus.CONFLICT)
    fun duplicate(e: DuplicateUserException) =
        mapOf("error" to "duplicate keycloakId is not allowed")
}