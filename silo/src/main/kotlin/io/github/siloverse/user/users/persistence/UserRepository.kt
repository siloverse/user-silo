package io.github.siloverse.user.users.persistence

import io.github.siloverse.user.users.domain.UserEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface UserRepository : JpaRepository<UserEntity, UUID> {
    fun existsByKeycloakId(keycloakId: UUID): Boolean
}