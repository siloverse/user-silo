package io.github.siloverse.user.users.controller

import io.github.siloverse.user.users.domain.UserEntity
import io.github.siloverse.user.users.error.DuplicateUserException
import io.github.siloverse.user.users.persistence.UserRepository
import io.github.siloverse.user.users.service.UserService
import io.github.siloverse.user.web.request.CreateUserRequest
import io.github.siloverse.user.web.response.CreateUserResponse
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/users")
class UserController(private val userService: UserService) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@Valid @RequestBody request: CreateUserRequest): CreateUserResponse {
        val user = userService.createUser(request)
        return CreateUserResponse(user.id!!)
    }

    @ExceptionHandler(DuplicateUserException::class)
    @ResponseStatus(HttpStatus.CONFLICT)
    fun duplicate(e: DuplicateUserException) = mapOf("error" to "duplicate keycloakId is not allowed")
}