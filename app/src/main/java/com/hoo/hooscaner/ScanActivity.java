package com.hoo.hooscaner;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import cn.bingoogolapple.qrcode.core.QRCodeView;
import cn.bingoogolapple.qrcode.zbar.ZBarView;
import cn.bingoogolapple.qrcode.zxing.ZXingView;
import ryqr.Decoder;
import ryqr.Ryqr;

public class ScanActivity extends AppCompatActivity implements QRCodeView.Delegate, DataCallBackListener {

    private ZXingView mZXingView;
    private TextView tvState, tvScan, tvOther;

    private Decoder mDecoder;

    private HooConnetServer server = null;

    private String mResult = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        tvState = findViewById(R.id.tv_state);
        tvScan = findViewById(R.id.tv_scan);
        tvOther = findViewById(R.id.tv_other);
        tvScan.setText(String.format("Decode完成:%d", i));

        mZXingView = findViewById(R.id.zxingview);
        mZXingView.setDelegate(this);


        server = new HooConnetServer(20000);
        server.setDataListener(this);
        server.start();


        mDecoder = Ryqr.newDecoder();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mZXingView.startCamera();
        mZXingView.startSpotAndShowRect();
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
}