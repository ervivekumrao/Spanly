import java.util.Properties

/*
 * Copyright 2025 Vivek Umrao
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.maven.publish)
    alias(libs.plugins.maven.central.publish)
    jacoco
}

// Load secrets safely from environment variables, system properties, user home, root secure.properties, or local key path
val secretProps = Properties()
val rootSecretProps = rootProject.file("secure.properties")
val securePropsCustomPath = System.getenv("SECURE_PROPERTIES_PATH") ?: project.findProperty("securePropertiesPath") as? String
val customKeyFile = securePropsCustomPath?.let { file(it) }
val userHomeKeyFile = file("${System.getProperty("user.home")}/.gradle/secure.properties")
val localKeyFile = file("H:/Key/secure.properties")
val resolvedKeyFile = when {
    rootSecretProps.exists() -> rootSecretProps
    customKeyFile?.exists() == true -> customKeyFile
    userHomeKeyFile.exists() -> userHomeKeyFile
    localKeyFile.exists() -> localKeyFile
    else -> null
}
resolvedKeyFile?.inputStream()?.use { secretProps.load(it) }

val gitTssUser: String? = System.getenv("GIT_TSS_USER") ?: secretProps.getProperty("GIT_TSS_USER")
val gitTssPass: String? = System.getenv("GIT_TSS_PASS") ?: secretProps.getProperty("GIT_TSS_PASS")

val libGroupID = "io.github.ervivekumrao"
val libArtifactID = "time-span-selector"
val libVersion = "0.0.7-alpha"

android {
    namespace = "vivek.umrao.time.span.selector"
    compileSdk = 37

    defaultConfig {
        minSdk = 24
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        debug {
            enableUnitTestCoverage = true
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            all {
                it.configure<JacocoTaskExtension> {
                    isIncludeNoLocationClasses = true
                    excludes = listOf("jdk.internal.*")
                }
            }
        }
    }
}

// Configuration for Maven Publish Plugin
// Credentials, Host (S01), and signing are configured via gradle.properties.
mavenPublishing {
    coordinates(libGroupID, libArtifactID, libVersion)

    pom {
        name.set("Spanly")
        description.set("Spanly - A highly customizable, high-performance Time Span Selector library for Android (Circular and Linear).")
        url.set("https://github.com/ervivekumrao/TimeSpanSelectorView")
        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }
        developers {
            developer {
                id.set("ervivekumrao")
                name.set("Vivek Umrao")
                email.set("manuscriptcode@gmail.com")
            }
        }
        scm {
            connection.set("scm:git:github.com/ervivekumrao/TimeSpanSelectorView.git")
            developerConnection.set("scm:git:ssh://github.com/ervivekumrao/TimeSpanSelectorView.git")
            url.set("https://github.com/ervivekumrao/TimeSpanSelectorView/tree/main")
        }
    }
}

// Add GitHub Packages as an additional repository.
// The plugin creates a publication named "maven" by default.
publishing {
    repositories {
        if (!gitTssUser.isNullOrBlank() && !gitTssPass.isNullOrBlank()) {
            maven {
                name = "GitHubPackages"
                url = uri("https://maven.pkg.github.com/ervivekumrao/TimeSpanSelectorView")
                credentials {
                    username = gitTssUser
                    password = gitTssPass
                }
            }
        }
    }
}

// Unified task to publish to both destinations in one command.
tasks.register("publishToAll") {
    description = "Publishes the library to both Maven Central and GitHub Packages."
    group = "publishing"
    dependsOn("publishToMavenCentral")
    dependsOn("publishMavenPublicationToGitHubPackagesRepository")
}

tasks.register<JacocoReport>("jacocoTestReport") {
    dependsOn("testDebugUnitTest")
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
    val fileFilter = listOf(
        "**/R.class", "**/R$*.class", "**/BuildConfig.*", "**/Manifest*.*"
    )
    val debugTree = fileTree("${layout.buildDirectory.get()}/intermediates/javac/debug/compileDebugJavaWithJavac/classes") {
        exclude(fileFilter)
    }
    val mainSrc = "${project.projectDir}/src/main/java"
    sourceDirectories.setFrom(files(mainSrc))
    classDirectories.setFrom(files(debugTree))
    executionData.setFrom(fileTree(layout.buildDirectory.get()) {
        include("outputs/unit_test_code_coverage/debugUnitTest/testDebugUnitTest.exec")
    })
}

dependencies {
    implementation(libs.appcompat)

    // Unit Testing
    testImplementation(libs.junit)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.core)

    // Android Instrumentation Testing
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
