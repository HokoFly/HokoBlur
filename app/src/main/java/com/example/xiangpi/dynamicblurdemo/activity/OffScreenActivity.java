package com.example.xiangpi.dynamicblurdemo.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.example.xiangpi.dynamicblurdemo.R;
import com.example.xiangpi.dynamicblurdemo.opengl.GLRenderer;
import com.example.xiangpi.dynamicblurdemo.opengl.offline.OffScreenBuffer;
import com.example.xiangpi.dynamicblurdemo.opengl.offline.OffScreenRendererImpl;
import com.example.xiangpi.dynamicblurdemo.util.ImageUtils;

public class OffScreenActivity extends AppCompatActivity {

    private Button mButton;

    private GLRenderer mGLRenderer;

    private OffScreenBuffer mOffScreenBuffer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_off_screen);

        mGLRenderer = new OffScreenRendererImpl(this);

        mOffScreenBuffer = new OffScreenBuffer(this);

        mOffScreenBuffer.setRenderer(mGLRenderer);

        mButton = (Button) findViewById(R.id.get_bitmap);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ImageUtils.saveBlurredImage(mOffScreenBuffer.getBitmap());
            }
        });
    }
}
