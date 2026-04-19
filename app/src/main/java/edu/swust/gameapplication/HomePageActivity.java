package edu.swust.gameapplication;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Random;

/**
 * 主页（个人中心）Activity
 * 显示用户信息、游戏统计数据，支持修改用户名和头像
 */
public class HomePageActivity extends AppCompatActivity {

    // ================= UI控件声明 =================

    // 返回按钮 - 返回主菜单
    private ImageButton btnBack;

    // 切换账户按钮 - 退出当前账户，返回登录界面
    private ImageButton btnSwitchAccount;

    // 头像显示区域
    private ImageView ivAvatar;

    // 用户名显示区域
    private TextView tvUsername;

    // 简单模式游戏统计
    private TextView tvEasyWinRate;      // 简单模式胜率
    private TextView tvEasyTotalGames;   // 简单模式总对局
    private TextView tvEasyWinGames;     // 简单模式胜利局数

    // 困难模式游戏统计
    private TextView tvHardWinRate;      // 困难模式胜率
    private TextView tvHardTotalGames;   // 困难模式总对局
    private TextView tvHardWinGames;     // 困难模式胜利局数

    // 操作提示文本
    private TextView tvChangeUsernameHint;  // "修改用户名"提示
    private TextView tvChangeAvatarHint;    // "更换头像"提示

    // ================= 其他变量 =================

    // 随机数生成器 - 用于随机选择头像
    private Random random = new Random();

    // ================= Activity生命周期 =================

    /**
     * Activity创建时的回调方法
     * @param savedInstanceState 保存的状态数据
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.homepage_activity);  // 设置布局文件

        // 初始化UI控件
        initViews();

        // 设置控件点击事件监听器
        setClickListeners();

        // 加载并显示用户数据
        loadUserData();

        // 恢复背景音乐播放
        GameApp.resumeBgMusic();
    }

    /**
     * 初始化所有UI控件，绑定布局文件中的视图
     */
    private void initViews() {
        // 绑定按钮
        btnBack = findViewById(R.id.btn_back);
        btnSwitchAccount = findViewById(R.id.btn_switch_account);

        // 绑定用户信息显示控件
        ivAvatar = findViewById(R.id.iv_avatar);
        tvUsername = findViewById(R.id.tv_username);

        // 绑定游戏统计数据控件
        tvEasyWinRate = findViewById(R.id.tv_easy_win_rate);
        tvEasyTotalGames = findViewById(R.id.tv_easy_total_games);
        tvEasyWinGames = findViewById(R.id.tv_easy_win_games);
        tvHardWinRate = findViewById(R.id.tv_hard_win_rate);
        tvHardTotalGames = findViewById(R.id.tv_hard_total_games);
        tvHardWinGames = findViewById(R.id.tv_hard_win_games);

        // 绑定操作提示控件
        tvChangeUsernameHint = findViewById(R.id.tv_change_username_hint);
        tvChangeAvatarHint = findViewById(R.id.tv_change_avatar_hint);
    }

    /**
     * 设置所有控件的点击事件监听器
     */
    private void setClickListeners() {
        // 返回按钮
        btnBack.setOnClickListener(v -> {
            GameApp.playSound(R.raw.button1);  // 播放按钮音效
            finish();
        });

        // 切换账户按钮 - 显示确认对话框
        btnSwitchAccount.setOnClickListener(v -> {
            GameApp.playSound(R.raw.button1);
            showSwitchAccountDialog();
        });

        // 头像点击 - 显示头像选择对话框
        ivAvatar.setOnClickListener(v -> {
            GameApp.playSound(R.raw.button1);
            showAvatarSelectionDialog();
        });

        // 用户名点击 - 显示用户名修改对话框
        tvUsername.setOnClickListener(v -> {
            GameApp.playSound(R.raw.button1);
            showUsernameChangeDialog();
        });

        // "修改用户名"提示文本点击 - 功能同用户名点击
        tvChangeUsernameHint.setOnClickListener(v -> {
            GameApp.playSound(R.raw.button1);
            showUsernameChangeDialog();
        });

        // "更换头像"提示文本点击 - 功能同头像点击
        tvChangeAvatarHint.setOnClickListener(v -> {
            GameApp.playSound(R.raw.button1);
            showAvatarSelectionDialog();
        });
    }

