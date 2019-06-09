# XJCraftPlot-Runtime
XJCraftPlot 测试环境

## 使用方法
1. 请先下载`BuildTools`，并构建一次目标版本的`Spigot`

    在目标`Minecraft`版本未改变时，这一步只需要做一次

    但最好每隔一段时间就构建一次，否则启动服务器之前会浪费几十秒等待时间

    <details>
    <summary>教程(点击展开)</summary>

    1. 到[下载页](https://hub.spigotmc.org/jenkins/job/BuildTools)下载`BuildTools`
    2. 执行`java -jar BuildTools.jar --rev <目标 Minecraft 版本>`来构建`Spigot`

        如`java -jar BuildTools.jar --rev 1.14.2`
    </details>
2. 在你的`IDE`中进行运行配置，此操作只需要做一次

    <details>
    <summary>IDEA 设置方法(点击展开)</summary>

    1. 在菜单中找到`Run` -> `Edit Configurations...`
    2. 点击左上角的加号(➕)，新建一个`Application`
    3. 将上方的`Name`改为你想使用的名字，如`Spigot`
    4. `Main class`填写为`org.bukkit.craftbukkit.Main`
    5. `Working directory`填写为`runtime/runtime/`
    6. `Use classpath of module`选择为`XJCraftPlot.runtime.main`
    7. 在下方的`Before launch`中，选中默认带的`Build`，然后点击减号(➖)移除它
    8. 点击`Before launch`中的加号(➕)，选择`Run Extenal tool`
    9. 在弹出的窗口中点击加号(➕)
    10. 在弹出的窗口中
        * `Name`填写为`Copy Plugin File(XJCraftPlot)`
        * `Program`填写为`runtime/init.sh`
        * `Working directory`填写为`$ProjectFileDir$`
    11. 最后一路 OK，直到关掉从第一步开始打开的所有窗口
    12. 现在你可以点击右上角的运行或调试按钮进行测试了
    </details>
3. `configures`中自带了一些方便更快启动服务器的配置

    当然，你可以选择是否使用，也可以根据需要进行任意修改

    以及，首次启动时你可能需要同意`eula`才能正常启动
