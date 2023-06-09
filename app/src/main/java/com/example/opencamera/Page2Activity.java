package com.example.opencamera;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import application.SettingPreference;
import android.Manifest;

import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Page2Activity extends AppCompatActivity
{
    String TAG = "Patty:P2";
    TextView m_cameraButton;
    TextView m_galleryButton;
    TextView m_title;
    Button m_uploadButton;
    Button m_updateButton;
    ImageView m_recordButton;
    int count = 0; //設定index初始值
    int continueCount; //紀錄count的數值
    boolean recordCondition = false;
    private String m_receiveJson2; //接收第二頁的PhotoList 儲存的資料
    private String m_currentPhotoPath; //照片文件路徑
    private List<PhotoList> m_photoList = new ArrayList<>();

    private static final int REQUEST_IMAGE_CAPTURE = 1; // 相機操作
    private static final int REQUEST_IMAGE_PICK = 2; // 圖庫操作
    private static final int PERMISSIONS_REQUEST_CAMERA = 100; //請求權限
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200; //請求權限

    private MediaRecorder m_Recorder;
    private String m_recordFilePath; //錄音路徑
    private String m_selectedPhotoPath; //第一頁選擇的要編輯照片的路徑
    private int m_selectedPhotoIndex; //第一頁選擇的要編輯照片的index
    private ImageView m_imageView;

    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_page2 );

        Log.d(TAG , "onCreate: " );

        m_cameraButton = findViewById( R.id.cameraButton );

        m_galleryButton = findViewById( R.id.galleryButton );
        m_imageView = findViewById( R.id.imageView );
        m_title = findViewById( R.id.title );
        m_uploadButton = findViewById( R.id.uploadButton );
        m_updateButton = findViewById( R.id.updateButton );
        m_recordButton = findViewById( R.id.recordButton );

        // 檢查錄音權限
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.RECORD_AUDIO }, REQUEST_RECORD_AUDIO_PERMISSION);
        }

        //用setting preference 再接收
        m_receiveJson2 = SettingPreference.getInstance().getSample();
        Gson gson = new Gson();
        Type type = new TypeToken<List<PhotoList>>(){}.getType();
        m_photoList = new ArrayList<>(gson.fromJson(m_receiveJson2,type));

        //按下cameraButton
        m_cameraButton.setOnClickListener( new View.OnClickListener()
        {
            @Override
            public void onClick( View view )
            {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                // 檢查是否有安裝相機程式
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    ContentValues values = new ContentValues();
                    values.put(MediaStore.Images.Media.DISPLAY_NAME, "JPEG_" + System.currentTimeMillis() + ".jpg");
                    values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
                    values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_DCIM + "/Camera");
                    Uri photoUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                    Log.d( "Patty", "onClick: " +photoUri );
                    m_currentPhotoPath = photoUri.toString();
                    Log.d( "Patty", "onClick:m_currentPhotoPath " +m_currentPhotoPath );
                    // 將文件的URI設置為照片的輸出
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                    // 開啟相機
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                    }
                }
        } );
        //按下galleryButton
        m_galleryButton.setOnClickListener( new View.OnClickListener()
        {
            @Override
            public void onClick( View view )
            {
                Intent pickPhotoIntent = new Intent( Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI );
                if ( pickPhotoIntent.resolveActivity( getPackageManager() ) != null )
                {
                    startActivityForResult( pickPhotoIntent, REQUEST_IMAGE_PICK );
                }
            }
        } );
        //上傳檔案
        m_uploadButton.setOnClickListener( new View.OnClickListener()
        {
            @Override
            public void onClick( View view )
            {
                Log.d( "Patty:Page2", "m_uploadButton: " + m_currentPhotoPath );
                if( m_currentPhotoPath != null){
                    count = SettingPreference.getInstance().getSample2();
                    Log.d( "Patty:Page2", "getSample2: " + count );
                    PhotoList photoList;
                    if(m_recordFilePath !=null){
                        photoList = new PhotoList( count, m_currentPhotoPath, m_recordFilePath );
                    } else{
                        photoList = new PhotoList( count, m_currentPhotoPath, null );
                    }
                    count++;
                    continueCount = count;
                    m_photoList.add( photoList );

                    // 將count轉成Json存至SettingPreferences
                    SettingPreference.getInstance().setSample2( continueCount );

                    // 將photoList轉成Json存至SettingPreferences
                    Gson gson = new Gson();
                    String photoListJson = gson.toJson( m_photoList );
                    SettingPreference.getInstance().setSample( photoListJson );
                    Log.d( "Patty:Page2", "createImageFile: " + photoListJson );

                    // 跳回第一頁
                    Intent intent = new Intent(Page2Activity.this, MainActivity.class);
                    startActivity(intent);
                }
                m_recordFilePath = null;
            }
        } );
        m_updateButton.setOnClickListener( new View.OnClickListener()
        {
            @Override
            public void onClick( View view )
            {
                Log.d( "Patty", "m_currentPhotoPath: " + m_currentPhotoPath );
                //找到當初那筆資料(從index來找地址)
                for ( int i = 0; i < m_photoList.size(); i++ )
                {
                    if ( m_selectedPhotoIndex == m_photoList.get( i ).getIndex() )
                    {
                        Log.d( "Patty:Page2", "m_selectedPhotoIndex:" + m_selectedPhotoIndex );
                        m_photoList.get( i ).setRecord( m_recordFilePath );
                        if ( m_currentPhotoPath == null )
                        {
                            m_currentPhotoPath = m_selectedPhotoPath;
                        }
                        m_photoList.get( i ).setPhoto( m_currentPhotoPath );
                        break; // 找到符合資料後結束迴圈
                    }
                }
                // 將photoList轉成Json存至SettingPreferences
                Gson gson = new Gson();
                String photoListJson = gson.toJson( m_photoList );
                SettingPreference.getInstance().setSample( photoListJson );
                Log.d( "Patty:Page2", "createImageFile: " + photoListJson );

                // 跳回第一頁
                Intent intent = new Intent(Page2Activity.this, MainActivity.class);
                startActivity(intent);
            }
        } );
        //按下錄音
        m_recordButton.setOnClickListener( new View.OnClickListener()
        {
            @Override
            public void onClick( View view )
            {
                if ( !recordCondition ){
                    startRecording();
                    recordCondition = true;
                    m_recordButton.setImageResource( R.drawable.stop );
                    Log.d( "Patty:Page2", "m_recordButton: 錄音中" );
                } else{
                    stopRecording();
                    recordCondition = false;
                    m_recordButton.setImageResource( R.drawable.record );
                }
            }
        } );

        //接收第一頁圖以進行編輯
        Intent intent = getIntent();
        m_selectedPhotoIndex = intent.getIntExtra("photoIndex", -1);
        Log.d( "Patty:Page2", "onActivityResult: 第一頁傳過來的照片index:" + m_selectedPhotoIndex );

        //找到當初那筆資料(從index來找地址)
        for(int i =0 ; i< m_photoList.size() ; i++){
            if( m_selectedPhotoIndex == m_photoList.get( i ).getIndex() ){
                Log.d( "Patty:Page2", "m_currentPhotoPath:"  +m_selectedPhotoIndex );
                m_selectedPhotoPath = m_photoList.get(i).getPhoto();
                break; // 找到符合資料後結束迴圈
            }
        }
        if( m_selectedPhotoPath != null){
            Bitmap imageBitmap = BitmapFactory.decodeFile( m_selectedPhotoPath );
            Log.d( "Patty:Page2", "result: 第一頁傳過來的照片path" + m_selectedPhotoPath );
            //在 ImageView 中顯示圖片
            Glide.with(this)
                    .load(m_selectedPhotoPath)
                    .into(m_imageView);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CAPTURE) {
                Log.d( "Patty", "onActivityResult: " + "123");
                Glide.with(this)
                        .load(m_currentPhotoPath)
                        .into(m_imageView);
            } else if (requestCode == REQUEST_IMAGE_PICK) {
                //將選擇的照片轉成Bitmap
                Uri imageUri = data.getData();
                try {
                    InputStream inputStream = getContentResolver().openInputStream(imageUri);
                    ExifInterface exif = new ExifInterface(inputStream);
                    int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
                    Matrix matrix = new Matrix();
                    switch (orientation) {
                        case ExifInterface.ORIENTATION_ROTATE_90:
                            matrix.postRotate(90);
                            break;
                        case ExifInterface.ORIENTATION_ROTATE_180:
                            matrix.postRotate(180);
                            break;
                        case ExifInterface.ORIENTATION_ROTATE_270:
                            matrix.postRotate(270);
                            break;
                        default:
                            break;
                    }
                    Bitmap imageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                    imageBitmap = Bitmap.createBitmap(imageBitmap, 0, 0, imageBitmap.getWidth(), imageBitmap.getHeight(), matrix, true);
                    //imageView顯示結果
                    Glide.with(this)
                            .load(imageBitmap)
                            .into(m_imageView);
                    // 保存图片
                    saveImage(imageBitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("currentPhotoPath", m_currentPhotoPath);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        m_currentPhotoPath = savedInstanceState.getString("currentPhotoPath");
        // 将照片显示到 ImageView 上
        Glide.with(this).load(m_currentPhotoPath).into(m_imageView);
    }
    private void saveImage(Bitmap bitmap) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, "JPEG_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".jpg");
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_DCIM + "/Camera");

        Uri imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        Log.d("Patty:Page2", "saveImage: " + imageUri);

        try {
            OutputStream outputStream = getContentResolver().openOutputStream(imageUri);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 保存文件路徑，稍後用於顯示圖片
        m_currentPhotoPath = imageUri.toString();
        Log.d("Patty:Page2", "saveImage: " + m_currentPhotoPath);
    }
    private void startRecording() {
        // 設定錄音檔案的儲存路徑
        m_recordFilePath = getExternalCacheDir().getAbsolutePath() + "/record_" + System.currentTimeMillis() + ".3gp";
        Log.d( "Patty", "startRecording: " +m_recordFilePath );
        // 初始化MediaRecorder物件
        m_Recorder = new MediaRecorder();
        // 設定音源來自麥克風
        m_Recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        // 設定輸出格式
        m_Recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        // 設定編碼格式
        m_Recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        // 設定錄音檔案的儲存路徑
        m_Recorder.setOutputFile( m_recordFilePath );

        try {
            // 準備MediaRecorder物件
            m_Recorder.prepare();
            // 開始錄音
            m_Recorder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void stopRecording() {
        Log.d( "Patty", "stopRecording: " );
        if ( m_Recorder != null) {
            // 停止錄音
            m_Recorder.stop();
            // 釋放MediaRecorder物件
            m_Recorder.release();
            m_Recorder = null;
        }
    }
}
