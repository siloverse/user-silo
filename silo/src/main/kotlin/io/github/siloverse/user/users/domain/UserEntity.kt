package io.github.siloverse.user.users.domain

import jakarta.persistence.Entity
import jakarta.persistence.*
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "users", schema = "user_silo")
class UserEntity(
    @Id
    var id: UUID = UUID.randomUUID(),

    @Column(name = "keycloak_id", nullable = false, unique = true)
    var keycloakId: UUID = UUID(0, 0),

    @Column(nullable = false, unique = true)
    var email: String = "",

    @Column(name = "display_name", nullable = false)
    var displayName: String = "",
) {
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: OffsetDateTime = OffsetDateTime.now()

    @Column(name = "updated_at", nullable = false)
    var updatedAt: OffsetDateTime = OffsetDateTime.now()
}