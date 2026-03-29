plugins {
    kotlin("jvm") version "1.9.25"
    signing
    id("com.vanniktech.maven.publish") version "0.30.0"
    id("org.jetbrains.dokka") version "1.9.20"
}

group = "com.cristianllanos"
version = "0.4.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.9.25")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:1.9.25")
}

signing {
    useGpgCmd()
}

mavenPublishing {
    publishToMavenCentral(com.vanniktech.maven.publish.SonatypeHost.CENTRAL_PORTAL)
    signAllPublications()

    coordinates("com.cristianllanos", "container", version.toString())

    pom {
        name.set("Kotlin Container")
        description.set("A lightweight dependency injection container for Kotlin")
        url.set("https://github.com/CristianLlanos/kotlin-container")

        licenses {
            license {
                name.set("MIT License")
                url.set("https://opensource.org/licenses/MIT")
            }
        }

        developers {
            developer {
                id.set("cristianllanos")
                name.set("Cristian Llanos")
                email.set("cristianllanos@outlook.com")
            }
        }

        scm {
            connection.set("scm:git:git://github.com/CristianLlanos/kotlin-container.git")
            developerConnection.set("scm:git:ssh://github.com/CristianLlanos/kotlin-container.git")
            url.set("https://github.com/CristianLlanos/kotlin-container")
        }
    }
}

