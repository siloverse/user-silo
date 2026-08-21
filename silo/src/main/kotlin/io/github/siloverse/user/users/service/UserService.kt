package io.github.siloverse.user.users.service

import io.github.siloverse.user.users.domain.UserEntity
import io.github.siloverse.user.users.error.DuplicateUserException
import io.github.siloverse.user.users.persistence.UserRepository
import io.github.siloverse.user.web.request.CreateUserRequest
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class UserService(
    private val repository: UserRepository
) {
    fun createUser(createUserRequest: CreateUserRequest): UserEntity {
        if (repository.existsByKeycloakId(createUserRequest.keycloakId)) {
            throw DuplicateUserException(createUserRequest.keycloakId)
        }
        return repository.save(
            UserEntity(
                id = UUID.randomUUID(),
                keycloakId = createUserRequest.keycloakId,
                email = createUserRequest.email,
                displayName = createUserRequest.displayName,
            )
        )
    }
}