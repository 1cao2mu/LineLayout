package com.example.linelayout;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private LineLayoutL line;
    private LineLayoutU line_u;
    private LineLayoutR line_r;
    private List<String> listData = new ArrayList<>();//线路数据

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sample_line_layout_u);
        line=findViewById(R.id.line);
        line_u=findViewById(R.id.line_u);
        line_r=findViewById(R.id.line_r);
        if (line!=null){
            for (int i = 0; i < 8; i++) {
                listData.add("第" + i + "站");
            }
            line.setListData(listData);
            line.setStopNumber(5,0);
        }
        if (line_u!=null){
            for (int i = 0; i < 11; i++) {
                listData.add("第" + i + "站");
            }
            line_u.setListData(listData);
            line_u.setStopNumber(5,0);
        }
        if (line_r!=null){
            for (int i = 0; i < 11; i++) {
                listData.add("第" + i + "站");
            }
            line_r.setListData(listData);
            line_r.setStopNumber(9,1);
        }
    }
}
