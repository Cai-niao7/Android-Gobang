package edu.swust.gameapplication;

import java.util.Stack;

/**
 * 游戏逻辑核心类
 * 管理五子棋的游戏规则、状态和AI对战逻辑
 */
public class GameLogic {

    // ================= 常量定义 =================
    // 棋盘尺寸（标准五子棋棋盘）
    private static final int BOARD_SIZE = 15;
    private static final int MAX_STONES = BOARD_SIZE * BOARD_SIZE;

    // ================= 游戏状态变量 =================
    // 棋盘状态：0=空，1=黑棋，2=白棋
    private int[][] board = new int[BOARD_SIZE][BOARD_SIZE];
    // 当前玩家：1=黑棋，2=白棋
    private int currentPlayer = 1;
    // 黑棋步数统计
    private int blackSteps = 0;
    // 白棋步数统计
    private int whiteSteps = 0;
    // 游戏是否结束标志
    private boolean gameEnded = false;
    // 游戏是否暂停标志
    private boolean isPaused = false;

    // ================= AI相关变量 =================
    // 是否为人机对战模式
    private boolean isAIGame = false;
    // AI引擎实例
    private GameAi gameAI = null;
    // AI执棋颜色,默认为白棋
    private int aiPlayer = 2;
    // 玩家最后落子位置
    private int lastHumanMoveRow = -1;
    private int lastHumanMoveCol = -1;

    // ================= 落子记录 =================
    // 使用栈记录每一步落子，支持悔棋功能
    private Stack<Move> moveStack = new Stack<>();

    // ================= 事件监听器接口 =================
    /**
     * 游戏逻辑事件监听器接口
     * 用于将游戏状态变化通知给UI层
     */
    public interface GameLogicListener {
        void onPlayerChanged(int currentPlayer);    // 玩家切换时调用
        void onGameEnded(int winner);               // 游戏结束时调用
        void onMoveMade(int row, int col, int player); // 落子成功时调用
        void onBoardChanged();                      // 棋盘状态变化时调用
    }

    // 游戏逻辑监听器实例
    private GameLogicListener gameLogicListener;

    // ================= 内部类 =================

    /**
     * 移动记录类
     * 记录每一步落子的位置和玩家信息
     */
    public class Move {
        public int row;     // 行坐标
        public int col;     // 列坐标
        public int player;  // 玩家（1=黑，2=白）

        /**
         * 构造函数
         * row 行坐标
         * col 列坐标
         * player 玩家
         */
        Move(int row, int col, int player) {
            this.row = row;
            this.col = col;
            this.player = player;
        }
    }

    // ================= 构造函数 =================

    /**
     * 构造函数
     * 初始化游戏状态
     */
    public GameLogic() {
        resetGame(); // 初始化游戏
    }

    // ================= 核心游戏方法 =================

    /**
     * 执行落子操作
     * row 行坐标（0-14）
     *  col 列坐标（0-14）
     * @return 落子是否成功
     */
    public boolean makeMove(int row, int col) {
        // 检查游戏状态：游戏结束或暂停时不能落子
        if (gameEnded || isPaused) {
            return false;
        }

        // 检查落子位置：必须在棋盘范围内且为空位
        if (!isValidPosition(row, col) || !isEmpty(row, col)) {
            return false;
        }

        // 更新棋盘状态
        board[row][col] = currentPlayer;

        // 记录落子历史（用于悔棋）
        moveStack.push(new Move(row, col, currentPlayer));

        // 更新步数统计
        if (currentPlayer == 1) {
            blackSteps++;
        } else {
            whiteSteps++;
        }

        // 记录玩家最后落子位置（AI对战模式下使用）
        if (isAIGame && currentPlayer != aiPlayer) {
            lastHumanMoveRow = row;
            lastHumanMoveCol = col;
        }

        // 检查游戏是否结束
        if (checkWin(row, col)) {
            // 当前玩家获胜
            gameEnded = true;
            if (gameLogicListener != null) {
                gameLogicListener.onGameEnded(currentPlayer);
            }
        } else {
            // 检查棋盘是否已满（平局）
            if (isBoardFull()) {
                gameEnded = true;
                if (gameLogicListener != null) {
                    // 棋盘已满，判白棋获胜（特殊规则）
                    gameLogicListener.onGameEnded(2);
                }
            } else {
                // 游戏继续，切换玩家
                currentPlayer = (currentPlayer == 1) ? 2 : 1;
                if (gameLogicListener != null) {
                    gameLogicListener.onPlayerChanged(currentPlayer);
                }
            }
        }

        // 通知UI更新
        if (gameLogicListener != null) {
            gameLogicListener.onMoveMade(row, col, board[row][col]);
            gameLogicListener.onBoardChanged();
        }

        return true;
    }

