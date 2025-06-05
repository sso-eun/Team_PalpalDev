package com.example.self7_2_ej;

import android.graphics.Color;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    LinearLayout baseLayout;
    Button btn1, btn2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("배경색 바꾸기(컨텍스트 메뉴)");

        baseLayout = findViewById(R.id.baseLayout);
        btn1 = findViewById(R.id.btn1);
        btn2 = findViewById(R.id.btn2);

        registerForContextMenu(btn1);
        registerForContextMenu(btn2);

        // 짧게 눌러도 메뉴 뜨도록 처리
        btn1.setOnClickListener(v -> openContextMenu(v));
        btn2.setOnClickListener(v -> openContextMenu(v));
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        MenuInflater mInflater = getMenuInflater();
        if(v == btn1) {
            menu.setHeaderTitle("배경색 변경");
            mInflater.inflate(R.menu.menu1, menu);
        }
        if(v == btn2) {
            menu.setHeaderTitle("버튼 변경");
            mInflater.inflate(R.menu.menu2, menu);
        }
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.itemRed) {
            baseLayout.setBackgroundColor(Color.RED);
            return true;
        } else if (id == R.id.itemGreen) {
            baseLayout.setBackgroundColor(Color.GREEN);
            return true;
        } else if (id == R.id.itemBlue) {
            baseLayout.setBackgroundColor(Color.BLUE);
            return true;
        } else if (id == R.id.subRotate) {
            btn2.setRotation(45);
            return true;
        } else if (id == R.id.subSize) {
            btn1.setScaleX(2);
            btn2.setScaleX(2);
            return true;
        }

        return super.onContextItemSelected(item);
    }
}