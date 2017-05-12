package com.unifyid.fivesec;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.sql.SQLData;

public class MainActivity extends AppCompatActivity implements PictureCallback {

    public class InitDbHelper extends SQLiteOpenHelper {
        // If you change the database schema, you must increment the database version.
        public static final int DATABASE_VERSION = 1;
        public static final String DATABASE_NAME = "FeedReader.db";

        public InitDbHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(SQL_CREATE_ENTRIES);
        }
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // This database is only a cache for online data, so its upgrade policy is
            // to simply to discard the data and start over
        }
        public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            onUpgrade(db, oldVersion, newVersion);
        }
    }

    private final String TAG = "SHOOT";
    private static final String DATABASE_NAME = "images.db";
    private static final String DB_TABLE = "captured_img";
    private static final String KEY_NAME = "id";
    private static final String KEY_IMAGE = "data";
    private static final String SQL_CREATE_ENTRIES = "CREATE TABLE " + DB_TABLE + "("+
                                                    KEY_NAME + " TEXT," +
                                                    KEY_IMAGE + " BLOB);";

    private SQLiteDatabase db;
    private Integer count = 0;


    private Camera mCamera;
    private Button shootButton;
    private PictureCallback mPicture;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        shootButton = (Button)findViewById(R.id.shoot_button);

        mPicture = this;
        mCamera = getCameraInstance();
        InitDbHelper dbHelper = new InitDbHelper(this);
        db = dbHelper.getWritableDatabase();

        shootButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new CountDownTimer(5000,500){

                    @Override
                    public void onFinish() {

                    }

                    @Override
                    public void onTick(long millisUntilFinished) {
                        mCamera.startPreview();
                        mCamera.takePicture(null, null,mPicture);
                    }

                }.start();
            }
        });
    }

    private Camera getCameraInstance() {
        Camera c = null;
        try {
            c = openFrontFacingCamera();
        }
        catch (Exception e){
        }
        return c;
    }

    private Camera openFrontFacingCamera()
    {
        int cameraCount = 0;
        Camera cam = null;
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        cameraCount = Camera.getNumberOfCameras();
        for ( int camIdx = 0; camIdx < cameraCount; camIdx++ ) {
            Camera.getCameraInfo( camIdx, cameraInfo );
            if ( cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT  ) {
                try {
                    cam = Camera.open( camIdx );
                } catch (RuntimeException e) {
                    Log.e(TAG, "Camera failed to open: " + e.getLocalizedMessage());
                }
            }
        }

        return cam;
    }

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        addEntry(count.toString(),data);
        count++;
    }

    private void addEntry( String name, byte[] image) throws SQLiteException {
        ContentValues cv = new ContentValues();
        cv.put(KEY_NAME,    name);
        cv.put(KEY_IMAGE,   image);
        db.insert( DB_TABLE, null, cv );
    }

}
