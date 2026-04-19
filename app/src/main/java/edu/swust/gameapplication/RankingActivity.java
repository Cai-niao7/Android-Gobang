package edu.swust.gameapplication;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;

public class RankingActivity extends AppCompatActivity {

    // UI控件
    private ImageButton btnBack;  // 返回按钮
    private Button btnEasyMode;   // 简单模式切换按钮
    private Button btnHardMode;   // 困难模式切换按钮
    private ListView lvRanking;   // 排行榜列表视图
    private TextView tvEmpty;     // 空数据提示文本
    private TextView tvCurrentRank;       // 当前用户排名显示
    private TextView tvCurrentWinRate;    // 当前用户胜率显示
    private View layoutCurrentRank;       // 当前用户信息布局容器

    // 适配器和数据
    private RankingAdapter rankingAdapter;    // 排行榜适配器
    private List<RankItem> rankItems = new ArrayList<>(); // 排行榜数据列表
    private boolean isEasyMode = true;        // 当前是否为简单模式

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ranking);

        // 初始化UI控件
        initViews();

        // 创建适配器
        rankingAdapter = new RankingAdapter();
        lvRanking.setAdapter(rankingAdapter);     // 设置适配器
        lvRanking.setEmptyView(tvEmpty);          // 设置空视图

        // 设置点击监听器
        setClickListeners();

        // 默认加载简单模式数据
        loadEasyRankingData();

        // 播放背景音乐
        GameApp.resumeBgMusic();
    }

    /**
     * 初始化UI控件
     */
    private void initViews() {
        btnBack = findViewById(R.id.btn_back);
        btnEasyMode = findViewById(R.id.btn_easy_mode);
        btnHardMode = findViewById(R.id.btn_hard_mode);
        lvRanking = findViewById(R.id.lv_ranking);
        tvEmpty = findViewById(R.id.tv_empty);
        tvCurrentRank = findViewById(R.id.tv_current_rank);
        tvCurrentWinRate = findViewById(R.id.tv_current_win_rate);
        layoutCurrentRank = findViewById(R.id.layout_current_rank);
    }

    /**
     * 设置点击监听器
     */
    private void setClickListeners() {
        // 返回按钮 - 返回上一页
        btnBack.setOnClickListener(v -> {
            GameApp.playSound(R.raw.button1);  // 播放按键音效
            finish();  // 结束当前Activity
        });

        // 简单模式按钮 - 切换到简单模式排行榜
        btnEasyMode.setOnClickListener(v -> {
            GameApp.playSound(R.raw.button1);  // 播放按键音效
            loadEasyRankingData();  // 加载简单模式数据
        });

        // 困难模式按钮 - 切换到困难模式排行榜
        btnHardMode.setOnClickListener(v -> {
            GameApp.playSound(R.raw.button1);  // 播放按键音效
            loadHardRankingData();  // 加载困难模式数据
        });

        // 列表项点击 - 显示玩家详情
        lvRanking.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                GameApp.playSound(R.raw.button);  // 播放按键音效
                RankItem item = rankItems.get(position);  // 获取点击的项
                showRankDetail(item);  // 显示详情
            }
        });
    }

    /**
     * 加载简单模式排行榜数据
     */
    private void loadEasyRankingData() {
        isEasyMode = true;  // 设置为简单模式
        GameApp.getGlobalExecutor().execute(() -> {  // 在后台线程执行数据库操作
            // 获取所有用户
            List<User> users = GameApp.getUserDao().getAllUsers();
            final List<RankItem> easyRankItems = new ArrayList<>();

            // 计算每个用户的简单模式胜率
            for (User user : users) {
                // 查询该用户的简单模式游戏数据
                int totalGames = GameApp.getGameRecordDao().countEasyGames(user.getId());      // 总场次
                int winGames = GameApp.getGameRecordDao().countEasyWins(user.getId());         // 胜利场次

                // 计算胜率（如果没玩过，胜率为0）
                double winRate = totalGames > 0 ? (double) winGames / totalGames * 100 : 0;

                // 创建排行榜项
                RankItem item = new RankItem();
                item.user = user;
                item.totalGames = totalGames;
                item.winGames = winGames;
                item.winRate = winRate;
                item.rank = 0;  // 初始排名为0

                // 只显示玩过游戏的用户（总场次>0）
                if (totalGames > 0) {
                    easyRankItems.add(item);
                }
            }

            // 按胜率降序排序
            sortByWinRate(easyRankItems);

            // 更新排名（设置实际排名）
            for (int i = 0; i < easyRankItems.size(); i++) {
                easyRankItems.get(i).rank = i + 1;  // 排名从1开始
            }

            // 更新当前用户信息
            String currentAccount = GameApp.getCurrentLoginAccount();  // 获取当前登录账号
            if (currentAccount != null) {
                User currentUser = GameApp.getUserDao().getUserByAccount(currentAccount);
                if (currentUser != null) {
                    for (RankItem item : easyRankItems) {
                        // 找到当前用户的排行榜项
                        if (item.user.getId() == currentUser.getId()) {
                            final RankItem finalItem = item;
                            runOnUiThread(() -> updateCurrentUserInfo(finalItem));  // 更新UI显示
                            break;
                        }
                    }
                }
            }

            // 更新UI
            runOnUiThread(() -> {
                rankItems.clear();  // 清空原数据
                rankItems.addAll(easyRankItems);  // 添加新数据
                rankingAdapter.notifyDataSetChanged();  // 通知适配器数据更新

                // 设置空列表提示
                if (rankItems.isEmpty()) {
                    tvEmpty.setText("暂无简单模式排行榜数据");
                } else {
                    tvEmpty.setText("");
                }
            });
        });
    }

    /**
     * 加载困难模式排行榜数据
     */
    private void loadHardRankingData() {
        isEasyMode = false;  // 设置为困难模式
        GameApp.getGlobalExecutor().execute(() -> {  // 在后台线程执行数据库操作
            // 获取所有用户
            List<User> users = GameApp.getUserDao().getAllUsers();
            final List<RankItem> hardRankItems = new ArrayList<>();

            // 计算每个用户的困难模式胜率
            for (User user : users) {
                // 查询该用户的困难模式游戏数据
                int totalGames = GameApp.getGameRecordDao().countHardGames(user.getId());      // 总场次
                int winGames = GameApp.getGameRecordDao().countHardWins(user.getId());         // 胜利场次

                // 计算胜率（如果没玩过，胜率为0）
                double winRate = totalGames > 0 ? (double) winGames / totalGames * 100 : 0;

                // 创建排行榜项
                RankItem item = new RankItem();
                item.user = user;
                item.totalGames = totalGames;
                item.winGames = winGames;
                item.winRate = winRate;
                item.rank = 0;

                // 只显示玩过游戏的用户
                if (totalGames > 0) {
                    hardRankItems.add(item);
                }
            }

            // 按胜率降序排序
            sortByWinRate(hardRankItems);

            // 更新排名
            for (int i = 0; i < hardRankItems.size(); i++) {
                hardRankItems.get(i).rank = i + 1;
            }

            // 更新当前用户信息
            String currentAccount = GameApp.getCurrentLoginAccount();
            if (currentAccount != null) {
                User currentUser = GameApp.getUserDao().getUserByAccount(currentAccount);
                if (currentUser != null) {
                    for (RankItem item : hardRankItems) {
                        if (item.user.getId() == currentUser.getId()) {
                            final RankItem finalItem = item;
                            runOnUiThread(() -> updateCurrentUserInfo(finalItem));
                            break;
                        }
                    }
                }
            }

            // 更新UI
            runOnUiThread(() -> {
                rankItems.clear();
                rankItems.addAll(hardRankItems);
                rankingAdapter.notifyDataSetChanged();

                // 设置空列表提示
                if (rankItems.isEmpty()) {
                    tvEmpty.setText("暂无困难模式排行榜数据");
                } else {
                    tvEmpty.setText("");
                }
            });
        });
    }

    /**
     * 按胜率排序（降序）
     * 胜率相同则按总游戏场次降序排序
     * items 要排序的排行榜项列表
     */
    private void sortByWinRate(List<RankItem> items) {
        items.sort((item1, item2) -> {
            // 首先按胜率降序排序（比较item2和item1，实现降序）
            if (item2.winRate != item1.winRate) {
                return Double.compare(item2.winRate, item1.winRate);
            }
            // 胜率相同，按总游戏场次降序排序
            return Integer.compare(item2.totalGames, item1.totalGames);
        });
    }

    /**
     * 更新当前用户信息显示
     * @param item 当前用户的排行榜项
     */
    private void updateCurrentUserInfo(RankItem item) {
        if (item != null) {
            // 显示当前用户信息
            layoutCurrentRank.setVisibility(View.VISIBLE);
            tvCurrentRank.setText(String.valueOf(item.rank));  // 设置排名
            tvCurrentWinRate.setText(String.format("%.1f%%", item.winRate));  // 设置胜率
        } else {
            // 当前用户没有游戏数据，隐藏信息区域
            layoutCurrentRank.setVisibility(View.GONE);
        }
    }

    /**
     * 显示排名详情对话框
     * item 要显示详情的排行榜项
     */
    private void showRankDetail(RankItem item) {
        String mode = isEasyMode ? "简单" : "困难";  // 根据当前模式显示文本
        // 格式化详情信息
        String detail = String.format("玩家: %s\n排名: %d\n胜率: %.1f%%\n总场次: %d\n胜场: %d",
                item.user.getAccount(),   // 玩家账号
                item.rank,                // 排名
                item.winRate,             // 胜率
                item.totalGames,          // 总场次
                item.winGames);           // 胜利场次

        // 创建并显示详情对话框
        new android.app.AlertDialog.Builder(this)
                .setTitle("玩家详情")
                .setMessage(detail)
                .setPositiveButton("确定", (dialog, which) -> {
                    GameApp.playSound(R.raw.button1);  // 播放按键音效
                })
                .setOnCancelListener(dialog -> {
                    GameApp.playSound(R.raw.button1);  // 播放按键音效
                })
                .show();
    }

    /**
     * 排行榜项数据类
     * 用于存储单个用户的排行榜数据
     */
    private static class RankItem {
        User user;          // 用户信息
        int rank;           // 排名
        int totalGames;     // 总游戏场次
        int winGames;       // 胜利场次
        double winRate;     // 胜率（百分比）
    }

    /**
     * 排行榜适配器
     * 负责将RankItem数据绑定到ListView的每一项视图上
     */
    private class RankingAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return rankItems.size();  // 返回数据项数量
        }

        @Override
        public Object getItem(int position) {
            return rankItems.get(position);  // 返回指定位置的数据项
        }

        @Override
        public long getItemId(int position) {
            return position;  // 返回位置作为ID
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            // 复用convertView提高性能
            if (convertView == null) {
                // 加载列表项布局
                convertView = getLayoutInflater().inflate(R.layout.item_ranking, parent, false);
                holder = new ViewHolder();
                // 绑定视图组件
                holder.tvRank = convertView.findViewById(R.id.tv_rank);
                holder.ivAvatar = convertView.findViewById(R.id.iv_avatar);
                holder.tvAccount = convertView.findViewById(R.id.tv_account);
                holder.tvWinRate = convertView.findViewById(R.id.tv_win_rate);
                convertView.setTag(holder);  // 保存ViewHolder到Tag中
            } else {
                holder = (ViewHolder) convertView.getTag();  // 复用ViewHolder
            }

            // 获取当前位置的数据
            RankItem item = rankItems.get(position);

            // 设置排名
            holder.tvRank.setText(String.valueOf(item.rank));

            // 设置头像
            int avatarResId = getAvatarResource(item.user.getAvatarId());  // 根据头像ID获取资源ID
            holder.ivAvatar.setImageResource(avatarResId);

            // 设置账号
            holder.tvAccount.setText(item.user.getAccount());

            // 设置胜率
            holder.tvWinRate.setText(String.format("%.1f%%", item.winRate));

            return convertView;
        }

        /**
         * ViewHolder类，用于缓存列表项的视图引用，提高性能
         */
        class ViewHolder {
            TextView tvRank;      // 排名文本
            ImageView ivAvatar;   // 头像图片
            TextView tvAccount;   // 账号文本
            TextView tvWinRate;   // 胜率文本
        }
    }

    /**
     * 根据头像ID获取对应的drawable资源ID
     * @param avatarId 头像ID（1-10）
     * @return drawable资源ID
     */
    private int getAvatarResource(int avatarId) {
        // 根据头像ID返回对应的资源ID
        switch (avatarId) {
            case 1: return R.drawable.avatar_1;
            case 2: return R.drawable.avatar_2;
            case 3: return R.drawable.avatar_3;
            case 4: return R.drawable.avatar_4;
            case 5: return R.drawable.avatar_5;
            case 6: return R.drawable.avatar_6;
            case 7: return R.drawable.avatar_7;
            case 8: return R.drawable.avatar_8;
            case 9: return R.drawable.avatar_9;
            case 10: return R.drawable.avatar_10;
            default: return R.drawable.avatar_default;  // 默认头像
        }
    }
}