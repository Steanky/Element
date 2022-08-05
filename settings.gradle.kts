rootProject.name = "element"

val localSettings = file("local.settings.gradle.kts")
if (localSettings.exists()) {
    apply(localSettings)
}


sequenceOf("core", "example").forEach {
    include(":${rootProject.name}-$it")
    project(":${rootProject.name}-$it").projectDir = file(it)
}