package com.example.opencamera;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import application.SettingPreference;
import android.Manifest;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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
    ImageView m_recordButton;
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

    private ImageView m_ImageView;

    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_page2 );

        Log.d(TAG , "onCreate: " );

        m_cameraButton = findViewById( R.id.cameraButton );

        m_galleryButton = findViewById( R.id.galleryButton );
        m_ImageView = findViewById( R.id.imageView );
        m_title = findViewById( R.id.title );
        m_uploadButton = findViewById( R.id.uploadButton );
        m_recordButton = findViewById( R.id.recordButton );

//        m_uploadButton.setEnabled( false );


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
                // Check if there is a camera app installed on the device
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    // Create a new empty file to save the image
                    File photoFile = null;
                    try {
                        photoFile = createImageFile();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                    if (photoFile != null) {
                        // Get the URI of the file using a FileProvider
                        Uri photoURI = FileProvider.getUriForFile(Page2Activity.this, "com.example.myapp.fileprovider", photoFile);
                        // Set the file URI as the output for the photo
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                        // Launch the camera app
                        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                    }
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
//                if (m_currentPhotoPath == null) {
//                    m_uploadButton.setEnabled(false);
//                    Toast toast = Toast.makeText(getApplicationContext(), "請先選擇照片或拍攝照片", Toast.LENGTH_SHORT);
//                    toast.show();
//                    return;
//                }
                if( m_currentPhotoPath != null){
                    m_uploadButton.setEnabled( true );
                    PhotoList photoList = new PhotoList( m_currentPhotoPath, m_recordFilePath );
                    m_photoList.add( photoList );

                    // 將photoList轉成Json存至SettingPreferences
                    Gson gson = new Gson();
                    String photoListJson = gson.toJson( m_photoList );
                    SettingPreference.getInstance().setSample( photoListJson );
                    Log.d( "Patty:Page2", "createImageFile: " + photoListJson );
                }
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
                    Log.d( "Patty:Page2", "m_recordButton: 錄音中" );
                } else{
                    stopRecording();
                    recordCondition = false;
                }
            }
        } );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CAPTURE) {
                //使用之前儲存的文件來顯示圖片
                //從路徑中讀取圖片
                Bitmap imageBitmap = BitmapFactory.decodeFile(m_currentPhotoPath);
                //在 ImageView 中顯示圖片
                m_ImageView.setImageBitmap(imageBitmap);
                saveImage(imageBitmap);

            } else if (requestCode == REQUEST_IMAGE_PICK) {
                //將選擇的照片轉成Bitmap
                Uri imageUri = data.getData();
                try {
                    Bitmap imageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                    //imageView顯示結果
                    m_ImageView.setImageBitmap(imageBitmap);
                    saveImage(imageBitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //創建一個用於儲存照片的文件(可指定文件的名稱和路徑)
    private File createImageFile() throws IOException {
        // 用時間戳命名文件，避免重複
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        //獲取用於儲存拍攝照片的目錄
        File storageDir = getExternalFilesDir( Environment.DIRECTORY_PICTURES );
        File imageFile = File.createTempFile( //創建臨時文件
                imageFileName,   /* prefix */
                ".jpg",          /* suffix */
                storageDir       /* directory */
        );
        // 保存文件路徑，稍後用於顯示圖片
        m_currentPhotoPath = imageFile.getAbsolutePath();
        Log.d( "Patty:Page2", "createImageFile: " +m_currentPhotoPath );
        return imageFile;

    }

    //將拍攝的照片（即Bitmap對象）保存到createImageFile創建的文件中
    private void saveImage( Bitmap bitmap )
    {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";

        //獲取用於儲存拍攝照片的目錄
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File imageFile = null;
        try {
            storageDir = getExternalFilesDir( Environment.DIRECTORY_PICTURES);
            Log.d( "Patty:Page2", "saveImage_: storageDir" + storageDir );
            imageFile = File.createTempFile(
                    imageFileName,  /* prefix */
                    ".jpg",         /* suffix */
                    storageDir      /* directory */
            );
            OutputStream outputStream = new FileOutputStream(imageFile);
            Log.d( "Patty:Page2", "saveImage_: outputStream" + outputStream );
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            outputStream.flush();
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 保存文件路徑，稍後用於顯示圖片
        m_currentPhotoPath = imageFile.getAbsolutePath();
        Log.d( "Patty:Page2", "saveImage: " +m_currentPhotoPath );
    }

    private void startRecording() {
        // 設定錄音檔案的儲存路徑
        m_recordFilePath = getExternalCacheDir().getAbsolutePath() + "/record_" + System.currentTimeMillis() + ".3gp";
        Log.d( "Patty", "startRecording: " );

        // 檢查錄音權限
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.RECORD_AUDIO }, REQUEST_RECORD_AUDIO_PERMISSION);
        }

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
