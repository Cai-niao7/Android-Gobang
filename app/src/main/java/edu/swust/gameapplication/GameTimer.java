package edu.swust.gameapplication;

import android.os.Handler;
import android.os.Looper;

/**
 * 游戏计时器 - 优化版
 * 使用单例模式管理游戏时间，支持多种时间格式和状态控制
 * 功能：开始、暂停、继续、停止、重置，并实时通知UI更新
 */
public class GameTimer {

    // ================= 接口定义 =================

    /**
     * 计时器监听器接口
     * UI组件通过实现此接口接收计时器事件
     */
    public interface TimerListener {
        /**
         * 每秒回调一次，更新显示的时间
         * formattedTime 格式化后的时间字符串
         */
        void onTimerTick(String formattedTime);

        /**
         * 计时器状态变化时回调
         * isRunning 计时器是否正在运行
         * isPaused 计时器是否处于暂停状态
         */
        void onTimerStateChanged(boolean isRunning, boolean isPaused);
    }

    // ================= 单例模式 =================

    private static GameTimer instance;
    private TimerListener timerListener;
    private Handler uiHandler; // 用于在主线程更新UI

    // ================= 状态变量 =================

    // 计时器是否正在运行（线程安全）
    private volatile boolean isRunning = false;
    // 计时器是否暂停（线程安全）
    private volatile boolean isPaused = false;

    // ================= 时间计算变量 =================

    private long startTime = 0;       // 计时器开始的时间戳（毫秒）
    private long pauseStartTime = 0;  // 暂停开始的时间戳（毫秒）
    private long totalPausedTime = 0; // 累计暂停时间（毫秒）

    // ================= 线程同步 =================

    // 同步锁，保证多线程环境下的线程安全
    private final Object lock = new Object();
    private Thread timerThread; // 计时器工作线程

    // ================= 私有构造函数 =================

    private GameTimer() {
        // 初始化UI线程的Handler，确保回调在主线程执行
        uiHandler = new Handler(Looper.getMainLooper());
    }

    /**
     * 获取GameTimer单例实例
     * @return GameTimer单例
     */
    public static synchronized GameTimer getInstance() {
        if (instance == null) {
            instance = new GameTimer();
        }
        return instance;
    }

    // ================= 监听器管理 =================

    /**
     * 设置计时器监听器
     * listener 实现TimerListener接口的对象
     */
    public void setTimerListener(TimerListener listener) {
        this.timerListener = listener;
    }

    /**
     * 移除计时器监听器
     */
    public void removeTimerListener() {
        this.timerListener = null;
    }

    // ================= 计时器控制方法 =================

    /**
     * 开始计时（如果未运行）
     * 设置初始时间并启动计时线程
     */
    public void start() {
        synchronized (lock) {
            if (isRunning) return; // 已经在运行则不重复启动

            // 设置初始状态
            isRunning = true;
            isPaused = false;
            startTime = System.currentTimeMillis();
            totalPausedTime = 0;

            // 通知状态变化
            notifyStateChange();

            // 创建并启动计时线程
            timerThread = new Thread(new TimerTask());
            timerThread.start();
        }
    }

    /**
     * 暂停计时（如果正在运行且未暂停）
     * 记录暂停开始的时间
     */
    public void pause() {
        synchronized (lock) {
            if (!isRunning || isPaused) return; // 未运行或已暂停则不处理

            isPaused = true;
            pauseStartTime = System.currentTimeMillis(); // 记录暂停开始时间
            notifyStateChange();
        }
    }

    /**
     * 继续计时（如果已暂停）
     * 计算暂停时间并累加到总暂停时间
     */
    public void resume() {
        synchronized (lock) {
            if (!isRunning || !isPaused) return; // 未运行或未暂停则不处理

            isPaused = false;
            // 累加本次暂停的时间
            totalPausedTime += System.currentTimeMillis() - pauseStartTime;
            notifyStateChange();
        }
    }

    /**
     * 停止计时
     * 中断计时线程并重置状态
     */
    public void stop() {
        synchronized (lock) {
            isRunning = false;
            isPaused = false;

            // 中断计时线程
            if (timerThread != null) {
                timerThread.interrupt();
                timerThread = null;
            }

            notifyStateChange();
        }
    }

