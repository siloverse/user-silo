package io.github.siloverse.user.web.request

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.util.UUID

data class CreateUserRequest(
    @field:NotNull val keycloakId: UUID,
    @field:NotBlank @field:Email val email: String,
    @field:NotBlank val displayName: String,
)