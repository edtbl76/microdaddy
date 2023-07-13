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
    implementation(platform("org.testcontainers:testcontainers-bom:1.18.3"))
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.cloud:spring-cloud-starter-stream-rabbit:4.0.3")
    implementation("org.springframework.cloud:spring-cloud-starter-stream-kafka:4.0.3")
    implementation("org.springframework.cloud:spring-cloud-starter-config:4.0.3")
    implementation("org.springframework.retry:spring-retry:2.0.2")
    implementation("io.micrometer:micrometer-tracing-bridge-otel:1.1.2")
    implementation("io.opentelemetry:opentelemetry-exporter-zipkin:1.27.0")
    implementation("org.springframework.cloud:spring-cloud-starter-bootstrap:4.0.3")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("mysql:mysql-connector-java:8.0.33")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.projectreactor:reactor-test")
    testImplementation("org.testcontainers:testcontainers")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:mysql")
}

dependencyManagement{
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:2022.0.3")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

