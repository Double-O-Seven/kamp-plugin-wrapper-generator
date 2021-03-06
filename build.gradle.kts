import groovy.lang.Closure

plugins {
    kotlin("jvm") version "1.3.11"
    `java-library`
    `java-gradle-plugin`
    `maven-publish`
    maven
    signing
    `build-scan`
    id("com.gradle.plugin-publish") version "0.10.1"
    id("org.jetbrains.dokka") version "0.9.17"
    id("com.palantir.git-version") version "0.12.0-rc2"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(group = "ch.leadrian.samp.kamp", name = "cidl-kotlin", version = "1.0.0")
    implementation(group = "ch.leadrian.samp.kamp", name = "kamp-annotations", version = "1.0.0-rc2")

    implementation(group = "org.jetbrains.kotlin", name = "kotlin-gradle-plugin", version = "1.3.11")
    implementation(group = "org.jetbrains.kotlin", name = "kotlin-stdlib-jdk8", version = "1.3.11")
    implementation(group = "com.squareup", name = "kotlinpoet", version = "1.1.0")
    implementation(group = "com.google.guava", name = "guava", version = "27.0.1-jre")
    implementation(group = "javax.inject", name = "javax.inject", version = "1")

    api(gradleApi())

    testImplementation(group = "org.junit.jupiter", name = "junit-jupiter-api", version = "5.4.0")
    testImplementation(group = "org.junit.jupiter", name = "junit-jupiter-params", version = "5.4.0")
    testImplementation(group = "org.assertj", name = "assertj-core", version = "3.11.1")

    testRuntimeOnly(group = "org.junit.jupiter", name = "junit-jupiter-engine", version = "5.4.0")
}

val gitVersion: Closure<String> by extra

version = gitVersion()

group = "ch.leadrian.samp.kamp"

tasks.register<Jar>("sourcesJar") {
    from(sourceSets.main.get().allSource)
    archiveClassifier.set("sources")
}

tasks.register<Jar>("javadocJar") {
    from(tasks.dokka)
    archiveClassifier.set("javadoc")
}

tasks {
    compileKotlin {
        sourceCompatibility = "1.8"
        kotlinOptions {
            jvmTarget = "1.8"
        }
    }

    compileTestKotlin {
        sourceCompatibility = "1.8"
        kotlinOptions {
            jvmTarget = "1.8"
        }
    }

    test {
        useJUnitPlatform()
    }

    dokka {
        reportUndocumented = false
    }
}

gradlePlugin {
    plugins {
        create("kampPluginWrapperGeneratorPlugin") {
            id = "ch.leadrian.samp.kamp.kamp-plugin-wrapper-generator"
            implementationClass = "ch.leadrian.samp.kamp.gradle.plugin.pluginwrappergenerator.PluginWrapperGeneratorPlugin"
        }
    }
}

pluginBundle {
    website = "https://github.com/Double-O-Seven/kamp-plugin-wrapper-generator"
    vcsUrl = "https://github.com/Double-O-Seven/kamp-plugin-wrapper-generator"
    description = "Gradle plugin to generate code base for SA-MP native plugin wrappers"
    tags = listOf("samp")

    (plugins) {
        "kampPluginWrapperGeneratorPlugin" {
            displayName = "Kamp Plugin Wrapper Generator plugin"
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])

            artifact(tasks["sourcesJar"])
            artifact(tasks["javadocJar"])

            pom {
                name.set("Kamp Plugin Wrapper Generator")
                description.set("Gradle plugin to generate Kotlin API for SA-MP native plugins")
                url.set("https://github.com/Double-O-Seven/kamp-plugin-wrapper-generator")
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set("Double-O-Seven")
                        name.set("Adrian-Philipp Leuenberger")
                        email.set("thewishwithin@gmail.com")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/Double-O-Seven/kamp-plugin-wrapper-generator.git")
                    developerConnection.set("scm:git:ssh://github.com/Double-O-Seven/kamp-plugin-wrapper-generator.git")
                    url.set("https://github.com/Double-O-Seven/kamp-plugin-wrapper-generator")
                }
            }
        }
    }
    repositories {
        maven {
            val snapshotsRepoUrl = uri("https://oss.sonatype.org/content/repositories/snapshots/")
            val releasesRepoUrl = uri("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
            url = if (version.toString().contains("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl
            credentials {
                val ossrhUsername: String? by extra
                val ossrhPassword: String? by extra
                username = ossrhUsername
                password = ossrhPassword
            }
        }
    }
}

signing {
    sign(publishing.publications["mavenJava"])
}

buildScan {
    termsOfServiceUrl = "https://gradle.com/terms-of-service"
    termsOfServiceAgree = "yes"
}
