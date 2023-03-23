package com.example.opencamera;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import application.SettingPreference;

public class MainActivity extends AppCompatActivity
{
    TextView m_addButton;
    private String m_receiveJson; //接收第二頁的PhotoList 儲存的資料
    private List<PhotoList> m_receivePhotoList = new ArrayList<>();
    private RecyclerView m_recyclerView;
    private RecyclerView.LayoutManager m_LayoutManager;
    private MyAdapter m_adapter;

    private MediaPlayer m_mediaPlayer;
    private String m_recordFilePath;
    boolean recordCondition = false;
    private static final int REQUEST_IMAGE_INTENT = 999; // 把選取到的照片傳到第二頁

    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main );

        m_addButton = findViewById( R.id.addButton );

        //用setting preference 接收
        m_receiveJson = SettingPreference.getInstance().getSample();
        Gson gson = new Gson();
        Type type = new TypeToken<List<PhotoList>>(){}.getType();
        m_receivePhotoList = new ArrayList<>(gson.fromJson(m_receiveJson,type));




        // 1.獲取 RecyclerView 的引用
        m_recyclerView = findViewById(R.id.recyclerView);
        // 2.Set LinearLayout Manager
        m_LayoutManager = new GridLayoutManager( this,2,RecyclerView.VERTICAL,false );
        m_recyclerView.setLayoutManager(m_LayoutManager);

        // 4.關鍵性的工作：要讓recyclerView把調變器整合上來
        m_adapter = new MyAdapter();
        m_recyclerView.setAdapter(m_adapter);

        m_addButton.setOnClickListener( new View.OnClickListener()
        {
            @Override
            public void onClick( View view )
            {
                Intent intent = new Intent(MainActivity.this, Page2Activity.class);
                startActivity(intent);
            }
        } );
    }
    // 3.Set Adapter-維護性
    // 參數記得傳回值
    private class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder>{
        class MyViewHolder extends RecyclerView.ViewHolder{
            public View itemView;
            public ImageButton playButton, deleteButton;
            public ImageView picture;

            public MyViewHolder(View view){
                super(view);
                itemView = view;

                picture = itemView.findViewById(R.id.item_picture);
                playButton = itemView.findViewById(R.id.item_play);
                deleteButton = itemView.findViewById(R.id.item_delete);
            }
        }
        @NonNull
        @Override
        public MyAdapter.MyViewHolder onCreateViewHolder( @NonNull ViewGroup parent, int viewType) {
            //產生的view，介紹給ViewHolder作為呈現
            View itemView = LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.item,parent,false);
            return new MyViewHolder(itemView);
        }
        @Override
        public void onBindViewHolder(@NonNull MyAdapter.MyViewHolder holder, int position) {
            //從m_receivePhotoList獲得數據(圖片的地址)
            String imagePath = m_receivePhotoList.get(position).getPhoto();
            // 调用loadImageFromStorage方法来加圖像
            Bitmap image = loadImageFromStorage(imagePath);
            // 設置圖像到ImageView中
            holder.picture.setImageBitmap(image);
            //從m_receivePhotoList獲得數據(錄音的地址)

            //點選圖片，進行編輯或新增錄音
            holder.picture.setOnClickListener( new View.OnClickListener()
            {
                @Override
                public void onClick( View view )
                {
                    Intent intent = new Intent(MainActivity.this, Page2Activity.class);
                    intent.putExtra("photoPath", imagePath);
                    Log.d( "Patty", "holder.picture: "  + imagePath);
                    startActivity(intent);
                }
            } );

            //播放錄音
            holder.playButton.setOnClickListener( new View.OnClickListener()
            {
                @Override
                public void onClick( View view )
                {
                    if ( !recordCondition ){
                        //取得錄音的位置
                        m_recordFilePath = m_receivePhotoList.get(position).getRecord();
                        playRecording();
                        recordCondition = true;
                        holder.playButton.setImageResource( R.drawable.stop2 );
                        Log.d( "Patty:Page", "m_recordButton: 播放中" );
                    } else{
                        stopPlaying();
                        recordCondition = false;
                        holder.playButton.setImageResource( R.drawable.play );
                    }
                }
            } );
            //刪除資料
            holder.deleteButton.setOnClickListener( new View.OnClickListener()
            {
                @Override
                public void onClick( View view )
                {
                    m_receivePhotoList.remove( holder.getAdapterPosition() );
                    Log.d( "Patty", "deleteButtonOnClick: 123" );
                    m_adapter.notifyDataSetChanged();

                    // 將photoList轉成Json存至SettingPreferences
                    Gson gson = new Gson();
                    String photoListJson = gson.toJson( m_receivePhotoList );
                    SettingPreference.getInstance().setSample( photoListJson );
                    Log.d( "Patty:Page2", "createImageFile: " + photoListJson );

                }
            } );

            }

        @Override
        public int getItemCount()
        {
            return m_receivePhotoList.size();
        }
    }
    //讀取圖像方法
    private Bitmap loadImageFromStorage(String path) {
        try {
            File f = new File(path);
            Bitmap b = BitmapFactory.decodeStream(new FileInputStream(f));
            return b;
        } catch ( FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
    // 播放錄音
    private void playRecording() {
        m_mediaPlayer = new MediaPlayer();
        try {
            m_mediaPlayer.setDataSource( m_recordFilePath );
            m_mediaPlayer.prepare();
            m_mediaPlayer.start();
        } catch ( IOException e) {
            e.printStackTrace();
        }
    }

    // 停止播放錄音
    private void stopPlaying() {
        if ( m_mediaPlayer != null) {
            m_mediaPlayer.release();
            m_mediaPlayer = null;
        }
    }

    @Override
    protected void onResume(){
        //用setting preference 接收
        m_receiveJson = SettingPreference.getInstance().getSample();
        Gson gson = new Gson();
        Type type = new TypeToken<List<PhotoList>>(){}.getType();
        m_receivePhotoList = new ArrayList<>(gson.fromJson(m_receiveJson,type));

        m_adapter.notifyDataSetChanged();
        super.onResume();

    }


}
