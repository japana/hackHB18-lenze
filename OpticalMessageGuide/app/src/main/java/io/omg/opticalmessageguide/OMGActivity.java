package io.omg.opticalmessageguide;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
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

public class OMGActivity extends AppCompatActivity implements View.OnTouchListener, CameraBridgeViewBase.CvCameraViewListener2 {

    private static final String TAG = "OMGActivity";

    private CameraBridgeViewBase mOpenCvCameraView;
    private Mat currentFrame;

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

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.activity_omg);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
        mOpenCvCameraView.enableFpsMeter();
        mOpenCvCameraView.setMaxFrameSize(640, 360);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
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

    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {

        currentFrame = inputFrame.rgba();

        processImage(currentFrame);

//        int rectHeight = currentFrame.height() / 24;
//        int rectWidth = rectHeight / 2;
//        int offX = currentFrame.width() / 2 - rectWidth / 2;
//        int offY = rectHeight;
//        int gap = rectHeight / 2;
//
//        for (int i = 0; i < 8; i++) {
//
//            // Compute average color
//            Mat mask = new Mat(currentFrame.height(), currentFrame.width(), CvType.CV_8UC1);
//            Imgproc.rectangle(mask, new Point(offX, offY+gap*i+rectHeight*i), new Point(offX+rectWidth, offY+gap*i+rectHeight*(i+1)), new Scalar(255), Core.FILLED);
//            Scalar mean = Core.mean(currentFrame, mask);
//
//            // print status text
//            Imgproc.putText (
//                    currentFrame,                          // Matrix obj of the image
//                    (mean.val[1] > 140 ? "1" : "0") + " (" +((int)mean.val[0]) + ", " + ((int)mean.val[1]) + ", " + ((int)mean.val[2]) + ")",          // Text to be added
//                    new Point(offX+rectWidth*2, offY+gap*i+rectHeight*(i+1)),               // point
//                    Core.FONT_HERSHEY_SIMPLEX ,      // front face
//                    1,                               // front scale
//                    new Scalar(0, 0, 255),             // Scalar object for color
//                    4                                // Thickness
//            );
//            mask.release();
//
//            // Draw red rectangle
//            Imgproc.rectangle(currentFrame, new Point(offX, offY+gap*i+rectHeight*i), new Point(offX+rectWidth, offY+gap*i+rectHeight*(i+1)), new Scalar(255, 0, 0), 4);
//        }


        return currentFrame;
    }

    public boolean onTouch(View v, MotionEvent event) {

        paused = !paused;

        return false; // don't need subsequent touch events
    }

    private boolean paused = true;
    private TrackerBoosting trackerBooster;
    private Rect2d bbox;

    public byte processImage(Mat frame) {

        if (paused) {
            bbox = new Rect2d(53, 21, 58, 234);
        } else if (trackerBooster == null ) {
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
