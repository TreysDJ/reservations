plugins {
	java
	`java-test-fixtures`
	id("org.springframework.boot") version "4.1.1"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "spring.learn"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(25)
	}
}

repositories {
	mavenCentral()
}

val componentTest = sourceSets.create("componentTest")
val integrationTest = sourceSets.create("integrationTest")

listOf(componentTest, integrationTest).forEach { sourceSet ->
	sourceSet.compileClasspath += sourceSets.main.get().output
	sourceSet.runtimeClasspath += sourceSets.main.get().output
	configurations[sourceSet.implementationConfigurationName].extendsFrom(configurations.testImplementation.get())
	configurations[sourceSet.runtimeOnlyConfigurationName].extendsFrom(configurations.testRuntimeOnly.get())
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-webmvc")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	runtimeOnly("org.postgresql:postgresql")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
	implementation("org.springframework.boot:spring-boot-starter-validation:4.2.0-M2")
	implementation("org.springframework.boot:spring-boot-starter-flyway")
	implementation("org.flywaydb:flyway-database-postgresql")

	testFixturesImplementation("org.springframework.boot:spring-boot-test")
	testFixturesImplementation("org.springframework.boot:spring-boot-testcontainers")
	testFixturesImplementation("org.testcontainers:testcontainers-postgresql")

	add(componentTest.implementationConfigurationName, testFixtures(project()))
	add(integrationTest.implementationConfigurationName, testFixtures(project()))
	add(integrationTest.implementationConfigurationName, "org.springframework.boot:spring-boot-resttestclient")
}

tasks.withType<Test> {
	useJUnitPlatform()
}

val componentTestTask = tasks.register<Test>("componentTest") {
	description = "Runs component tests against PostgreSQL."
	group = "verification"
	testClassesDirs = componentTest.output.classesDirs
	classpath = componentTest.runtimeClasspath
	shouldRunAfter(tasks.test)
}

val integrationTestTask = tasks.register<Test>("integrationTest") {
	description = "Runs integration tests through HTTP against PostgreSQL."
	group = "verification"
	testClassesDirs = integrationTest.output.classesDirs
	classpath = integrationTest.runtimeClasspath
	shouldRunAfter(tasks.test, componentTestTask)
}

tasks.check {
	dependsOn(componentTestTask, integrationTestTask)
}
