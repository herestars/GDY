package com.herestars.utils

import com.herestars.data.CustomData
import com.herestars.data.PlayerData
import net.mamoe.mirai.contact.User

/**
 * create by HanZiXin on 2022/10/25
 */
val User.GDYData
    get() = PlayerData.data.getOrPut(this.id) { CustomData(0, 0) }

val User.winTimes
    get() = GDYData.winTimes

val User.gameTimes
    get() = GDYData.gameTimes

val User.winRate
    get() = winTimes.toFloat() / gameTimes.toFloat()

val User.data
    get() = PlayerData.data.getOrPut(this.id) { CustomData() }

fun User.addPoints(number: Int) {
    this.data.addPoints(number)
}

fun User.pay(number: Int) {
    this.data.pay(number)
}

fun User.enough(number: Int): Boolean = this.data.coins >= number