package edu.swust.gameapplication;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {
    private EditText Account;
    private EditText Password;
    private EditText ConfirmPassword;
    private ImageButton backButton;
    private Button registerButton;

    // 使用 GameApp 提供的全局资源
    private UserDao userDao;

    // 账户名长度限制：1-16位
    private static final int MIN_ACCOUNT_LENGTH = 1;
    private static final int MAX_ACCOUNT_LENGTH = 16;

    // 密码长度限制：6-18位
    private static final int MIN_PASSWORD_LENGTH = 6;
    private static final int MAX_PASSWORD_LENGTH = 18;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // 1. 初始化控件
        Account = findViewById(R.id.editTextAccount);
        Password = findViewById(R.id.editTextPassword);
        ConfirmPassword = findViewById(R.id.editTextConfirmPassword);
        backButton = findViewById(R.id.backButton);
        registerButton = findViewById(R.id.buttonRegister);

        // 2. 使用 GameApp 提供的数据库访问对象
        userDao = GameApp.getUserDao();

        // 3. 返回按钮逻辑
        backButton.setOnClickListener(v -> {
            GameApp.playSound(R.raw.button1);
            finish();
        });

        // 4. 注册按钮点击事件
        registerButton.setOnClickListener(v -> {
            GameApp.playSound(R.raw.button1);
            performRegister();
        });
    }

    /**
     * 核心注册逻辑
     */
    private void performRegister() {
        // 获取输入数据
        String account = Account.getText().toString().trim();
        String password = Password.getText().toString();
        String confirmPassword = ConfirmPassword.getText().toString();

        // 输入校验
        if (!validateInput(account, password, confirmPassword)) {
            return;
        }

        // 禁用注册按钮，防止重复点击
        registerButton.setEnabled(false);

        // 使用 GameApp 的全局线程池执行数据库操作
        GameApp.getGlobalExecutor().execute(() -> {
            try {
                // 查询账号是否已存在
                User existingUser = userDao.getUserByAccount(account);

                if (existingUser != null) {
                    // 账号已存在
                    runOnUiThread(() -> {
                        registerButton.setEnabled(true);
                        Toast.makeText(RegisterActivity.this,
                                "注册失败：该账号已被注册",
                                Toast.LENGTH_LONG).show();
                    });
                } else {
                    // 插入新用户
                    User newUser = new User(account, password);
                    userDao.insertUser(newUser);

                    // 注册成功
                    runOnUiThread(() -> {
                        registerButton.setEnabled(true);
                        Toast.makeText(RegisterActivity.this,
                                "注册成功！",
                                Toast.LENGTH_LONG).show();

                        // 延迟关闭页面
                        Account.postDelayed(() -> finish(), 1500);
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    registerButton.setEnabled(true);
                    Toast.makeText(RegisterActivity.this,
                            "注册失败，请稍后重试",
                            Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    /**
     * 输入验证
     */
    private boolean validateInput(String account, String password, String confirmPassword) {
        // 账户名非空检查
        if (TextUtils.isEmpty(account)) {
            Toast.makeText(this, "请输入账号", Toast.LENGTH_SHORT).show();
            Account.requestFocus();
            return false;
        }
        // 账户名长度检查：1-16位
        if (account.length() < MIN_ACCOUNT_LENGTH || account.length() > MAX_ACCOUNT_LENGTH) {
            Toast.makeText(this,
                    String.format("账户名长度应在%d-%d位之间", MIN_ACCOUNT_LENGTH, MAX_ACCOUNT_LENGTH),
                    Toast.LENGTH_SHORT).show();
            Account.requestFocus();
            Account.selectAll();
            return false;
        }
        // 密码非空检查
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "请输入密码", Toast.LENGTH_SHORT).show();
            Password.requestFocus();
            return false;
        }
        // 确认密码非空检查
        if (TextUtils.isEmpty(confirmPassword)) {
            Toast.makeText(this, "请确认密码", Toast.LENGTH_SHORT).show();
            ConfirmPassword.requestFocus();
            return false;
        }
        // 密码一致性检查
        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "两次输入的密码不一致", Toast.LENGTH_SHORT).show();
            ConfirmPassword.requestFocus();
            return false;
        }
        // 密码长度检查：6-18位
        if (password.length() < MIN_PASSWORD_LENGTH || password.length() > MAX_PASSWORD_LENGTH) {
            Toast.makeText(this,
                    String.format("密码长度应在%d-%d位之间", MIN_PASSWORD_LENGTH, MAX_PASSWORD_LENGTH),
                    Toast.LENGTH_SHORT).show();
            Password.requestFocus();
            Password.selectAll();
            return false;
        }
        return true;
    }
}