import java.util.Properties
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.external.javadoc.StandardJavadocDocletOptions
import org.gradle.api.tasks.compile.JavaCompile

plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

val cameraXVersion = "1.5.3"

val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        localPropertiesFile.inputStream().use { load(it) }
    }
}

android {
    namespace = "com.example.allot"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.allot"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        manifestPlaceholders["MAPS_API_KEY"] = localProperties.getProperty("MAPS_API_KEY", "")
        buildConfigField("String", "PLACES_API_KEY", "\"${localProperties.getProperty("PLACES_API_KEY", "")}\"")
    }

    buildTypes {
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

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(platform("com.google.firebase:firebase-bom:34.11.0"))
    implementation("com.google.firebase:firebase-storage")
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.camera:camera-camera2:$cameraXVersion")
    implementation("androidx.camera:camera-lifecycle:$cameraXVersion")
    implementation("androidx.camera:camera-view:$cameraXVersion")
    implementation("com.google.zxing:core:3.5.3")
    implementation("com.google.firebase:firebase-storage")
    implementation("com.github.bumptech.glide:glide:4.16.0")

    implementation(platform("com.google.firebase:firebase-bom:33.9.0"))
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-messaging")
    
    // Google Maps
    implementation("com.google.android.gms:play-services-maps:20.0.0")
    implementation("com.google.android.gms:play-services-location:21.3.0")
    implementation("com.google.android.libraries.places:places:5.1.1")
    implementation("com.google.maps.android:android-maps-utils:3.8.2")

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}

tasks.register<Javadoc>("projectJavadocs") {
    group = "documentation"
    description = "Generates Javadocs for the app, unit tests, and instrumentation tests."

    val mainSourceSet = android.sourceSets.getByName("main")
    val testSourceSet = android.sourceSets.getByName("test")
    val androidTestSourceSet = android.sourceSets.getByName("androidTest")

    val javaSources = files(
        mainSourceSet.java.srcDirs,
        testSourceSet.java.srcDirs,
        androidTestSourceSet.java.srcDirs
    ).asFileTree.matching {
        include("**/*.java")
        exclude("**/R.java")
        exclude("**/BuildConfig.java")
        exclude("**/Manifest.java")
    }

    setSource(javaSources)

    val mainCompileTask = tasks.named<JavaCompile>("compileDebugJavaWithJavac")
    val unitTestCompileTask = tasks.named<JavaCompile>("compileDebugUnitTestJavaWithJavac")
    val androidTestCompileTask = tasks.named<JavaCompile>("compileDebugAndroidTestJavaWithJavac")

    dependsOn(mainCompileTask, unitTestCompileTask, androidTestCompileTask)

    classpath = files(
        android.bootClasspath,
        mainCompileTask.map { it.classpath },
        unitTestCompileTask.map { it.classpath },
        androidTestCompileTask.map { it.classpath }
    )

    destinationDir = layout.buildDirectory.dir("docs/javadocs").get().asFile
    isFailOnError = false

    (options as StandardJavadocDocletOptions).apply {
        encoding = "UTF-8"
        memberLevel = org.gradle.external.javadoc.JavadocMemberLevel.PROTECTED
        addStringOption("Xdoclint:none", "-quiet")
        links("https://docs.oracle.com/en/java/javase/11/docs/api/")
        title = "Allot Project Javadocs"
        windowTitle = "Allot Project Javadocs"
    }
}
