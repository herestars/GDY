package com.herestars

import com.herestars.common.Command
import com.herestars.common.ManagementCommand
import com.herestars.config.GDYConfig
import com.herestars.game.Game
import kotlinx.coroutines.launch
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
import net.mamoe.mirai.console.permission.AbstractPermitteeId
import net.mamoe.mirai.console.permission.PermissionService.Companion.permit
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.event.globalEventChannel
import net.mamoe.mirai.event.subscribeGroupMessages
import net.mamoe.mirai.utils.info

object GDY : KotlinPlugin(
    JvmPluginDescription(
        id = "com.example.demo",
        name = "GDY",
        version = "0.1.0",
    ) {
        author("HanZiXin")
    }
) {
    override fun onEnable() {
        logger.info { "GYD Plugin loaded" }

        GDYConfig.reload()

        Command.register()
        ManagementCommand.register()

        GDYConfig.groups.forEach {
            AbstractPermitteeId.AnyMember(it).permit(Command.permission)
        }

        globalEventChannel().subscribeGroupMessages {
            case("创建游戏"){
                //只有允许的群聊可以玩斗地主
                if (group.id in GDYConfig.groups) {
                    launch { Game(group).gameStart() }
                    subject.sendMessage("创建成功（底分：200）！发送“上桌”即可参与游戏")
                }
            }
        }
    }
}