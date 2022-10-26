package com.herestars.utils

import com.herestars.data.*

/**
 * create by HanZiXin on 2022/10/26
 */
fun String.deserializeToCard(): CardSet {
    val cardSet = CardSet()
    var raw = this

    //接下来处理“10”这个数字
    //递归搜索10,
    fun find10() {
        val indexOfTen = raw.indexOf("10")
        if (indexOfTen == -1) {
            return
        } else {
            cardSet += Card.TEN
            raw = raw.removeRange(indexOfTen, indexOfTen + 2)
            find10()
        }
    }
    find10()
    //结束10的处理，剩下的字符串中没有10.
    raw.forEach { cardSet += Card.findCard(it.toString()) }
    return cardSet
}

/**
 * 找到对应的[Combination]。如果是乱出牌的，则返回[NotACombination]
 */
fun CardSet.findCombination(): Combination {
    return listOf(
        isSingle(),
        isDouble(),
        isBomb(),
        isSmooth(),
    ).firstOrNull { it != null } ?: NotACombination
}