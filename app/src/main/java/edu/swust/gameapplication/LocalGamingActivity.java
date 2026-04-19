package edu.swust.gameapplication;

import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

/**
 * 本地对战Activity - 双人同屏对战模式
 * 实现完整的五子棋对战逻辑，包括悔棋、暂停、胜负判定等功能
 * 实现boardView.GameEventListener接口以接收棋盘事件
 */
public class LocalGamingActivity extends AppCompatActivity implements
        View.OnClickListener,
        // 实现棋盘事件监听器接口
        boardView.GameEventListener {

    // ================= UI控件声明 =================

    // 控制按钮
    private ImageButton backButton;      // 返回按钮
    private ImageButton pauseButton;     // 暂停/继续按钮
    private ImageButton undoButton;      // 悔棋按钮

    // 游戏信息显示
    private TextView blackSteps;         // 黑棋步数显示
    private TextView whiteSteps;         // 白棋步数显示
    private TextView turnInfo;           // 当前回合信息显示

    // 棋盘视图
    private boardView boardView;         // 自定义棋盘View

    // ================= 游戏状态变量 =================

    // 游戏是否暂停
    private boolean isPaused = false;

    // ================= Activity生命周期 =================

    /**
     * Activity创建时的回调方法
     * savedInstanceState 保存的状态数据
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pvp); // 使用双人对战布局文件

        // 初始化所有UI控件
        initViews();

        // 设置视图属性
        setupViews();

        // 设置点击事件监听器
        setClickListeners();

        // 初始化游戏状态（黑棋先手，步数为0）
        updateStatus(1, 0, 0);

        // 恢复背景音乐
        GameApp.resumeBgMusic();
    }

    /**
     * 初始化所有UI控件，绑定布局文件中的视图
     */
    private void initViews() {
        // 绑定按钮控件
        backButton = findViewById(R.id.backButton);
        pauseButton = findViewById(R.id.pauseButton);
        undoButton = findViewById(R.id.undoButton);

        // 绑定信息显示控件
        blackSteps = findViewById(R.id.blackSteps);
        whiteSteps = findViewById(R.id.whiteSteps);
        turnInfo = findViewById(R.id.turnInfo);

        // 绑定棋盘视图
        boardView = findViewById(R.id.boardView);
    }

    /**
     * 设置视图的初始属性和配置
     */
    private void setupViews() {
        // 设置棋盘的事件监听器（将本Activity作为监听器）
        boardView.setGameEventListener(this);
    }

    /**
     * 设置所有控件的点击事件监听器
     */
    private void setClickListeners() {
        // 返回按钮
        backButton.setOnClickListener(v -> {
            GameApp.playSound(R.raw.button1);  // 播放按钮音效
            onClick(v);  // 调用统一的点击处理方法
        });

        // 暂停/继续按钮
        pauseButton.setOnClickListener(v -> {
            GameApp.playSound(R.raw.button1);  // 播放按钮音效
            onClick(v);
        });

        // 悔棋按钮
        undoButton.setOnClickListener(v -> {
            GameApp.playSound(R.raw.button);  // 播放特殊按钮音效
            onClick(v);
        });
    }

    // ================= 点击事件处理 =================

    /**
     * 统一的点击事件处理方法
     * @param v 被点击的View
     */
    @Override
    public void onClick(View v) {
        int id = v.getId();

        // 根据按钮ID执行不同的操作
        if (id == R.id.backButton) {
            handleBackClick();
        } else if (id == R.id.pauseButton) {
            handlePauseClick();
        } else if (id == R.id.undoButton) {
            handleUndoClick();
        }
    }

    /**
     * 处理返回按钮点击
     * 返回上一个界面
     */
    private void handleBackClick() {
        // 直接结束当前Activity，返回上一个界面
        finish();
    }

    /**
     * 处理暂停/继续按钮点击
     * 切换游戏暂停状态
     */
    private void handlePauseClick() {
        if (!isPaused) {
            // 暂停游戏
            pauseGame();
        } else {
            // 继续游戏
            resumeGame();
        }
    }

    /**
     * 暂停游戏
     */
    private void pauseGame() {
        isPaused = true;
        boardView.setPaused(true);                // 通知棋盘暂停
        undoButton.setEnabled(false);             // 禁用悔棋按钮
        pauseButton.setImageResource(android.R.drawable.ic_media_play);  // 切换为播放图标
        turnInfo.setText("游戏已暂停");             // 更新回合信息
        turnInfo.setBackgroundColor(Color.parseColor("#F5F5F5"));  // 设置暂停背景色
    }

    /**
     * 继续游戏
     */
    private void resumeGame() {
        isPaused = false;
        boardView.setPaused(false);               // 通知棋盘继续
        undoButton.setEnabled(true);              // 启用悔棋按钮
        pauseButton.setImageResource(android.R.drawable.ic_media_pause); // 切换为暂停图标
        // 恢复回合信息显示
        updateTurnInfo(boardView.getCurrentPlayer());
    }

    /**
     * 处理悔棋按钮点击
     * 调用棋盘的悔棋功能
     */
    private void handleUndoClick() {
        // 调用棋盘视图的悔棋方法
        boardView.undo();
        // 更新游戏状态显示
        updateStatus(boardView.getCurrentPlayer(),
                boardView.getBlackSteps(),
                boardView.getWhiteSteps());
    }

    // ================= 游戏状态更新 =================

    /**
     * 更新游戏状态显示（步数和当前玩家）
     * @param currentPlayer 当前玩家（1:黑棋，2:白棋）
     * @param blackStepsCount 黑棋步数
     * @param whiteStepsCount 白棋步数
     */
    public void updateStatus(int currentPlayer, int blackStepsCount, int whiteStepsCount) {
        runOnUiThread(() -> {
            // 更新步数显示
            blackSteps.setText("黑棋: " + blackStepsCount + "步");
            whiteSteps.setText("白棋: " + whiteStepsCount + "步");
            // 更新回合信息
            updateTurnInfo(currentPlayer);
        });
    }

    /**
     * 更新回合信息显示
     * @param currentPlayer 当前玩家（1:黑棋，2:白棋）
     */
    public void updateTurnInfo(int currentPlayer) {
        if (!isPaused) {
            String turnText = (currentPlayer == 1) ? "当前回合: 黑棋" : "当前回合: 白棋";
            turnInfo.setText(turnText);

            // 根据当前玩家设置不同的背景色
            if (currentPlayer == 1) {
                turnInfo.setBackgroundColor(Color.parseColor("#E8F5E9")); // 黑棋回合：浅绿色
            } else {
                turnInfo.setBackgroundColor(Color.parseColor("#FFF8E1")); // 白棋回合：浅黄色
            }
        }
    }

    // ================= 棋盘事件监听器实现 =================

    /**
     * 玩家切换时调用
     * @param currentPlayer 新回合的玩家（1:黑棋，2:白棋）
     */
    @Override
    public void onPlayerChanged(int currentPlayer) {
        // 更新回合信息显示
        updateTurnInfo(currentPlayer);
    }

    /**
     * 游戏结束时调用
     * @param winner 获胜者（1:黑棋，2:白棋，0:平局）
     */
    @Override
    public void onGameEnded(int winner) {
        runOnUiThread(() -> {
            String winnerText;
            boolean boardFull = boardView.isBoardFull();  // 检查棋盘是否已满

            if (boardFull) {
                // 棋盘已满的特殊情况：判白棋获胜（特殊规则）
                winnerText = "平局！棋盘已满";
            } else {
                // 正常胜负判定
                if (winner == 0) {
                    // 其他平局情况（理论上不应该出现）
                    winnerText = "平局！";
                } else {
                    // 正常胜负
                    winnerText = (winner == 1) ? "黑棋获胜！" : "白棋获胜！";
                }
            }

            // 更新回合信息为获胜信息
            turnInfo.setText(winnerText);
            turnInfo.setBackgroundColor(Color.parseColor("#FFECB3")); // 获胜提示色：淡黄色

            // 禁用悔棋按钮
            undoButton.setEnabled(false);

            // 显示获胜提示
            Toast.makeText(this, winnerText, Toast.LENGTH_LONG).show();

            // 显示游戏结束对话框
            showGameEndDialog(winnerText);
        });
    }

    /**
     * 显示游戏结束对话框
     * @param winnerText 获胜信息文本
     */
    private void showGameEndDialog(String winnerText) {
        new AlertDialog.Builder(this)
                .setTitle("游戏结束")
                .setMessage(winnerText)
                .setPositiveButton("重新开始", (dialog, which) -> {
                    // 重新开始游戏
                    resetGame();
                })
                .setNegativeButton("继续", (dialog, which) -> {
                    // 关闭对话框，保持当前棋局状态
                    dialog.dismiss();
                })
                .setNeutralButton("退出", (dialog, which) -> {
                    // 退出当前Activity
                    finish();
                })
                .setOnCancelListener(dialog -> {
                    // 对话框被取消时的处理
                })
                .show();
    }

    /**
     * 落子成功时调用
     * @param row 落子的行
     * @param col 落子的列
     * @param player 落子的玩家（1:黑棋，2:白棋）
     */
    @Override
    public void onMoveMade(int row, int col, int player) {
        // 更新步数显示
        updateStatus(boardView.getCurrentPlayer(),
                boardView.getBlackSteps(),
                boardView.getWhiteSteps());
    }


    // ================= 游戏控制 =================

    /**
     * 重新开始游戏
     * 重置棋盘和游戏状态
     */
    private void resetGame() {
        // 重置棋盘
        boardView.resetGame();
        // 重置游戏状态显示
        updateStatus(1, 0, 0);
        // 启用悔棋按钮
        undoButton.setEnabled(true);
    }

    // ================= Activity生命周期管理 =================

    /**
     * Activity销毁时的清理工作
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 移除棋盘的事件监听器，防止内存泄漏
        if (boardView != null) {
            boardView.setGameEventListener(null);
        }
    }
}