package com.example.origami;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

/**
 * Created with IntelliJ IDEA.
 * User: marshal
 * Date: 13-3-16
 * Time: 下午11:15
 * To change this template use File | Settings | File Templates.
 */
public class MyActivity2 extends Activity implements View.OnClickListener{
    private Button searchButton, closeButton;

    private View contentView;

    private ResultsAnimationView resultsAnimationView;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main1);

        searchButton = (Button) this.findViewById(R.id.searchButton);
        searchButton.setOnClickListener(this);
        closeButton = (Button) this.findViewById(R.id.closeButton);
        closeButton.setOnClickListener(this);

        contentView = this.findViewById(R.id.contentView);
        contentView.setVisibility(View.INVISIBLE);

        resultsAnimationView = (ResultsAnimationView) this.findViewById(R.id.resultAnimationView);
        resultsAnimationView.setContentView(this.contentView);
        resultsAnimationView.setCallback(new ResultsAnimationView.AnimationEndCallback() {
            @Override
            public void callbackForOpened() {
                Toast.makeText(getApplicationContext(), "执行自定义打开后动画显示", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void callbackForClosed() {
                Toast.makeText(getApplicationContext(), "执行自定义关闭后动画显示", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onClick(View view) {
        if (view == searchButton) {
            this.resultsAnimationView.openResults();
        } else {
            this.resultsAnimationView.closeResults();
        }
    }
}
