package io.omg.opticalmessageguide;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect2d;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.tracking.TrackerBoosting;

import java.io.IOException;
import java.util.Observable;
import java.util.Observer;

import io.omg.opticalmessageguide.streamprocessor.Message;
import io.omg.opticalmessageguide.streamprocessor.MessageDecoderStatus;
import io.omg.opticalmessageguide.streamprocessor.OMGDecoder;

import static org.opencv.core.Core.log;
import static org.opencv.core.Core.mean;
import static org.opencv.core.Core.pow;

public class OMGActivity extends AppCompatActivity implements View.OnTouchListener, CameraBridgeViewBase.CvCameraViewListener2, Observer {

    private static final String TAG = "OMGActivity";

    private CameraBridgeViewBase mOpenCvCameraView;
    private Mat currentFrame;
    private OMGDecoder decoder;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                    mOpenCvCameraView.setOnTouchListener(OMGActivity.this);
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

    public OMGActivity() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_omg);


        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            // No explanation needed; request the permission
            // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
            // app-defined int constant. The callback method gets the
            // result of the request.
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.CAMERA)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA},
                        1);
            }
        } else {
            // Permission has already been granted
        }

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.activity_omg);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
        mOpenCvCameraView.enableFpsMeter();
        mOpenCvCameraView.setMaxFrameSize(640, 360);

        try {
            decoder = new OMGDecoder(this);
        } catch (IOException ex) {
            Log.e(TAG, ex.getMessage());
        }
        updateMessage();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
        try {
            decoder.close();
            decoder = null;
        } catch (IOException ex) {
            Log.e(TAG, ex.getMessage());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            if (decoder == null) {
                decoder = new OMGDecoder(this);
            }
        } catch (IOException ex) {
            Log.e(TAG, ex.getMessage());
        }
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
        updateMessage();
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {
        currentFrame = new Mat(height, width, CvType.CV_8UC4);
    }

    public void onCameraViewStopped() {
        currentFrame.release();
    }

    public void showMessage(Message message) {
        Intent intent = new Intent(this, MessageActivity.class);
        intent.putExtra("message", message);
        startActivity(intent);
    }

    private int lastByte = 0;
    private long millis = 0;

    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {

        currentFrame = inputFrame.rgba();
/*
        long nowMillis = System.currentTimeMillis();
        // print status text
        Imgproc.putText(
                currentFrame,                          // Matrix obj of the image
                "FPS: " + 1000 / (nowMillis - millis),          // Text to be added
                new Point(10, 100),               // point
                Core.FONT_HERSHEY_SIMPLEX,      // front face
                1,                               // front scale
                new Scalar(0, 255, 255),             // Scalar object for color
                3                                // Thickness
        );
        millis = nowMillis;
*/

//        Imgproc.putText(
//                currentFrame,                          // Matrix obj of the image
//                decoder.getStatus().name(),          // Text to be added
//                new Point(10, 20),               // point
//                Core.FONT_HERSHEY_DUPLEX,      // front face
//                0.7,                               // front scale
//                new Scalar(139, 210, 239),             // Scalar object for color
//                1                                // Thickness
//        );

//        byte currentByte = processImage(currentFrame);

        int currentByte = fallbackProcess(currentFrame);

        try {
            if (lastByte == currentByte) {
                decoder.getOutputStream().write(currentByte);
            }
        } catch (IOException ex) {
            Log.e(TAG, ex.getMessage());
        }

        lastByte = currentByte;

        return currentFrame;
    }

    public boolean onTouch(View v, MotionEvent event) {

        paused = !paused;

        return false; // don't need subsequent touch events
    }

    @Override
    public void update(Observable o, Object arg) {
        updateMessage();
    }

    public void updateMessage() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (decoder != null) {
                    switch (decoder.getStatus()) {
                        case ERROR:
                            ((TextView) findViewById(R.id.decoderStatus)).setText(R.string.transmissionFailed);
                            break;
                        case WAITING:
                        case INITIALIZED:
                            ((TextView) findViewById(R.id.decoderStatus)).setText(R.string.alignLEDs);
                            break;
                        case STARTED:
                            ((TextView) findViewById(R.id.decoderStatus)).setText(R.string.holdStill);
                            break;
                        case DONE:
                            showMessage(decoder.getMsg());
                            break;
                    }
                }
            }
        });
    }

    public int fallbackProcess(Mat frame) {

        int rectHeight = currentFrame.height() / 20;
        int rectWidth = rectHeight / 2;
        int offX = currentFrame.width() / 2 - rectWidth / 2;
        int offY = rectHeight;
        int gap = rectHeight;

        int currentByte = 0;
        int hgap = 0;

        for (int i = 0, power = 1; i < 8; i++, power *= 2) {


            Mat submat = frame.submat(offY + hgap + rectHeight * i, offY + hgap + rectHeight * (i + 1), offX, offX + rectWidth);

            int value;
            boolean status;

            boolean meanMode = false;

            if (meanMode) {
                Scalar mean = Core.mean(submat);
                value = (int) mean.val[1];
                status = value > 180;
            } else {
                value = 0;
                for (int y = 0; y < submat.rows(); y++) {
                    for (int x = 0; x < submat.cols(); x++) {
                        if (submat.get(y, x)[1] > 180) {
                            value++;
                        }
                    }
                }
                value = value * 255 / (rectWidth * rectHeight);
                status = value > 160;
            }

            if (status) {
                currentByte += power;
            }
/*
            // print status text
            Imgproc.putText(
                    currentFrame,                          // Matrix obj of the image
                    (status ? "1" : "0") + " ("
                            // + ((int) mean.val[0]) + ", " +
                            + (value)
                            // + ", " + ((int) mean.val[2])
                            + ")",          // Text to be added
                    new Point(offX + rectWidth * 2, offY + gap * i + rectHeight * (i + 1)),               // point
                    Core.FONT_HERSHEY_SIMPLEX,      // front face
                    1,                               // front scale
                    new Scalar(0, 0, 255),             // Scalar object for color
                    4                                // Thickness
            );

*/
            // Draw white rectangle
            Imgproc.rectangle(currentFrame, new Point(offX, offY + hgap + rectHeight * i), new Point(offX + rectWidth, offY + hgap + rectHeight * (i + 1)), decoder.getStatus() == MessageDecoderStatus.ERROR ? new Scalar(255, 0, 0) : new Scalar(139, 210, 239), 2);


            hgap += (int) (gap * (1 + Math.abs(0.25 - i / 12.0)));
        }
        return currentByte;
    }

    private boolean paused = true;
    private TrackerBoosting trackerBooster;
    private Rect2d bbox;

    public byte processImage(Mat frame) {

        if (paused && bbox == null) {
            bbox = new Rect2d(53, 21, 58, 234);
            if (trackerBooster != null) {
                trackerBooster.clear();
            }
            trackerBooster = null;
        }

        if (!paused && trackerBooster == null) {
            trackerBooster = TrackerBoosting.create();
            trackerBooster.init(frame, bbox);
        }

        if (!paused) {
            boolean found = trackerBooster.update(frame, bbox);
        }

//        if (found) {
        Point p1 = new Point(bbox.x, bbox.y);
        Point p2 = new Point(bbox.x + bbox.width, bbox.height + bbox.y);

        Point smallTopLeft = new Point(bbox.x, bbox.y + 51);
        Point smallBottomRight = new Point(bbox.x + 12, bbox.y + 151);

        Imgproc.rectangle(frame, p1, p2, new Scalar(255, 0, 0), 2);
        Imgproc.rectangle(frame, smallTopLeft, smallBottomRight, new Scalar(0, 0, 255), 2);

//        }

        return 0x0;

    }

}
