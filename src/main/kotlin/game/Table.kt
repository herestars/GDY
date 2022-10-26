package com.herestars.game

import com.herestars.data.CardSet
import com.herestars.data.HandCards
import net.mamoe.mirai.contact.Member

/**
 * create by HanZiXin on 2022/10/25
 */
class Table : Iterable<Member> {
    val players = mutableListOf<Member>()
    var cards = CardSet()
    var cardIndex = 0

    var index = 0

    var banker: Member? = null

    /*
       构建玩家到手牌的映射
     */
    lateinit var handCard: MutableMap<Member, HandCards>

    /**
     * 加入游戏
     */
    fun enter(player: Member): Boolean {
        return if (!isFull() && player !in players) {
            players.add(player)
            true
        } else {
            false
        }
    }

    private fun isFull(): Boolean {
        return players.size == 7
    }

    override fun iterator(): Iterator<Member> {
        return TableIterator(players, index)
    }

    class TableIterator(private val players: List<Member>, private var index: Int = 0) : Iterator<Member> {
        override fun next(): Member {
            val p = players[index]
            if (index < players.size - 1) {
                index++
            } else index = 0
            return p
        }

        override fun hasNext(): Boolean {
            return true
        }
    }
}