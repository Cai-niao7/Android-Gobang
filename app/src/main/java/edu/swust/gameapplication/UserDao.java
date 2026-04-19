package edu.swust.gameapplication;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

@Dao
public interface UserDao {

    @Insert
    void insertUser(User user);


    @Update
    void updateUser(User user);


    @Query("SELECT * FROM User ORDER BY id DESC")
    List<User> getAllUsers();

    @Query("SELECT * FROM User WHERE user_account = :account")
    User getUserByAccount(String account);

    @Query("SELECT COUNT(*) FROM User WHERE user_account = :account")
    int checkAccountExists(String account);

    @Query("UPDATE User SET avatar_id = :avatarId WHERE user_account = :account")
    int updateAvatar(String account, int avatarId);

}