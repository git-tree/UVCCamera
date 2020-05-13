package com.serenegiant.usbcameratest0;

import android.graphics.Color;
import android.hardware.usb.UsbDevice;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.serenegiant.common.BaseActivity;
import com.serenegiant.usb.USBMonitor.OnDeviceConnectListener;
import com.serenegiant.usb.CameraDialog;
import com.serenegiant.usb.USBMonitor;
import com.serenegiant.usb.UVCCamera;
import com.serenegiant.usbcameratest0.utils.SDFileHelper;
import com.serenegiant.usbcameratest0.utils.ToastUtils;

public class Main2Activity extends BaseActivity implements CameraDialog.CameraDialogParent{
    private static final boolean DEBUG = true;
    private USBMonitor mUSBMonitor;
    private static final String TAG = "CameraTest_Activity";
    private final Object mSync = new Object();
    // for accessing USB and USB camera
    private UVCCamera mUVCCamera;
    private SurfaceView mUVCCameraView;
    // for open&start / stop&close camera preview
    private Button mCameraButton;
    private Surface mPreviewSurface;
    private boolean isActive, isPreview;
    private EditText txt_input;
    private int testCount=0;
    Handler handler=new Handler();
    private Button btn_choice;
    private Button btn_stop;
    private Button btn_report;
    private TextView lave_num;
    Runnable runnable=null;
    private boolean begintest=false;
    private int exceptioncount=0;

    public String people_choice_camera="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        txt_input= findViewById(R.id.txt_input);
        btn_choice=findViewById(R.id.btn_choice);
        btn_stop=findViewById(R.id.btn_stop);
        lave_num=findViewById(R.id.txt_lave_num);
        btn_report=findViewById(R.id.btn_report);
        mCameraButton = findViewById(R.id.btn_start);
        mCameraButton.setOnClickListener(mOnClickListener);
        mUVCCameraView = (SurfaceView)findViewById(R.id.surfaceView);
        mUVCCameraView.getHolder().addCallback(mSurfaceViewCallback);
        mUSBMonitor = new USBMonitor(this, mOnDeviceConnectListener);
        btn_choice.setOnClickListener(btn_choiceclick);
        btn_stop.setOnClickListener(btn_stopclick);
        btn_report.setOnClickListener(btn_reportlisen);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (DEBUG) Log.v(TAG, "onStart:");
        synchronized (mSync) {
            if (mUSBMonitor != null) {
                mUSBMonitor.register();
            }
        }
    }

    @Override
    protected void onStop() {
        if (DEBUG) Log.v(TAG, "onStop:");
        synchronized (mSync) {
            if (mUSBMonitor != null) {
                mUSBMonitor.unregister();
            }
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (DEBUG) Log.v(TAG, "onDestroy:");
        synchronized (mSync) {
            isActive = isPreview = false;
            if (mUVCCamera != null) {
                mUVCCamera.destroy();
                mUVCCamera = null;
            }
            if (mUSBMonitor != null) {
                mUSBMonitor.destroy();
                mUSBMonitor = null;
            }
        }
        mUVCCameraView = null;
        mCameraButton = null;
        super.onDestroy();
    }

    //选择相机
    private final View.OnClickListener btn_choiceclick=new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            closeCamera();
            CameraDialog.disabledialog=false;
            CameraDialog.showDialog(Main2Activity.this);
            people_choice_camera=CameraDialog.whichCamera_name;
            //shortShow(""+people_choice_camera);
        }
    };
    //停止
    private final View.OnClickListener btn_stopclick=new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            closeCamera();
            people_choice_camera="";
            CameraDialog.disabledialog=false;
            longErrorShow("结束成功！");
            if(runnable!=null){
                handler.removeCallbacks(runnable);
            }
            begintest=false;
            lave_num.setText("0");
        }
    };
    //导出日志
    private final View.OnClickListener btn_reportlisen=new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            reportLog();
        }
    };
    //导出日志
    private void reportLog() {
        if(exceptioncount>0){
            longShow("导出成功,文件路径:/sdcard/camera_exception.txt");
        }else{
            longShow("暂时未发现异常");
        }
    }
