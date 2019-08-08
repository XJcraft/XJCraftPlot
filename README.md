# XJCraftPlot
XJCraft 的地块管理插件

## 构建
1. 执行`./gradlew clean jar`来构建本插件
2. 在`build/libs`中你可以找到构建结果，把它放到服务器的`plugins`目录
3. 下载[Bukkit-Boot](https://oss.sonatype.org/content/repositories/snapshots/org/cat73/bukkitboot/bukkit-boot)，把它放到服务器的`plugins`目录
4. 启动服务器测试，你或许需要修改`plugins/XJCraftPlot/config.yml`来让它正常工作

## 代码分层
* Mapper: 基础的数据库操作
* Service: 将一个或多个 Mapper 操作组合成一个业务
    * 一般带事务
* 其他(如 Command / Listener): 和 Bukkit 对接，将具体业务传递给 Service
    * 如果有多个 Service 操作，可通过事务包装放在一个事务里

## TODO
* 其实部分数据库操作可以在主线程之外异步操作
