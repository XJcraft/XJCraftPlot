plugins {
    `java-library`
}

group = "org.xjcraft.plot"
version = "1.0.0"

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
    maven("https://hub.spigotmc.org/nexus/content/repositories/public")
}

// 源文件编码
tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

// 依赖
dependencies {
    compileOnly             ("org.bukkit:bukkit:1.14.2-R0.1-SNAPSHOT")
    annotationProcessor     ("org.projectlombok:lombok:1.18.8")
    compileOnly             ("org.projectlombok:lombok:1.18.8")

    compileOnly             ("org.cat73.bukkitboot:bukkit-boot:1.0.0-SNAPSHOT")
}
