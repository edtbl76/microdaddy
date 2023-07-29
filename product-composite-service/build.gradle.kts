plugins {
    java
    id("org.springframework.boot") version "3.1.0"
    id("io.spring.dependency-management") version "1.1.0"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":api"))
    implementation(project(":util"))
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-aop")
    implementation("org.springframework.security:spring-security-oauth2-resource-server")
    implementation("org.springframework.security:spring-security-oauth2-jose")
    implementation("org.springframework.cloud:spring-cloud-starter-stream-rabbit:4.0.3")
    implementation("org.springframework.cloud:spring-cloud-starter-stream-kafka:4.0.3")
    implementation("org.springframework.cloud:spring-cloud-commons:4.0.3")
    implementation("io.micrometer:micrometer-tracing-bridge-otel:1.1.2")
    implementation("io.opentelemetry:opentelemetry-exporter-zipkin:1.27.0")
    implementation("org.springframework.retry:spring-retry:2.0.2")
    implementation("org.springframework.boot:spring-boot-starter-aop")
    implementation("org.springdoc:springdoc-openapi-starter-webflux-ui:2.1.0")
    implementation("io.github.resilience4j:resilience4j-spring-boot3:2.0.2")
    implementation("io.github.resilience4j:resilience4j-reactor:2.0.2")
    testImplementation("org.projectlombok:lombok:1.18.26")
    compileOnly("org.projectlombok:lombok")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    annotationProcessor("org.projectlombok:lombok")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.cloud:spring-cloud-stream-test-binder:4.0.3")
    testImplementation("io.projectreactor:reactor-test")
}

dependencyManagement{
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:2022.0.3")

        dependencies {
            dependency("io.github.resilience4j:resilience4j-spring:2.0.2")
            dependency("io.github.resilience4j:resilience4j-annotations:2.0.2")
            dependency("io.github.resilience4j:resilience4j-consumer:2.0.2")
            dependency("io.github.resilience4j:resilience4j-core:2.0.2")
            dependency("io.github.resilience4j:resilience4j-circuitbreaker:2.0.2")
            dependency("io.github.resilience4j:resilience4j-ratelimiter:2.0.2")
            dependency("io.github.resilience4j:resilience4j-retry:2.0.2")
            dependency("io.github.resilience4j:resilience4j-bulkhead:2.0.2")
            dependency("io.github.resilience4j:resilience4j-timelimiter:2.0.2")
            dependency("io.github.resilience4j:resilience4j-micrometer:2.0.2")
            dependency("io.github.resilience4j:resilience4j-circularbuffer:2.0.2")
        }
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

