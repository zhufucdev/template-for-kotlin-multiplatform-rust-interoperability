import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget
import org.jetbrains.kotlin.konan.target.KonanTarget

plugins {
    `kotlin-multiplatform`
    `android-library`
}

tasks.register("generateHeaders") {
    doLast {
        rustupRun(
            "--output",
            project.layout.buildDirectory.file("target/libvectoria.h").get().asFile.path,
            program = "cbindgen"
        )
    }
}

val setup =
    listOf(KonanTarget.MACOS_ARM64, KonanTarget.MACOS_X64, KonanTarget.LINUX_ARM64)
        .map { setupRustTask(it, project, Vectoria.name) }


afterEvaluate {
    kotlin.targets.filterIsInstance<KotlinJvmTarget>().first()
}
