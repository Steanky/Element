plugins {
    id("element.java-library-conventions")
}

repositories {
    maven("https://dl.cloudsmith.io/public/steanky/ethylene/maven/")
    maven("https://dl.cloudsmith.io/public/steanky/toolkit/maven/")
}

dependencies {
    implementation(libs.commons.lang3)
    api(libs.ethylene.mapper)
    api(libs.adventure.key)
}