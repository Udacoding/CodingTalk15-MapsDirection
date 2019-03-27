package com.example.mapskotlin

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.startActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnDirection.onClick {
            startActivity<MapsActivity>(
                "origin" to etAsal.text.toString(),
                "destination" to etTujuan.text.toString()
            )
        }

    }
}
