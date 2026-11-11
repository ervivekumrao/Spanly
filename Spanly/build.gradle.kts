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

/*
 * Secret & Key File Resolution Options:
 * Option 1: `securePropertiesPath` in git-ignored `local.properties` (e.g. securePropertiesPath=H\:/Key/secure.properties)
 * Option 2: `securePropertiesPath` in global `~/.gradle/gradle.properties`
 * Option 3: `SECURE_PROPERTIES_PATH` environment variable
 * Option 4: `secure.properties` file located in root project or `~/.gradle/secure.properties`
 */
val secretProps = Properties()

// Load local.properties if present (git-ignored)
val localPropsFile = rootProject.file("local.properties")
val localProps = Properties().apply {
    if (localPropsFile.exists()) {
        localPropsFile.inputStream().use { load(it) }
    }
}

val rootSecretProps = rootProject.file("secure.properties")
val securePropsCustomPath: String? = System.getenv("SECURE_PROPERTIES_PATH")
    ?: (project.findProperty("securePropertiesPath") as? String)
    ?: localProps.getProperty("securePropertiesPath")
val customKeyFile = securePropsCustomPath?.let { file(it) }
val userHomeKeyFile = file("${System.getProperty("user.home")}/.gradle/secure.properties")

val resolvedKeyFile = when {
    rootSecretProps.exists() -> rootSecretProps
    customKeyFile?.exists() == true -> customKeyFile
    userHomeKeyFile.exists() -> userHomeKeyFile
    else -> null
}
resolvedKeyFile?.inputStream()?.use { secretProps.load(it) }

// Expose properties loaded from secretProps and localProps as Gradle project extra properties
secretProps.forEach { (key, value) ->
    val k = key.toString()
    if (!project.hasProperty(k)) {
        project.extensions.extraProperties.set(k, value)
    }
}
localProps.forEach { (key, value) ->
    val k = key.toString()
    if (!project.hasProperty(k)) {
        project.extensions.extraProperties.set(k, value)
    }
}

val gitTssUser: String? = System.getenv("GIT_TSS_USER")
    ?: System.getenv("GITHUB_ACTOR")
    ?: secretProps.getProperty("GIT_TSS_USER")
    ?: (project.findProperty("GIT_TSS_USER") as? String)
    ?: (project.findProperty("gittss.user") as? String)

val gitTssPass: String? = System.getenv("GIT_TSS_PASS")
    ?: System.getenv("GITHUB_TOKEN")
    ?: secretProps.getProperty("GIT_TSS_PASS")
    ?: (project.findProperty("GIT_TSS_PASS") as? String)
    ?: (project.findProperty("gittss.pass") as? String)

val libGroupID = "io.github.ervivekumrao"
val libArtifactID = "spanly"
val libVersion = "1.0.0"

android {
    namespace = "umrao.spanly"
    compileSdk = 37

    defaultConfig {
        minSdk = 24
        testOptions {
            targetSdk = 37
        }
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
        url.set("https://github.com/ervivekumrao/Spanly")
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
            connection.set("scm:git:github.com/ervivekumrao/Spanly.git")
            developerConnection.set("scm:git:ssh://github.com/ervivekumrao/Spanly.git")
            url.set("https://github.com/ervivekumrao/Spanly/tree/main")
        }
    }
}

// Add GitHub Packages as an additional repository.
// The plugin creates a publication named "maven" by default.
publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/ervivekumrao/Spanly")
            credentials {
                username = gitTssUser ?: (project.findProperty("gittss.user") as? String) ?: ""
                password = gitTssPass ?: (project.findProperty("gittss.pass") as? String) ?: ""
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
    description = "test run"
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