    // ================= 切换账户功能 =================

    /**
     * 显示切换账户确认对话框
     * 用户确认后退出当前账户，返回登录界面
     */
    private void showSwitchAccountDialog() {
        new AlertDialog.Builder(this)
                .setTitle("切换账户")
                .setMessage("确定要切换账户吗？当前账户的游戏数据将被保存。")
                .setPositiveButton("确定", (dialog, which) -> {
                    // 清除全局登录状态
                    GameApp.setCurrentLoginAccount(null);

                    // 创建跳转到登录界面的Intent
                    Intent intent = new Intent(HomePageActivity.this, MainActivity.class);
                    // 清除任务栈并创建新任务，确保用户不能通过返回键回到主页
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();  // 结束当前Activity

                    // 显示操作成功的提示
                    Toast.makeText(HomePageActivity.this, "已退出当前账户", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("取消", (dialog, which) -> dialog.dismiss())  // 取消操作
                .show();
    }

    // ================= 头像管理功能 =================

    /**
     * 显示头像选择对话框
     * 用户确认后随机更换头像
     */
    private void showAvatarSelectionDialog() {
        new AlertDialog.Builder(this)
                .setTitle("更换头像")
                .setMessage("确定要更换头像吗？将随机一个新头像。")
                .setPositiveButton("确定", (dialog, which) -> randomChangeAvatar())
                .setNegativeButton("取消", (dialog, which) -> dialog.dismiss())
                .show();
    }

    /**
     * 随机更换头像（从1-10中随机选择）
     * 在后台线程中更新数据库，在主线程中更新UI
     */
    private void randomChangeAvatar() {
        // 生成1-10的随机数作为新头像ID
        int newAvatarId = random.nextInt(10) + 1;

        // 使用全局线程池执行数据库操作
        GameApp.getGlobalExecutor().execute(() -> {
            // 获取当前登录用户的账号
            String account = GameApp.getCurrentLoginAccount();

            // 更新数据库中的头像ID
            int rowsAffected = GameApp.getUserDao().updateAvatar(account, newAvatarId);

            // 如果更新成功，在主线程更新UI
            if (rowsAffected > 0) {
                runOnUiThread(() -> {
                    // 更新头像显示
                    updateAvatar(newAvatarId);
                    Toast.makeText(HomePageActivity.this, "头像已更换", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    // ================= 用户名管理功能 =================

    /**
     * 显示用户名修改对话框
     * 包含输入框让用户输入新用户名
     */
    private void showUsernameChangeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("修改用户名");

        // 创建输入框并设置初始值为当前用户名
        final EditText input = new EditText(this);
        input.setText(tvUsername.getText());
        input.setHint("请输入新的用户名（1-16个字符）");
        input.setMaxLines(1);
        input.setSelection(input.getText().length());  // 光标移动到文本末尾
        builder.setView(input);

        // 确定按钮
        builder.setPositiveButton("确定", (dialog, which) -> {
            String newUsername = input.getText().toString().trim();
            if (validateUsername(newUsername)) {
                changeUsername(newUsername);
            }
        });

        // 取消按钮
        builder.setNegativeButton("取消", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    /**
     * 验证用户名的有效性
     * username 待验证的用户名
     * @return 验证是否通过
     */
    private boolean validateUsername(String username) {
        // 检查是否为空
        if (username.isEmpty()) {
            Toast.makeText(this, "用户名不能为空", Toast.LENGTH_SHORT).show();
            return false;
        }

        // 检查长度是否在1-16个字符之间
        if (username.length() > 16) {
            Toast.makeText(this, "用户名长度限制16个字符内", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    /**
     * 修改用户名
     * 先检查用户名是否已存在，然后更新数据库
     * newUsername 新用户名
     */
    private void changeUsername(final String newUsername) {
        // 在后台线程中检查用户名是否存在
        GameApp.getGlobalExecutor().execute(() -> {
            // 检查新用户名是否已存在于数据库中
            int count = GameApp.getUserDao().checkAccountExists(newUsername);

            // 在主线程中处理检查结果
            runOnUiThread(() -> {
                if (count > 0) {
                    // 用户名已存在，提示用户
                    Toast.makeText(HomePageActivity.this, "用户名已存在，请选择其他用户名", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 用户名可用，更新数据库
                updateUsernameInDatabase(newUsername);
            });
        });
    }

    /**
     * 在数据库中更新用户名
     * newUsername 新用户名
     */
    private void updateUsernameInDatabase(final String newUsername) {
        GameApp.getGlobalExecutor().execute(() -> {
            try {
                // 获取当前用户信息
                String currentAccount = GameApp.getCurrentLoginAccount();
                User user = GameApp.getUserDao().getUserByAccount(currentAccount);

                if (user != null) {
                    // 更新用户名
                    user.setAccount(newUsername);
                    GameApp.getUserDao().updateUser(user);

                    // 更新全局登录账号
                    GameApp.setCurrentLoginAccount(newUsername);

                    // 在主线程更新UI
                    runOnUiThread(() -> {
                        tvUsername.setText(newUsername);
                        Toast.makeText(HomePageActivity.this, "用户名已修改为: " + newUsername, Toast.LENGTH_SHORT).show();
                    });
                }
            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(HomePageActivity.this, "修改用户名失败", Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    // ================= 数据加载功能 =================

    /**
     * 加载用户数据和游戏统计信息
     * 在后台线程中查询数据库，在主线程中更新UI
     */
    private void loadUserData() {
        GameApp.getGlobalExecutor().execute(() -> {
            try {
                // 获取当前登录用户账号
                String account = GameApp.getCurrentLoginAccount();

                // 从数据库获取用户完整信息
                User currentUser = GameApp.getUserDao().getUserByAccount(account);

                // 获取游戏统计数据（简单模式）
                final int easyTotal = GameApp.getGameRecordDao().countEasyGames(currentUser.getId());
                final int easyWins = GameApp.getGameRecordDao().countEasyWins(currentUser.getId());

                // 获取游戏统计数据（困难模式）
                final int hardTotal = GameApp.getGameRecordDao().countHardGames(currentUser.getId());
                final int hardWins = GameApp.getGameRecordDao().countHardWins(currentUser.getId());

                // 计算胜率
                final double easyWinRate = easyTotal > 0 ? (easyWins * 100.0 / easyTotal) : 0;
                final double hardWinRate = hardTotal > 0 ? (hardWins * 100.0 / hardTotal) : 0;

                // 在主线程中更新UI
                runOnUiThread(() -> {
                    // 更新用户信息
                    tvUsername.setText(currentUser.getAccount());

                    // 更新游戏统计数据
                    updateGameStats(easyTotal, easyWins, easyWinRate,
                            hardTotal, hardWins, hardWinRate);

                    // 设置头像
                    updateAvatar(currentUser.getAvatarId());
                });

            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(HomePageActivity.this, "加载数据失败", Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    /**
     * 更新游戏统计数据到UI控件
     */
    private void updateGameStats(int easyTotal, int easyWins, double easyWinRate,
                                 int hardTotal, int hardWins, double hardWinRate) {
        // 更新简单模式数据
        tvEasyWinRate.setText(String.format("胜率: %.1f%%", easyWinRate));
        tvEasyTotalGames.setText("总对局: " + easyTotal);
        tvEasyWinGames.setText("胜利: " + easyWins);

        // 更新困难模式数据
        tvHardWinRate.setText(String.format("胜率: %.1f%%", hardWinRate));
        tvHardTotalGames.setText("总对局: " + hardTotal);
        tvHardWinGames.setText("胜利: " + hardWins);
    }

    // ================= 头像资源管理 =================

    /**
     * 根据头像ID更新头像显示
     * avatarId 头像ID（1-10）
     */
    private void updateAvatar(int avatarId) {
        int avatarResourceId = getAvatarResourceId(avatarId);
        ivAvatar.setImageResource(avatarResourceId);
    }

    /**
     * 根据头像ID获取对应的drawable资源ID
     * avatarId 头像ID（1-10）
     * drawable资源ID
     */
    private int getAvatarResourceId(int avatarId) {
        // 根据头像ID返回对应的资源ID
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
            default: return R.drawable.avatar_default;  // 默认头像
        }
    }
}