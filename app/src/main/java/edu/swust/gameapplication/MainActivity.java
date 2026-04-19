package edu.swust.gameapplication;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;

public class MainActivity extends AppCompatActivity {

    // UI控件
    private TextInputEditText etAccount;
    private TextInputEditText etPassword;
    private Button btnLogin;
    private TextView tvRegister;
    private TextView tvFindPassword;

    // 线程相关
    private Handler mainHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // 1. 初始化主线程Handler
        mainHandler = new Handler(Looper.getMainLooper());

        // 2. 绑定控件
        etAccount = findViewById(R.id.etAccount);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.btnRegister);
        tvFindPassword = findViewById(R.id.btForgotPassword);

        // 3. 设置事件监听
        setupEventListeners();
    }

    /**
     * 设置事件监听器
     */
    private void setupEventListeners() {
        // 登录按钮
        btnLogin.setOnClickListener(v -> {
            // 播放按键音效
            GameApp.playSound(R.raw.button);
            handleLogin();
        });

        // 注册按钮
        tvRegister.setOnClickListener(v -> {
            GameApp.playSound(R.raw.button1);
            Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        // 找回密码按钮
        tvFindPassword.setOnClickListener(v -> {
            GameApp.playSound(R.raw.button1);
            Intent intent = new Intent(MainActivity.this, FindPasswordActivity.class);
            startActivity(intent);
        });
    }

    /**
     * 处理登录逻辑
     */
    private void handleLogin() {
        String account = etAccount.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // 空值校验
        if (account.isEmpty()) {
            Toast.makeText(this, "请输入账号", Toast.LENGTH_SHORT).show();
            return;
        }
        if (password.isEmpty()) {
            Toast.makeText(this, "请输入密码", Toast.LENGTH_SHORT).show();
            return;
        }

        // 使用GameApp的全局线程池执行数据库操作
        GameApp.getGlobalExecutor().execute(() -> {
            // 从GameApp获取UserDao实例进行查询
            User user = GameApp.getUserDao().getUserByAccount(account);

            // 切换到主线程更新UI
            mainHandler.post(() -> handleLoginResult(user, account, password));
        });
    }

    /**
     * 处理登录结果
     */
    private void handleLoginResult(User user, String account, String password) {
        if (user == null) {
            Toast.makeText(MainActivity.this, "登录失败：账号未注册", Toast.LENGTH_SHORT).show();
        } else if (user.getPassword().equals(password)) {
            // 登录成功：保存账号到GameApp的全局变量
            GameApp.setCurrentLoginAccount(account);
            Toast.makeText(MainActivity.this, "登录成功！账号：" + account, Toast.LENGTH_LONG).show();

            // 跳转到主游戏界面
            Intent intent = new Intent(MainActivity.this, MainGameActivity.class);
            startActivity(intent);
            finish(); // 关闭登录页
        } else {
            Toast.makeText(MainActivity.this, "登录失败：密码错误", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 清理Handler
        mainHandler.removeCallbacksAndMessages(null);
    }
}