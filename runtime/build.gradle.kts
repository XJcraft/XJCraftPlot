plugins {
    `java-library`
}

// Java 版本
configure<JavaPluginConvention> {
    val javaVersion = JavaVersion.VERSION_11

    sourceCompatibility = javaVersion
    targetCompatibility = javaVersion
}

// 仓库配置
repositories {
    mavenLocal()
}

// 依赖
val dependencyNames: Map<String, String> by rootProject.extra
dependencies {
    implementation         ("${dependencyNames["spigot"]}")
    implementation         ("${dependencyNames["bukkitboot"]}")
}
