# 干瞪眼
一款能在群里干瞪眼的插件（误，目前是预览版，正常的流程都差不多了。~~不能出牌只能干瞪眼（名字由来~~
>参考项目[doudizhu](https://github.com/kono-dada/doudizhu)
### 安装
- 项目地址：![GitHub Repo stars](https://img.shields.io/github/stars/herestars/GDY?style=social)
- 下载地址：![GitHub release (latest by date)](https://img.shields.io/github/downloads/herestars/GDY/dev/total)
### 游戏玩法
写在前面：需要在配置文件中配置管理员和群聊，或者使用 `/gdyc addadmin [qq]` 和 `/gdyc addgroup [群聊]` 添加
1. 发送**创建干瞪眼**创建一场游戏
2. 玩家发送**上桌**或者**下桌**控制上下桌
3. 人齐后发送**开始干瞪眼**开始游戏
4. 出牌阶段使用`/要出的牌`出牌，王做癞子时不需要说王，可以直接打出来，例如手牌是 `2王` 可以直接打`/22`
5. 管理员可以在游戏开始后发送**结束游戏**来强制结束
### 指令
#### 普通指令
- `/<gdy\干瞪眼> help` 查询在群内玩法
- `/<gdy\干瞪眼> gamehelp` 查询干瞪眼规则
- `/<gdy\干瞪眼> beg` 低保
- `/<gdy\干瞪眼> me` 查询胜率
#### 管理员指令
- `/<gdyc\gdyadmin> addadmin [qq]` 添加管理员
- `/<gdyc\gdyadmin> addgroup [群号]` 添加群
### 干瞪眼规则
1. 游戏开始时，由东家先出，每人抓5张牌，东家抓6张。
2. 无人接牌时，最大的人摸牌。
3. 每次接牌必须是同一牌且刚好大1（2、炸弹例外），如33必须跟44。
4. 主要牌型为单张、对子、炸弹（三张一样的牌）、顺子（三张起步）。
5. 王可以当任何一张牌，但是不能单出。