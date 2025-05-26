package com.example.self7_1_ej;

import android.os.Bundle;

import androidx.annotation.NonNull;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.annotation.NonNull;

public class MainActivity extends AppCompatActivity {

    private EditText edit1;
    private ImageView img1;
    private RelativeLayout baseLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        baseLayout = (RelativeLayout) findViewById(R.id.baseLayout);
        edit1 = (EditText) findViewById(R.id.edit1);
        img1 = (ImageView) findViewById(R.id.img1);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.baseLayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.rotation) {
            String rot = edit1.getText().toString();
            img1.setRotation(Integer.parseInt(rot));
            edit1.setText(null);
            return true;
        } else if (id == R.id.item1) {
            img1.setImageResource(R.drawable.hamberger);
            return true;
        } else if (id == R.id.item2) {
            img1.setImageResource(R.drawable.chicken);
            return true;
        } else if (id == R.id.item3) {
            img1.setImageResource(R.drawable.cake);
            return true;
        }
        return false;
    }

}