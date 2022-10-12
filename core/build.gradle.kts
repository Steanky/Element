plugins {
    id("element.java-library-conventions")
}

repositories {
    maven("https://dl.cloudsmith.io/public/steank-f1g/ethylene/maven/")
}

dependencies {
    implementation(libs.commons.lang3)
    api(libs.ethylene.mapper)
    api(libs.adventure.key)
}