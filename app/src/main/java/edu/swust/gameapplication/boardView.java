package edu.swust.gameapplication;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * 棋盘视图类 - 自定义View，负责棋盘绘制和触摸交互
 */
public class boardView extends View {

    // ================= 常量定义 =================
    // 边距（单位：dp）
    private static final int MARGIN_DP = 5;
    // 棋盘大小（15x15的五子棋标准棋盘）
    private static final int BOARD_SIZE = 15;

    // ================= 成员变量 =================
    // 游戏逻辑控制器
    private GameLogic gameLogic;
    // 网格画笔（用于绘制棋盘线）
    private Paint gridPaint = new Paint();
    // 棋子画笔（用于绘制黑白棋子）
    private Paint stonePaint = new Paint();

    // 棋盘绘制相关参数
    private int cellSize;  // 每个格子的像素大小
    private int startX;    // 棋盘左上角起始X坐标
    private int startY;    // 棋盘左上角起始Y坐标

    // ================= 游戏事件监听器接口 =================
    /**
     * 游戏事件监听器接口
     * 用于将棋盘事件传递给Activity或Fragment
     */
    public interface GameEventListener {
        void onPlayerChanged(int currentPlayer);    // 玩家切换时调用
        void onGameEnded(int winner);               // 游戏结束时调用
        void onMoveMade(int row, int col, int player); // 落子时调用
    }

    // 游戏事件监听器实例
    private GameEventListener gameEventListener;

    // ================= 构造函数 =================

    /**
     * 构造函数
     * context 上下文
     * attrs 属性集
     */
    public boardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    /**
     * 初始化方法
     * 设置画笔属性，初始化游戏逻辑
     */
    private void init() {
        // 初始化网格画笔
        gridPaint.setColor(Color.BLACK);  // 设置网格颜色为黑色
        gridPaint.setStrokeWidth(2);      // 设置网格线宽为2像素

        // 初始化棋子画笔
        stonePaint.setAntiAlias(true);    // 开启抗锯齿，使棋子边缘更平滑

        // 初始化游戏逻辑控制器
        gameLogic = new GameLogic();

        // 设置游戏逻辑监听器，将游戏事件传递给棋盘视图
        gameLogic.setGameLogicListener(new GameLogic.GameLogicListener() {
            @Override
            public void onPlayerChanged(int currentPlayer) {
                // 玩家切换时，通知外部监听器
                if (gameEventListener != null) {
                    gameEventListener.onPlayerChanged(currentPlayer);
                }
            }

            @Override
            public void onGameEnded(int winner) {
                // 游戏结束时，通知外部监听器
                if (gameEventListener != null) {
                    gameEventListener.onGameEnded(winner);
                }
            }

            @Override
            public void onMoveMade(int row, int col, int player) {
                // 落子时，通知外部监听器并播放落子音效
                if (gameEventListener != null) {
                    GameApp.playSound(R.raw.move);  // 播放落子音效
                    gameEventListener.onMoveMade(row, col, player);
                }
            }

            @Override
            public void onBoardChanged() {
                // 棋盘状态变化时，重绘
                invalidate();
            }
        });
    }

    // ================= View生命周期方法 =================

