package io.github.siloverse.user.config

import io.github.siloverse.pagination.spring.config.PaginationConfiguration
import io.github.siloverse.projector.spring.config.ProjectorConfiguration
import io.github.siloverse.selector.spring.config.SelectorConfiguration
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

/**
 * springdoc describes this silo's API; the committed openapi-spec.yaml is the contract the
 * kotlin-client is generated from. The query-chapter configurations ride along so list
 * endpoints, when they come, are described completely (filters, paging, sort, includes).
 */
@Configuration(proxyBeanMethods = false)
@Import(
    io.github.siloverse.openapi.springdoc.OpenApiConfiguration::class,
    PaginationConfiguration::class,
    SelectorConfiguration::class,
    ProjectorConfiguration::class,
)
class OpenApiConfiguration
