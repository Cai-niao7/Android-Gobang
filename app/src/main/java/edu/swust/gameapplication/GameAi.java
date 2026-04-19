package edu.swust.gameapplication;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * AI玩家类 - 实现五子棋的AI逻辑
 * 支持不同难度级别：简单、困难
 */
public class GameAi {

    /**
     * AI难度
     * EASY: 简单难度 - 随机落子与简单策略结合
     * HARD: 困难难度 - 使用评分算法，考虑攻防平衡
     */
    public enum Difficulty {
        EASY,    // 简单：混合策略，60%使用困难算法，40%在玩家周围3×3范围随机落子
        HARD     // 困难：使用完整的评分算法，考虑攻防策略
    }

    // ================= 成员变量 =================
    private Difficulty difficulty;    // AI难度级别
    private Random random;            // 随机数生成器
    private int aiPlayer;             // AI的棋子颜色 1:黑棋, 2:白棋
    private int opponentPlayer;       // 对手的棋子颜色

    // ================= 构造函数 =================

    /**
     * 构造函数
     * difficulty AI难度
     * aiPlayer AI的棋子颜色（1:黑棋, 2:白棋）
     */
    public GameAi(Difficulty difficulty, int aiPlayer) {
        this.difficulty = difficulty;
        this.random = new Random();
        this.aiPlayer = aiPlayer;
        // 根据AI颜色确定对手颜色
        this.opponentPlayer = (aiPlayer == 1) ? 2 : 1;
    }

    // ================= 核心方法 =================

    /**
     * 获取AI的落子位置
     * board 当前棋盘状态，15x15二维数组
     * lastMoveRow 玩家最后落子的行（简单难度使用）
     * lastMoveCol 玩家最后落子的列（简单难度使用）
     * [row, col] 落子位置数组
     */
    public int[] getMove(int[][] board, int lastMoveRow, int lastMoveCol) {
        // 根据难度选择不同的落子策略
        switch (difficulty) {
            case EASY:
                return getEasyMove(board, lastMoveRow, lastMoveCol);
            case HARD:
                return getHardMove(board);
            default:
                return getRandomMove(board); // 默认使用随机落子
        }
    }

    // ================= 简单难度策略 =================

    /**
     * 简单难度落子策略
     * 60%概率使用困难算法，40%概率在玩家周围3×3范围随机落子
     */
    private int[] getEasyMove(int[][] board, int lastRow, int lastCol) {
        // 60%的概率使用困难算法（提高简单难度的智能性）
        if (random.nextDouble() < 0.6) {
            return getHardMove(board);
        }

        // 检查是否是AI先手（判断棋盘是否为空）
        boolean isAIFirst = true;
        for (int i = 0; i < 15 && isAIFirst; i++) {
            for (int j = 0; j < 15 && isAIFirst; j++) {
                if (board[i][j] != 0) {
                    isAIFirst = false; // 棋盘有棋子，不是AI先手
                }
            }
        }

        // AI先手情况：直接下在中心位置（天元）
        if (isAIFirst) {
            int center = 7;
            if (board[center][center] == 0) {
                return new int[]{center, center}; // 下在天元位置
            }
            return getRandomMove(board);
        }

        // AI后手情况：如果玩家最后落子位置无效，直接随机落子
        if (lastRow == -1 || lastCol == -1) {
            return getRandomMove(board);
        }

        // 统计棋盘上的棋子数量
        int stoneCount = 0;
        for (int i = 0; i < 15; i++) {
            for (int j = 0; j < 15; j++) {
                if (board[i][j] != 0) {
                    stoneCount++;
                }
            }
        }

        // 收集玩家最后落子周围3×3范围内的空位
        List<int[]> candidates = new ArrayList<>();
        int startRow = Math.max(0, lastRow - 1);
        int endRow = Math.min(14, lastRow + 1);
        int startCol = Math.max(0, lastCol - 1);
        int endCol = Math.min(14, lastCol + 1);

        // 遍历3×3范围，收集空位
        for (int i = startRow; i <= endRow; i++) {
            for (int j = startCol; j <= endCol; j++) {
                if (board[i][j] == 0) {
                    candidates.add(new int[]{i, j});
                }
            }
        }

        // 优先在3×3范围内落子
        if (!candidates.isEmpty()) {
            return candidates.get(random.nextInt(candidates.size()));
        }

        // 3×3范围没有空位，全盘随机
        return getRandomMove(board);
    }

