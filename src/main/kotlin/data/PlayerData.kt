package com.herestars.data

import kotlinx.serialization.Serializable
import net.mamoe.mirai.console.data.AutoSavePluginData
import net.mamoe.mirai.console.data.value
import java.util.*

/**
 * create by HanZiXin on 2022/10/25
 */
object PlayerData : AutoSavePluginData("playData") {
    var data: MutableMap<Long, CustomData> by value()
}

@Serializable
data class CustomData(
    var coins: Int = 0,
    var winTimes: Int = 0,
    var gameTimes: Int = 0,
    var lastApplyTime: Long = 0L
){
    fun addPoints(number: Int) {
        if (number <= 0) throw Exception("AddMinusPoints")
        coins += number
    }

    fun pay(number: Int) {
        if (number <= 0) throw Exception("PayMinusPoints")
        coins -= number
    }

    fun dailyApply(): String {
        val lastTieDay = lastApplyTime / 1000 / 60 / 60 / 24
        val today = Date().time / 1000 / 60 / 60 / 24
        return if (today - lastTieDay >= 1) {
            coins += 200
            lastApplyTime = Date().time
            "又输光了吗……喏，这是200个point，别再输了哦"
        } else
            "你今天已经领取过200个point了，别得寸进尺了哦！"
    }
}