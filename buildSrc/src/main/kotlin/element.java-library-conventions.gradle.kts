import java.net.URI

plugins {
    `java-library`
    `maven-publish`
    id("element.java-conventions")
}

java {
    withJavadocJar()
    withSourcesJar()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }

    repositories {
        maven {
            name = project.name
            url = URI.create("https://maven.cloudsmith.io/steank-f1g/element/")

            credentials {
                username = System.getenv("CLOUDSMITH_USERNAME")
                password = System.getenv("CLOUDSMITH_PASSWORD")
            }
        }
    }
}