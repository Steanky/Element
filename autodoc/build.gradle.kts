plugins {
    `java-gradle-plugin`
    kotlin("jvm") version "1.8.20-Beta"
    kotlin("plugin.serialization") version "1.8.0"
}

val functionalTest: SourceSet by sourceSets.creating

gradlePlugin {
    plugins {
        create("element-autodoc") {
            id = "com.github.steanky.element-autodoc"
            implementationClass = "com.github.steanky.element.gradle.plugin.autodoc.AutodocPlugin"
        }
    }

    testSourceSets(functionalTest)
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.8.20-Beta")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json-jvm:1.4.1")
    implementation(project(":element-core"))

    "functionalTestImplementation"("org.junit.jupiter:junit-jupiter-api:5.9.0")
    "functionalTestImplementation"("org.jetbrains.kotlinx:kotlinx-serialization-json-jvm:1.4.1")
    "functionalTestRuntimeOnly"("org.junit.jupiter:junit-jupiter-engine:5.9.0")
    "functionalTestImplementation"(project)
}

val functionalTestTask = tasks.register<Test>("functionalTest") {
    description = "Runs the functional tests for this plugin."
    group = "verification"
    testClassesDirs = functionalTest.output.classesDirs
    classpath = functionalTest.runtimeClasspath
}

tasks.check {
    dependsOn(functionalTestTask)
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}