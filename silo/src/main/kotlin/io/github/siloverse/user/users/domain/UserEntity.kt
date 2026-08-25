package io.github.siloverse.user.users.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.OffsetDateTime
import java.util.*

@Entity
@Table(name = "users", schema = "user_silo")
class UserEntity(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(name = "keycloak_id", nullable = false, unique = true)
    var keycloakId: UUID,

    @Column(nullable = false, unique = true)
    var email: String,

    @Column(name = "display_name", nullable = false)
    var displayName: String,
) {
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: OffsetDateTime = OffsetDateTime.now()

    @Column(name = "updated_at", nullable = false)
    var updatedAt: OffsetDateTime = OffsetDateTime.now()
}