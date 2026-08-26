package io.github.siloverse.user.event

import io.github.siloverse.messaging.core.api.Event
import java.time.OffsetDateTime
import java.util.UUID

data class UserRegistered(
    val userId: UUID,
    val keycloakId: UUID,
    val email: String,
    val displayName: String,
    val occurredAt: OffsetDateTime
) : Event
