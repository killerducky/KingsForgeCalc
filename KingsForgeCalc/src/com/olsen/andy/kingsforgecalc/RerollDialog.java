package com.olsen.andy.kingsforgecalc;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class RerollDialog extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        TextView title = new TextView(this);
        title.setTextSize(40);
        title.setText("Pick dice to reroll");
        layout.addView(title);
        for (Integer i=0; i<5; i++) {
            TextView textView = new TextView(this);
            textView.setText(String.format("%d", i));
            textView.setTextSize(20);
            textView.setClickable(true);
            textView.setOnClickListener(new OnClickListener() {
               public void onClick(View view) {
                   TextView tv = (TextView) view;
                   Toast.makeText(getApplicationContext(), "selected" + tv.getText(), Toast.LENGTH_LONG).show();
                   finish();
               }
            });
            layout.addView(textView);
        }
        setContentView(layout);
    }
}
