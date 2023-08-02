plugins {
    id("element.java-library-conventions")
}

repositories {
    maven("https://dl.cloudsmith.io/public/steanky/ethylene/maven/")
    maven("https://dl.cloudsmith.io/public/steanky/toolkit/maven/")
}

dependencies {
    implementation(libs.commons.lang3)
    compileOnly(libs.ethylene.mapper)
    compileOnly(libs.adventure.key)
    compileOnly(libs.reflections)

    testImplementation(libs.ethylene.mapper)
    testImplementation(libs.adventure.key)
    testImplementation(libs.reflections)
}