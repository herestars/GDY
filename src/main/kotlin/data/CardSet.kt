package com.herestars.data

import java.util.StringJoiner

/**
 * create by HanZiXin on 2022/10/21
 */
open class CardSet : ArrayList<Card> {

    constructor(vararg elements: Card) : super(elements.toMutableList())
    constructor(list: List<Card>) : super(list)

    /**
     * 用于判断是否有手牌
     */
    infix fun have(other: CardSet): Boolean {
        val copy = ArrayList<Card>(this)
        other.forEach {
            if (!copy.remove(it)) return false
        }
        return true
    }

    fun sort() = sortedBy { it.value }

    fun toSortedString(): String {
        sort()
        var str = ""
        forEach {
            str += "[${it.id}]"
        }
        return str
    }

    /**
     * 不排序变成字符串
     */
    override fun toString(): String {
        var str = ""
        forEach {
            str += "[${it.id}]"
        }
        return str
    }

    override fun equals(other: Any?): Boolean {
        return if (other is CardSet) {
            this have other && other have this
        } else false
    }

    override fun hashCode(): Int {
        return this.size
    }
}

/**
 * ### 玩家的手牌
 */
class HandCards(elements: MutableList<Card>) : CardSet(elements) {

    //出牌
    fun play(cardSet: CardSet) {
        cardSet.forEach {
            this.remove(it)
        }
    }
}

fun List<Card>.toCardSet(): CardSet {
    return CardSet(*this.toTypedArray())
}