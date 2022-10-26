package com.herestars.data

/**
 * create by HanZiXin on 2022/10/21
 */
open class CardSet : ArrayList<Card> {

    constructor(vararg elements: Card) : super(elements.toMutableList())
    constructor(list: List<Card>) : super(list)

    /**
     * 用于判断是否有手牌
     * 王可以当癞子，所以要统计一下
     */
    infix fun have(other: CardSet): Boolean {
        val copy = ArrayList<Card>(this)
        // 统计王的数量
        var cnt = copy.count { Card.isKing(it) }
        other.forEach {
            if (!copy.remove(it)) {
                if (--cnt < 0) return false
                if (this is HandCards) {
                    this.isKing = true
                }
            }
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

    var isKing = false

    //出牌
    fun play(cardSet: CardSet) {
        cardSet.forEach {
            // 如果没有这张牌，说明是王当癞子
            if (!this.remove(it))
                this.remove(Card.KING)
        }
    }
}

fun List<Card>.toCardSet(): CardSet {
    return CardSet(*this.toTypedArray())
}