/**
 * 个人收款 https://gitee.com/DaLianZhiYiKeJi/xpay
 * 大连致一科技有限公司
 */

package com.weilan.mynotify;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NotificationMonitorService extends NotificationListenerService {
    private static final String AliPay = "alipay";
    private static final String WeixinPay = "wechat";
    private static int version;
    //	private MyHandler handler;
    public long lastTimePosted = System.currentTimeMillis();
    private Pattern pJxYmf_Nofity;
    private Pattern pAlipay;
    private Pattern pAlipay2;
    private Pattern pAlipayDianyuan;
    private Pattern pWeixin;
    private MediaPlayer payComp;
    private MediaPlayer payRecv;
    private MediaPlayer payNetWorkError;
    private PowerManager.WakeLock wakeLock;


    public void onCreate() {
        super.onCreate();
        //支付宝
        String pattern = "(\\S*)通过扫码向你付款([\\d\\.]+)元";
        pAlipay = Pattern.compile(pattern);
        pattern = "成功收款([\\d\\.]+)元。享免费提现等更多专属服务，点击查看";
        pAlipay2 = Pattern.compile(pattern);
        pAlipayDianyuan = Pattern.compile("支付宝成功收款([\\d\\.]+)元。收钱码收钱提现免费，赶紧推荐顾客使用");
        pWeixin = Pattern.compile("微信支付收款([\\d\\.]+)元");
        pJxYmf_Nofity = Pattern.compile("一笔收款交易已完成，金额([\\d\\.]+)元");
        payComp = MediaPlayer.create(this, R.raw.paycomp);
        payRecv = MediaPlayer.create(this, R.raw.payrecv);
        payNetWorkError = MediaPlayer.create(this, R.raw.networkerror);

    }


    public void onDestroy() {
        if (wakeLock != null) {
            wakeLock.release();
            wakeLock = null;
        }
        Intent localIntent = new Intent();
        localIntent.setClass(this, NotificationMonitorService.class);
        startService(localIntent);
    }

    public void onNotificationPosted(StatusBarNotification sbn) {
        Bundle bundle = sbn.getNotification().extras;
        String pkgName = sbn.getPackageName();
        if (getPackageName().equals(pkgName)) {
            //测试成功
            Intent intent = new Intent();
            intent.setAction("com.weilan.Notification");
            Uri uri = new Uri.Builder().scheme("app").path("log").query("msg=测试成功").build();
            intent.setData(uri);
            sendBroadcast(intent);
            return;
        }
        String title = bundle.getString("android.title");
        String text = bundle.getString("android.text");
        if (text == null) {
            //没有消息.垃圾
            return;
        }
        this.lastTimePosted = System.currentTimeMillis();
        //支付宝com.eg.android.AlipayGphone
        //com.eg.android.AlipayGphone]:支付宝通知 & 新哥通过扫码向你付款0.01元
        if (pkgName.equals("com.eg.android.AlipayGphone") && text != null) {
            // 现在创建 matcher 对象
            do {
                Matcher m = pAlipay.matcher(text);
                if (m.find()) {
                    String uname = m.group(1);
                    String money = m.group(2);
                    postMethod(AliPay, money, uname, false);
                    break;
                }
                m = pAlipay2.matcher(text);
                if (m.find()) {
                    String money = m.group(1);
                    postMethod(AliPay, money, "支付宝用户", false);
                    break;
                }
                m = pAlipayDianyuan.matcher(text);
                if (m.find()) {
                    String money = m.group(1);
                    postMethod(AliPay, money, "支付宝-店员", true);
                    break;
                }
            } while (false);
        }
        //微信
        //com.tencent.mm]:微信支付 & 微信支付收款0.01元
        else if (pkgName.equals("com.tencent.mm") && text != null) {
            // 现在创建 matcher 对象
            Matcher m = pWeixin.matcher(text);
            if (m.find()) {
                String uname = "微信用户";
                String money = m.group(1);
                postMethod(WeixinPay, money, uname, false);
            }
        }
    }


    public void onNotificationRemoved(StatusBarNotification paramStatusBarNotification) {
        if (Build.VERSION.SDK_INT >= 19) {
            Bundle localObject = paramStatusBarNotification.getNotification().extras;
            String pkgName = paramStatusBarNotification.getPackageName();
            String title = localObject.getString("android.title");
            String text = (localObject).getString("android.text");
        }
    }

    public int onStartCommand(Intent paramIntent, int paramInt1, int paramInt2) {
        return START_NOT_STICKY;
    }


    /**
     * 获取道的支付通知发送到服务器
     *
     * @param payType  支付方式
     * @param money    支付金额
     * @param username 支付者名字
     */
    public void postMethod(final String payType, final String money, final String username, boolean dianYuan) {
        final Map<String, String> params = new HashMap<String, String>();
        params.put("payType", payType);
        params.put("money", money);
        params.put("merchant_id", User.instance().merchant_id);
        params.put("pay_time", System.currentTimeMillis() + "");
        final String strUrlPath = "http://pay.tinyfizz.com/api.php?app=home&act=index";
        new Thread() {
            @Override
            public void run() {
                super.run();
                String steResult = HttpUtils.submitPostData(strUrlPath, params, "utf-8");
            }
        }.start();


    }

}