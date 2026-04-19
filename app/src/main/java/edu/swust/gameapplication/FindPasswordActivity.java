package edu.swust.gameapplication;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class FindPasswordActivity extends AppCompatActivity {
    // 控件变量
    private EditText editTextAccount;
    private TextView TextFindPassword;
    private ImageButton backButton;
    private Button buttonFindPassword;

    // 线程相关
    private Handler mainHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.acticity_findpassword);

        // 初始化控件
        initViews();

        // 设置事件监听
        setupEventListeners();
    }

    /**
     * 初始化控件
     */
    private void initViews() {
        editTextAccount = findViewById(R.id.editTextAccount);
        TextFindPassword = findViewById(R.id.TextFindPassword);
        backButton = findViewById(R.id.backButton);
        buttonFindPassword = findViewById(R.id.buttonFindPassword);

        // 初始化主线程Handler
        mainHandler = new Handler(Looper.getMainLooper());
    }

    /**
     * 设置事件监听器
     */
    private void setupEventListeners() {
        // 返回按钮
        backButton.setOnClickListener(v -> finish());

        // 找回密码按钮
        buttonFindPassword.setOnClickListener(v -> performFindPassword());
    }

    /**
     * 执行找回密码逻辑
     */
    private void performFindPassword() {
        // 获取输入的账号
        String account = editTextAccount.getText().toString().trim();

        // 输入校验
        if (TextUtils.isEmpty(account)) {
            Toast.makeText(this, "请输入要找回密码的账号", Toast.LENGTH_SHORT).show();
            TextFindPassword.setText("");
            return;
        }

        // 使用GameApp的全局线程池执行数据库查询
        GameApp.getGlobalExecutor().execute(() -> {
            // 从GameApp获取UserDao查询用户
            User foundUser = GameApp.getUserDao().getUserByAccount(account);

            // 在主线程更新UI
            mainHandler.post(() -> handleFindPasswordResult(foundUser));
        });
    }

    /**
     * 处理找回密码结果
     */
    private void handleFindPasswordResult(User foundUser) {
        if (foundUser == null) {
            // 账号不存在
            Toast.makeText(FindPasswordActivity.this, "该账号未注册", Toast.LENGTH_SHORT).show();
            TextFindPassword.setText("");
        } else {
            // 显示密码
            String password = foundUser.getPassword();
            TextFindPassword.setText(password);
            Toast.makeText(FindPasswordActivity.this, "密码找回成功", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 清理Handler
        mainHandler.removeCallbacksAndMessages(null);
    }
}