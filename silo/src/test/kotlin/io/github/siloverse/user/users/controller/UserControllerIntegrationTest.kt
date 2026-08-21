package io.github.siloverse.user.users.controller

import io.github.siloverse.user.users.persistence.UserRepository
import io.github.siloverse.user.web.request.CreateUserRequest
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import java.util.UUID

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerIntegrationTest {

    @Autowired
    final lateinit var userRepository: UserRepository

    @Autowired
    final lateinit var mockMvc: MockMvc

    @Test
    fun `test unique user creation successful`() {
        mockMvc.post("/api/users") {
            with(jwt().jwt {
                it.subject("some-uuid").claim("preferred_username", "system_service")
            }.authorities(SimpleGrantedAuthority("ROLE_SYSTEM")))
            contentType = MediaType.APPLICATION_JSON
            content = CreateUserRequest(
                keycloakId = UUID.randomUUID(),
                email = "xyz@xyz.xyz",
                displayName = "XYZ"
            )
        }.andExpect {
            status { isCreated() }
        }
    }
}