    /**
     * View大小变化时调用
     * 计算棋盘绘制参数，使棋盘居中显示
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        // 将dp转换为px
        int marginPx = dpToPx(MARGIN_DP);//marginPx与边缘的距离
        // 计算棋盘最大可用大小（取宽高中的较小值）
        int maxBoardSize = Math.min(w, h) - 2 * marginPx;
        // 计算每个格子的大小
        cellSize = maxBoardSize / BOARD_SIZE;
        // 计算棋盘起始位置，使棋盘居中显示
        startX = (w - cellSize * (BOARD_SIZE - 1)) / 2;
        startY = (h - cellSize * (BOARD_SIZE - 1)) / 2;
    }

    /**
     * dp转px工具方法
     * @param dp 密度无关像素值
     * @return 像素值
     */
    private int dpToPx(int dp) {
        float density = getContext().getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    /**
     * 绘制方法
     * 绘制棋盘网格和棋子
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // 绘制棋盘网格
        drawGrid(canvas);
        // 绘制棋子
        drawStones(canvas);
    }

    /**
     * 绘制棋盘网格
     * 绘制横线和竖线组成15x15的网格
     */
    private void drawGrid(Canvas canvas) {
        // 绘制横线
        for (int i = 0; i < BOARD_SIZE; i++) {
            canvas.drawLine(startX, startY + i * cellSize,
                    startX + (BOARD_SIZE - 1) * cellSize, startY + i * cellSize, gridPaint);
        }
        // 绘制竖线
        for (int i = 0; i < BOARD_SIZE; i++) {
            canvas.drawLine(startX + i * cellSize, startY,
                    startX + i * cellSize, startY + (BOARD_SIZE - 1) * cellSize, gridPaint);
        }
    }

    /**
     * 绘制所有棋子
     * 遍历棋盘，根据状态绘制黑子或白子
     */
    private void drawStones(Canvas canvas) {
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                int stone = gameLogic.getBoardValue(row, col);
                if (stone == 1) {  // 黑棋
                    drawBlackStone(canvas, row, col);
                } else if (stone == 2) {  // 白棋
                    drawWhiteStone(canvas, row, col);
                }
            }
        }
    }

    /**
     * 绘制黑棋
     * canvas 画布
     * row 行号
     * col 列号
     */
    private void drawBlackStone(Canvas canvas, int row, int col) {
        stonePaint.setColor(Color.BLACK);
        // 绘制黑色实心圆（棋子半径比格子一半稍小，留出边距）
        canvas.drawCircle(
                startX + col * cellSize,   // 圆心X坐标
                startY + row * cellSize,   // 圆心Y坐标
                cellSize / 2 - 2,          // 半径
                stonePaint                 // 画笔
        );
    }

    /**
     * 绘制白棋
     */
    private void drawWhiteStone(Canvas canvas, int row, int col) {
        // 绘制白色实心圆
        stonePaint.setColor(Color.WHITE);
        canvas.drawCircle(
                startX + col * cellSize,
                startY + row * cellSize,
                cellSize / 2 - 2,
                stonePaint
        );

        // 为白棋添加黑色边框
        stonePaint.setColor(Color.BLACK);
        stonePaint.setStyle(Paint.Style.STROKE);  // 设置画笔为描边模式
        stonePaint.setStrokeWidth(2);              // 设置边框宽度
        canvas.drawCircle(
                startX + col * cellSize,
                startY + row * cellSize,
                cellSize / 2 - 2,
                stonePaint
        );
        // 恢复画笔为填充模式（避免影响后续绘制）
        stonePaint.setStyle(Paint.Style.FILL);
    }

    // ================= 触摸事件处理 =================

    /**
     * 处理触摸事件
     * 将屏幕坐标转换为棋盘坐标，并处理落子
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // 只处理抬起事件，避免连续触发
        if (event.getAction() == MotionEvent.ACTION_UP) {
            // 检查游戏状态，如果游戏已结束或暂停，不处理点击
            if (gameLogic.isGameEnded() || gameLogic.isPaused()) {
                return true;
            }

            // ！！！！！如果是AI游戏且当前是AI回合，不处理玩家点击
            if (gameLogic.isAIGame() && gameLogic.getCurrentPlayer() == gameLogic.getAIPlayer()) {
                return true;
            }

            // 将屏幕坐标转换为棋盘行列坐
            //这两行代码的作用是把屏幕点击坐标转换成棋盘行列索引。
            //(event.getX() - startX) 得到相对棋盘的像素偏移。
            //+ cellSize / 2 是为了居中判断，避免边缘误判。
            // cellSize 把像素转换成格子单位。
            //最终用 (int) 取整得到行列索引。
            int x = (int) ((event.getX() - startX + cellSize / 2) / cellSize);
            int y = (int) ((event.getY() - startY + cellSize / 2) / cellSize);

            // 检查坐标是否在棋盘范围内
            if (x >= 0 && x < BOARD_SIZE && y >= 0 && y < BOARD_SIZE) {
                // 检查棋盘是否已满（防止无效点击）
                if (isBoardFull()) {
                    return true;
                }

                // 执行落子（行列坐标顺序：y是行，x是列）
                gameLogic.makeMove(y, x);

                // 如果是AI游戏且轮到AI，延迟触发AI落子
                if (gameLogic.isAIGame() &&
                        gameLogic.getCurrentPlayer() == gameLogic.getAIPlayer() &&
                        !gameLogic.isGameEnded()) {

                    // 延迟800毫秒后执行AI落子（模拟思考时间）
                    postDelayed(() -> {
                        int[] aiMove = gameLogic.makeAIMove();
                        if (aiMove != null) {
                            // AI落子后刷新棋盘
                            invalidate();
                        }
                    }, 800);
                }
            }
        }
        return true;  // 消费所有触摸事件
    }

    // ================= 公开接口方法 =================

    /**
     * 检查棋盘是否已满
     * @return true:棋盘已满，false:还有空位
     */
    public boolean isBoardFull() {
        return gameLogic.isBoardFull();
    }

    /**
     * 悔棋
     */
    public void undo() {
        gameLogic.undo();
    }

    /**
     * 重置游戏
     */
    public void resetGame() {
        gameLogic.resetGame();
    }

    /**
     * 获取当前玩家
     * @return 1:黑棋，2:白棋
     */
    public int getCurrentPlayer() {
        return gameLogic.getCurrentPlayer();
    }

    /**
     * 获取黑棋步数
     */
    public int getBlackSteps() {
        return gameLogic.getBlackSteps();
    }

    /**
     * 获取白棋步数
     */
    public int getWhiteSteps() {
        return gameLogic.getWhiteSteps();
    }

    /**
     * 检查游戏是否结束
     */
    public boolean isGameEnded() {
        return gameLogic.isGameEnded();
    }

    /**
     * 设置游戏事件监听器
     */
    public void setGameEventListener(GameEventListener listener) {
        this.gameEventListener = listener;
    }

    /**
     * 初始化AI游戏
     * aiPlayerColor AI执棋颜色（1:黑棋，2:白棋）
     * difficulty AI难度等级
     * playerIsBlack 玩家是否执黑棋
     */
    public void initAIGame(int aiPlayerColor, GameAi.Difficulty difficulty, boolean playerIsBlack) {
        gameLogic.initAIGame(aiPlayerColor, difficulty, playerIsBlack);

        // 如果AI先手（AI执黑棋且玩家执白棋），让AI下第一步
        if (aiPlayerColor == 1 && !playerIsBlack) {
            // 延迟600毫秒确保UI就绪后再让AI落子
            postDelayed(() -> {
                // 确保当前是AI的回合且游戏未结束且棋盘未满
                if (gameLogic.getCurrentPlayer() == gameLogic.getAIPlayer() &&
                        gameLogic.isAIGame() &&
                        !gameLogic.isGameEnded() &&
                        !isBoardFull()) {

                    // 触发AI下棋
                    int[] aiMove = gameLogic.makeAIMove();
                    if (aiMove != null) {
                        // AI落子后刷新棋盘
                        invalidate();
                    }
                }
            }, 1000);  // 600毫秒延迟，让玩家有时间看到游戏开始
        }
    }

    /**
     * 设置AI难度
     */
    public void setAIDifficulty(GameAi.Difficulty difficulty) {
        gameLogic.setAIDifficulty(difficulty);
    }

    /**
     * 设置游戏暂停状态
     */
    public void setPaused(boolean paused) {
        gameLogic.setPaused(paused);
    }
}