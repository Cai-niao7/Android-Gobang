// GameRecordDao.java
package edu.swust.gameapplication;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

@Dao
public interface GameRecordDao {

    /**
     * 插入游戏记录
     */
    @Insert
    void insertRecord(GameRecord record);

    /**
     * 统计简单模式游戏
     */
    @Query("SELECT COUNT(*) FROM GameRecord WHERE user_id = :userId AND is_easy_mode = 1")
    int countEasyGames(int userId);

    /**
     * 统计简单模式胜利
     */
    @Query("SELECT COUNT(*) FROM GameRecord WHERE user_id = :userId AND is_easy_mode = 1 AND is_win = 1")
    int countEasyWins(int userId);

    /**
     * 统计困难模式游戏
     */
    @Query("SELECT COUNT(*) FROM GameRecord WHERE user_id = :userId AND is_easy_mode = 0")
    int countHardGames(int userId);

    /**
     * 统计困难模式胜利
     */
    @Query("SELECT COUNT(*) FROM GameRecord WHERE user_id = :userId AND is_easy_mode = 0 AND is_win = 1")
    int countHardWins(int userId);

}