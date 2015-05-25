package com.konst.module.scale;

import android.content.Intent;
import android.os.Handler;

/**
 * Created by Kostya on 30.04.2015.
 */
public class ConnectScaleThread extends Thread {
    ScaleModule scales;
    Handler mHandler;

    ConnectScaleThread(ScaleModule scaleModule, Handler handler){
        scales = scaleModule;
        mHandler = handler;
    }

    @Override
    public void run() {
        if (scales.connect()) {
            if (scales.isScales()) {
                try {
                    if (scales.ScaleVersion.load()) {
                        mHandler.sendMessage(mHandler.obtainMessage(ScaleModule.STATUS_OK));
                    }else {
                        mHandler.sendMessage(mHandler.obtainMessage(ScaleModule.STATUS_SCALE_ERROR));
                    }
                } catch (Exception e) {
                    Intent intent = new Intent();
                    intent.putExtra("error", e.getMessage());
                    mHandler.sendMessage(mHandler.obtainMessage(ScaleModule.STATUS_TERMINAL_ERROR, intent));
                }
            } else {
                scales.disconnect();
                Intent intent = new Intent();
                intent.putExtra("device", scales.getBtDeviceName());
                mHandler.sendMessage(mHandler.obtainMessage(ScaleModule.STATUS_SCALE_UNKNOWN));
                throw new RuntimeException("illegal version scale");
            }
        } else {
            mHandler.sendMessage(mHandler.obtainMessage(ScaleModule.STATUS_CONNECT_ERROR));
            throw new RuntimeException("bluetooth error connecting ");
        }
    }
}
