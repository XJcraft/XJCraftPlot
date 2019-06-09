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
    jcenter()
    maven("https://oss.sonatype.org/content/repositories/snapshots")
}

val copyDep by configurations.creating

// 依赖
val dependencyNames: Map<String, String> by rootProject.extra
dependencies {
    implementation         ("${dependencyNames["spigot"]}")
    copyDep                ("${dependencyNames["bukkitboot"]}")
}

// 用于拷贝 BukkitBoot 到测试服务器的插件目录
tasks.register("copyDep") {
    val bukkitBoot = copyDep.first()

    val target = File("runtime/runtime/plugins/BukkitBoot.jar")
    if (target.exists()) {
        target.delete()
    }
    bukkitBoot.copyTo(target)
}