    /**
     * 检查棋盘是否已满
     * @return true:棋盘已满，false:还有空位
     */
    public boolean isBoardFull() {
        return (blackSteps + whiteSteps) >= MAX_STONES;
    }

    // ================= AI对战相关方法 =================

    /**
     * AI执行落子（人机对战模式下调用）
     * @return AI落子的位置数组[row, col]，如果无法落子则返回null
     */
    public int[] makeAIMove() {
        // 检查AI状态：AI未初始化、非AI模式、游戏结束、暂停、非AI回合等情况
        if (gameAI == null || !isAIGame || gameEnded || isPaused || currentPlayer != aiPlayer) {
            return null;
        }

        // 检查棋盘是否已满，如果已满则AI不落子
        if (isBoardFull()) {
            return null;
        }

        // 获取当前棋盘状态
        int[][] boardState = getBoardState();

        // 获取AI决策的落子位置
        int[] move = gameAI.getMove(boardState, lastHumanMoveRow, lastHumanMoveCol);

        // 验证并执行AI落子
        if (move != null && isValidPosition(move[0], move[1]) && isEmpty(move[0], move[1])) {
            // 执行落子
            makeMove(move[0], move[1]);

            // 重置玩家最后落子记录（因为现在是AI回合）
            lastHumanMoveRow = -1;
            lastHumanMoveCol = -1;
        }

        return move;
    }

    // ================= 悔棋功能 =================

    /**
     * 悔棋操作
     * @return 悔棋是否成功
     */
    public boolean undo() {
        // 检查是否有棋可以悔
        if (moveStack.isEmpty()) {
            return false;
        }

        // 获取最后一步落子记录
        Move lastMove = moveStack.pop();

        // 清除棋盘上的该棋子
        board[lastMove.row][lastMove.col] = 0;

        // 更新步数统计
        if (lastMove.player == 1) {
            blackSteps--;
        } else {
            whiteSteps--;
        }

        // 恢复当前玩家为被撤销棋子的玩家
        currentPlayer = lastMove.player;

        // 重置游戏结束状态（因为撤销了最后一步）
        gameEnded = false;

        // 通知UI更新
        if (gameLogicListener != null) {
            gameLogicListener.onPlayerChanged(currentPlayer);
            gameLogicListener.onBoardChanged();
        }

        return true;
    }

    // ================= 游戏重置 =================

