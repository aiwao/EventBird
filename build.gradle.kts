plugins {
    kotlin("jvm") version "2.4.0"
    id("com.vanniktech.maven.publish") version "0.37.0"
}

group = "io.github.aiwao"
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

mavenPublishing {
    publishToMavenCentral(automaticRelease = true)
    signAllPublications()

    coordinates(group.toString(), "eventbird", version.toString())

    pom {
        name.set("EventBird")
        description.set("A lightweight annotation-based event bus for Kotlin/JVM.")
        inceptionYear.set("2026")
        url.set("https://github.com/aiwao/EventBird")

        licenses {
            license {
                name.set("MIT License")
                url.set("https://opensource.org/license/mit")
                distribution.set("repo")
            }
        }

        developers {
            developer {
                id.set("aiwao")
                name.set("aiwao")
                email.set("aiwao@users.noreply.github.com")
                url.set("https://github.com/aiwao")
                organization.set("aiwao")
                organizationUrl.set("https://github.com/aiwao")
            }
        }

        scm {
            url.set("https://github.com/aiwao/EventBird")
            connection.set("scm:git:git://github.com/aiwao/EventBird.git")
            developerConnection.set("scm:git:ssh://git@github.com/aiwao/EventBird.git")
        }
    }
}

publishing {
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
