package com.example.linelayout;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.drawable.AnimatedImageDrawable;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private LineLayout line;
    private LineLayoutU line_u;
    private List<String> listData = new ArrayList<>();//线路数据

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sample_line_layout_u);
        line=findViewById(R.id.line);
        line_u=findViewById(R.id.line_u);
        if (line!=null){
            for (int i = 0; i < 6; i++) {
                listData.add("第" + i + "站");
            }
            line.setListData(listData);
        }
        if (line_u!=null){
            for (int i = 0; i < 10; i++) {
                listData.add("第" + i + "站");
            }
            line_u.setListData(listData);
        }
    }
}
