import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import java.util.Calendar

plugins {
    kotlin("jvm") version "2.3.21"
    id("org.jetbrains.dokka") version "2.1.0"
    id("org.jetbrains.dokka-javadoc") version "2.1.0"
    id("com.github.hierynomus.license") version "0.16.1"
    id("me.champeau.jmh") version "0.7.2"
    `maven-publish`
    signing
}

group = "com.github.penemue"
version = (findProperty("keapVersion") as String?) ?: "0.3.0-SNAPSHOT"

val isSnapshot = version.toString().endsWith("SNAPSHOT")
val mavenPublishUrl = (findProperty("mavenPublishUrl") as String?) ?: ""
val mavenPublishUsername = (findProperty("mavenPublishUsername") as String?) ?: ""
val mavenPublishPassword = (findProperty("mavenPublishPassword") as String?) ?: ""
val signingKeyId = (findProperty("signingKeyId") as String?) ?: ""
val signingPasswordProp = (findProperty("signingPassword") as String?) ?: ""
val signingSecretKeyRingFile = (findProperty("signingSecretKeyRingFile") as String?) ?: ""

defaultTasks("clean", "build")

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("junit:junit:4.13.2")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_1_8)
        languageVersion.set(KotlinVersion.KOTLIN_2_3)
        apiVersion.set(KotlinVersion.KOTLIN_2_3)
    }
}

license {
    header = rootProject.file("copyright.ftl")
    strictCheck = true
    ext["inceptionYear"] = 2016
    ext["year"] = Calendar.getInstance().get(Calendar.YEAR)
    ext["owner"] = "Vyacheslav Lukianov"
    ext["ownerURL"] = "https://github.com/penemue"
    include("**/*.kt")
    include("**/*.java")
    mapping("kt", "JAVADOC_STYLE")
}

tasks.test {
    testLogging.showStandardStreams = true
}

jmh {
    jmhVersion.set("1.37")
    jvmArgsPrepend.set(listOf("-Xmx1g", "-Xms1g"))
    duplicateClassesStrategy.set(DuplicatesStrategy.WARN)
    forceGC.set(true)
}

dokka {
    dokkaSourceSets.configureEach {
        reportUndocumented.set(false)
    }
}

tasks.jar {
    manifest {
        attributes(
            "Implementation-Title" to project.name,
            "Implementation-Version" to project.version
        )
    }
}

val sourceJar by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets["main"].allSource)
}

val dokkaGenerateJavadocTask = tasks.named("dokkaGeneratePublicationJavadoc")
val javadocJar by tasks.registering(Jar::class) {
    dependsOn(dokkaGenerateJavadocTask)
    archiveClassifier.set("javadoc")
    from(dokkaGenerateJavadocTask)
}

artifacts {
    add("archives", tasks.jar)
    add("archives", javadocJar)
    add("archives", sourceJar)
}

if (!isSnapshot) {
    extra["signing.keyId"] = signingKeyId
    extra["signing.password"] = signingPasswordProp
    extra["signing.secretKeyRingFile"] = signingSecretKeyRingFile
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            artifact(sourceJar)
            artifact(javadocJar)
            pom {
                name.set("Keap")
                description.set("Keap is a heap data structure presenting stable PriorityQueue and stable Keapsort sorting algorithm")
                url.set("https://github.com/penemue/keap")
                inceptionYear.set("2016")
                packaging = "jar"
                scm {
                    url.set("https://github.com/penemue/keap")
                    connection.set("scm:git:https://github.com/penemue/keap.git")
                    developerConnection.set("scm:git:https://github.com/penemue/keap.git")
                }
                licenses {
                    license {
                        name.set("The Apache Software License, Version 2.0")
                        url.set("http://www.apache.org/license/LICENSE-2.0.txt")
                        distribution.set("repo")
                    }
                }
                developers {
                    developer {
                        id.set("penemue")
                        name.set("Vyacheslav Lukianov")
                    }
                }
            }
        }
    }
    repositories {
        maven {
            url = if (mavenPublishUrl.isNotEmpty()) {
                uri(mavenPublishUrl)
            } else {
                layout.buildDirectory.dir("repo").get().asFile.toURI()
            }
            if (mavenPublishUsername.isNotEmpty()) {
                credentials {
                    username = mavenPublishUsername
                    password = mavenPublishPassword
                }
            }
        }
    }
}

signing {
    setRequired({ !isSnapshot && gradle.taskGraph.allTasks.any { it.name.startsWith("publish") } })
    sign(publishing.publications["mavenJava"])
}
