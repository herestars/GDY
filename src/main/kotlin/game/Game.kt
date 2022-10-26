package com.herestars.game

import com.herestars.config.GDYConfig
import com.herestars.data.*
import com.herestars.utils.*
import kotlinx.coroutines.CompletableJob
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.coroutineScope
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.event.EventPriority
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.event.globalEventChannel
import net.mamoe.mirai.event.subscribeGroupMessages
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.content

/**
 * create by HanZiXin on 2022/10/25
 */
class Game(private val gameGroup: Group, private val basicBet: Int = 1) : CompletableJob by SupervisorJob() {

    private val table = Table()

    private var magnification = 1

    // 创建游戏阶段
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

    // 玩家准备阶段
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
                    if (table.players.size >= 2) {
                        started = true
                        prepareJob.cancel()
                    } else {
                        "至少要2个人才能开始游戏捏\n" + "当前玩家：${table.players.map { it.nick }}"
                    }
                }
            }
        }
        //等待job结束，即成功开始游戏
        job.join()
        if (started) deal()
    }

    // 发牌阶段
    private suspend fun deal() {
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
            player.sendMessage("当前手牌：${player.handCards().sort()}")
        }
        table.cardIndex = start
        if (table.banker == null) table.banker = table.players[0]
        startGame();
    }

    // 游戏阶段
    private suspend fun startGame() {
        var isRunning = true;

        var lastCardSet = CardSet()
        var lastCombination: Combination = NotACombination
        var lastPlayer = table.players[table.index]
        var lastKing = false
        val lastOtherCombination: Combination = NotACombination

        for (player in table.iterator()) {
            if (player == lastPlayer) lastCombination = NotACombination

            if (this.isActive)
                reply(At(player) + " 轮到你出牌了")

            val startJob = Job(this)

            val gameEventChannel = coroutineScope {
                globalEventChannel()
                    .parentJob(startJob)
                    .filterIsInstance<MessageEvent>()
                    .filter { event: MessageEvent -> event.sender.id == player.id }
            }
            /*
            玩家每次发送以“/“开头的消息，都视为一次出牌请求。出牌请求有多种处理结果，包括
            1.玩家并没有要出的牌
            2.玩家出牌不符合规则(与上家出的牌不是同类型的，或者比上家出的小)
            3.玩家把所有的牌都出完了，赢得游戏
            4.玩家顺利出牌，并进入下家的回合
            5.玩家跳过
            以上结果都会迎来onEvent的结束，但只有情况4和情况5会顺利进入下一家的回合
            其中，情况3会直接进入settle环节
             */
            val job = if (this.isActive) {
                gameEventChannel.subscribeAlways<MessageEvent> playCard@{
                    if (message.content[0].toString() == "/") {
                        val rawCardsString = message.content.substring(1)
                        val deserializedCards = rawCardsString.deserializeToCard()
                        if (!(player.handCards() have deserializedCards)) {
                            player.sendMessage("没在你的牌中找到你想出的牌哦")
                            return@playCard
                        }
                        val comb = deserializedCards.findCombination() //玩家想出的牌
                        if (comb == NotACombination) {
                            player.sendMessage("没看懂你想要出什么牌啦")
                            return@playCard
                        }
                        /*
                            有可能可以出牌的情况：
                            1.牌权回到自己手上
                            2.出了和上一次相同牌型并且比他大
                            3.上一次不是炸弹，但这次是炸弹
                            4.上一次是软炸，这次是硬炸
                            5.上一次是顺子，带了王
                         */
                        if (
                            (lastCombination == NotACombination)
                            || (lastCombination.sameType(comb)
                                    && (comb > lastCombination)
                                    && (deserializedCards.size == lastCardSet.size))
                            || ((lastCombination !is Bomb) && (comb is Bomb))
                            || (lastCombination is Bomb && lastKing && !player.handCards().isKing)
                            || (lastOtherCombination.sameType(comb)
                                    && comb > lastOtherCombination
                                    && deserializedCards.size == lastCardSet.size)
                        ) {
                            player.play(deserializedCards)
                            lastKing = player.handCards().isKing

                            /**
                             * 如果是带王的顺子，就记一下另一种顺
                             */
                            lastCombination = NotACombination
                            if (comb is Smooth && lastKing) {
                                if (!player.handCards().contains(comb.start)) {
                                    if (comb.end != Card.TWO) {
                                        lastCombination = Smooth(
                                            Card.findCard(comb.start.value + 1),
                                            Card.findCard(comb.end.value + 1)
                                        )
                                    }
                                }
                                if (!player.handCards().contains(comb.end)) {
                                    if (comb.start != Card.THREE) {
                                        lastCombination = Smooth(
                                            Card.findCard(comb.start.value - 1),
                                            Card.findCard(comb.end.value - 1)
                                        )
                                    }
                                }
                            }

                            /*
                                炸弹有特殊回复，并且翻倍
                             */
                            if (comb is Bomb) {
                                magnification *= 2
                                reply("炸弹！<${player.nick}>出了$comb")
                                reply("当前倍率：$magnification")
                            } else reply("<${player.nick}>出了$comb")

                            //获胜判断
                            if (player.handCards().size == 0) {
                                settle(player)
                                isRunning = false
                                startJob.cancel()
                                return@playCard
                            }

                            // 抓一张牌
                            if (table.cardIndex < table.cards.size) {
                                player.handCards().add(table.cards[table.cardIndex])
                                table.cardIndex++
                            } else {
                                // 重新发牌
                                reply("重新发牌中，请稍后")
                                val cards =
                                    (Card.cards() + Card.cards() + Card.cards() + Card.cards() + Card.kinds()).shuffled()
                                        .toCardSet()
                                for (p in table.players) {
                                    for (card in p.handCards()) {
                                        cards.remove(card)
                                    }
                                }
                                table.cards = cards
                                player.handCards().add(table.cards.first())
                                table.cardIndex = 1
                            }
                            player.handCards().sort()
                            player.sendMessage("你还剩\n ${player.handCards()}")

                            lastCardSet = deserializedCards
                            lastCombination = comb
                            lastPlayer = player
                            startJob.cancel()
                            return@playCard
                        }
                        player.sendMessage("你出的牌貌似不符合规则哦")
                        return@playCard
                    }

                    // 不出牌的情况
                    if (message.content == "要不起" || message.content == "不要"
                        || message.content == "过" || message.content == "2"
                    ) {
                        if (lastPlayer == player) player.sendMessage("这是你的回合，不可以不出哦")
                        else {
                            reply("<${player.nick}>选择了不出")
                            startJob.cancel()
                        }
                    }
                }
            } else return
            job.join()
            if (!isRunning) {
                this.cancel()
                break
            }
        }
    }

    /*
    获胜结算
    斗地主的获胜结算依赖CoinManager，即明乃币的管理系统。
     */
    private suspend fun settle(winner: Member) {
        //获胜场次，总场次的变化
        winner.GDYData.winTimes += 1
        for (player in table.players) {
            player.GDYData.gameTimes += 1
        }

        var amount = 0
        for (player in table.players) {
            if (winner == player) continue
            val money: Int = if (player.handCards().size == 5) 10 * magnification
            else player.handCards().size * magnification
            player.pay(money)
            amount += money
        }
        winner.addPoints(amount)
        table.banker = winner
        reply(
            "${winner.nick} 赢得了 $amount 个 point\n" +
                    "当前玩家剩余 point ：\n${table.players.map { it.data.coins.toString() + "\n" }}"
        )
    }

    //工具函数，别忘了
    private fun Member.handCards(): HandCards = table.handCard[this]!!

    private fun Member.play(cardSet: CardSet) = handCards().play(cardSet)

    private suspend fun reply(msg: Message) {
        gameGroup.sendMessage(msg)
    }

    private suspend fun reply(msg: String) {
        gameGroup.sendMessage(msg)
    }
}