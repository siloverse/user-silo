package io.github.siloverse.user

import io.github.siloverse.openapi.springdoc.OpenApiSpec
import io.github.siloverse.user.config.TestcontainersConfiguration
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.context.annotation.Import
import org.springframework.test.web.servlet.MockMvc

/**
 * The committed openapi-spec.yaml must match what the application describes. A drift fails here
 * and names the fix: `./gradlew test -PupdateOpenApiSpec`, then review the diff and commit.
 */
@SpringBootTest
@Import(TestcontainersConfiguration::class)
@AutoConfigureMockMvc
class OpenApiSpecTest(@Autowired private val mockMvc: MockMvc) {

    @Test
    fun `the committed contract matches the application`() = OpenApiSpec.verify(mockMvc)
}
