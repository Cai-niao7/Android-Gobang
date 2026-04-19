package edu.swust.gameapplication;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "User")
public class User {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "user_account")
    private String account;

    @ColumnInfo(name = "user_password")
    private String password;

    @ColumnInfo(name = "avatar_id", defaultValue = "1")
    private int avatarId = 0; // 默认头像ID为0

    public User() {}

    @Ignore
    public User(String account, String password) {
        this.account = account;
        this.password = password;
        this.avatarId = 0;
    }

    @Ignore
    public User(String account, String password, int avatarId) {
        this.account = account;
        this.password = password;
        this.avatarId = avatarId;
    }

    // Getter 和 Setter
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getAccount() { return account; }
    public void setAccount(String account) { this.account = account; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public int getAvatarId() { return avatarId; }
    public void setAvatarId(int avatarId) { this.avatarId = avatarId; }
}