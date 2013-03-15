package com.example.origami;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

public class MyActivity extends Activity implements View.OnClickListener {

    private Button searchButton, closeButton;

    private View contentView;

    private ResultsAnimationView resultsAnimationView;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

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
            public void callback() {
                Toast.makeText(getApplicationContext(),"执行自定义动画显示",Toast.LENGTH_SHORT).show();
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
