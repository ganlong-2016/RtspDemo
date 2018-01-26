package com.drkj.rtspdemo;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class Main3Activity extends AppCompatActivity implements
        View.OnClickListener,
        TextureView.SurfaceTextureListener, MediaPlayer.OnBufferingUpdateListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnVideoSizeChangedListener {

    EditText rtspUrl;
    Button playButton;
    Button btnCapture;
    private MediaPlayer mMediaPlayer;
    private TextureView mPreview;
    private Surface surface;
    String TAG = "ganlongtest";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);
        rtspUrl = (EditText) this.findViewById(R.id.editText);
        playButton = (Button) this.findViewById(R.id.playButton);
        playButton.setOnClickListener(this);

        btnCapture = (Button) this.findViewById(R.id.btnCapture);
        btnCapture.setOnClickListener(this);

        mPreview = (TextureView) findViewById(R.id.rtspVideo);
        mPreview.setSurfaceTextureListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.playButton:
//                RtspStream(rtspUrl.getEditableText().toString());
                break;
            case R.id.btnCapture:
                if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {

                    getBitmap(mPreview);
                } else {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
                }
//                storeImage(getBitmap());
                break;
        }
    }

    private void storeImage(Bitmap image) {
        File pictureFile = getOutputMediaFile();
        if (pictureFile == null) {
            Log.d(TAG,
                    "Error creating media file, check storage permissions: ");// e.getMessage());
            return;
        }
        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            image.compress(Bitmap.CompressFormat.PNG, 90, fos);
            fos.close();
        } catch (FileNotFoundException e) {
            Log.d(TAG, "File not found: " + e.getMessage());
        } catch (IOException e) {
            Log.d(TAG, "Error accessing file: " + e.getMessage());
        }
    }

    private File getOutputMediaFile() {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory().toString());

        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null;
            }
        }
        // Create a media file name
        String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmm").format(new Date());
        File mediaFile;
        String mImageName = "MI_" + timeStamp + ".jpg";
        mediaFile = new File(mediaStorageDir.getPath() + File.separator + mImageName);
        return mediaFile;
    }


    private void RtspStream(String rtspUrl) {
        if (mPreview.isAvailable()) {

            try {
                mMediaPlayer = new MediaPlayer();
                mMediaPlayer.setDataSource(this, Uri.parse(rtspUrl));
                mMediaPlayer.setSurface(surface);
                mMediaPlayer.setLooping(true);

                // don't forget to call MediaPlayer.prepareAsync() method when you use constructor for
                // creating MediaPlayer
                mMediaPlayer.prepareAsync();
                // Play video when the media source is ready for playback.
                mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mediaPlayer) {
                        mediaPlayer.start();
                    }
                });

            } catch (IllegalArgumentException | SecurityException | IllegalStateException | IOException e) {
                Log.d(TAG, e.getMessage());
            }
        }
    }

    public Bitmap getBitmap() {
        return mPreview.getBitmap();
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
        surface = new Surface(surfaceTexture);
        Log.i(TAG, "onSurfaceTextureAvailable: ");
        Surface s = new Surface(surfaceTexture);

        try {
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setDataSource(rtspUrl.getEditableText().toString());
            mMediaPlayer.setSurface(s);
            mMediaPlayer.prepare();
            mMediaPlayer.setLooping(true);

            mMediaPlayer.setOnBufferingUpdateListener(this);
            mMediaPlayer.setOnCompletionListener(this);
            mMediaPlayer.setOnPreparedListener(this);
            mMediaPlayer.setOnVideoSizeChangedListener(this);

            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    mediaPlayer.start();
                    Log.i(TAG, "onPrepared: 开始播放");
                    Timer timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            Log.i(TAG, "run: 截图");
                            getBitmap(mPreview);
                        }
                    },125,125);
                }
            });

        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mMediaPlayer != null) {
            // Make sure we stop video and release resources when activity is destroyed.
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

    }


    public void getBitmap(TextureView vv) {

        File mediaStorageDir = new File(Environment.getExternalStorageDirectory().toString()+"/pic");

        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return ;
            }
        }
        String ss = System.currentTimeMillis()+"";
        final String mPath = mediaStorageDir
                + "/" + ss + ".png";
        Toast.makeText(getApplicationContext(), "Capturing Screenshot: " + mPath, Toast.LENGTH_SHORT).show();
        if (vv.isAvailable()) {
            Log.i(TAG, "getBitmap: ");

            SurfaceTexture surfaceTexture = vv.getSurfaceTexture();

        }
//         = Bitmap.createBitmap(800, 800, Bitmap.Config.ARGB_8888);
        final Bitmap bm = vv.getBitmap();
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (bm == null)
                    Log.e(TAG, "bitmap is null");

                OutputStream fout = null;
                File imageFile = new File(mPath);

                if (!imageFile.exists())
                    try {
                        imageFile.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                try {
                    fout = new FileOutputStream(imageFile);
                    bm.compress(Bitmap.CompressFormat.PNG, 90, fout);
                    fout.flush();
                    fout.close();
                    Log.i("ganlongtest", "getBitmap: 截取了一张图片:"+mPath);
                } catch (FileNotFoundException e) {
                    Log.e(TAG, "FileNotFoundException");
                    e.printStackTrace();
                } catch (IOException e) {
                    Log.e(TAG, "IOException");
                    e.printStackTrace();
                }
            }
        }).start();

    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {

    }

    @Override
    public void onCompletion(MediaPlayer mp) {

    }

    @Override
    public void onPrepared(MediaPlayer mp) {

    }

    @Override
    public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {

    }

//    private Bitmap getScreenshot() {
//        mPlayerLayout.buildDrawingCache();
//        Bitmap content = mTextureView.getBitmap();
//        Bitmap layout = mPlayerLayout.getDrawingCache();
//        Bitmap screenshot = Bitmap.createBitmap(layout.getWidth(), layout.getHeight(), Bitmap.Config.ARGB_4444);
//        // 把两部分拼起来，先把视频截图绘制到上下左右居中的位置，再把播放器的布局元素绘制上去。
//        Canvas canvas = new Canvas(screenshot);
//        canvas.drawBitmap(content, (layout.getWidth() - content.getWidth()) / 2, (layout.getHeight() - content.getHeight()) / 2, new Paint());
//        canvas.drawBitmap(layout, 0, 0, new Paint());
//        canvas.save();
//        canvas.restore();
//        return screenshot;
//    }
}
