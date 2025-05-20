package com.example.enter_exit

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val recyclerView = findViewById<RecyclerView>(R.id.activityRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val activityList = listOf(
            ActivityLog("귀가", "04/01", "오후 5:30"),
            ActivityLog("외출", "04/01", "오후 1:03"),
            ActivityLog("귀가", "03/31", "오후 9:30"),
            ActivityLog("외출", "04/01", "오전 11:30")
        )

        recyclerView.adapter = ActivityAdapter(activityList)
    }
}
