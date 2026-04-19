package edu.swust.gameapplication;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "GameRecord",
        foreignKeys = @ForeignKey(
                entity = User.class,
                parentColumns = "id",
                childColumns = "user_id",
                onDelete = ForeignKey.CASCADE))
public class GameRecord {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "record_id")
    private int recordId;

    @ColumnInfo(name = "user_id", index = true)
    private int userId;

    @ColumnInfo(name = "is_easy_mode")
    private boolean isEasyMode;

    @ColumnInfo(name = "is_win")
    private boolean isWin;

    @ColumnInfo(name = "record_time")
    private long recordTime;

    public GameRecord() {}

    @Ignore
    public GameRecord(int userId, boolean isEasyMode, boolean isWin) {
        this.userId = userId;
        this.isEasyMode = isEasyMode;
        this.isWin = isWin;
        this.recordTime = System.currentTimeMillis();
    }

    // Getter 和 Setter
    public int getRecordId() { return recordId; }
    public void setRecordId(int recordId) { this.recordId = recordId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public boolean isEasyMode() { return isEasyMode; }
    public void setEasyMode(boolean easyMode) { isEasyMode = easyMode; }

    public boolean isWin() { return isWin; }
    public void setWin(boolean win) { isWin = win; }

    public long getRecordTime() { return recordTime; }
    public void setRecordTime(long recordTime) { this.recordTime = recordTime; }
}