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

object Nbnhhsh : KotlinPlugin(
    JvmPluginDescription(
        id = "pers.shennoter.nbnhhsh",
        name = "nbnhhsh",
        version = "1.0.0",
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
            if (event.message.content.contains(it)) {
                val abbr = event.message.content.substringAfter(" ")
                if (abbr.matches("[a-zA-Z0-9]+".toRegex())) { // 仅限数字和字母，否则丢弃消息
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
                    if(requestStr!!.contains(",\"inputting\":[]}]")||requestStr == "[]") {
                        subject.sendMessage("找不到此缩写")
                        return@subscribeAlways
                    }
                    val words = Gson().fromJson(requestStr, Res::class.java)
                    var text = ""
                    if (Config.replyType) {
                        words[0].trans.forEach { word ->
                            if (word == words[0].trans.last()) {
                                text += word
                            } else {
                                text = "$text$word\n"
                            }
                        }
                        val forward: ForwardMessage = buildForwardMessage {
                            add(event.sender, PlainText(text))
                        }
                        subject.sendMessage(forward)
                    } else {
                        words[0].trans.forEach { word ->
                            if (word == words[0].trans.last()) {
                                text += word
                            } else {
                                text = "$text$word、"
                            }
                        }
                        subject.sendMessage(text)
                    }
                }
            }
        }
    }
}

