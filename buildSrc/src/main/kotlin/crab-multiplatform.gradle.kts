import com.android.build.gradle.tasks.MergeSourceSetFolders
import org.gradle.kotlin.dsl.support.uppercaseFirstChar
import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget
import org.jetbrains.kotlin.konan.target.KonanTarget

plugins {
    `kotlin-multiplatform`
    `android-library`
}

tasks.register("generateHeaders", CbindGenerate::class)

data class AndroidSetup(
    val abi: String,
    val konanTarget: KonanTarget,
    val crab: RustSetupResult = setupRustTask(konanTarget, project, Vectoria.name),
)

val androidSetups = listOf(
    AndroidSetup("x86_64", KonanTarget.ANDROID_X64),
    AndroidSetup("arm64-v8a", KonanTarget.ANDROID_ARM64)
)
val buildJniLibsDir = project.layout.buildDirectory.dir("jniLibs")
val copyAbiJniLibs = androidSetups.associate { setup ->
    val copy = tasks.register("copy${setup.abi.uppercaseFirstChar()}JniLibs", Copy::class) {
        dependsOn(setup.crab.build)
        from(setup.crab.build.binaryFile)
        into(buildJniLibsDir.map { it.file(setup.abi) })
    }
    setup.crab.build.finalizedBy(copy)
    setup.abi to copy
}

androidComponents {
    onVariants { variant ->
        afterEvaluate {
            tasks.withType<MergeSourceSetFolders>().configureEach {
                dependsOn(*(copyAbiJniLibs.values.toTypedArray()))
            }
        }
    }
}

val jvmSetups =
    listOf(KonanTarget.MACOS_ARM64, KonanTarget.MACOS_X64, KonanTarget.LINUX_ARM64)
        .map { setupRustTask(it, project, Vectoria.name) }

afterEvaluate {
    runCatching { kotlin }.getOrNull()?.targets?.withType<KotlinJvmTarget> {

    }
    android {
        sourceSets.configureEach {
            jniLibs.srcDir(buildJniLibsDir)
        }
    }
}

