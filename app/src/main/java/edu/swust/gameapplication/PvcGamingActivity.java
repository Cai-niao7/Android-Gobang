package edu.swust.gameapplication;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

/**
 * 人机对战Activity - 玩家与AI对战模式
 * 支持选择难度、选择玩家执棋颜色、计时、悔棋、保存战绩等功能
 */
public class PvcGamingActivity extends AppCompatActivity implements
        View.OnClickListener,
        boardView.GameEventListener,
        RadioGroup.OnCheckedChangeListener,
        AdapterView.OnItemSelectedListener,
        GameTimer.TimerListener {

    // ================= UI控件声明 =================

    // 控制按钮
    private ImageButton backButton;      // 返回按钮
    private ImageButton pauseButton;     // 暂停/继续按钮
    private ImageButton undoButton;      // 悔棋按钮

    // 游戏信息显示
    private TextView blackSteps;         // 黑棋步数显示
    private TextView whiteSteps;         // 白棋步数显示
    private TextView turnInfo;           // 当前回合信息显示
    private TextView timerTextView;      // 计时器显示

    // 棋盘视图
    private boardView boardView;         // 自定义棋盘View

    // 游戏设置控件
    private Spinner difficultySpinner;   // 难度选择下拉框
    private RadioGroup playerColorGroup; // 玩家执棋颜色选择组
    private RadioButton playerBlackRadio; // 玩家执黑选项
    private RadioButton playerWhiteRadio; // 玩家执白选项

    // ================= 游戏状态变量 =================

    // 游戏暂停状态
    private boolean isPaused = false;
    // 玩家是否执黑棋（true:玩家执黑，false:玩家执白）
    private boolean playerIsBlack = true;
    // 当前AI难度
    private GameAi.Difficulty currentDifficulty = GameAi.Difficulty.EASY;
    // 悔棋次数统计（用于处理AI对战的特殊情况）
    private int undoCount = 0;

    // ================= 计时器相关 =================

    // 游戏计时器（单例）
    private GameTimer gameTimer;
    // 标记是否因应用进入后台而自动暂停
    private boolean wasAutoPaused = false;

    // ================= Activity生命周期 =================

    /**
     * Activity创建时的回调方法
     * @param savedInstanceState 保存的状态数据
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pvc); // 使用人机对战布局文件

        // 初始化UI控件
        initViews();

        // 设置视图属性
        setupViews();

        // 设置点击事件监听器
        setupClickListeners();

        // 初始化游戏计时器
        initGameTimer();

        // 开始新游戏
        startNewGame();

        // 恢复背景音乐
        GameApp.resumeBgMusic();
    }

    /**
     * 初始化游戏计时器
     */
    private void initGameTimer() {
        gameTimer = GameTimer.getInstance();
        gameTimer.setTimerListener(this);
    }

    /**
     * 设置点击事件监听器
     */
    private void setupClickListeners() {
        // 返回按钮
        backButton.setOnClickListener(v -> {
            GameApp.playSound(R.raw.button1);
            onClick(v);
        });

        // 暂停/继续按钮
        pauseButton.setOnClickListener(v -> {
            GameApp.playSound(R.raw.button1);
            onClick(v);
        });

        // 悔棋按钮
        undoButton.setOnClickListener(v -> {
            GameApp.playSound(R.raw.button);
            onClick(v);
        });
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
        timerTextView = findViewById(R.id.timerTextView);

        // 绑定棋盘视图
        boardView = findViewById(R.id.boardView);

        // 绑定游戏设置控件
        difficultySpinner = findViewById(R.id.difficultySpinner);
        playerColorGroup = findViewById(R.id.playerColorGroup);
        playerBlackRadio = findViewById(R.id.playerBlack);
        playerWhiteRadio = findViewById(R.id.playerWhite);
    }

    /**
     * 设置视图的初始属性和配置
     */
    private void setupViews() {
        // 设置难度下拉框适配器
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.difficulty_levels, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        difficultySpinner.setAdapter(adapter);
        difficultySpinner.setSelection(0); // 默认选择简单难度

        // 2. 设置棋盘事件监听器
        boardView.setGameEventListener(this);

        // 3. 设置监听器
        playerColorGroup.setOnCheckedChangeListener(this);
        difficultySpinner.setOnItemSelectedListener(this);

        // 4. 设置默认玩家执棋颜色
        if (playerIsBlack) {
            playerBlackRadio.setChecked(true);
        } else {
            playerWhiteRadio.setChecked(true);
        }
    }

    /**
     * 开始新游戏
     * 重置棋盘、初始化AI、更新UI状态
     */
    private void startNewGame() {
        // 重置棋盘
        boardView.resetGame();

        // 初始化AI游戏
        // 如果玩家执黑，AI执白(2)；如果玩家执白，AI执黑(1)
        int aiPlayerColor = playerIsBlack ? 2 : 1;
        boardView.initAIGame(aiPlayerColor, currentDifficulty, playerIsBlack);

        // 更新初始状态显示
        updateStatus(playerIsBlack ? 1 : 2, 0, 0);

        // 启用所有控制按钮
        undoButton.setEnabled(true);
        difficultySpinner.setEnabled(true);
        playerColorGroup.setEnabled(true);
        pauseButton.setEnabled(true);
        playerBlackRadio.setEnabled(true);
        playerWhiteRadio.setEnabled(true);

        // 重置状态变量
        wasAutoPaused = false;
        undoCount = 0;
        // 重置并开始计时器
        gameTimer.reset();
        gameTimer.start();
        // 更新UI
        updateUIForResumeState();
    }

    // ================= 点击事件处理 =================

    /**
     * 统一的点击事件处理方法
     */
    @Override
    public void onClick(View v) {
        int id = v.getId();

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
     */
    private void handleBackClick() {
        pauseGame();
        // 游戏未结束，显示选项对话框
        new AlertDialog.Builder(this)
                .setTitle("游戏选项")
                .setMessage("游戏尚未结束，请选择操作：")
                .setPositiveButton("重新开始", (dialog, which) -> {
                    GameApp.playSound(R.raw.button1);
                    playerIsBlack = true;  // 重置为玩家执黑
                    playerBlackRadio.setChecked(true);
                    resumeGame();
                    startNewGame();
                })
                .setNegativeButton("返回主菜单", (dialog, which) -> {
                    GameApp.playSound(R.raw.button1);
                    finish();
                })
                .setNeutralButton("取消", (dialog, which) -> {
                    GameApp.playSound(R.raw.button1);
                    dialog.dismiss();
                })
                .setOnCancelListener(dialog -> GameApp.playSound(R.raw.button1))
                .show();

    }

    /**
     * 处理暂停/继续按钮点击
     */
    private void handlePauseClick() {
        if (!isPaused) {
            // 手动暂停游戏
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
        boardView.setPaused(true);   // 通知棋盘暂停
        gameTimer.pause();           // 暂停计时器

        updateUIForPauseState();     // 更新UI为暂停状态
        Toast.makeText(this, "游戏已暂停", Toast.LENGTH_SHORT).show();
    }

    /**
     * 继续游戏
     */
    private void resumeGame() {
        isPaused = false;
        boardView.setPaused(false);  // 通知棋盘继续
        gameTimer.resume();          // 继续计时器

        updateUIForResumeState();    // 更新UI为继续状态
        Toast.makeText(this, "游戏继续", Toast.LENGTH_SHORT).show();
    }

    /**
     * 更新UI为暂停状态
     */
    private void updateUIForPauseState() {
        runOnUiThread(() -> {
            // 切换按钮图标为播放图标
            pauseButton.setImageResource(android.R.drawable.ic_media_play);
            turnInfo.setText("游戏已暂停");

            // 禁用相关控件
            undoButton.setEnabled(false);
            playerColorGroup.setEnabled(false);
            playerBlackRadio.setEnabled(false);
            playerWhiteRadio.setEnabled(false);
            difficultySpinner.setEnabled(false);
        });
    }

    /**
     * 更新UI为继续状态
     */
    private void updateUIForResumeState() {
        runOnUiThread(() -> {
            // 切换按钮图标为暂停图标
            pauseButton.setImageResource(android.R.drawable.ic_media_pause);
            undoButton.setEnabled(true);

            // 根据游戏状态更新控件可用性
            checkAndUpdateControls();

            // 更新回合信息
            updateTurnInfo(boardView.getCurrentPlayer());
        });
    }

    /**
     * 处理悔棋按钮点击
     * AI对战模式下需要特殊处理：悔棋需要撤销两步（玩家一步+AI一步）
     */
    private void handleUndoClick() {
        int totalPieces = boardView.getBlackSteps() + boardView.getWhiteSteps();

        // 限制：棋子总数小于等于10时不能悔棋
        if (totalPieces <= 10) {
            Toast.makeText(this, "棋子总数小于等于10，不能悔棋", Toast.LENGTH_SHORT).show();
            return;
        }

        // 悔棋两步（玩家一步+AI一步）
        boardView.undo();
        undoCount++;

        if (undoCount % 2 == 1) {
            // 如果是奇数次悔棋，再悔一步（撤销AI的回合）
            boardView.undo();
            undoCount++;
        }

        // 更新游戏状态显示
        updateStatus(boardView.getCurrentPlayer(),
                boardView.getBlackSteps(),
                boardView.getWhiteSteps());

        // 更新控件状态
        checkAndUpdateControls();
    }

    /**
     * 检查和更新控件可用性
     * 游戏开始后不允许更改难度和执棋颜色
     */
    private void checkAndUpdateControls() {
        runOnUiThread(() -> {
            boolean hasMoves = boardView.getBlackSteps() > 0 || boardView.getWhiteSteps() > 0;
            difficultySpinner.setEnabled(!hasMoves);
            playerColorGroup.setEnabled(!hasMoves);
            playerBlackRadio.setEnabled(!hasMoves);
            playerWhiteRadio.setEnabled(!hasMoves);
        });
    }

    // ================= Activity生命周期管理 =================

    /**
     * Activity暂停时自动暂停游戏
     */
    @Override
    protected void onPause() {
        super.onPause();
        // 如果不是配置变更（如旋转屏幕）且不是正在结束，则自动暂停游戏
        if (!isChangingConfigurations() && !isFinishing()) {
            if (!isPaused && !boardView.isGameEnded()) {
                wasAutoPaused = true;
                pauseGame();
                runOnUiThread(() -> {
                    turnInfo.setText("游戏已暂停（应用在后台）");
                    Toast.makeText(this, "游戏已自动暂停", Toast.LENGTH_SHORT).show();
                });
            }
        }
    }

    /**
     * Activity恢复时更新UI状态
     */
    @Override
    protected void onResume() {
        super.onResume();
        if (wasAutoPaused) {
            runOnUiThread(() -> {
                turnInfo.setText("游戏已暂停（点击继续按钮恢复）");
            });
        }
    }

    // ================= 游戏状态更新 =================

    /**
     * 更新游戏状态显示
     */
    private void updateStatus(int currentPlayer, int blackStepsCount, int whiteStepsCount) {
        runOnUiThread(() -> {
            blackSteps.setText("黑棋: " + blackStepsCount);
            whiteSteps.setText("白棋: " + whiteStepsCount);
            updateTurnInfo(currentPlayer);
        });
    }

    /**
     * 更新回合信息显示
     */
    private void updateTurnInfo(int currentPlayer) {
        if (isPaused) return;  // 暂停时不更新回合信息

        String turnText;
        int aiPlayer = playerIsBlack ? 2 : 1;  // AI的棋子颜色

        if (currentPlayer == aiPlayer) {
            // AI回合
            turnText = playerIsBlack ? "AI回合(白棋)" : "AI回合(黑棋)";
            turnInfo.setBackgroundColor(Color.parseColor("#FFF3E0")); // 橙色背景
        } else {
            // 玩家回合
            turnText = playerIsBlack ? "你的回合(黑棋)" : "你的回合(白棋)";
            turnInfo.setBackgroundColor(Color.parseColor("#E8F5E9")); // 绿色背景
        }

        turnInfo.setText(turnText);
    }

    // ================= 计时器监听器实现 =================

    /**
     * 计时器每秒回调，更新时间显示
     */
    @Override
    public void onTimerTick(String formattedTime) {
        runOnUiThread(() -> timerTextView.setText(formattedTime));
    }

    /**
     * 计时器状态变化回调
     */
    @Override
    public void onTimerStateChanged(boolean isRunning, boolean isPaused) {
        // 可以在这里处理计时器状态变化，如果需要的话
    }

    // ================= 棋盘事件监听器实现 =================

    /**
     * 玩家切换时调用
     */
    @Override
    public void onPlayerChanged(int currentPlayer) {
        updateTurnInfo(currentPlayer);
    }

    /**
     * 游戏结束时调用
     * 处理胜负判定、保存游戏记录、显示结果对话框
     */
    @Override
    public void onGameEnded(int winner) {
        runOnUiThread(() -> {
            String winnerText;
            boolean playerWon;
            boolean boardFull = boardView.isBoardFull();

            // 自动暂停游戏
            handlePauseClick();

            // 判断胜负结果
            if (boardFull) {
                // 棋盘已满的特殊规则：判执白棋者获胜
                winnerText = playerIsBlack ? "AI赢了！" : "你赢了！";
                playerWon = !playerIsBlack;
                winnerText = "棋盘已满，" + winnerText;
            } else {
                // 正常胜负判定
                // winner为1表示黑棋胜，为2表示白棋胜
                playerWon = (winner == 1 && playerIsBlack) || (winner == 2 && !playerIsBlack);
                winnerText = playerWon ? "你赢了！" : "AI赢了！";
            }

            // 播放胜负音效
            if (GameApp.isSoundEnabled()) {
                GameApp.playSound(playerWon ? R.raw.victory : R.raw.defeat);
            }

            // 保存游戏记录到数据库（不需要步数和时间参数）
            saveGameRecord(playerWon, currentDifficulty == GameAi.Difficulty.EASY);

            // 构建简化结果信息
            String stats = String.format(Locale.getDefault(),
                    "%s\n\n游戏时间: %s\n总步数: %d\n黑棋步数: %d\n白棋步数: %d",
                    winnerText,
                    gameTimer.getFormattedTime(),
                    boardView.getBlackSteps() + boardView.getWhiteSteps(),
                    boardView.getBlackSteps(),
                    boardView.getWhiteSteps());

            // 显示游戏结束对话框
            new AlertDialog.Builder(this)
                    .setTitle("游戏结束")
                    .setMessage(stats)
                    .setPositiveButton("退出", (d, w) -> {
                        GameApp.playSound(R.raw.button1);
                        finish();
                    })
                    .setNegativeButton("继续", (d, w) -> {
                        GameApp.playSound(R.raw.button1);
                        // 禁用暂停按钮，进入查看模式
                        pauseButton.setEnabled(false);
                        Toast.makeText(this, "已进入查看模式", Toast.LENGTH_SHORT).show();
                    })
                    .setCancelable(false)  // 必须选择按钮，不能取消
                    .show();
        });
    }

    /**
     * 落子成功时调用
     */
    @Override
    public void onMoveMade(int row, int col, int player) {
        // 更新步数显示
        updateStatus(boardView.getCurrentPlayer(),
                boardView.getBlackSteps(),
                boardView.getWhiteSteps());

        // 游戏开始后禁用难度和颜色选择
        runOnUiThread(() -> {
            boolean hasMoves = boardView.getBlackSteps() > 0 || boardView.getWhiteSteps() > 0;
            if (hasMoves) {
                difficultySpinner.setEnabled(false);
                playerColorGroup.setEnabled(false);
                playerBlackRadio.setEnabled(false);
                playerWhiteRadio.setEnabled(false);
            }
        });
    }


    // ================= 其他监听器方法 =================

    /**
     * 玩家执棋颜色选择变化
     */
    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        // 如果控件被禁用，不处理
        if (!playerColorGroup.isEnabled()) return;

        boolean previousValue = playerIsBlack;

        // 更新玩家执棋颜色
        if (checkedId == R.id.playerBlack) {
            playerIsBlack = true;
        } else if (checkedId == R.id.playerWhite) {
            playerIsBlack = false;
        } else {
            return;
        }

        // 如果执棋颜色有变化，重新开始游戏
        if (previousValue != playerIsBlack) {
            startNewGame();
        }
    }

    /**
     * 难度选择变化
     */
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        // 如果游戏已经开始，不允许更改难度，并恢复到之前的选择
        if (boardView.getBlackSteps() > 0 || boardView.getWhiteSteps() > 0) {
            difficultySpinner.setSelection(currentDifficulty == GameAi.Difficulty.EASY ? 0 : 1);
            Toast.makeText(this, "游戏已开始，不可更改", Toast.LENGTH_SHORT).show();
            return;
        }

        // 更新难度设置
        switch (position) {
            case 0:
                currentDifficulty = GameAi.Difficulty.EASY;
                break;
            case 1:
                currentDifficulty = GameAi.Difficulty.HARD;
                break;
        }

        // 通知棋盘更新AI难度
        boardView.setAIDifficulty(currentDifficulty);

        // 显示提示信息
        String[] difficulties = getResources().getStringArray(R.array.difficulty_levels);
        Toast.makeText(this, "已切换为" + difficulties[position] + "难度", Toast.LENGTH_SHORT).show();
    }

    /**
     * 难度选择未选中（通常不会发生）
     */
    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // 什么都不做
    }

    // ================= Activity销毁清理 =================

    /**
     * Activity销毁时的清理工作
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 移除计时器监听器
        gameTimer.removeTimerListener();
        // 移除棋盘事件监听器
        if (boardView != null) {
            boardView.setGameEventListener(null);
        }
    }

    // ================= 游戏记录保存 =================

    /**
     * 保存游戏记录到数据库
     * playerWon 玩家是否获胜
     * isEasyMode 是否为简单模式
     */
    private void saveGameRecord(boolean playerWon, boolean isEasyMode) {
        // 在后台线程中执行数据库操作
        GameApp.getGlobalExecutor().execute(() -> {
            try {
                // 获取当前登录用户
                String currentAccount = GameApp.getCurrentLoginAccount();
                UserDao userDao = GameApp.getUserDao();
                User user = userDao.getUserByAccount(currentAccount);

                // 创建游戏记录对象
                GameRecord record = new GameRecord(
                        user.getId(),      // 用户ID
                        isEasyMode,        // 是否为简单模式
                        playerWon          // 是否获胜
                );

                // 插入数据库
                GameRecordDao recordDao = GameApp.getGameRecordDao();
                recordDao.insertRecord(record);

            } catch (Exception e) {
                Log.e("GameRecord", "保存游戏记录失败", e);
            }
        });
    }
}