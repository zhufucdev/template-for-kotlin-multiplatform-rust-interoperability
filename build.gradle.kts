@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `crab-multiplatform`
    alias(libs.plugins.kotest.multiplatform)
    alias(libs.plugins.dokka)
    alias(libs.plugins.publishOnCentral)
}

group = "com.zhufucdev.vectoria"
version = "0.1.0-SNAPSHOT"

repositories {
    google()
    mavenCentral()
}

kotlin {
    applyDefaultHierarchyTemplate()

    androidTarget {
        publishLibraryVariants("release")
        compilations.all {
            compileTaskProvider.configure {
                compilerOptions {
                    jvmTarget.set(JvmTarget.JVM_1_8)
                }
            }
        }
    }

    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "11"
        }
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }

    listOf(
        linuxX64(),
        linuxArm64(),
        mingwX64(),
        macosX64(),
        macosArm64(),
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.setupRustCompilationTask()
        it.binaries {
            sharedLib()
            staticLib()
        }
    }

    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
        allWarningsAsErrors = true
    }

    sourceSets {
        val commonMain by getting { }
        val commonTest by getting {
            dependencies {
                implementation(libs.bundles.kotlin.testing.common)
                implementation(libs.bundles.kotest.common)
            }
        }
        val jvmMain by getting {

        }
        val jvmTest by getting {
            dependencies {
                implementation(libs.kotest.runner.junit5)
            }
        }
        val nativeMain by getting {
        }
        val nativeTest by getting {
        }
    }
}

android {
    namespace = project.group.toString()
    compileSdk = 34
    defaultConfig {
        minSdk = 27
    }
}

tasks.dokkaJavadoc {
    enabled = false
}

publishOnCentral {
    projectLongName = "Vectoria"
    projectDescription = "Single purpose KMP vector database for KNN search."
    repoOwner = "zhufucdev"
    scmConnection = "scm:git:git://github.com/zhufucdev/Vectoria.git"

    publishing {
        publications {
            withType<MavenPublication> {
                artifactId = "kotlin"
                pom {
                    licenses {
                        license {
                            name = "The Apache License, Version 2.0"
                            url = "http://www.apache.org/licenses/LICENSE-2.0.txt"
                        }
                    }
                    developers {
                        developer {
                            name = "Steve Reed"
                            email = "zhufuzhufu1@gmail.com"
                            id = "zhufucdev"
                        }
                    }
                }
            }
        }
    }
}
