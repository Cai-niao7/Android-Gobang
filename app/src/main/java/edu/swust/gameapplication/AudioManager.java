package edu.swust.gameapplication;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.SoundPool;

/**
 * 音频管理器类
 * 单例模式，负责管理游戏中的背景音乐和音效
 */
public class AudioManager {
    // 单例实例
    private static AudioManager instance;
    // 应用上下文
    private Context context;

    // ================= 背景音乐相关 =================
    // 背景音乐播放器
    private MediaPlayer bgMusicPlayer;
    // 背景音乐开关状态，默认开启
    private boolean isBgMusicEnabled = true;
    // 背景音乐音量，默认50%
    private float bgMusicVolume = 0.5f;

    // ================= 音效相关 =================
    // 音效池
    private SoundPool soundPool;
    // 音效开关状态，默认开启
    private boolean isSoundEnabled = true;

    // ================= 构造函数和单例方法 =================

    /**
     * 私有构造函数
     * context 应用上下文
     */
    private AudioManager(Context context) {
        // 获取应用上下文，防止内存泄漏
        this.context = context.getApplicationContext();
        // 初始化音效池
        initSoundPool();
    }

    /**
     * 获取AudioManager单例实例
     * context 应用上下文
     * AudioManager单例实例
     */
    public static synchronized AudioManager getInstance(Context context) {
        // 如果实例不存在，创建新实例
        if (instance == null) {
            instance = new AudioManager(context);
        }
        return instance;
    }

    /**
     * 初始化SoundPool音效池
     */
    private void initSoundPool() {
        // 创建音频属性配置
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)          // 设置用途为游戏
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION) // 设置内容类型为音效
                .build();

        // 创建SoundPool实例
        soundPool = new SoundPool.Builder()
                .setMaxStreams(4)                     // 设置最大同时播放流数为4
                .setAudioAttributes(audioAttributes)  // 设置音频属性
                .build();
    }

    // ================= 背景音乐功能方法 =================

    /**
     * 播放背景音乐（自动循环播放）
     * @param musicResId 背景音乐资源ID
     */
    public void playBgMusic(int musicResId) {
        // 如果背景音乐未启用，直接返回
        if (!isBgMusicEnabled) return;

        // 先停止当前正在播放的背景音乐
        stopBgMusic();

        try {
            // 创建MediaPlayer并设置资源
            bgMusicPlayer = MediaPlayer.create(context, musicResId);
            if (bgMusicPlayer != null) {
                bgMusicPlayer.setLooping(true);                    // 设置循环播放
                bgMusicPlayer.setVolume(bgMusicVolume, bgMusicVolume); // 设置音量
                bgMusicPlayer.start();                            // 开始播放
            }
        } catch (Exception e) {
            e.printStackTrace(); // 打印异常信息
        }
    }

    /**
     * 暂停背景音乐
     */
    public void pauseBgMusic() {
        // 如果播放器存在且正在播放，则暂停
        if (bgMusicPlayer != null && bgMusicPlayer.isPlaying()) {
            bgMusicPlayer.pause();
        }
    }

    /**
     * 恢复背景音乐播放
     */
    public void resumeBgMusic() {
        // 如果背景音乐启用、播放器存在且未播放，则恢复播放
        if (isBgMusicEnabled && bgMusicPlayer != null && !bgMusicPlayer.isPlaying()) {
            bgMusicPlayer.start();
        }
    }

    /**
     * 停止背景音乐（内部方法）
     */
    private void stopBgMusic() {
        if (bgMusicPlayer != null) {
            // 如果正在播放，先停止
            if (bgMusicPlayer.isPlaying()) {
                bgMusicPlayer.stop();
            }
            // 释放播放器资源
            bgMusicPlayer.release();
            bgMusicPlayer = null;
        }
    }

    // ================= 音效功能方法 =================

    /**
     * 播放音效（如按键音效等短音效）
     * @param soundResId 音效资源ID
     */
    public void playSound(int soundResId) {
        // 如果音效未启用，直接返回
        if (!isSoundEnabled) return;

        // 加载音效资源
        soundPool.load(context, soundResId, 1);
        // 设置加载完成监听器
        soundPool.setOnLoadCompleteListener((soundPool, sampleId, status) -> {
            // 如果加载成功（status为0），播放音效
            if (status == 0) {
                // 参数说明：sampleId, 左音量, 右音量, 优先级, 循环次数, 播放速度
                soundPool.play(sampleId, 1.0f, 1.0f, 1, 0, 1.0f);
            }
        });
    }

    // ================= 控制功能方法 =================

    /**
     * 设置音效开关
     * @param enabled true:开启音效 false:关闭音效
     */
    public void setSoundEnabled(boolean enabled) {
        isSoundEnabled = enabled;
    }

    /**
     * 设置背景音乐开关
     * @param enabled true:开启背景音乐 false:关闭背景音乐
     */
    public void setBgMusicEnabled(boolean enabled) {
        isBgMusicEnabled = enabled;
        if (!enabled) {
            // 如果关闭，暂停背景音乐
            pauseBgMusic();
        } else if (bgMusicPlayer != null) {
            // 如果开启且播放器存在，恢复播放
            resumeBgMusic();
        }
    }

    /**
     * 设置背景音乐音量
     * @param volume 音量值，范围0.0f ~ 1.0f
     */
    public void setBgMusicVolume(float volume) {
        // 确保音量在有效范围内（0.0f ~ 1.0f）
        bgMusicVolume = Math.max(0.0f, Math.min(1.0f, volume));
        // 如果播放器存在，立即应用新音量
        if (bgMusicPlayer != null) {
            bgMusicPlayer.setVolume(bgMusicVolume, bgMusicVolume);
        }
    }

    /**
     * 检查音效是否开启
     * @return true:音效已开启 false:音效已关闭
     */
    public boolean isSoundEnabled() {
        return isSoundEnabled;
    }

    /**
     * 检查背景音乐是否开启
     * @return true:背景音乐已开启 false:背景音乐已关闭
     */
    public boolean isBgMusicEnabled() {
        return isBgMusicEnabled;
    }

    /**
     * 释放所有音频资源
     * 在Activity销毁或游戏退出时调用，防止资源泄漏
     */
    public void release() {
        // 释放背景音乐资源
        if (bgMusicPlayer != null) {
            if (bgMusicPlayer.isPlaying()) {
                bgMusicPlayer.stop();
            }
            bgMusicPlayer.release();
            bgMusicPlayer = null;
        }

        // 释放音效池资源
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }

        // 清除单例实例
        instance = null;
    }
}