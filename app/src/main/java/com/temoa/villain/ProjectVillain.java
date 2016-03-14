package com.temoa.villain;

import android.accessibilityservice.AccessibilityService;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

import java.util.List;

public class ProjectVillain extends AccessibilityService {

    //打印目标名
    static final String TAG = "ProjectVillain";
    //微信包名
    //static final String WECHAT_PACKAGENAME = "com.tencent.mm";
    //微信拆红包类
    static final String WECHAT_RECEIVER_CLASS = "com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyReceiveUI";
    //微信聊天界面
    static final String WECHAT_LAUNCHER = "com.tencent.mm.ui.LauncherUI";
    //微信红包详情类
    static final String WECHAT_DETATL = "com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyDetailUI";
    //红包消息关键词
    static final String HONGBAO_TEXT_KEY = "[微信红包]";

    private boolean isFirstChecked;

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        final int eventType = event.getEventType();
        //通知栏事件
        if (eventType == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) {
            List<CharSequence> texts = event.getText();
            if (!texts.isEmpty()) {
                for (CharSequence t : texts) {
                    String text = String.valueOf(t);
                    if (text.contains(HONGBAO_TEXT_KEY)) {
                        openNotify(event);
                        break;
                    }
                }
            }
            //窗口改变事件
        } else if (eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            openRedBag(event);
        }
    }

    //连接AccessibilityService
    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        Toast.makeText(this, "启动抢红包服务", Toast.LENGTH_SHORT).show();
    }

    //中断AccessibilityService
    @Override
    public void onInterrupt() {
        Toast.makeText(this, "关闭抢红包服务", Toast.LENGTH_SHORT).show();
    }

    //打开通知栏消息
    private void openNotify(AccessibilityEvent event) {
        if (event.getParcelableData() == null || !(event.getParcelableData() instanceof Notification)) {
            return;
        }
        ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        ComponentName cn1 = am.getRunningTasks(1).get(0).topActivity;
        Log.d(TAG, "cn1----" + cn1.toString());
        //将微信的通知栏消息打开
        Notification notification = (Notification) event.getParcelableData();
        PendingIntent pendingIntent = notification.contentIntent;
        isFirstChecked = true;
        try {
            pendingIntent.send();
            ComponentName cn2 = am.getRunningTasks(1).get(0).topActivity;
            Log.d(TAG, "cn2----" + cn2.toString());
            if (cn1.equals(cn2)) {
                searchKeyWord2();
            }
        } catch (PendingIntent.CanceledException e) {
            e.printStackTrace();
        }
    }

    private void openRedBag(AccessibilityEvent event) {
        if (WECHAT_RECEIVER_CLASS.equals(event.getClassName())) {
            //点中红包，下一步拆红包
            Log.d(TAG, "已点中红包");
            searchKeyWord1();
        } else if (WECHAT_LAUNCHER.equals(event.getClassName())) {
            //在聊天界面，点击红包
            Log.d(TAG, "在聊天界面点红包");
            searchKeyWord2();
        } else if (WECHAT_DETATL.equals(event.getClassName())) {
            //在红包详情页，模拟HOME键回到桌面
            performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
            performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME);
        }
    }

    //查找关键字“领取红包”“微信红包”
    private void searchKeyWord2() {
        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
        if (nodeInfo == null) {
            Log.w(TAG, "rootWindow为空");
            return;
        }
        List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByText("领取红包");
        if (list.isEmpty()) {
            list = nodeInfo.findAccessibilityNodeInfosByText(HONGBAO_TEXT_KEY);
            for (AccessibilityNodeInfo n : list) {
                Log.i(TAG, "-->微信红包:" + n);
                n.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                break;
            }
        } else {
            //最新的红包领起
            for (int i = list.size() - 1; i >= 0; i--) {
                AccessibilityNodeInfo parent = list.get(i).getParent();
                Log.i(TAG, "-->领取红包:" + parent);
                if (parent != null) {
                    if (isFirstChecked) {
                        parent.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                        isFirstChecked = false;
                    }
                    break;
                }
            }
        }
    }

    //查询关键字“拆红包”
    private void searchKeyWord1() {
        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
        if (nodeInfo == null) {
            Log.w(TAG, "rootWindow为空");
            return;
        }
        //List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByText("拆红包");
        List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/b43");
        for (AccessibilityNodeInfo n : list) {
            n.performAction(AccessibilityNodeInfo.ACTION_CLICK);
        }
    }
}

