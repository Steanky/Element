plugins {
    id("element.java-library-conventions")
}

repositories {
    maven("https://dl.cloudsmith.io/public/steank-f1g/ethylene/maven/")
}

dependencies {
    implementation(libs.apache.commons.lang3)
    api(libs.ethylene.core)
    api(libs.adventure.key)
}