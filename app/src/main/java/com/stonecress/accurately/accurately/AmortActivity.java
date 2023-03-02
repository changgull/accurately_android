package com.stonecress.accurately.accurately;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.TextView;

public class AmortActivity extends AppCompatActivity {
    CalcApplication app = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_amort);
        app = (CalcApplication) getApplicationContext();

        TextView textAmortSchedule = (TextView) findViewById(R.id.textAmortSchedule);
        textAmortSchedule.setText(app.getAmortSchedule());
        textAmortSchedule.setMovementMethod(new ScrollingMovementMethod());
    }

    public void closeOnClick(View v) {
        finish();
    }

    public void copyOnClick(View v) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        ClipData data = ClipData.newPlainText("Copied Text", app.getAmortSchedule());
        clipboard.setPrimaryClip(data);
    }
}
