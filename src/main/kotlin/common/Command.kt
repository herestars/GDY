package com.herestars.common

import com.herestars.GDY
import com.herestars.config.GDYConfig
import com.herestars.utils.data
import com.herestars.utils.gameTimes
import com.herestars.utils.winRate
import com.herestars.utils.winTimes
import net.mamoe.mirai.console.command.CompositeCommand
import net.mamoe.mirai.console.command.ConsoleCommandSender
import net.mamoe.mirai.console.command.UserCommandSender
import net.mamoe.mirai.console.permission.AbstractPermitteeId
import net.mamoe.mirai.console.permission.PermissionService.Companion.permit

/**
 * create by HanZiXin on 2022/10/25
 */
object Command : CompositeCommand(
    GDY,
    "gdy",
    description = "查询指令"
) {
    @SubCommand("beg")
    @Description("领豆子")
    suspend fun UserCommandSender.beg() {
        val msg = user.data.dailyApply()
        subject.sendMessage(msg)
    }

    @SubCommand("me")
    @Description("查询玩家的胜率")
    suspend fun UserCommandSender.me() {
        subject.sendMessage("<${user.nick}>现在有${user.data.coins}个point，总共进行了${user.gameTimes}场游戏，" +
                "获胜${user.winTimes}场，胜率${user.winRate}")
    }
}

object ManagementCommand : CompositeCommand(
    GDY,
    "gdyadmin", "gdyc",
    description = "管理指令"
) {
    @SubCommand("addadmin")
    suspend fun ConsoleCommandSender.addAdmin(id: Long) {
        GDYConfig.admin.add(id)
        AbstractPermitteeId.ExactUser(id).permit(Command.permission)
        sendMessage("OK")
    }

    @SubCommand("addgroup")
    suspend fun ConsoleCommandSender.addGroup(id: Long) {
        GDYConfig.groups.add(id)
        AbstractPermitteeId.AnyMember(id).permit(Command.permission)
        sendMessage("OK")
    }
}