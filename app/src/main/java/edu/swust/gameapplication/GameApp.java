package edu.swust.gameapplication;

import android.app.Application;
import androidx.room.Room;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 自定义Application类 - 应用的全局入口点
 * 负责初始化全局资源和提供全局访问点
 * 需要在AndroidManifest.xml中配置为application的name属性
 */
public class GameApp extends Application {

    // ================= 全局静态变量 =================

    // 全局数据库实例 - 使用Room持久化库
    private static UserDatabase userDatabase;

    // 全局登录账号 - 存储当前登录用户的账号
    private static String CURRENT_LOGIN_ACCOUNT;

    // 全局线程池 - 用于执行数据库操作等后台任务
    private static ExecutorService GLOBAL_EXECUTOR;

    // 全局音频管理器 - 统一管理游戏音效和背景音乐
    private static AudioManager AUDIO_MANAGER;

    // ================= 应用生命周期 =================

    /**
     * 应用创建时的回调方法
     * 初始化所有全局资源
     */
    @Override
    public void onCreate() {
        super.onCreate();

        // 初始化全局数据库
        initializeDatabase();

        // 初始化全局线程池
        initializeExecutor();

        // 初始化音频管理器
        initializeAudioManager();
    }

    /**
     * 初始化Room数据库
     */
    private void initializeDatabase() {
        userDatabase = Room.databaseBuilder(
                        getApplicationContext(),    // 应用上下文
                        UserDatabase.class,        // 数据库类
                        "user-database"            // 数据库文件名
                )
                .fallbackToDestructiveMigration()  // 数据库版本迁移失败时，删除旧表重建
                .build();                          // 构建数据库实例
    }

    /**
     * 初始化线程池
     */
    private void initializeExecutor() {
        // 创建缓存线程池，根据需要创建新线程，空闲线程会被保留60秒
        GLOBAL_EXECUTOR = Executors.newCachedThreadPool();
    }

    /**
     * 初始化音频管理器
     */
    private void initializeAudioManager() {
        AUDIO_MANAGER = AudioManager.getInstance(getApplicationContext());
    }

    // ================= 数据库相关方法 =================

    /**
     * 获取全局数据库实例
     * @return UserDatabase实例
     */
    public static UserDatabase getUserDatabase() {
        return userDatabase;
    }

    /**
     * 获取用户数据访问对象(UserDao)
     * 用于执行用户表的CRUD操作
     * @return UserDao实例
     */
    public static UserDao getUserDao() {
        return userDatabase.getuserDao();  // 注意：方法名是小写的getuserDao()
    }

    /**
     * 获取游戏记录数据访问对象(GameRecordDao)
     * 用于执行游戏记录表的CRUD操作
     * @return GameRecordDao实例
     */
    public static GameRecordDao getGameRecordDao() {
        return userDatabase.getGameRecordDao();
    }

    // ================= 登录状态管理 =================

    /**
     * 设置当前登录账号
     * account 登录用户的账号
     */
    public static void setCurrentLoginAccount(String account) {
        CURRENT_LOGIN_ACCOUNT = account;
    }

    /**
     * 获取当前登录账号
     * 当前登录用户的账号，如果未登录则返回null
     */
    public static String getCurrentLoginAccount() {
        return CURRENT_LOGIN_ACCOUNT;
    }

    // ================= 线程池管理 =================

    /**
     * 获取全局线程池
     * 用于执行数据库操作等异步任务
     * @return ExecutorService线程池实例
     */
    public static ExecutorService getGlobalExecutor() {
        return GLOBAL_EXECUTOR;
    }

    /**
     * 关闭线程池
     * 在应用退出时调用，释放线程资源
     */
    public static void shutdownExecutor() {
        if (GLOBAL_EXECUTOR != null && !GLOBAL_EXECUTOR.isShutdown()) {
            GLOBAL_EXECUTOR.shutdown();  // 平缓关闭，不再接受新任务
        }
    }

    // ================= 音频管理相关方法 =================

    /**
     * 获取音频管理器单例
     * @return AudioManager实例
     */
    public static AudioManager getAudioManager() {
        return AUDIO_MANAGER;
    }

    /**
     * 播放背景音乐（循环播放）
     * 通常用于游戏主界面的背景音乐
     * @param musicResId 背景音乐资源ID（R.raw.xxx）
     */
    public static void playBgMusic(int musicResId) {
        // 安全调用，防止AudioManager未初始化
        if (AUDIO_MANAGER != null) {
            AUDIO_MANAGER.playBgMusic(musicResId);
        }
    }

    /**
     * 暂停背景音乐
     * 通常用于游戏暂停或切换到其他页面时
     */
    public static void pauseBgMusic() {
        if (AUDIO_MANAGER != null) {
            AUDIO_MANAGER.pauseBgMusic();
        }
    }

    /**
     * 恢复背景音乐播放
     * 通常用于游戏恢复或返回游戏页面时
     */
    public static void resumeBgMusic() {
        if (AUDIO_MANAGER != null) {
            AUDIO_MANAGER.resumeBgMusic();
        }
    }

    /**
     * 设置背景音乐音量
     * @param volume 音量值，范围0.0f ~ 1.0f（0% ~ 100%）
     */
    public static void setBgMusicVolume(float volume) {
        if (AUDIO_MANAGER != null) {
            AUDIO_MANAGER.setBgMusicVolume(volume);
        }
    }

    /**
     * 播放音效（如按键音效、落子音效等）
     * 使用SoundPool播放短音效，适合频繁播放的效果音
     * @param soundResId 音效资源ID（R.raw.xxx）
     */
    public static void playSound(int soundResId) {
        if (AUDIO_MANAGER != null) {
            AUDIO_MANAGER.playSound(soundResId);
        }
    }

    /**
     * 设置音效开关
     * @param enabled true:开启音效 false:关闭音效
     */
    public static void setSoundEnabled(boolean enabled) {
        if (AUDIO_MANAGER != null) {
            AUDIO_MANAGER.setSoundEnabled(enabled);
        }
    }

    /**
     * 设置背景音乐开关
     * @param enabled true:开启背景音乐 false:关闭背景音乐
     */
    public static void setBgMusicEnabled(boolean enabled) {
        if (AUDIO_MANAGER != null) {
            AUDIO_MANAGER.setBgMusicEnabled(enabled);
        }
    }

    /**
     * 检查音效是否开启
     * @return true:音效已开启 false:音效已关闭或AudioManager未初始化
     */
    public static boolean isSoundEnabled() {
        return AUDIO_MANAGER != null && AUDIO_MANAGER.isSoundEnabled();
    }

    /**
     * 检查背景音乐是否开启
     * @return true:背景音乐已开启 false:背景音乐已关闭或AudioManager未初始化
     */
    public static boolean isBgMusicEnabled() {
        return AUDIO_MANAGER != null && AUDIO_MANAGER.isBgMusicEnabled();
    }

    // ================= 资源清理 =================

    /**
     * 应用退出时清理所有全局资源
     * 应该在应用完全退出前调用
     */
    public static void cleanup() {
        // 关闭线程池，释放线程资源
        shutdownExecutor();

        // 释放音频资源，停止播放并释放MediaPlayer和SoundPool
        if (AUDIO_MANAGER != null) {
            AUDIO_MANAGER.release();
        }

        // 关闭数据库连接
        if (userDatabase != null && userDatabase.isOpen()) {
            userDatabase.close();
        }

        // 清空静态变量引用，帮助垃圾回收
        userDatabase = null;
        GLOBAL_EXECUTOR = null;
        AUDIO_MANAGER = null;
        CURRENT_LOGIN_ACCOUNT = null;
    }
}