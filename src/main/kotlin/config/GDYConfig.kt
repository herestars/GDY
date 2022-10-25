package com.herestars.config

import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.value

/**
 * create by HanZiXin on 2022/10/25
 */
object GDYConfig : AutoSavePluginConfig("GDYConfig"){

    val admin: MutableList<Long> by value()

    val groups: MutableList<Long> by value()

}