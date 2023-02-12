import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.Test
import java.io.File
import kotlin.io.path.createTempDirectory

class AutodocPluginTest {
    val testProjectDir = createTempDirectory().toFile()
    val buildFile = File(testProjectDir, "build.gradle")

    init {
        buildFile.writeText("""
                plugins {
                    id 'com.github.steanky.element-autodoc'
                }
                
            """.trimIndent())
    }

    @Test
    fun testRun() {
        buildFile.appendText("""
            elementAutodoc {
                message = 'This is a test!'
            }
        """.trimIndent())

        val result = GradleRunner.create()
                .withProjectDir(testProjectDir)
                .withArguments("elementAutodoc")
                .withPluginClasspath()
                .build()

        println(result.output)
    }
}