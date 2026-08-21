package io.github.siloverse.auth.error

class DuplicateUserException(email: String) : RuntimeException("user with this email [$email] already exists")