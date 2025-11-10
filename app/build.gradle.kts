plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.android)
  alias(libs.plugins.ksp.plugin)
}

android {
  namespace = "br.com.bkpautomacao.bkpreco_lite"
  compileSdk = 35

  defaultConfig {
    applicationId = "br.com.bkpautomacao.bkpreco_lite"
    minSdk = 26
    targetSdk = 35
    versionCode = 5
    versionName = "1.4"
    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
//    applicationVariants.all {
//      val variant = this
//      variant.outputs.forEach { output ->
//        val buildType = variant.buildType
//        val versionNAme = variant.versionName
//
//        val filename = "bkpreco_lite_$buildType$versionName"
//        output.
//      }
//    }
  }
//        .map { it as com.android.build.gradle.internal.api.BaseVariantImpl }
//        .filter {
//          val names = it.name.split("-")
//          it.name.lowercase().contains(names[0],true) && it.name.lowercase().contains(names[1],true)
//        }
//        .forEach { output ->
//          val outputFileName = "bkpreco_lite${variant.flavorName}_${variant.buildType.name}_${variant.versionName}.apk"
//          output. = outputFileName
//        }


  packaging {
    resources {
      // Ignora arquivos duplicados de metadados
      excludes += setOf(
        "META-INF/INDEX.LIST",
        "META-INF/*.SF",
        "META-INF/*.DSA",
        "META-INF/*.RSA",
        "META-INF/*.properties",
      )
    }
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
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }
  kotlinOptions {
    jvmTarget = "17"
  }
}

dependencies {

  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.appcompat)
  implementation(libs.androidx.activity)
  implementation(libs.androidx.lifecycle.viewmodel)
  implementation(libs.androidx.constraintlayout)

  implementation(libs.material)
  implementation(libs.kotlin.coroutines)

  //koin di
  implementation(libs.koin.android)
  implementation(libs.koin.ktor)

  //usb-serial
  implementation(libs.usb.serial)

  //ktor server
  implementation(libs.ktor.server.core)
  implementation(libs.ktor.server.netty)
  implementation(libs.ktor.server.cio)
  implementation(libs.ktor.server.content.negotiation)
  implementation(libs.ktor.serialization)

  implementation(libs.room.runtime)
  ksp(libs.room.compiler)
  implementation(libs.room.ktx)

  //room persistence


  //log
  implementation(libs.slf4j)
  //test
  testImplementation(libs.junit)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.espresso.core)
}