package com.example.opencamera;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity
{
    TextView m_addButton;
    private ImageView m_ImageView;

    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main );

        m_addButton = findViewById( R.id.addButton );


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


}