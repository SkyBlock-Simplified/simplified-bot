plugins {
    id("java")
}

group = "dev.sbs"
version = "0.1.0"

repositories {
    mavenCentral()
    maven(url = "https://central.sonatype.com/repository/maven-snapshots")
    maven(url = "https://jitpack.io")
}

dependencies {
    // Simplified Annotations
    annotationProcessor(libs.simplified.annotations)

    // Lombok Annotations
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
    testCompileOnly(libs.lombok)
    testAnnotationProcessor(libs.lombok)

    // Tests
    testImplementation(libs.hamcrest)
    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)

    // Optaplanner
    implementation(libs.optaplanner.core)
    testImplementation(libs.optaplanner.benchmark)

    // Spring
    implementation(libs.spring.boot)
    implementation(libs.spring.boot.web)
    implementation(libs.spring.boot.security)
    testImplementation(libs.spring.security.test)

    // Projects
    implementation("dev.sbs:api:0.1.0")
    implementation("dev.sbs:minecraft-api:0.1.0")
    implementation("dev.sbs:discord-api:0.1.0")
}

tasks {
    withType<JavaCompile> {
        sourceCompatibility = "21"
        targetCompatibility = "21"
    }
    test {
        useJUnitPlatform()
    }
}
