package com.example.origami;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class MyActivity extends Activity implements View.OnClickListener {

    private Button searchButton, closeButton;

//    private ViewGroup contentLayout;

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

//        contentLayout = (ViewGroup) this.findViewById(R.id.contentLayout);

        contentView = this.findViewById(R.id.contentView);
        contentView.setVisibility(View.INVISIBLE);

        resultsAnimationView = (ResultsAnimationView) this.findViewById(R.id.resultAnimationView);
        resultsAnimationView.setContentView(this.contentView);
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
