package io.github.siloverse.user.users.error

import java.util.UUID

class DuplicateUserException(keyCloakId: UUID) : RuntimeException(
    "user with this KeyCloakId [$keyCloakId] already exists"
)