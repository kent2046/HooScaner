package com.hoo.hooscaner;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.util.ArrayList;

public class HooApplication extends Application {

    public static HooApplication app;
    public static Context context;

    ArrayList<Activity> list = new ArrayList<Activity>();
    @Override
    public void onCreate() {
        context = this.getApplicationContext();
        app = this;
        super.onCreate();
    }

    public void init(){
        //设置该CrashHandler为程序的默认处理器
        UnCeHandler catchExcep = new UnCeHandler(this);
        Thread.setDefaultUncaughtExceptionHandler(catchExcep);
    }

    /**
     * Activity关闭时，删除Activity列表中的Activity对象*/
    public void removeActivity(Activity a){
        list.remove(a);
    }

    /**
     * 向Activity列表中添加Activity对象*/
    public void addActivity(Activity a){
        list.add(a);
    }

    /**
     * 关闭Activity列表中的所有Activity*/
    public void finishActivity(){
        for (Activity activity : list) {
            if (null != activity) {
                activity.finish();
            }
        }
        //杀死该应用进程
        android.os.Process.killProcess(android.os.Process.myPid());
    }


    public class UnCeHandler implements Thread.UncaughtExceptionHandler {

        private Thread.UncaughtExceptionHandler mDefaultHandler;
        public static final String TAG = "CatchExcep";
        HooApplication application;

        public UnCeHandler(HooApplication application){
            //获取系统默认的UncaughtException处理器
            mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
            this.application = application;
        }

        @Override
        public void uncaughtException(Thread thread, Throwable ex) {
            if(!handleException(ex) && mDefaultHandler != null){
                //如果用户没有处理则让系统默认的异常处理器来处理
                mDefaultHandler.uncaughtException(thread, ex);
            }else{

                Intent intent = new Intent(application.getApplicationContext(), ScanActivity.class);
                PendingIntent restartIntent = PendingIntent.getActivity(
                        application.getApplicationContext(), 0, intent,
                        PendingIntent.FLAG_CANCEL_CURRENT);
                //退出程序
                AlarmManager mgr = (AlarmManager)application.getSystemService(Context.ALARM_SERVICE);
                mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100,
                        restartIntent); // 1秒钟后重启应用
                application.finishActivity();
            }
        }

        /**
         * 自定义错误处理,收集错误信息 发送错误报告等操作均在此完成.
         *
         * @param ex
         * @return true:如果处理了该异常信息;否则返回false.
         */
        private boolean handleException(Throwable ex) {
            if (ex == null) {
                return false;
            }
            //使用Toast来显示异常信息
//        new Thread(){
//            @Override
//            public void run() {
//                Looper.prepare();
//                Toast.makeText(application.getApplicationContext(), "很抱歉,程序出现异常,即将退出.",
//                        Toast.LENGTH_SHORT).show();
//                Looper.loop();
//            }
//        }.start();
            return true;
        }
    }

}
