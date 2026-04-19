package edu.swust.gameapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainGameActivity extends AppCompatActivity {

    // 声明控件
    private ImageButton btnAvatar;
    private TextView tvUsername;
    private ImageButton btnAiBattle;
    private ImageButton btnLocalBattle;
    private ImageButton btnRanking;
    private ImageButton btnExitGame;
    private ImageButton btnMusicControl; // 新增：音乐控制按钮

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_game);

        // 初始化控件
        initViews();

        // 设置点击事件监听
        setupEventListeners();

        // 加载用户数据
        loadUserData();

        // 初始化音乐按钮状态
        initMusicButtonState();

        // 播放背景音乐（如果开启）
        playBackgroundMusic();
    }

    /**
     * 初始化所有控件
     */
    private void initViews() {
        btnAvatar = findViewById(R.id.btn_avatar);
        tvUsername = findViewById(R.id.tv_username);
        btnAiBattle = findViewById(R.id.btn_ai_battle);
        btnLocalBattle = findViewById(R.id.btn_local_battle);
        btnRanking = findViewById(R.id.btn_ranking);
        btnExitGame = findViewById(R.id.btn_exit_game);
        btnMusicControl = findViewById(R.id.btn_music_control); // 新增
    }

    /**
     * 初始化音乐按钮状态
     */
    private void initMusicButtonState() {
        // 根据当前音乐状态设置按钮图标
        updateMusicButtonIcon();
    }

    /**
     * 更新音乐按钮图标
     */
    private void updateMusicButtonIcon() {
        if (GameApp.isBgMusicEnabled()) {
            // 音乐开启状态，显示开启图标
            btnMusicControl.setBackgroundResource(R.drawable.music_on);
        } else {
            // 音乐关闭状态，显示关闭图标
            btnMusicControl.setBackgroundResource(R.drawable.music_off);
        }
    }

    /**
     * 设置事件监听器
     */
    private void setupEventListeners() {
        // 头像按钮
        btnAvatar.setOnClickListener(v -> {
            GameApp.playSound(R.raw.button1);
            handleAvatarClick();
        });

        // 人机对战按钮
        btnAiBattle.setOnClickListener(v -> {
            GameApp.playSound(R.raw.gamestart);
            handleAiBattleClick();
        });

        // 本地对战按钮
        btnLocalBattle.setOnClickListener(v -> {
            GameApp.playSound(R.raw.gamestart);
            handleLocalBattleClick();
        });

        // 排行榜按钮
        btnRanking.setOnClickListener(v -> {
            GameApp.playSound(R.raw.button1);
            handleRankingClick();
        });

        // 退出游戏按钮
        btnExitGame.setOnClickListener(v -> {
            GameApp.playSound(R.raw.button1);
            handleExitGameClick();
        });

        // 音乐控制按钮
        btnMusicControl.setOnClickListener(v -> {
            GameApp.playSound(R.raw.button1);
            toggleMusicState();
        });
    }

    /**
     * 切换音乐状态
     */
    private void toggleMusicState() {
        boolean currentState = GameApp.isBgMusicEnabled();
        boolean newState = !currentState;

        // 更新应用中的音乐设置
        GameApp.setBgMusicEnabled(newState);

        // 根据新状态执行相应操作
        if (newState) {
            // 如果开启音乐，开始播放背景音乐
            GameApp.playBgMusic(R.raw.background);
        } else {
            // 如果关闭音乐，暂停背景音乐
            GameApp.pauseBgMusic();
        }

        // 更新按钮图标
        updateMusicButtonIcon();
    }


    /**
     * 播放背景音乐
     */
    private void playBackgroundMusic() {
        // 检查背景音乐是否开启
        if (GameApp.isBgMusicEnabled()) {
            GameApp.playBgMusic(R.raw.background);
        }
    }

    /**
     * 加载用户数据
     */
    private void loadUserData() {
        // 获取当前登录账号
        String account = GameApp.getCurrentLoginAccount();

        // 如果没有登录账号，显示默认
        if (account == null || account.isEmpty()) {
            tvUsername.setText("用户");
            return;
        }

        // 设置用户名
        tvUsername.setText(account);

        // 在后台线程加载头像
        GameApp.getGlobalExecutor().execute(() -> {
            User user = GameApp.getUserDao().getUserByAccount(account);
            if (user != null) {
                runOnUiThread(() -> {
                    updateAvatar(user.getAvatarId());
                });
            }
        });
    }

    /**
     * 更新头像
     */
    private void updateAvatar(int avatarId) {
        int avatarResourceId = getAvatarResourceId(avatarId);
        btnAvatar.setImageResource(avatarResourceId);
    }

    /**
     * 根据头像ID获取资源ID
     */
    private int getAvatarResourceId(int avatarId) {
        if (avatarId == 0) {
            return R.drawable.avatar_default;
        } else {
            switch (avatarId) {
                case 1: return R.drawable.avatar_1;
                case 2: return R.drawable.avatar_2;
                case 3: return R.drawable.avatar_3;
                case 4: return R.drawable.avatar_4;
                case 5: return R.drawable.avatar_5;
                case 6: return R.drawable.avatar_6;
                case 7: return R.drawable.avatar_7;
                case 8: return R.drawable.avatar_8;
                case 9: return R.drawable.avatar_9;
                case 10: return R.drawable.avatar_10;
                default: return R.drawable.avatar_default;
            }
        }
    }

    /**
     * 头像点击事件
     */
    private void handleAvatarClick() {
        Intent intent = new Intent(this, HomePageActivity.class);
        startActivity(intent);
    }

    /**
     * 人机对战点击事件
     */
    private void handleAiBattleClick() {
        Intent intent = new Intent(this, PvcGamingActivity.class);
        startActivity(intent);
    }

    /**
     * 本地对战点击事件
     */
    private void handleLocalBattleClick() {
        Intent intent = new Intent(this, LocalGamingActivity.class);
        startActivity(intent);
    }

    /**
     * 排行榜点击事件
     */
    private void handleRankingClick() {
        Intent intent = new Intent(this, RankingActivity.class);
        startActivity(intent);
    }

    /**
     * 退出游戏点击事件
     */
    private void handleExitGameClick() {
        new android.app.AlertDialog.Builder(this)
                .setTitle("退出游戏")
                .setMessage("确定要退出游戏吗？")
                .setPositiveButton("确定", (dialog, which) -> {
                    // 播放确定按钮音效
                    // 清理资源并退出
                    GameApp.cleanup();
                    finishAffinity();
                })
                .setNegativeButton("取消", (dialog, which) -> {
                })
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 重新加载用户数据
        loadUserData();
        // 恢复背景音乐（如果开启）
        if (GameApp.isBgMusicEnabled()) {
            GameApp.resumeBgMusic();
        }
        // 更新音乐按钮状态
        updateMusicButtonIcon();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // 暂停背景音乐
        GameApp.pauseBgMusic();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}