# 🎮 Android-Gobang（五子棋）

基于 Android 平台的五子棋游戏应用，支持人机对战和本地双人对战，内置 AI 引擎、用户系统、排行榜和音效管理。

## ✨ 功能特性

- **人机对战**：支持简单/困难两种 AI 难度，可选择执黑或执白
- **本地双人**：同屏双人对战模式
- **用户系统**：注册、登录、找回密码
- **个人中心**：修改用户名、更换头像、查看胜率统计
- **排行榜**：按简单/困难模式分别排名，按胜率降序排列
- **游戏控制**：悔棋、暂停/继续、计时器
- **音效系统**：背景音乐、落子音效、按钮音效、胜负音效

## 🛠️ 技术栈

| 技术 | 用途 |
|------|------|
| Java 11 | 开发语言 |
| Android SDK 34 | 基础框架 |
| Room 2.8.4 | 本地数据库 ORM |
| Material Design | UI 组件 |
| ConstraintLayout | 布局 |
| SoundPool | 短音效播放 |
| MediaPlayer | 背景音乐 |
| ExecutorService | 异步线程管理 |

## 📐 架构设计

项目采用 **MVC 架构**：

```
┌─────────────────────────────────────────────────┐
│                    View 层                       │
│  boardView（自定义棋盘View） + Activity 布局 XML  │
├─────────────────────────────────────────────────┤
│                 Controller 层                    │
│  MainActivity / PvcGamingActivity / ...          │
├─────────────────────────────────────────────────┤
│                  Model 层                        │
│  GameLogic / GameAi / User / GameRecord / Room   │
└─────────────────────────────────────────────────┘
```

**全局管理**：`GameApp`（Application 子类）统一管理数据库、线程池、音频和登录状态。

## 📂 项目结构

```
app/src/main/java/edu/swust/gameapplication/
├── GameApp.java              # Application 全局入口
├── MainActivity.java         # 登录页
├── RegisterActivity.java     # 注册页
├── FindPasswordActivity.java # 找回密码页
├── MainGameActivity.java     # 主菜单页
├── PvcGamingActivity.java    # 人机对战页
├── LocalGamingActivity.java  # 本地双人对战页
├── HomePageActivity.java     # 个人中心页
├── RankingActivity.java      # 排行榜页
├── GameLogic.java            # 五子棋核心逻辑引擎
├── GameAi.java               # AI 引擎（简单/困难）
├── boardView.java            # 自定义棋盘 View
├── GameTimer.java            # 游戏计时器（单例）
├── AudioManager.java         # 音频管理器（单例）
├── User.java                 # 用户实体
├── UserDao.java              # 用户数据访问对象
├── GameRecord.java           # 游戏记录实体
├── GameRecordDao.java        # 游戏记录数据访问对象
└── UserDatabase.java         # Room 数据库定义
```

## 🔄 页面导航

```
MainActivity（登录）
  ├── RegisterActivity（注册）
  ├── FindPasswordActivity（找回密码）
  └── MainGameActivity（主菜单）
       ├── PvcGamingActivity（人机对战）
       ├── LocalGamingActivity（本地双人）
       ├── RankingActivity（排行榜）
       └── HomePageActivity（个人中心）
```

## 🤖 AI 算法

### 简单难度
- 60% 概率使用困难算法
- 40% 概率在玩家落子周围 3×3 范围随机落子

### 困难难度
1. **优先级判断**：先检查是否有直接获胜位置，再检查是否需要防守
2. **评分算法**：对全盘空位评分，综合考虑进攻和防守
   - 进攻权重：10
   - 防守权重：8
3. **棋型评分**：
   - 活四：1000 分
   - 活三：100 分
   - 冲三：50 分
   - 活二：10 分
   - 冲二：5 分
   - 单子：1 分
4. **位置加分**：越靠近棋盘中心得分越高（曼哈顿距离）

## 🏗️ 构建与运行

### 环境要求
- Android Studio Hedgehog 或更高版本
- JDK 11+
- Android SDK，compileSdk 34，minSdk 26

### 构建步骤

1. 克隆仓库：
   ```bash
   git clone https://github.com/Cai-niao7/Android-Gobang.git
   ```

2. 用 Android Studio 打开项目

3. 同步 Gradle

4. 连接 Android 设备或启动模拟器，点击运行

## 📊 数据库设计

### User 表

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | INTEGER | 主键，自增 | 用户唯一标识 |
| user_account | TEXT | 唯一 | 用户账号 |
| user_password | TEXT | - | 用户密码 |
| avatar_id | INTEGER | 默认 0 | 头像 ID（0-10） |

### GameRecord 表

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| record_id | INTEGER | 主键，自增 | 记录唯一标识 |
| user_id | INTEGER | 外键，级联删除 | 关联 User.id |
| is_easy_mode | BOOLEAN | - | 是否为简单模式 |
| is_win | BOOLEAN | - | 是否获胜 |
| record_time | LONG | - | 记录时间戳 |

### 表关系
```
User (1) ──< GameRecord (N)
```
- 一个用户可以有多条游戏记录
- 删除用户时自动删除其所有游戏记录（CASCADE）

## 📄 许可证

MIT License