    // ================= 困难难度策略 =================

    /**
     * 困难难度落子策略
     * 使用评分算法，综合考虑攻防
     */
    private int[] getHardMove(int[][] board) {
        int bestScore = Integer.MIN_VALUE; // 最佳得分
        List<int[]> bestMoves = new ArrayList<>(); // 最佳位置列表

        // 检查是否有直接获胜的机会（进攻优先）
        int[] winMove = findWinningMove(board, aiPlayer);
        if (winMove != null) {
            return winMove; // 有获胜机会就直接下
        }

        // 检查是否需要防守（防止对手获胜）
        int[] blockMove = findWinningMove(board, opponentPlayer);
        if (blockMove != null) {
            return blockMove; // 需要防守就阻挡
        }

        // 使用评分算法选择最佳位置
        for (int row = 0; row < 15; row++) {
            for (int col = 0; col < 15; col++) {
                if (board[row][col] == 0) {
                    // 评估当前位置的得分
                    int score = evaluatePosition(board, row, col);

                    // 更新最佳得分和最佳位置
                    if (score > bestScore) {
                        bestScore = score;
                        bestMoves.clear();
                        bestMoves.add(new int[]{row, col});
                    } else if (score == bestScore) {
                        bestMoves.add(new int[]{row, col}); // 得分相同的位置也加入
                    }
                }
            }
        }

        // 如果找到最佳位置，随机选择一个（增加AI的变化性）
        if (!bestMoves.isEmpty()) {
            return bestMoves.get(random.nextInt(bestMoves.size()));
        }

        // 如果都没找到，随机落子
        return getRandomMove(board);
    }

    /**
     * 查找能够获胜的落子位置
     * board 棋盘状态
     * player 要检查的玩家
     * 获胜位置 [row, col]，如果没有则返回null
     */
    private int[] findWinningMove(int[][] board, int player) {
        // 遍历棋盘所有空位
        for (int row = 0; row < 15; row++) {
            for (int col = 0; col < 15; col++) {
                if (board[row][col] == 0) {
                    // 模拟在这个位置落子
                    board[row][col] = player;
                    boolean canWin = checkWinForPosition(board, row, col, player);
                    board[row][col] = 0; // 恢复棋盘

                    if (canWin) {
                        return new int[]{row, col}; // 找到获胜位置
                    }
                }
            }
        }
        return null; // 没有找到获胜位置
    }

