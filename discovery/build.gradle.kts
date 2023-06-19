plugins {
    java
    id("org.springframework.boot") version "3.1.0"
    id("io.spring.dependency-management") version "1.1.0"
}

group = "org.tbl.microdaddy.discovery"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-server")
    implementation("org.glassfish.jaxb:jaxb-runtime")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

dependencyManagement{
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:2022.0.3")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}