package com.herestars.game

import com.herestars.config.GDYConfig
import com.herestars.data.Card
import com.herestars.data.CardSet
import com.herestars.data.HandCards
import com.herestars.data.toCardSet
import com.herestars.utils.enough
import kotlinx.coroutines.CompletableJob
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.coroutineScope
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.event.EventPriority
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.globalEventChannel
import net.mamoe.mirai.event.subscribeGroupMessages

/**
 * create by HanZiXin on 2022/10/25
 */
class Game(private val gameGroup: Group, private val basicBet: Int = 200) : CompletableJob by SupervisorJob() {

    private val table = Table()

    private var magnification = 1

    suspend fun gameStart() {
        coroutineScope {
            val channel = globalEventChannel()
                .parentJob(this@Game)
                .filterIsInstance<GroupMessageEvent>()
                .filter { event: GroupMessageEvent -> event.group == gameGroup }

            // 当游戏存在时拦截创建游戏的指令
            channel.subscribeGroupMessages(priority = EventPriority.HIGH) {
                (case("创建游戏") and sentFrom(gameGroup)) reply {
                    this.intercept()
                    "已经有一个游戏了"
                }
                //强制结束游戏
                //只有Config中的admin可以结束游戏
                (case("结束游戏") and sentFrom(gameGroup)) {
                    if (sender.id in GDYConfig.admin) {
                        group.sendMessage("结束成功")
                        this@Game.cancel()
                    }
                }
                "当前玩家" reply { "当前玩家：${table.players.map { it.nick }}" }
            }
        }
        prepare()
    }

    private suspend fun prepare() {
        var started = false
        /*
            创建一个job用于终结订阅器，这个job是this的子job
         */
        val prepareJob = Job(this)
        val scopedChannel = coroutineScope {
            globalEventChannel().parentJob(prepareJob)
                .filterIsInstance<GroupMessageEvent>()
                .filter { event: GroupMessageEvent -> event.group == gameGroup }
        }
        val job = scopedChannel.subscribeGroupMessages {
            (case("上桌") and sentFrom(gameGroup)) reply {
                if (!sender.enough(200)) {
                    "你的point不够200个哦，你没钱了"
                } else if (table.enter(sender)) {
                    "加入成功\n当前玩家：${table.players.map { it.nick }}"
                } else {
                    "人满了或你已经在游戏中了，无法加入"
                }
            }
            case("下桌") {
                if (sender in table.players) {
                    table.players.remove(sender)
                    subject.sendMessage("<${sender.nick}>下桌成功")
                }
            }
            (case("开始游戏")) reply {
                if (sender in table.players) {
                    if (table.isFull()) {
                        started = true
                        prepareJob.cancel()
                    } else {
                        "还没满人，无法开始\n" + "当前玩家：${table.players.map { it.nick }}"
                    }
                }
            }
        }
        //等待job结束，即成功开始游戏
        job.join()
        if (started) deal()
    }

    private fun deal() {
        val cards =
            (Card.cards() + Card.cards() + Card.cards() + Card.cards() + Card.kinds()).shuffled().toCardSet()

        table.cards = cards
        table.handCard = mutableMapOf()
        var start = 0
        for (player in table.players) {
            if (player == table.banker) {
                table.handCard[player] = HandCards(cards.subList(start, start + 6))
                start += 6
            } else {
                table.handCard[player] = HandCards(cards.subList(start, start + 5))
                start += 5
            }
        }
        table.cardIndex = start

    }
}