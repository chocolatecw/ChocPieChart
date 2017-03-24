package com.xysk.chocpiechart;

import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.xysk.library.bean.PieData;
import com.xysk.library.view.ChocPieChart;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ChocPieChart chocPieChart;
    private List<PieData> pieDatas = new ArrayList<PieData>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        chocPieChart = (ChocPieChart) findViewById(R.id.chocpiechart);
        pieDatas.add(new PieData("四测试", 2, 0xFFD15FEE));
        pieDatas.add(new PieData("一测试一测试一测试", 2, Color.YELLOW));
        pieDatas.add(new PieData("二测试", 3, Color.BLUE));
        pieDatas.add(new PieData("三测试", 1, Color.GREEN));
        chocPieChart.setDatas(pieDatas);
//        chocPieChart.setTextColor(Color.WHITE);
        chocPieChart.setBackgroundColor(Color.WHITE);
//        chocPieChart.setBackgroundResource(R.drawable.shadow);
    }
}