    /**
     * 重置游戏到初始状态
     */
    public void resetGame() {
        // 清空棋盘
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                board[i][j] = 0;
            }
        }

        // 清空历史记录
        moveStack.clear();

        // 重置游戏状态
        currentPlayer = 1;     // 黑棋先手
        blackSteps = 0;
        whiteSteps = 0;
        gameEnded = false;
        isPaused = false;
        lastHumanMoveRow = -1;
        lastHumanMoveCol = -1;

        // 通知UI更新
        if (gameLogicListener != null) {
            gameLogicListener.onPlayerChanged(currentPlayer);
            gameLogicListener.onBoardChanged();
        }
    }

    // ================= 游戏规则检查 =================

    /**
     * 检查是否获胜（五子连珠）
     * @param row 最后落子的行
     * @param col 最后落子的列
     * @return 是否获胜
     */
    private boolean checkWin(int row, int col) {
        int player = board[row][col];

        // 四个检查方向：水平、垂直、主对角线、副对角线
        int[][] directions = {
                {1, 0},   // 垂直方向
                {0, 1},   // 水平方向
                {1, 1},   // 主对角线方向（右下）
                {1, -1}   // 副对角线方向（左下）
        };

        // 检查每个方向
        for (int[] dir : directions) {
            int count = 1; // 当前位置算1个

            // 正向检查
            int r = row;
            int c = col;
            while (true) {
                r += dir[0];
                c += dir[1];
                if (!isValidPosition(r, c) || board[r][c] != player) break;
                count++;
            }

            // 反向检查
            r = row;
            c = col;
            while (true) {
                r -= dir[0];
                c -= dir[1];
                if (!isValidPosition(r, c) || board[r][c] != player) break;
                count++;
            }

            // 如果连续5个或以上，获胜
            if (count >= 5) {
                return true;
            }
        }
        return false;
    }

    /**
     * 验证位置是否在棋盘范围内
     */
    private boolean isValidPosition(int row, int col) {
        return row >= 0 && row < BOARD_SIZE && col >= 0 && col < BOARD_SIZE;
    }

    // ================= 公开接口方法 =================

    /**
     * 获取当前棋盘状态的深拷贝
     * @return 15×15的棋盘状态数组
     */
    public int[][] getBoardState() {
        int[][] copy = new int[BOARD_SIZE][BOARD_SIZE];
        for (int i = 0; i < BOARD_SIZE; i++) {
            System.arraycopy(board[i], 0, copy[i], 0, BOARD_SIZE);
        }
        return copy;
    }

    /**
     * 获取当前玩家
     * @return 1:黑棋，2:白棋
     */
    public int getCurrentPlayer() {
        return currentPlayer;
    }

    /**
     * 获取黑棋步数
     */
    public int getBlackSteps() {
        return blackSteps;
    }

    /**
     * 获取白棋步数
     */
    public int getWhiteSteps() {
        return whiteSteps;
    }

    /**
     * 检查游戏是否结束
     */
    public boolean isGameEnded() {
        return gameEnded;
    }

    /**
     * 检查游戏是否暂停
     */
    public boolean isPaused() {
        return isPaused;
    }

    /**
     * 检查指定位置是否为空
     * @param row 行坐标
     * @param col 列坐标
     * @return 是否为空
     */
    public boolean isEmpty(int row, int col) {
        return isValidPosition(row, col) && board[row][col] == 0;
    }

    /**
     * 获取指定位置的棋子状态
     * @param row 行坐标
     * @param col 列坐标
     * @return 0:空，1:黑棋，2:白棋，-1:无效位置
     */
    public int getBoardValue(int row, int col) {
        if (isValidPosition(row, col)) {
            return board[row][col];
        }
        return -1;
    }

    /**
     * 检查是否为人机对战模式
     */
    public boolean isAIGame() {
        return isAIGame;
    }

    /**
     * 获取AI执棋颜色
     */
    public int getAIPlayer() {
        return aiPlayer;
    }

    // ================= AI对战初始化 =================

    /**
     * 初始化人机对战模式
     * aiPlayerColor AI执棋颜色（1:黑棋，2:白棋）
     * difficulty AI难度等级
     * playerIsBlack 玩家是否执黑棋
     */
    public void initAIGame(int aiPlayerColor, GameAi.Difficulty difficulty, boolean playerIsBlack) {
        // 设置AI对战模式
        this.isAIGame = true;
        this.aiPlayer = aiPlayerColor;

        // 初始化AI引擎
        this.gameAI = new GameAi(difficulty, aiPlayerColor);

        // 设置初始玩家
        if (playerIsBlack) {
            // 玩家执黑棋
            currentPlayer = 1;
        } else {
            // AI执黑棋先手
            currentPlayer = aiPlayerColor;
        }

        // 重置玩家最后落子记录
        lastHumanMoveRow = -1;
        lastHumanMoveCol = -1;

        // 通知玩家切换
        if (gameLogicListener != null) {
            gameLogicListener.onPlayerChanged(currentPlayer);
        }
    }

    /**
     * 设置AI难度
     * @param difficulty 难度等级
     */
    public void setAIDifficulty(GameAi.Difficulty difficulty) {
        if (gameAI != null) {
            gameAI.setDifficulty(difficulty);
        }
    }

    /**
     * 设置游戏暂停状态
     * @param paused true:暂停，false:继续
     */
    public void setPaused(boolean paused) {
        this.isPaused = paused;
    }

    /**
     * 设置游戏逻辑监听器
     * @param listener 监听器实例
     */
    public void setGameLogicListener(GameLogicListener listener) {
        this.gameLogicListener = listener;
    }
}