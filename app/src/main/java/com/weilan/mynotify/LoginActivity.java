package com.weilan.mynotify;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    EditText tel, pwd;
    Button sumbit;

    String StrTel, StrPwd;

    String strUrlPath = "http://pay.tinyfizz.com/api.php?app=home&act=login";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        tel = findViewById(R.id.tel);
        pwd = findViewById(R.id.pwd);
        sumbit = findViewById(R.id.submit);
        sumbit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                StrTel = tel.getText().toString();
                StrPwd = pwd.getText().toString();
                if (checkText()) {
                    new Thread() {
                        @Override
                        public void run() {
                            login();
                        }
                    }.start();
                }
            }
        });
        if (SPUtils.get(LoginActivity.this, "autologin", "").toString().equals("1")) {
            new Thread() {
                @Override
                public void run() {
                    autoLogin();
                }
            }.start();
        }
    }

    private boolean checkText() {
        if (StrTel == null) {
            Toast.makeText(this, "请输入账号", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (StrPwd == null) {
            Toast.makeText(this, "请输入密码", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void login() {
        final Map<String, String> params = new HashMap<String, String>();
        params.put("tel", StrTel);
        params.put("password", StrPwd);
        String steResult = HttpUtils.submitPostData(strUrlPath, params, "utf-8");
        try {
            JSONObject jsonObject = new JSONObject(steResult);
            String msg = jsonObject.optString("msg");
            JSONObject dataObj = jsonObject.getJSONObject("data");
            if (dataObj != null) {
                SPUtils.put(LoginActivity.this, "autologin", "1");
                SPUtils.put(LoginActivity.this, "tel", StrTel);
                SPUtils.put(LoginActivity.this, "pwd", StrPwd);
                User.instance().merchant_id = dataObj.optString("merchant_id");
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                LoginActivity.this.startActivity(intent);
                LoginActivity.this.finish();
            } else {
                SPUtils.put(LoginActivity.this, "autologin", "0");
                Toast.makeText(LoginActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void autoLogin() {
        final Map<String, String> params = new HashMap<String, String>();
        params.put("tel", SPUtils.get(LoginActivity.this, "tel", "").toString());
        params.put("password", SPUtils.get(LoginActivity.this, "pwd", "").toString());
        String steResult = HttpUtils.submitPostData(strUrlPath, params, "utf-8");
        try {
            JSONObject jsonObject = new JSONObject(steResult);
            String msg = jsonObject.optString("msg");
            JSONObject dataObj = jsonObject.getJSONObject("data");
            if (dataObj != null) {
                User.instance().merchant_id = dataObj.optString("merchant_id");
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                LoginActivity.this.startActivity(intent);
                LoginActivity.this.finish();
            } else {
                SPUtils.put(LoginActivity.this, "autologin", "0");
                Toast.makeText(LoginActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
