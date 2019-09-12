package com.hoo.hooscaner;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.hoo.hooscaner.modle.SocketErrorBean;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import cn.bingoogolapple.qrcode.core.QRCodeView;
import cn.bingoogolapple.qrcode.zbar.ZBarView;
import cn.bingoogolapple.qrcode.zxing.ZXingView;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;
import ryqr.Decoder;
import ryqr.Ryqr;

public class ScanActivity extends AppCompatActivity implements QRCodeView.Delegate, DataCallBackListener, EasyPermissions.PermissionCallbacks {

    private ZXingView mZXingView;
    private TextView tvState, tvScan, tvOther;

    private Decoder mDecoder;

    private HooConnetServer server = null;

    private String mResult = "";

    private static final int REQUEST_CODE_QRCODE_PERMISSIONS = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);
        EventBus.getDefault().register(this);

        HooApplication.app.init();
        HooApplication.app.addActivity(this);

        tvState = findViewById(R.id.tv_state);
        tvScan = findViewById(R.id.tv_scan);
        tvOther = findViewById(R.id.tv_other);
        tvScan.setText(String.format("Decode完成:%d", i));

        mZXingView = findViewById(R.id.zxingview);
        mZXingView.setDelegate(this);


        server = new HooConnetServer(20000);
        server.setDataListener(this);
        server.start();

        tvState.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String a =null;
                a.split(",");
            }
        });


        mDecoder = Ryqr.newDecoder();
    }

    @Override
    protected void onStart() {
        super.onStart();
        String[] perms = {Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE};
        if (!EasyPermissions.hasPermissions(this, perms)) {
            requestCodeQRCodePermissions();
        } else {
            mZXingView.startCamera();
            mZXingView.startSpotAndShowRect();
        }

    }

    @Override
    protected void onStop() {
        mZXingView.stopCamera();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        mZXingView.onDestroy();
        super.onDestroy();
        try {
            server.stop();
            server = null;
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    long i = 0;
    long j = 0;

    @Override
    public void onScanQRCodeSuccess(final String result) {
        if (result.contains("|ok")) {
            setResult("ok");
        } else {
            if (mDecoder.isCompleted()) {
                setResult(mDecoder.data());
            } else {
                try {
                    mDecoder.decode(result);
                    if (mDecoder.isCompleted()) {
                        setResult(mDecoder.data());
                    } else {
                        mZXingView.startSpot();
                    }
                } catch (Exception e) {
                    Log.d("kentson-decod", "出错了");
                    tvOther.setText(String.format("出错数:%d", j++));
                    mDecoder.reset();
                    mZXingView.startSpot();
                    e.printStackTrace();
                }
            }
        }
    }

    private void setResult(String result) {
        mResult = result;
        tvScan.setText(String.format("Decode完成:%d", i++));
        mDecoder.reset();
        mZXingView.startSpot();
    }

    @Override
    public void onCameraAmbientBrightnessChanged(boolean isDark) {
        String tipText = mZXingView.getScanBoxView().getTipText();
        String ambientBrightnessTip = "\n环境过暗，请打开闪光灯";
        if (isDark) {
            if (!tipText.contains(ambientBrightnessTip)) {
                mZXingView.getScanBoxView().setTipText(tipText + ambientBrightnessTip);
            }
        } else {
            if (tipText.contains(ambientBrightnessTip)) {
                tipText = tipText.substring(0, tipText.indexOf(ambientBrightnessTip));
                mZXingView.getScanBoxView().setTipText(tipText);
            }
        }
    }

    @Override
    public void onScanQRCodeOpenCameraError() {
        showToast("打开相机出错");
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mZXingView.startSpotAndShowRect();
    }

    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onReceive(final String clientString) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvState.setText(clientString);
                switch (clientString) {
                    case Command.START:
                        mZXingView.startCamera();
                        mZXingView.startSpotAndShowRect();
                        showToast("Start");
                        break;
                    case Command.NEXT:
                        server.send(mResult);
                        break;
                    case Command.END:
                        mResult = "";
                        mDecoder.reset();
                        break;
                }
            }
        });

    }

    @Override
    public void onState(final SocketState state) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                switch (state) {
                    case WAITING:
                        tvState.setText("等待连接...");
                        break;
                    case CONNET:
                        tvState.setText("已连接");
                        break;
                    case DISCONNET:
                        tvState.setText("已断开");
                        break;
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);

    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        finish();
        startActivity(new Intent(ScanActivity.this, ScanActivity.class));
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        finish();
    }

    @AfterPermissionGranted(REQUEST_CODE_QRCODE_PERMISSIONS)
    private void requestCodeQRCodePermissions() {
        String[] perms = {Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE};
        if (!EasyPermissions.hasPermissions(this, perms)) {
            EasyPermissions.requestPermissions(this, "扫描二维码需要打开相机和散光灯的权限", REQUEST_CODE_QRCODE_PERMISSIONS, perms);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGetMessage(SocketErrorBean message) {
        if (server != null) {
            try {
                server.stop();
                server = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        showToast("new Socket");
        server = new HooConnetServer(20000);
        server.setDataListener(this);
        server.start();
    }
}
