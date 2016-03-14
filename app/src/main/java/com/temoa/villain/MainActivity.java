package com.temoa.villain;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private SharedPreferences.Editor editor;
    private long exitTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        ImageView mImageView = (ImageView) findViewById(R.id.helpImg);
        SharedPreferences mSharedPreferences = getSharedPreferences("villain", MODE_PRIVATE);
        editor = mSharedPreferences.edit();
        boolean isFirstTime = mSharedPreferences.getBoolean("isFirstTime", true);
        //是否第一次打开APP
        if (isFirstTime) {
            showTeachDialog();
            editor.putBoolean("isFirstTime", false);
            editor.apply();
        }

        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openService();
                editor.putInt("counts", 0);
                editor.apply();
            }
        });

        mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTeachDialog();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    //打开抢红包服务
    private void openService() {
        try {
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            startActivity(intent);
            Toast.makeText(this, "在菜单中找到Villain，开启服务即可", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //显示教程Dialog
    private void showTeachDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("使用教程");
        builder.setMessage(R.string.howtouse);
        builder.setPositiveButton("知道了", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    //退出APP
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            if (System.currentTimeMillis() - exitTime > 2000) {
                Toast.makeText(this, "再按一下退出应用", Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
            } else {
                finish();
                System.exit(0);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
