package com.herestars.data

/**
 * create by HanZiXin on 2022/10/21
 */
open class Combination(private val comparableCard: Card) {

    /**
     * 是否是同一牌型
     * @param other
     */
    fun sameType(other: Combination): Boolean = (this::class == this::class) || (this is Bomb && other is Bomb)

    open operator fun compareTo(other: Combination): Int = when {
        comparableCard > other.comparableCard -> 1
        comparableCard == other.comparableCard -> 0
        comparableCard < other.comparableCard -> -1
        else -> 0
    }

}

/**
 * ### 在玩家乱出牌时用的组合
 */
object NotACombination : Combination(Card.NOT_A_CARD)

/**
 * ### 单牌
 */
class Single(private val card: Card) : Combination(card) {
    override fun toString(): String = CardSet(card).toString()
}

/**
 * 不能单出王
 */
fun CardSet.isSingle(): Single? = if (size == 1 && !Card.isKing(this[0])) Single(get(0)) else null

/**
 * ### 对子
 */
class Double(private val card: Card) : Combination(card) {
    override fun toString(): String = CardSet(card, card).toString()
}

fun CardSet.isDouble(): Double? =
    if (size == 2) {
        this.sort()
        if (this[1] == this[0] || Card.isKing(this[1])) Double(this[1])
        null
    }
    else null

/**
 * ### 炸弹
 * @param   card    手牌
 * @param   soft    是否是软炸
 */
class Bomb(private val card: Card) : Combination(card) {
    override fun toString(): String = CardSet(card, card, card).toString()
}

fun CardSet.isBomb(): Bomb? =
    if (size == 3 && this[0] == this[1] && this[1] == this[2]) Bomb(this[0])
    else null

/**
 * ### 顺子
 */
class Smooth(val start: Card, val end: Card) : Combination(start) {
    override fun toString(): String {
        val cardSet = CardSet()
        for (i in (start.value..end.value)) {
            cardSet += Card.findCard(i)
        }
        return cardSet.toString()
    }
}

/*
    解析思路：
    1.先把牌排序，最后一张不能超过“2”
    2.如果每个牌都比前一个牌大1，就是顺子，否则不是顺子
 */
fun CardSet.isSmooth(): Smooth? {
    sort()
    return if (this[lastIndex].value <= 12 && this.size >= 3) {
        for (i in (0 until size - 1)) {
            if (this[i + 1].value - this[i].value != 1) return null
        }
        return Smooth(this[0], this[lastIndex])
    } else null
}