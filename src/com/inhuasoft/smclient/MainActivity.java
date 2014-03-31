package com.inhuasoft.smclient;

import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.LibVlcException;
import org.videolan.libvlc.IVideoPlayer;
import org.videolan.libvlc.WeakHandler;


import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.util.Log;
import android.view.Menu;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;

public class MainActivity extends Activity implements SurfaceHolder.Callback, IVideoPlayer {

	
    public final static String TAG = "VLC/MainActivity";
	  // size of the video
    private int mVideoHeight;
    private int mVideoWidth;
    private int mVideoVisibleHeight;
    private int mVideoVisibleWidth;
    private int mSarNum;
    private int mSarDen;
    
    
    private static final int SURFACE_BEST_FIT = 0;
    private static final int SURFACE_FIT_HORIZONTAL = 1;
    private static final int SURFACE_FIT_VERTICAL = 2;
    private static final int SURFACE_FILL = 3;
    private static final int SURFACE_16_9 = 4;
    private static final int SURFACE_4_3 = 5;
    private static final int SURFACE_ORIGINAL = 6;
    private int mCurrentSize = SURFACE_BEST_FIT;
    
    
    private static final int OVERLAY_TIMEOUT = 4000;
    private static final int OVERLAY_INFINITE = 3600000;
    private static final int FADE_OUT = 1;
    private static final int SHOW_PROGRESS = 2;
    private static final int SURFACE_SIZE = 3;
    private static final int AUDIO_SERVICE_CONNECTION_SUCCESS = 5;
    private static final int AUDIO_SERVICE_CONNECTION_FAILED = 6;
    private static final int FADE_OUT_INFO = 4;
	
    
    SurfaceHolder mSurfaceHolder ;
	SurfaceView mSurfaceView ; 
	LibVLC mLibVLC ;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mSurfaceView = (SurfaceView)findViewById(R.id.surfaceView1);
		mSurfaceHolder = mSurfaceView.getHolder() ;
		mSurfaceHolder.addCallback(this);
		mSurfaceHolder.setFormat(PixelFormat.RGBX_8888);
		try {
			mLibVLC = LibVLC.getInstance();
		} catch (LibVlcException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(mLibVLC != null)
		{
			try {
				mLibVLC.init(getApplicationContext());
			} catch (LibVlcException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if(mLibVLC != null)
		{
			mLibVLC.setHardwareAcceleration(LibVLC.HW_ACCELERATION_AUTOMATIC);
			mLibVLC.eventVideoPlayerActivityCreated(true);
			//mLibVLC.playMRL("rtsp://218.204.223.237:554/live/1/66251FC11353191F/e7ooqwcfbqjoo80j.sdp");
			mLibVLC.playMRL("rtsp://192.168.4.102:8086");
		}
		
		
	}
    
	@Override
	public void setSurfaceSize(int width, int height, int visible_width,
			int visible_height, int sar_num, int sar_den) {
		// TODO Auto-generated method stub
		  // store video size
        mVideoHeight = height;
        mVideoWidth = width;
        mVideoVisibleHeight = visible_height;
        mVideoVisibleWidth  = visible_width;
        mSarNum = sar_num;
        mSarDen = sar_den;
        Message msg = mHandler.obtainMessage(SURFACE_SIZE);
        mHandler.sendMessage(msg);
	}

	
	  /**
     * Handle resize of the surface and the overlay
     */
    private final Handler mHandler = new VideoPlayerHandler(this);

    private static class VideoPlayerHandler extends WeakHandler<MainActivity> {
        public VideoPlayerHandler(MainActivity owner) {
            super(owner);
        }

        @Override
        public void handleMessage(Message msg) {
            MainActivity activity = getOwner();
            if(activity == null) // WeakReference could be GC'ed early
                return;

            switch (msg.what) {
                case FADE_OUT:
                    //activity.hideOverlay(false);
                    break;
                case SHOW_PROGRESS:
                  /*  int pos = activity.setOverlayProgress();
                    if (activity.canShowProgress()) {
                        msg = obtainMessage(SHOW_PROGRESS);
                        sendMessageDelayed(msg, 1000 - (pos % 1000));
                    } */
                    break;
                case SURFACE_SIZE:
                    activity.changeSurfaceSize();
                    break;
                case FADE_OUT_INFO:
                   // activity.fadeOutInfo();
                    break;
                case AUDIO_SERVICE_CONNECTION_SUCCESS:
                   // activity.startPlayback();
                    break;
                case AUDIO_SERVICE_CONNECTION_FAILED:
                    activity.finish();
                    break;
            }
        }
    };
	
    
    private void changeSurfaceSize() {
        int sw;
        int sh;


        sw = getWindow().getDecorView().getWidth();
        sh = getWindow().getDecorView().getHeight();


        double dw = sw, dh = sh;

        // sanity check
        if (dw * dh == 0 || mVideoWidth * mVideoHeight == 0) {
            Log.e(TAG, "Invalid surface size");
            return;
        }

        // compute the aspect ratio
        double ar, vw;
        double density = (double)mSarNum / (double)mSarDen;
        if (density == 1.0) {
            /* No indication about the density, assuming 1:1 */
            vw = mVideoVisibleWidth;
            ar = (double)mVideoVisibleWidth / (double)mVideoVisibleHeight;
        } else {
            /* Use the specified aspect ratio */
            vw = mVideoVisibleWidth * density;
            ar = vw / mVideoVisibleHeight;
        }

        // compute the display aspect ratio
        double dar = dw / dh;

        switch (mCurrentSize) {
            case SURFACE_BEST_FIT:
                if (dar < ar)
                    dh = dw / ar;
                else
                    dw = dh * ar;
                break;
            case SURFACE_FIT_HORIZONTAL:
                dh = dw / ar;
                break;
            case SURFACE_FIT_VERTICAL:
                dw = dh * ar;
                break;
            case SURFACE_FILL:
                break;
            case SURFACE_16_9:
                ar = 16.0 / 9.0;
                if (dar < ar)
                    dh = dw / ar;
                else
                    dw = dh * ar;
                break;
            case SURFACE_4_3:
                ar = 4.0 / 3.0;
                if (dar < ar)
                    dh = dw / ar;
                else
                    dw = dh * ar;
                break;
            case SURFACE_ORIGINAL:
                dh = mVideoVisibleHeight;
                dw = vw;
                break;
        }
       

    // force surface buffer size
    
    mSurfaceHolder.setFixedSize(mVideoWidth, mVideoHeight);
	LayoutParams lp = mSurfaceView.getLayoutParams();
	lp.width =  (int) Math.ceil(dw * mVideoWidth / mVideoVisibleWidth);;
	lp.height = (int) Math.ceil(dh * mVideoHeight / mVideoVisibleHeight);
	mSurfaceView.setLayoutParams(lp);
	mSurfaceView.invalidate();

    }
    
    /**
     * attach and disattach surface to the lib
     */
    
	@Override
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
		// TODO Auto-generated method stub
		 if(arg1 == PixelFormat.RGBX_8888)
             Log.d(TAG, "Pixel format is RGBX_8888");
         else if(arg1 == PixelFormat.RGB_565)
             Log.d(TAG, "Pixel format is RGB_565");
         else if(arg1 == ImageFormat.YV12)
             Log.d(TAG, "Pixel format is YV12");
         else
             Log.d(TAG, "Pixel format is other/unknown");
         mLibVLC.attachSurface(arg0.getSurface(), MainActivity.this);
	}

	@Override
	public void surfaceCreated(SurfaceHolder arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder arg0) {
		// TODO Auto-generated method stub
		 mLibVLC.detachSurface();
	}

	

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	//	mLibVLC.eventVideoPlayerActivityCreated(false);
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	//	mLibVLC.stop();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}