plugins {
    id("io.github.siloverse.spring-boot-application")
    id("io.github.siloverse.openapi-spec")
}

application {
    mainClass.set("io.github.siloverse.user.ApplicationKt")
}

dependencies {

    implementation(project(":messages"))
    implementation(project(":ui"))
    implementation(project(":web"))

    implementation(libs.bundles.spring.web)
    implementation(libs.spring.boot.starter.data.jpa)

    implementation(local.spring.boot.starter.flyway)
    implementation(local.bundles.siloverse.messaging)
    implementation(local.bundles.spring.security)
    implementation(libs.bundles.siloverse.observability)
    implementation(local.siloverse.openapi.springdoc)

    runtimeOnly(local.flyway.postgresql)
    runtimeOnly(local.postgresql)

    testImplementation(local.spring.security.test)
    testImplementation(local.spring.boot.webmvc.test)

    testImplementation(local.spring.boot.testcontainers)
    testImplementation(libs.testcontainers.postgresql)
    testImplementation(libs.testcontainers.rabbitmq)
}