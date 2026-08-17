plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.pulgares.app"
    // 35 y no 36: es la plataforma instalada en el Mac de Rubén, asi que la
    // build local funciona sin bajar SDKs y el CI compila lo mismo.
    compileSdk = 35

    defaultConfig {
        applicationId = "com.pulgares.app"
        minSdk = 26
        targetSdk = 35
        // El CI pasa -PappVersionCode=$GITHUB_RUN_NUMBER para que cada build
        // sea una version superior y Android permita actualizar sin desinstalar.
        versionCode = (project.findProperty("appVersionCode") as String? ?: "1").toInt()
        versionName = "1.0.${(project.findProperty("appVersionCode") as String? ?: "1")}"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    signingConfigs {
        // Clave fija compartida para que todas las builds (locales y de CI)
        // tengan la misma firma y las actualizaciones instalen sin desinstalar.
        // Solo para reparto domestico entre colegas.
        create("shared") {
            storeFile = file("ci-debug.keystore")
            storePassword = "pulgares"
            keyAlias = "pulgares"
            keyPassword = "pulgares"
        }
    }

    buildTypes {
        debug {
            signingConfig = signingConfigs.getByName("shared")
        }
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("shared")
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

    buildFeatures {
        compose = true
        buildConfig = true
    }

    // Sincronizacion opcional de grupos (Lambda + DynamoDB en la cuenta AWS
    // personal). El token llega por secreto de CI o variable local; si esta
    // vacio la app funciona 100% offline y oculta el boton de sincronizar.
    defaultConfig.buildConfigField(
        "String", "SYNC_URL",
        "\"${System.getenv("CDP_SYNC_URL")?.takeIf { it.isNotBlank() } ?: "https://v11t97w5g8.execute-api.eu-west-1.amazonaws.com/pulgares"}\""
    )
    defaultConfig.buildConfigField(
        "String", "SYNC_TOKEN",
        "\"${System.getenv("CDP_SYNC_TOKEN") ?: ""}\""
    )

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    testOptions {
        // android.util.Log y similares devuelven valores por defecto en los
        // tests JVM en vez de lanzar "not mocked".
        unitTests.isReturnDefaultValues = true
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
    arg("room.incremental", "true")
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.06.00")

    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.activity:activity-compose:1.10.1")

    implementation(composeBom)
    androidTestImplementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")

    implementation("androidx.navigation:navigation-compose:2.8.8")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.work:work-runtime-ktx:2.10.0")

    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    implementation("androidx.datastore:datastore-preferences:1.1.2")

    // Confeti cuando un grupo queda a cero. Es la unica libreria de adorno que
    // se permite la app: 90 KB y sin dependencias de Google.
    implementation("nl.dionsegijn:konfetti-compose:2.0.5")

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.json:json:20240303")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test:core-ktx:1.6.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    androidTestImplementation("androidx.room:room-testing:2.6.1")

    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
