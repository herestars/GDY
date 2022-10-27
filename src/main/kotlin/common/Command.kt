package com.herestars.common

import com.herestars.GDY
import com.herestars.config.GDYConfig
import com.herestars.utils.data
import com.herestars.utils.gameTimes
import com.herestars.utils.winRate
import com.herestars.utils.winTimes
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.CompositeCommand
import net.mamoe.mirai.console.command.UserCommandSender
import net.mamoe.mirai.console.permission.AbstractPermitteeId
import net.mamoe.mirai.console.permission.PermissionService.Companion.permit

/**
 * create by HanZiXin on 2022/10/25
 */
object Command : CompositeCommand(
    GDY,
    "gdy", "干瞪眼",
    description = "查询指令"
) {
    @SubCommand
    @Description("领豆子")
    suspend fun UserCommandSender.beg() {
        val msg = user.data.dailyApply()
        subject.sendMessage(msg)
    }

    @SubCommand
    @Description("查询玩家的胜率")
    suspend fun UserCommandSender.me() {
        subject.sendMessage("<${user.nick}>现在有${user.data.coins}个point，总共进行了${user.gameTimes}场游戏，" +
                "获胜${user.winTimes}场，胜率${user.winRate}")
    }

    @SubCommand
    @Description("查询规则")
    suspend fun UserCommandSender.help() {
        subject.sendMessage("游戏规则：\n" +
                "'/ + 要出的牌' 出牌，不需要特地出王，比如手牌'2王'，可以直接出'/22'\n" +
                "过牌可以说：过、要不起、不要、2\n" +
                "/gdy help   查询帮助\n" +
                "/gdy beg    领低保\n" +
                "/gdy me     查询胜率\n" +
                "/gdy gdyhelp 查询干瞪眼游戏规则")
    }

    @SubCommand("gdyhelp")
    @Description("查询玩家的胜率")
    suspend fun UserCommandSender.gdyhelp() {
        subject.sendMessage("干瞪眼规则：\n" +
                "1、游戏开始时，由东家先出，每人抓5张牌，东家抓6张。\n" +
                "2、无人接牌时，最大的人摸牌。\n" +
                "3、每次接牌必须是同一牌且刚好大1（2、炸弹例外），如33必须跟44。\n" +
                "4、主要牌型为单张、对子、炸弹（三张一样的牌）、顺子（三张起步）。\n" +
                "5、王可以当任何一张牌，但是不能单出。")
    }
}

object ManagementCommand : CompositeCommand(
    GDY,
    "gdyadmin", "gdyc",
    description = "管理指令"
) {
    @SubCommand("addadmin")
    suspend fun CommandSender.addAdmin(id: Long) {
        GDYConfig.admin.add(id)
        AbstractPermitteeId.ExactUser(id).permit(Command.permission)
        sendMessage("OK")
    }

    @SubCommand("addgroup")
    suspend fun CommandSender.addGroup(id: Long) {
        GDYConfig.groups.add(id)
        AbstractPermitteeId.AnyMember(id).permit(Command.permission)
        sendMessage("OK")
    }
}