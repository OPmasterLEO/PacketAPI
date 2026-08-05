plugins {
    `java-library`
    id("com.gradleup.shadow")
    `maven-publish`
    id("io.papermc.paperweight.userdev") apply false
}

val pluginVersion: String by project

allprojects {
    group = "org.mastersmp.packet"
    version = pluginVersion

    repositories {
        mavenCentral()
        mavenLocal()
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
        maven("https://repo.codemc.io/repository/nms/")
        maven("https://repo.codemc.org/repository/nms/")
    }
}

subprojects {
    apply(plugin = "java")

    tasks.withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
    }

    extensions.configure<JavaPluginExtension>("java") {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
        }
    }
}

apply(from = rootProject.file("gradle/nms-buckets.gradle.kts"))

@Suppress("UNCHECKED_CAST")
val nmsBuckets = extra["nmsBuckets"] as List<String>

dependencies {
    implementation(project(":api"))
    implementation(project(":plugin"))
    implementation(project(":nms-api"))
    nmsBuckets.forEach { bucket ->
        implementation(project(":nms:$bucket"))
    }
    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks.jar {
    enabled = false
    dependsOn(tasks.shadowJar)
}

tasks.shadowJar {
    archiveBaseName.set("PacketAPI")
    archiveClassifier.set("")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    exclude(
        "META-INF/*.SF",
        "META-INF/*.DSA",
        "META-INF/*.RSA",
        "META-INF/maven/**"
    )
    mergeServiceFiles()
    manifest {
        attributes["Automatic-Module-Name"] = "org.mastersmp.packet"
        attributes["Implementation-Title"] = "PacketAPI"
        attributes["Implementation-Version"] = project.version
        attributes["paperweight-mappings-namespace"] = "mojang"
    }
}

listOf("apiElements", "runtimeElements").forEach { name ->
    configurations.named(name).configure {
        outgoing.artifacts.clear()
        outgoing.artifact(tasks.shadowJar)
    }
}

tasks.compileJava {
    options.release.set(21)
}

tasks.build {
    dependsOn(tasks.shadowJar)
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifactId = "masterpacketapi"
            artifact(tasks.shadowJar) {
                classifier = null
            }
            pom {
                name.set("MasterPacketAPI")
                description.set(
                    "Multi-version NMS / packet abstraction for Paper, Folia and Canvas (1.16–26.x)."
                )
                url.set("https://github.com/OPmasterLEO/MasterPacketAPI")
                licenses {
                    license {
                        name.set("GNU Lesser General Public License v3.0")
                        url.set("https://www.gnu.org/licenses/lgpl-3.0.html")
                    }
                }
                developers {
                    developer {
                        id.set("opmasterleo")
                        name.set("OPmasterLEO")
                    }
                }
                withXml {
                    asNode().appendNode("dependencies")
                }
            }
        }
        create<MavenPublication>("api") {
            artifactId = "packetapi-api"
            from(project(":api").components["java"])
            pom {
                name.set("packetapi-api")
                description.set("Version-agnostic PacketAPI interfaces (compile-only for consumers).")
                url.set("https://github.com/OPmasterLEO/MasterPacketAPI")
                licenses {
                    license {
                        name.set("GNU Lesser General Public License v3.0")
                        url.set("https://www.gnu.org/licenses/lgpl-3.0.html")
                    }
                }
            }
        }
    }
    repositories {
        fun org.gradle.api.artifacts.repositories.MavenArtifactRepository.reposiliteAuth() {
            isAllowInsecureProtocol = true
            credentials {
                username = project.findProperty("reposilite.user") as String? ?: System.getenv("REPOSILITE_USER")
                password = project.findProperty("reposilite.token") as String? ?: System.getenv("REPOSILITE_TOKEN")
            }
            authentication {
                create<BasicAuthentication>("basic")
            }
        }
        maven {
            name = "ReposiliteReleases"
            url = uri("http://repo.mastersmp.net/releases")
            reposiliteAuth()
        }
        maven {
            name = "ReposiliteSnapshots"
            url = uri("http://repo.mastersmp.net/snapshots")
            reposiliteAuth()
        }
    }
}

val publishingSnapshot = gradle.startParameter.taskNames.any {
    it == "publishSnapshot" || it.endsWith(":publishSnapshot")
}

tasks.named("publishMavenPublicationToReposiliteReleasesRepository").configure {
    onlyIf { !publishingSnapshot }
}
tasks.named("publishMavenPublicationToReposiliteSnapshotsRepository").configure {
    onlyIf { publishingSnapshot }
}
tasks.named("publishApiPublicationToReposiliteReleasesRepository").configure {
    onlyIf { !publishingSnapshot }
}
tasks.named("publishApiPublicationToReposiliteSnapshotsRepository").configure {
    onlyIf { publishingSnapshot }
}

tasks.named("publish").configure {
    group = "publishing"
    description = "Publish fat jar to Reposilite releases"
    dependsOn(tasks.shadowJar)
}

tasks.register("publishSnapshot") {
    group = "publishing"
    description = "Publish fat jar to Reposilite snapshots"
    dependsOn(tasks.shadowJar, "publishMavenPublicationToReposiliteSnapshotsRepository", "publishApiPublicationToReposiliteSnapshotsRepository")
}

tasks.register("printVersion") {
    doLast {
        println(version)
    }
}

tasks.named("publishToMavenLocal").configure {
    dependsOn(tasks.shadowJar)
}
