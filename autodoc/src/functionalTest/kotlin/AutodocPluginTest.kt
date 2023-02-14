import kotlinx.serialization.json.*
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import java.io.File
import java.io.IOException
import java.nio.file.Path
import kotlin.io.path.createTempDirectory
import kotlin.io.path.toPath

class AutodocPluginTest {
    companion object {
        @JvmStatic
        private val testProjectDir: Path = createTempDirectory()

        @JvmStatic
        @AfterAll
        fun cleanup() {
            testProjectDir.toFile().deleteRecursively()
        }
    }

    private fun fileFromResource(name: String) : File {
        return (AutodocPluginTest::class.java.getResource(name) ?: fail("Unable to load resource " +
                "with name $name; does it not exist?")).toURI().toPath().toFile()
    }

    private fun test(vararg args: String) {
        val testProjectFile = testProjectDir.toFile()
        testProjectFile.listFiles()?.forEach {
            it.deleteRecursively()
        }

        val callerName = StackWalker.getInstance().walk {
            it.skip(1).findFirst().get()
        }.methodName ?: fail("Unable to find caller method, can't assume a resource folder")

        val expected = fileFromResource(callerName + "_expected.json")
        val expectedJson = Json.parseToJsonElement(expected.readText())

        val baseFile = fileFromResource(callerName)
        val baseTargetFile = testProjectFile.resolve(callerName)

        baseFile.listFiles()?.forEach {
            it.copyRecursively(baseTargetFile.resolve(it.relativeTo(baseFile)), true) {
                    file: File, exception: IOException ->
                println("IOException when copying resource $file, it will be skipped: $exception")
                return@copyRecursively OnErrorAction.SKIP
            }
        }

        val list = mutableListOf<String>()
        list.add("elementAutodoc")
        list.addAll(args)

        val result = GradleRunner.create()
                .withProjectDir(baseTargetFile)
                .withArguments(list)
                .withPluginClasspath()
                .build()

        if (result.tasks.any { it.outcome == TaskOutcome.FAILED }) {
            println(result.output)
            fail<String>("Gradle build had a failed task; see above")
        }
        else {
            println(result.output)
        }

        val actual = baseTargetFile.resolve("build").resolve("elementAutodoc").resolve("model.json")
        val actualJson = Json.parseToJsonElement(actual.readText())

        assertEquals(expectedJson, actualJson)
    }

    @Test
    fun checkExtensionParameters() {
        test()
    }

    @Test
    fun basicElement() {
        test()
    }
}