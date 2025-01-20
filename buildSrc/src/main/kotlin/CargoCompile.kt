import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.jetbrains.kotlin.konan.target.KonanTarget
import java.io.File

abstract class CargoCompile : DefaultTask() {
    @get:Input
    protected abstract val konanTargetProperty: Property<KonanTarget>

    @get:Optional
    @get:Input
    protected abstract val dirNameProperty: Property<String>

    @get:Optional
    @get:Input
    protected abstract val libNameProperty: Property<String>

    @get:Internal
    var konanTarget: KonanTarget
        get() = konanTargetProperty.get()
        set(value) {
            konanTargetProperty.set(value)
        }

    fun konanTarget(value: KonanTarget) {
        konanTargetProperty.set(value)
    }

    @get:Internal
    var dirName: String
        get() = dirNameProperty.get()
        set(value) {
            dirNameProperty.set(value)
        }

    fun dirName(value: String) {
        dirNameProperty.set(value)
    }

    @get:Internal
    var libName: String
        get() = libNameProperty.get()
        set(value) {
            libNameProperty.set(value)
        }

    fun libName(value: String) {
        libNameProperty.set(value)
    }

    private val platformTuple get() = rustupTarget[konanTarget] ?: error("Unsupported platform: ${konanTarget.name}.")

    /**
     * Cargo build root directory, where directories represent various platform,
     * named by their target tuples.
     * This property is determined by [org.gradle.api.file.ProjectLayout] and [dirNameProperty].
     *
     * Example: `$projectRoot/build/target`
     */
    @get:Internal
    val targetDir = project.layout.buildDirectory.zip(dirNameProperty) { d, n -> d.file(n).asFile }

    /**
     * Platform build root directory, containing the [libPath] directory.
     *
     * Example: `$projectRoot/build/target/aarch-apple-darwin
     */
    @get:Internal
    val buildRoot = targetDir.map { File(it, platformTuple) }

    @get:Internal
    val libPath = buildRoot.map { File(it, "release") }

    /**
     * Product dynamic library. To use this property, specify the [libNameProperty] first.
     *
     * Example: `$projectRoot/build/target/aarch-apple-darwin/release/libcoolname.dylib`
     */
    @get:Internal
    val binaryFile = libPath.zip(konanTargetProperty.zip(libNameProperty) { k, l -> k to l }) { product, (konan, libName) ->
        File(
            product,
            "${konan.family.dynamicPrefix}${libName}.${konan.family.dynamicSuffix}"
        )
    }

    init {
        dirNameProperty.set("target")
    }

    @TaskAction
    protected fun compile() {
        project.cargoRun(
            "build",
            "--release",
            "--target=$platformTuple",
            "--target-dir",
            targetDir.get().path
        )
    }
}