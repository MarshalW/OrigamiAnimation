package com.example.origami;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

/**
 * Created with IntelliJ IDEA.
 * User: marshal
 * Date: 13-3-16
 * Time: 下午11:04
 * To change this template use File | Settings | File Templates.
 */
public class MainActivity extends Activity implements View.OnClickListener {

    private Button button1, button2;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.root);

        button1=(Button)this.findViewById(R.id.button1);
        button1.setOnClickListener(this);
        button2=(Button)this.findViewById(R.id.button2);
        button2.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        Intent intent = new Intent();

        if(view==button1){
            intent.setClass(MainActivity.this, MyActivity2.class);
        }

        if(view==button2){
            intent.setClass(MainActivity.this, MyActivity.class);
        }
        startActivity(intent);
    }
}
