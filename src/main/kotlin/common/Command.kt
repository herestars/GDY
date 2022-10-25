package com.herestars.common

import com.herestars.GDY
import com.herestars.config.GDYConfig
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
    "gdy"
) {
    @SubCommand("beg")
    suspend fun UserCommandSender.beg() {
        val msg = user.remark
        subject.sendMessage(msg)
    }
}

object ManagementCommand : CompositeCommand(
    GDY,
    "gdyadmin", "gdyc"
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