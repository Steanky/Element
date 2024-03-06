plugins {
    id("element.java-library-conventions")
}

repositories {
    maven("https://dl.cloudsmith.io/public/steanky/ethylene/maven/")
    maven("https://dl.cloudsmith.io/public/steanky/toolkit/maven/")
}

dependencies {
    implementation(libs.commons.lang3)
    compileOnly(libs.ethylene.core)
    compileOnly(libs.ethylene.mapper)
    compileOnly(libs.adventure.key)
    compileOnly(libs.reflections)
    compileOnly(libs.toolkit.collection)
    compileOnly(libs.toolkit.function)

    testImplementation(libs.ethylene.core)
    testImplementation(libs.ethylene.mapper)
    testImplementation(libs.adventure.key)
    testImplementation(libs.reflections)
    testImplementation(libs.toolkit.collection)
    testImplementation(libs.toolkit.function)
}