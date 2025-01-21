import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.io.File

abstract class CbindGenerate : DefaultTask() {
    @get:OutputFile
    abstract val headerFileProperty: Property<File>

    init {
        val defaultFile = project.layout.buildDirectory.file("target/libvectoria.h").get().asFile
        headerFileProperty.set(defaultFile)
    }

    @get:Internal
    var headerFile: File
        get() = headerFileProperty.get()
        set(value) {
            headerFileProperty.set(value)
        }

    @TaskAction
    fun generate() {
        project.rustupRun(
            "--output",
            headerFile.path,
            program = "cbindgen"
        )
    }
}