plugins {
    `java-library`
    id("template.java-conventions")
}

java {
    withJavadocJar()
    withSourcesJar()
}