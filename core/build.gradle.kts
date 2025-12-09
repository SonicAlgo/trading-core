plugins {
    kotlin("jvm") version "2.2.21"
    id("com.vanniktech.maven.publish") version "0.35.0"
    signing
}

group = "io.github.sonicalgo"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.squareup.okhttp3:okhttp:5.3.0")
    implementation("com.squareup.okhttp3:logging-interceptor:5.3.0")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.20.1")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.20.1")
}

kotlin {
    jvmToolchain(11)
}

tasks.matching { it.name == "generateMetadataFileForMavenPublication" }.configureEach {
    dependsOn(tasks.matching { it.name == "plainJavadocJar" })
}

signing {
    useGpgCmd()
    if (System.getProperty("os.name").lowercase().contains("mac")) {
        project.extra["signing.gnupg.executable"] = "/opt/homebrew/bin/gpg"
    }
}

mavenPublishing {
    coordinates(
        groupId = "io.github.sonicalgo",
        artifactId = "trading-core",
        version = "1.0.0",
    )

    pom {
        name.set("Trading Core")
        description.set(
            "Core library for trading SDKs. Provides HTTP client, WebSocket client with auto-reconnection, " +
                    "and common configuration interfaces."
        )
        url.set("https://github.com/SonicAlgo/trading-core")

        licenses {
            license {
                name.set("MIT License")
                url.set("https://opensource.org/licenses/MIT")
                distribution.set("repo")
            }
        }

        developers {
            developer {
                id.set("SonicAlgo")
                name.set("SonicAlgo")
                url.set("https://github.com/SonicAlgo")
            }
        }

        scm {
            url.set("https://github.com/SonicAlgo/trading-core")
            connection.set("scm:git:git://github.com/SonicAlgo/trading-core.git")
            developerConnection.set("scm:git:ssh://git@github.com/SonicAlgo/trading-core.git")
        }
    }

    publishToMavenCentral()
    signAllPublications()
}