    /**
     * 重置计时器
     * 停止后重新开始，或者直接重置时间变量
     */
    public void reset() {
        synchronized (lock) {
            boolean wasRunning = isRunning; // 保存之前的状态

            // 如果正在运行，先停止
            if (wasRunning) {
                stop();
            }

            // 重置所有时间变量
            startTime = 0;
            pauseStartTime = 0;
            totalPausedTime = 0;

            // 如果之前是运行状态，重新开始
            if (wasRunning) {
                start();
            }
        }
    }

    // ================= 时间获取方法 =================

    /**
     * 获取已用时间（毫秒）
     * 计算公式：当前时间 - 开始时间 - 总暂停时间
     * @return 已用时间的毫秒数
     */
    public long getElapsedTime() {
        synchronized (lock) {
            if (!isRunning) return 0; // 未运行返回0

            if (isPaused) {
                // 暂停状态：到暂停时刻的时间
                return pauseStartTime - startTime - totalPausedTime;
            }

            // 运行状态：当前时间减去开始时间和暂停时间
            return System.currentTimeMillis() - startTime - totalPausedTime;
        }
    }

    /**
     * 获取格式化后的时间字符串
     * format 时间格式（MM_SS或HH_MM_SS）
     * @return 格式化后的时间字符串
     */
    public String getFormattedTime(TimeFormat format) {
        long time = getElapsedTime();
        return formatTime(time, format);
    }

    /**
     * 获取默认格式的时间字符串（MM:SS）
     * @return MM:SS格式的时间字符串
     */
    public String getFormattedTime() {
        return getFormattedTime(TimeFormat.MM_SS);
    }

    // ================= 私有工具方法 =================

    /**
     * 将毫秒时间格式化为字符串
     * milliseconds 毫秒数
     * format 目标格式
     * @return 格式化后的时间字符串
     */
    private String formatTime(long milliseconds, TimeFormat format) {
        long seconds = milliseconds / 1000;  // 总秒数
        long minutes = seconds / 60;         // 总分钟数
        long hours = minutes / 60;           // 总小时数

        seconds = seconds % 60;  // 剩余秒数
        minutes = minutes % 60;  // 剩余分钟数

        switch (format) {
            case HH_MM_SS:
                return String.format("%02d:%02d:%02d", hours, minutes, seconds);
            case MM_SS:
            default:
                return String.format("%02d:%02d", minutes, seconds);
        }
    }

    /**
     * 通知UI线程时间更新
     * @param formattedTime 格式化后的时间字符串
     */
    private void notifyTick(final String formattedTime) {
        if (timerListener != null) {
            uiHandler.post(() -> timerListener.onTimerTick(formattedTime));
        }
    }

    /**
     * 通知UI线程状态变化
     */
    private void notifyStateChange() {
        if (timerListener != null) {
            uiHandler.post(() ->
                    timerListener.onTimerStateChanged(isRunning, isPaused));
        }
    }

    // ================= 内部计时任务类 =================

    /**
     * 计时器工作线程的任务
     * 每秒计算一次时间并通知UI更新
     */
    private class TimerTask implements Runnable {
        @Override
        public void run() {
            try {
                // 循环直到计时器停止或线程被中断
                while (isRunning && !Thread.currentThread().isInterrupted()) {
                    synchronized (lock) {
                        if (!isPaused) {
                            // 只在非暂停状态下更新时间
                            long elapsedTime = getElapsedTime();
                            String formattedTime = formatTime(elapsedTime, TimeFormat.MM_SS);
                            notifyTick(formattedTime);
                        }
                    }
                    // 每秒更新一次
                    Thread.sleep(1000);
                }
            } catch (InterruptedException e) {
                // 线程被正常中断，恢复中断状态
                Thread.currentThread().interrupt();
            }
        }
    }

    // ================= 时间格式枚举 =================

    /**
     * 时间格式枚举
     */
    public enum TimeFormat {
        MM_SS,      // 分:秒格式，如 05:30
        HH_MM_SS    // 时:分:秒格式，如 01:05:30
    }
}