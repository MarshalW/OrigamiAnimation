package com.example.origami;

import android.app.Activity;
import android.os.Bundle;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

public class MyActivity extends Activity {

    private RowSwitchAnimationView rowSwitchAnimationView;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.main);

        List<ViewItem> items = new ArrayList<ViewItem>() {
            {
                this.add(new ViewItem(R.layout.title1,R.layout.content1));
                this.add(new ViewItem(R.layout.title2,R.layout.content2));
            }
        };
        rowSwitchAnimationView = new RowSwitchAnimationView(this, 150, 300, items);

        ViewGroup rootLayout=(ViewGroup)this.findViewById(R.id.rootLayout);
        rootLayout.addView(rowSwitchAnimationView);
    }
}
