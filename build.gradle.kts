plugins {
    `java-library`
}

group = "org.xjcraft.plot"
version = "1.0.0"

// ext
val bukkitVersion     =         "1.14.2-R0.1-SNAPSHOT"
val lombokVersion     =         "1.18.8"
val bukkitbootVersion =         "1.0.0-SNAPSHOT"
val dependencyNames = mapOf(
        "bukkit"               to "org.bukkit:bukkit:$bukkitVersion",
        "spigot"               to "org.spigotmc:spigot:$bukkitVersion",
        "lombok"               to "org.projectlombok:lombok:$lombokVersion",
        "bukkitboot"           to "org.cat73.bukkitboot:bukkit-boot:$bukkitbootVersion"
)
extra["dependencyNames"] = dependencyNames

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

val share by configurations.creating

// 依赖
dependencies {
    compileOnly             ("${dependencyNames["bukkit"]}")
    annotationProcessor     ("${dependencyNames["lombok"]}")
    compileOnly             ("${dependencyNames["lombok"]}")

    compileOnly             ("${dependencyNames["bukkitboot"]}")
    compileOnly             ("com.zaxxer:HikariCP:3.3.1")
    share                   ("com.zaxxer:HikariCP:3.3.1")
    compileOnly             ("org.mybatis:mybatis:3.5.1")
    share                   ("org.mybatis:mybatis:3.5.1")
    compileOnly             ("mysql:mysql-connector-java:8.0.16")
    share                   ("mysql:mysql-connector-java:8.0.16")
}

tasks.withType<Jar> {
    from(share.map { if (it.isDirectory) it else zipTree(it) })
}
