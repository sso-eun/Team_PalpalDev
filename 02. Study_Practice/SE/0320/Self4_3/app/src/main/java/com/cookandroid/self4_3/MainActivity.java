package com.cookandroid.self4_3;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    EditText edit1, edit2;
    Button btnAdd, btnSub, btnMul, btnDiv, btnRem, btnClear;
    TextView textResult;
    String num1, num2;
    Double result; // 실수값으로 계산

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("초간단 계산기(수정)_박소은");

        edit1 = (EditText) findViewById(R.id.Edit1);
        edit2 = (EditText) findViewById(R.id.Edit2);

        btnAdd = (Button) findViewById(R.id.BtnAdd);
        btnSub = (Button) findViewById(R.id.BtnSub);
        btnMul = (Button) findViewById(R.id.BtnMul);
        btnDiv = (Button) findViewById(R.id.BtnDiv);
        btnRem = (Button) findViewById(R.id.BtnRem);
        btnClear = (Button) findViewById(R.id.BtnClear);

        textResult = (TextView) findViewById(R.id.TextResult);

        btnAdd.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                num1 = edit1.getText().toString();
                num2 = edit2.getText().toString();
                // num1이나 num2가 비어 있다면
                if (num1.trim().equals("") || num2.trim().equals("")) {
                    Toast.makeText(getApplicationContext(), "입력 값을 확인 해 주세요.", Toast.LENGTH_SHORT).show();
                } else {
                    result = Double.parseDouble(num1)
                            + Double.parseDouble(num2);
                    textResult.setText("계산 결과 : " + result);
                }
            }
        });

        btnSub.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                num1 = edit1.getText().toString();
                num2 = edit2.getText().toString();

                // num1이나 num2가 비어 있다면
                if (num1.trim().equals("") || num2.trim().equals("")) {
                    Toast.makeText(getApplicationContext(), "입력 값을 확인 해 주세요.", Toast.LENGTH_SHORT).show();
                } else {
                    result = Double.parseDouble(num1)
                            - Double.parseDouble(num2);
                    textResult.setText("계산 결과 : " + result);
                }

            }
        });

        btnMul.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                num1 = edit1.getText().toString();
                num2 = edit2.getText().toString();
                // num1이나 num2가 비어 있다면
                if (num1.trim().equals("") || num2.trim().equals("")) {
                    Toast.makeText(getApplicationContext(), "입력 값을 확인 해 주세요.", Toast.LENGTH_SHORT).show();
                } else {
                    result = Double.parseDouble(num1)
                            * Double.parseDouble(num2);
                    textResult.setText("계산 결과 : " + result);
                }

            }
        });

        btnDiv.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                num1 = edit1.getText().toString();
                num2 = edit2.getText().toString();
                // num1이나 num2가 비어 있다면
                if (num1.trim().equals("") || num2.trim().equals("")) {
                    Toast.makeText(getApplicationContext(), "입력 값을 확인 해 주세요", Toast.LENGTH_SHORT).show();
                    edit1.requestFocus();
                } else {
                    // num2가 0이면 나누지 않는다.
                    if (num2.trim().equals("0")) {
                        Toast.makeText(getApplicationContext(), "0으로 나눌 수 없습니다.", Toast.LENGTH_SHORT).show();
                        edit2.requestFocus();
                    } else {
                        result = Double.parseDouble(num1)
                                / Double.parseDouble(num2);
                        result = (int) (result * 10) / 10.0; // 소수점 아래 1자리까지만 출력
                        textResult.setText("계산 결과 : " + result.toString(result));
                    }
                }

            }
        });

        //나머지값 계산하기
        btnRem.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                num1 = edit1.getText().toString();
                num2 = edit2.getText().toString();
                // num1이나 num2가 비어 있다면
                if (num1.trim().equals("") || num2.trim().equals("")) {
                    Toast.makeText(getApplicationContext(), "입력 값을 확인 해 주세요", Toast.LENGTH_SHORT).show();
                    edit1.requestFocus();
                } else {
                    // num2가 0이면 나누지 않는다.
                    if (num2.trim().equals("0")) {
                        Toast.makeText(getApplicationContext(), "0으로 나눌 수 없습니다.", Toast.LENGTH_SHORT).show();
                        edit2.requestFocus();
                    } else {
                        result = Double.parseDouble(num1) % Double.parseDouble(num2);
                        textResult.setText("계산 결과 : " + result);
                    }
                }

            }
        }); // end btnRem

        btnClear.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                edit1.setText("");
                edit2.setText("");
                textResult.setText("계산 결과 : ");
            }
        });// end btnClear

    }// end onCreate

}