    /**
     * 检查在指定位置落子后是否能获胜
     * board 棋盘状态
     * row 行号
     * col 列号
     * player 玩家
     * @return 是否能获胜
     */
    private boolean checkWinForPosition(int[][] board, int row, int col, int player) {
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
            int r = row + dir[0];
            int c = col + dir[1];
            while (r >= 0 && r < 15 && c >= 0 && c < 15 && board[r][c] == player) {
                count++;
                r += dir[0];
                c += dir[1];
            }

            // 反向检查
            r = row - dir[0];
            c = col - dir[1];
            while (r >= 0 && r < 15 && c >= 0 && c < 15 && board[r][c] == player) {
                count++;
                r -= dir[0];
                c -= dir[1];
            }

            // 如果连续5个或以上，获胜
            if (count >= 5) {
                return true;
            }
        }
        return false;
    }

    /**
     * 评估某个位置的综合得分
     * 考虑进攻和防守两个方面
     */
    private int evaluatePosition(int[][] board, int row, int col) {
        int score = 0;

        // 四个评估方向
        int[][] directions = {{1, 0}, {0, 1}, {1, 1}, {1, -1}};

        // 分别评估AI和对手在各个方向上的棋型
        for (int[] dir : directions) {
            // 评估AI在这个方向的棋型（进攻）
            int aiScore = evaluateLine(board, row, col, dir[0], dir[1], aiPlayer);
            // 评估对手在这个方向的棋型（防守）
            int opponentScore = evaluateLine(board, row, col, dir[0], dir[1], opponentPlayer);

            score += aiScore * 10;     // 进攻权重较高
            score += opponentScore * 8; // 防守权重稍低，体现攻守平衡
        }

        // 中心位置加分：越靠近中心得分越高
        int centerDist = Math.abs(row - 7) + Math.abs(col - 7); // 到中心的曼哈顿距离
        score += (14 - centerDist) * 2; // 最大加分28分（角上为0分，中心为14分×2）

        return score;
    }

    /**
     * 评估一条线上的棋型得分
     * 棋型评分标准：
     * - 活四（两端都空）：1000分
     * - 活三（两端都空）：100分
     * - 冲三（一端空）：50分
     * - 活二（两端都空）：10分
     * - 冲二（一端空）：5分
     * - 单子（两端都空）：1分
     */
    private int evaluateLine(int[][] board, int row, int col, int dr, int dc, int player) {
        int score = 0;

        // 模拟在这个位置落子
        board[row][col] = player;

        // 统计连续棋子数量
        int consecutive = 1;

        // 正向检查
        int r = row + dr;
        int c = col + dc;
        while (r >= 0 && r < 15 && c >= 0 && c < 15 && board[r][c] == player) {
            consecutive++;
            r += dr;
            c += dc;
        }

        // 检查正向是否开放（有空位）
        boolean forwardOpen = (r >= 0 && r < 15 && c >= 0 && c < 15 && board[r][c] == 0);

        // 反向检查
        r = row - dr;
        c = col - dc;
        while (r >= 0 && r < 15 && c >= 0 && c < 15 && board[r][c] == player) {
            consecutive++;
            r -= dr;
            c -= dc;
        }

        // 检查反向是否开放（有空位）
        boolean backwardOpen = (r >= 0 && r < 15 && c >= 0 && c < 15 && board[r][c] == 0);

        // 恢复棋盘状态
        board[row][col] = 0;

        // 根据连续数量和开放情况评分
        if (consecutive >= 4) return 1000; // 活四（实际上应该是连五，这里处理活四）

        if (consecutive == 3) {
            if (forwardOpen && backwardOpen) return 100;  // 活三：两端都空
            if (forwardOpen || backwardOpen) return 50;   // 冲三：一端空
        }

        if (consecutive == 2) {
            if (forwardOpen && backwardOpen) return 10;   // 活二：两端都空
            if (forwardOpen || backwardOpen) return 5;    // 冲二：一端空
        }

        if (consecutive == 1) {
            if (forwardOpen && backwardOpen) return 1;    // 单子：两端都空
        }

        return 0;
    }

    /**
     * 随机选择一个空位置
     * 兜底策略，当所有策略都失效时使用
     */
    private int[] getRandomMove(int[][] board) {
        List<int[]> emptyCells = new ArrayList<>();

        // 收集所有空位置
        for (int row = 0; row < 15; row++) {
            for (int col = 0; col < 15; col++) {
                if (board[row][col] == 0) {
                    emptyCells.add(new int[]{row, col});
                }
            }
        }

        // 如果棋盘已满，返回null
        if (emptyCells.isEmpty()) {
            return null;
        }

        // 随机选择一个空位置
        return emptyCells.get(random.nextInt(emptyCells.size()));
    }

    // ================= 设置方法 =================

    /**
     * 设置AI难度
     * @param difficulty 新的难度级别
     */
    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
    }
}