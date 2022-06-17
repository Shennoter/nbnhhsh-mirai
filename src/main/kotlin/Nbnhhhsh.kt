package pers.shennoter

import com.google.gson.Gson
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.event.GlobalEventChannel
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

class Res : ArrayList<ResItem>()

data class ResItem(
    val name: String,
    val trans: List<String>
)

class Res2 : ArrayList<Res2Item>()

data class Res2Item(
    val inputting: List<String>,
    val name: String
)

object Nbnhhsh : KotlinPlugin(
    JvmPluginDescription(
        id = "pers.shennoter.nbnhhsh",
        name = "nbnhhsh",
        version = "1.0.2",
    ) {
        author("Shennoter")
    }
) {
    override fun onEnable() {
        logger.info("缩写查询已加载")
        logger.info("能不能好好说话？")
        Config.reload()
        nbnhhsh()
    }
    override fun onDisable(){
        Config.save()
    }
}

@OptIn(DelicateCoroutinesApi::class)
fun nbnhhsh() {
    GlobalEventChannel.parentScope(GlobalScope).subscribeAlways<GroupMessageEvent> { event ->
        Config.customComm.forEach {
            val maxLen = if(event.message.content.length < it.length){
                event.message.content.length
            }else{
                it.length
            }
            if (event.message.content.substring(0,maxLen) == it) {
                val abbr = event.message.content.substringAfter(it.last())
                if (abbr.matches("[a-zA-Z0-9 ]+".toRegex())) { // 仅限数字和字母，否则丢弃消息
                    val url = "https://lab.magiconch.com/api/nbnhhsh/guess"
                    val json = "text=$abbr"
                    val JSON = "application/x-www-form-urlencoded; charset=utf-8".toMediaType()
                    val body = json.toRequestBody(JSON)
                    val client = OkHttpClient()
                    val request: Request = Request.Builder()
                        .url(url)
                        .post(body)
                        .build()
                    val response = try {
                        client.newCall(request).execute()
                    } catch (e: Exception) {
                        subject.sendMessage("查询错误")
                        return@subscribeAlways
                    }
                    val requestStr = response.body?.string()
                    response.close()
                    if(requestStr == "[]") {
                        subject.sendMessage("找不到此缩写")
                        return@subscribeAlways
                    }
                    var text = ""
                    val words1 = Gson().fromJson(requestStr, Res::class.java)
                    val words2 = Gson().fromJson(requestStr, Res2::class.java)
                    if(requestStr!!.contains("inputting")){ // 第一种返回json格式
                        if(words2[0].inputting.isEmpty()) { // 检查是否为空
                            subject.sendMessage("找不到此缩写")
                            return@subscribeAlways
                        }
                        if (Config.replyType) { // 转发消息模式
                            words2[0].inputting.forEach { word ->
                                if (word == words2[0].inputting.last()) {
                                    text += word
                                } else {
                                    text = "$text$word\n"
                                }
                            }
                            val forward: ForwardMessage = buildForwardMessage {
                                add(event.sender, PlainText(text))
                            }
                            subject.sendMessage(forward)
                            return@subscribeAlways
                        } else { // 直接发送模式
                            words2[0].inputting.forEach { word ->
                                if (word == words2[0].inputting.last()) {
                                    text += word
                                } else {
                                    text = "$text$word、"
                                }
                            }
                            subject.sendMessage(text)
                            return@subscribeAlways
                        }
                    }else { // 第二种返回json格式
                        if(words1[0].trans.isEmpty()) { // 检查是否为空
                            subject.sendMessage("找不到此缩写")
                            return@subscribeAlways
                        }
                        if (Config.replyType) { // 转发消息模式
                            words1[0].trans.forEach { word ->
                                if (word == words1[0].trans.last()) {
                                    text += word
                                } else {
                                    text = "$text$word\n"
                                }
                            }
                            val forward: ForwardMessage = buildForwardMessage {
                                add(event.sender, PlainText(text))
                            }
                            subject.sendMessage(forward)
                            return@subscribeAlways
                        } else { // 直接发送模式
                            words1[0].trans.forEach { word ->
                                if (word == words1[0].trans.last()) {
                                    text += word
                                } else {
                                    text = "$text$word、"
                                }
                            }
                            subject.sendMessage(text)
                            return@subscribeAlways
                        }
                    }
                }
            }
        }
    }
}

