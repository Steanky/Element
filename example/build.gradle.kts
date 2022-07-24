plugins {
    id("element.java-conventions")
}

repositories {
    maven("https://dl.cloudsmith.io/public/steank-f1g/ethylene/maven/")
}

dependencies {
    implementation(libs.ethylene.core)
    implementation(libs.adventure.text)
    implementation(project(":element-core"))
}
