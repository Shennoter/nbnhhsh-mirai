package pers.shennoter

import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value


object Config : AutoSavePluginConfig("Config") {
    @ValueDescription("自定义指令")
    val customComm: List<String> by value(listOf("?","？","gus"))
    @ValueDescription("回复方式:true为转发消息，false为直接发送")
    val replyType: Boolean by value(false)
}