//开始测试
    private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(final View view) {
            //获取选择
            try {
                //shortShow(""+people_choice_camera);
                if("".equals(people_choice_camera)||people_choice_camera==null){
                    longErrorShow("请选择相机在开始测试!");
                    return;
                }
            } catch (Exception e) {
                longErrorShow(e.toString());
            }
            //获取次数
            String inputstr=txt_input.getText().toString();
            if("".equals(inputstr)|| null==inputstr){
                longShow("请输入测试次数!");
                return;
            }
            else{
                try {
                    testCount=Integer.parseInt(inputstr);
                    CameraDialog.disabledialog=true;
                    begintest=true;
                    closeCamera();
                    startcameraTest();
                } catch (Exception e) {
                    exceptioncount++;
                    StackTraceElement stackTraceElement= e.getStackTrace()[0];// 得到异常棧的首个元素
                    longErrorShow("相机测试异常+1");
                    try {
                        SDFileHelper sdFileHelper=new SDFileHelper();
                        String exceptionStr="异常方法名:"+stackTraceElement.getMethodName()+"\n"
                                +"异常代码行"+stackTraceElement.getLineNumber()+"\n"
                                +"异常内容:"+e.toString();
                        longErrorShow(exceptionStr);
                        sdFileHelper.savaFileToSD("camera_exception.txt",exceptionStr);
                    } catch (Exception ex) {
                        longShow(ex.toString());
                    }
                }
            }
        }
    };
    public void openCamera(){
        CameraDialog.showDialog(Main2Activity.this);
        shortShow("开启相机");
    }
    public void closeCamera(){
        //关闭相机
        if(mUVCCamera!=null){
            synchronized (mSync) {
                mUVCCamera.destroy();
                mUVCCamera = null;
                isActive = isPreview = false;
                shortShow("关闭相机");
            }
        }else{
            return;
        }
    }

    //开始循环打开关闭相机
    private void startcameraTest()  {
        testCount--;
        if(testCount<0){
            return;
        }
        if(!begintest){
            return;
        }
        lave_num.setTextColor(Color.rgb(30,144,255));
        lave_num.setText(""+testCount);
        openCamera();
        runnable=new Runnable() {
            @Override
            public void run() {
                closeCamera();
                if(testCount>=0&&begintest){
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                                startcameraTest();
                        }
                    }, 2000);
                }
            }
        };
        handler.postDelayed(runnable,2000);
    }

    private void shortShow(String s) {

        Toast toast = Toast.makeText(this, s, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER_VERTICAL|Gravity.CENTER_HORIZONTAL , 0, 0);  //设置显示位置
        TextView v = (TextView) toast.getView().findViewById(android.R.id.message);
        v.setTextColor(Color.rgb(30,144,255));     //设置字体颜色,道奇蓝
        toast.show();
    }
    private void longShow(String s) {
        Toast toast = Toast.makeText(this, s, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER_VERTICAL|Gravity.CENTER_HORIZONTAL , 0, 0);  //设置显示位置
        TextView v = (TextView) toast.getView().findViewById(android.R.id.message);
        v.setTextColor(Color.rgb(30,144,255));     //设置字体颜色,道奇蓝
        toast.show();
    }
    private void longErrorShow(String s) {
        Toast toast = Toast.makeText(this, s, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER_VERTICAL|Gravity.CENTER_HORIZONTAL , 0, 0);  //设置显示位置
        TextView v = (TextView) toast.getView().findViewById(android.R.id.message);
        v.setTextColor(Color.rgb(220,20,60));     //设置字体颜色,猩红
        toast.show();
    }
    private final USBMonitor.OnDeviceConnectListener mOnDeviceConnectListener = new OnDeviceConnectListener(){

        @Override
        public void onAttach(UsbDevice device) {
            if (DEBUG) Log.v(TAG, "onAttach:");
            Toast.makeText(Main2Activity.this, "USB_DEVICE_连接", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onDettach(UsbDevice device) {
            if (DEBUG) Log.v(TAG, "onDettach:");
            Toast.makeText(Main2Activity.this, "USB_DEVICE_DETACHED", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onConnect(UsbDevice device, USBMonitor.UsbControlBlock ctrlBlock, boolean createNew) {
            if (DEBUG) Log.v(TAG, "onConnect:");
            synchronized (mSync) {
                if (mUVCCamera != null) {
                    mUVCCamera.destroy();
                }
                isActive = isPreview = false;
            }

            queueEvent(new Runnable() {
                @Override
                public void run() {
                    synchronized (mSync) {
                        final UVCCamera camera = new UVCCamera();
                        camera.open(ctrlBlock);
                        if (DEBUG) Log.i(TAG, "supportedSize:" + camera.getSupportedSize());
                        //Toast.makeText(Main2Activity.this,"*****"+camera.getSupportedSize(),Toast.LENGTH_LONG).show();
                        try {
                            camera.setPreviewSize(UVCCamera.DEFAULT_PREVIEW_WIDTH, UVCCamera.DEFAULT_PREVIEW_HEIGHT, UVCCamera.FRAME_FORMAT_MJPEG);
                        } catch (final IllegalArgumentException e) {
                            try {
                                // fallback to YUV mode
                                camera.setPreviewSize(UVCCamera.DEFAULT_PREVIEW_WIDTH, UVCCamera.DEFAULT_PREVIEW_HEIGHT, UVCCamera.DEFAULT_PREVIEW_MODE);
                            } catch (final IllegalArgumentException e1) {
                                camera.destroy();
                                return;
                            }
                        }
                        mPreviewSurface = mUVCCameraView.getHolder().getSurface();
                        if (mPreviewSurface != null) {
                            isActive = true;
                            camera.setPreviewDisplay(mPreviewSurface);
                            camera.startPreview();
                            isPreview = true;
                        }
                        synchronized (mSync) {
                            mUVCCamera = camera;
                        }
                    }
                }
            }, 0);
        }

        @Override
        public void onDisconnect(UsbDevice device, USBMonitor.UsbControlBlock ctrlBlock) {
            if (DEBUG) Log.v(TAG, "onDisconnect:");
            // XXX you should check whether the comming device equal to camera device that currently using
            queueEvent(new Runnable() {
                @Override
                public void run() {
                    synchronized (mSync) {
                        if (mUVCCamera != null) {
                            mUVCCamera.close();
                            if (mPreviewSurface != null) {
                                mPreviewSurface.release();
                                mPreviewSurface = null;
                            }
                            isActive = isPreview = false;
                        }
                    }
                }
            }, 0);
        }

        @Override
        public void onCancel(UsbDevice device) {

        }
    };
    private final SurfaceHolder.Callback mSurfaceViewCallback = new SurfaceHolder.Callback(){

        @Override
        public void surfaceCreated(SurfaceHolder surfaceHolder) {
            if (DEBUG) Log.v(TAG, "surfaceCreated:");
        }

        @Override
        public void surfaceChanged(final SurfaceHolder holder, final int format, final int width, final int height) {
            if ((width == 0) || (height == 0)) return;
            if (DEBUG) Log.v(TAG, "surfaceChanged:");
            mPreviewSurface = holder.getSurface();
            synchronized (mSync) {
                if (isActive && !isPreview && (mUVCCamera != null)) {
                    mUVCCamera.setPreviewDisplay(mPreviewSurface);
                    mUVCCamera.startPreview();
                    isPreview = true;
                }
            }
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
            if (DEBUG) Log.v(TAG, "surfaceDestroyed:");
            synchronized (mSync) {
                if (mUVCCamera != null) {
                    mUVCCamera.stopPreview();
                }
                isPreview = false;
            }
            mPreviewSurface = null;
        }
    };






    @Override
    public USBMonitor getUSBMonitor() {
        return mUSBMonitor;
    }

    @Override
    public void onDialogResult(boolean canceled) {
        if (canceled) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // FIXME
                }
            }, 0);
        }
    }
/*
    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }*/
}
