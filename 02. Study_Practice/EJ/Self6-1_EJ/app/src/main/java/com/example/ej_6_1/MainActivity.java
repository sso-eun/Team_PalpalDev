package com.example.ej_6_1;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.TimePicker;

public class MainActivity extends AppCompatActivity {

    Chronometer chrono;
    Button btnStart, btnEnd;
    CalendarView calView;
    TimePicker tPicker;
    TextView tvResult;

    int selectYear, selectMonth, selectDay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("시간 예약");

        // 위젯 연결
        btnStart = findViewById(R.id.btnStart);
        btnEnd = findViewById(R.id.btnEnd);
        chrono = findViewById(R.id.chronometer1);
        calView = findViewById(R.id.calendarView1);
        tPicker = findViewById(R.id.timePicker1);
        tvResult = findViewById(R.id.tvResult);

        // 초기 설정
        tPicker.setVisibility(View.VISIBLE);
        calView.setVisibility(View.VISIBLE);

        calView.setDate(System.currentTimeMillis());

        // 날짜 선택 리스너
        calView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {
                selectYear = year;
                selectMonth = month + 1;
                selectDay = dayOfMonth;
            }
        });


        // 타이머 시작 버튼
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chrono.setBase(SystemClock.elapsedRealtime());
                chrono.start();
                chrono.setTextColor(Color.RED);
            }
        });

        // 타이머 종료 버튼
        btnEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chrono.stop();
                chrono.setTextColor(Color.BLUE);

                int hour = tPicker.getHour();
                int minute = tPicker.getMinute();

                String result = selectYear + "년 " + selectMonth + "월 " + selectDay + "일 "
                        + hour + "시 " + minute + "분 예약됨";
                tvResult.setText(result);
            }
        });
    }
}
