package io.github.siloverse.user.users.service

import io.github.siloverse.messaging.core.api.TransactionAwareAsynchronousBus
import io.github.siloverse.user.event.UserRegistered
import io.github.siloverse.user.users.domain.UserEntity
import io.github.siloverse.user.users.error.DuplicateUserException
import io.github.siloverse.user.users.persistence.UserRepository
import io.github.siloverse.user.web.request.CreateUserRequest
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserService(
    private val repository: UserRepository,
    private val transactionAwareAsynchronousBus: TransactionAwareAsynchronousBus
) {
    @Transactional
    fun createUser(createUserRequest: CreateUserRequest): UserEntity {
        if (repository.existsByKeycloakId(createUserRequest.keycloakId)) {
            throw DuplicateUserException(createUserRequest.keycloakId)
        }
        val user = repository.save(
            UserEntity(
                keycloakId = createUserRequest.keycloakId,
                email = createUserRequest.email,
                displayName = createUserRequest.displayName,
            )
        )
        transactionAwareAsynchronousBus.publish(
            UserRegistered(
                userId = user.id,
                keycloakId = user.keycloakId,
                email = user.email,
                displayName = user.displayName,
                occurredAt = user.updatedAt,
            )
        )
        return user
    }
}