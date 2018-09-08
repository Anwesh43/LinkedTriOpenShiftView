package com.anwesh.uiprojects.linkedtriopenshiftview

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.anwesh.uiprojects.triopenshiftview.TriOpenShiftView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        TriOpenShiftView.create(this)
    }
}
