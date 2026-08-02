plugins {
    kotlin("jvm") version "2.4.0"
    `maven-publish`
}

group = "com.github.aiwao"
version = providers.gradleProperty("eventBirdVersion")
    .getOrElse("1.0-SNAPSHOT")

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("reflect"))
    implementation("org.reflections:reflections:0.10.2")
    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(25)
}

publishing {
    publications {
        create<MavenPublication>("githubPackages") {
            from(components["java"])
            artifactId = "eventbird"
        }
    }

    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/aiwao/EventBird")
            credentials {
                username = providers.gradleProperty("gpr.user")
                    .orElse(providers.environmentVariable("GITHUB_ACTOR"))
                    .orNull
                password = providers.gradleProperty("gpr.key")
                    .orElse(providers.environmentVariable("GITHUB_TOKEN"))
                    .orNull
            }
        }
    }
}

tasks.test {
    useJUnitPlatform()
}
