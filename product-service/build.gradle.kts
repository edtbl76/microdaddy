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
	implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client:4.0.2")
	implementation("org.springframework.cloud:spring-cloud-starter-config:4.0.3")
	implementation("org.springframework.retry:spring-retry:2.0.2")
	implementation("org.springframework.cloud:spring-cloud-starter-bootstrap:4.0.3")
	implementation("org.springframework.boot:spring-boot-starter-data-mongodb-reactive")
	implementation("de.flapdoodle.embed:de.flapdoodle.embed.mongo.spring30x:4.7.0")
	developmentOnly("org.springframework.boot:spring-boot-devtools")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("io.projectreactor:reactor-test")
	testImplementation("org.testcontainers:testcontainers")
	testImplementation("org.testcontainers:junit-jupiter")
	testImplementation("org.testcontainers:mongodb")
	testImplementation("io.micrometer:micrometer-tracing-test")
	testImplementation("io.micrometer:micrometer-observation-test")

}

dependencyManagement{
	imports {
		mavenBom("org.springframework.cloud:spring-cloud-dependencies:2022.0.3")
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}

tasks.register("prepareKotlinBuildScriptModel") {}