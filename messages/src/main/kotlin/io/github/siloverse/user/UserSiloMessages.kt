package io.github.siloverse.user

import io.github.siloverse.messaging.core.naming.MessageNameRegistry
import io.github.siloverse.user.event.UserRegistered

object UserSiloMessages {
    fun names(): MessageNameRegistry {
        return MessageNameRegistry.builder()
            .register(UserRegistered::class.java, "user-silo.user-registered")
            .freeze()
    }
}