package com.assignment.sjsu.hudoassenco.cmpe137;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import java.io.InputStream;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class BitmapDownloader<T> extends HandlerThread {

    public static final String TAG = "BitmapDownloader";
    public static final int DOWNLOAD_MESSAGE_WHAT = 0;

    private Handler mRequestHandler;
    private Handler mResponseHandler;

    private OnBitmapDownloadedListenner<T> mOnBitmapDownloadedListenner;

    private ConcurrentMap<T, String> mHolderMap;

    public BitmapDownloader(Handler responseHandler) {
        super(TAG);
        mHolderMap = new ConcurrentHashMap<>();

        mResponseHandler = responseHandler;
    }

    @Override
    protected void onLooperPrepared() {
        Log.v(TAG, "onLooperPrepared");
        super.onLooperPrepared();

        mRequestHandler = new Handler(getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if(msg.what != DOWNLOAD_MESSAGE_WHAT) {
                    Log.e(TAG, "Wrong message what");
                    return;
                }
                handleRequest((T) msg.obj);
            }
        };
    }

    public interface OnBitmapDownloadedListenner<T> {
        public void onBitmapDownloaded(T holder, Bitmap image);
    }

    public OnBitmapDownloadedListenner<T> getmOnBitmapDownloadedListenner() {
        return mOnBitmapDownloadedListenner;
    }

    public void setmOnBitmapDownloadedListenner(OnBitmapDownloadedListenner<T> mOnBitmapDownloadedListenner) {
        this.mOnBitmapDownloadedListenner = mOnBitmapDownloadedListenner;
    }

    public void queueUrl(T holder, String url) {
        Log.v(TAG, "Queue url: "+url);
        if(url == null) {
            mHolderMap.remove(holder);
        } else {
            mHolderMap.put(holder, url);
            mRequestHandler.obtainMessage(DOWNLOAD_MESSAGE_WHAT, holder)
                    .sendToTarget();
        }
    }

    private void handleRequest(final T holder) {
        final String url = mHolderMap.get(holder);
        Log.v(TAG, "Downloading bitmap from url: "+url);

        try {
            InputStream in = new java.net.URL(url).openStream();
            final Bitmap image = BitmapFactory.decodeStream(in);

            mResponseHandler.post(new Runnable() {
                @Override
                public void run() {
                    Log.v(TAG, "Image downloaded, url: "+url);
                    if(mHolderMap.get(holder) == null) {
                        //Holder changed by recycler view
                        return;
                    }

                    mHolderMap.remove(holder);
                    mOnBitmapDownloadedListenner.onBitmapDownloaded(holder, image);
                }
            });

        } catch (Exception e) {
            Log.e(TAG, (e.getMessage() == null) ? "Error downloading image." : e.getMessage());
            e.printStackTrace();
        }
    }
}
