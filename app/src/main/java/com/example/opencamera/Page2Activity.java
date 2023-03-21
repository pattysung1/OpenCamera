package com.example.opencamera;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Page2Activity extends AppCompatActivity
{
    String TAG = "Patty:P2";
    TextView m_cameraButton;
    TextView m_galleryButton;
    TextView m_title;
    Button m_uploadButton;

    private static final int REQUEST_IMAGE_CAPTURE = 1; // 相機操作
    private static final int REQUEST_IMAGE_PICK = 2; // 圖庫操作

    private static final int PERMISSIONS_REQUEST_CAMERA = 100; //請求權限

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



        //按下cameraButton
        m_cameraButton.setOnClickListener( new View.OnClickListener()
        {
            @Override
            public void onClick( View view )
            {
                // Create a new empty file to save the image
                File photoFile = null;
                try
                {
                    photoFile = createImageFile();
                }
                catch ( IOException ex )
                {
                    // Error occurred while creating the File
                    ex.printStackTrace();
                }

                // Continue only if the File was successfully created
                if ( photoFile != null )
                {
                    // Create the Intent to capture a photo
                    Intent takePictureIntent = new Intent( MediaStore.ACTION_IMAGE_CAPTURE );

                    // Specify the file URI as the output for the photo
                    Uri photoURI = FileProvider.getUriForFile( Page2Activity.this,
                            "com.example.myapp.fileprovider",
                            photoFile );
                    takePictureIntent.putExtra( MediaStore.EXTRA_OUTPUT, photoURI );

                    // Launch the camera app
                    if ( takePictureIntent.resolveActivity( getPackageManager() ) != null )
                    {
                        startActivityForResult( takePictureIntent, REQUEST_IMAGE_CAPTURE );
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

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CAPTURE) {
                Bundle extras = data.getExtras();
                //將拍攝的照片轉成Bitmap，透過"data"獲得
                Bitmap imageBitmap = (Bitmap) extras.get("data");
                //imageView顯示結果
                m_ImageView.setImageBitmap(imageBitmap);
                saveImage(imageBitmap);
            } else if (requestCode == REQUEST_IMAGE_PICK) {
                //將選擇的照片轉成Bitmap
                Uri imageUri = data.getData();
                //imageView顯示結果
                m_ImageView.setImageURI(imageUri);
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";

        File storageDir = getExternalFilesDir( Environment.DIRECTORY_PICTURES);
        File imageFile = File.createTempFile(
                imageFileName,   /* prefix */
                ".jpg",          /* suffix */
                storageDir       /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        String currentPhotoPath = imageFile.getAbsolutePath();
        return imageFile;
    }

    private void saveImage( Bitmap bitmap )
    {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        //        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        try {
            File storageDir = getExternalFilesDir( Environment.DIRECTORY_PICTURES);
            File imageFile = File.createTempFile(
                    imageFileName,  /* prefix */
                    ".jpg",         /* suffix */
                    storageDir      /* directory */
            );
            OutputStream outputStream = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            outputStream.flush();
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}