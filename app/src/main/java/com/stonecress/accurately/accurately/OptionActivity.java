package com.stonecress.accurately.accurately;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Spinner;

public class OptionActivity extends AppCompatActivity {
    CalcApplication app = null;
    Spinner spinPeriodsPerYear = null;
    Spinner spinFractionDigits = null;
    CheckBox checkClearTVM = null;
    Intent amortIntent = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_option);

        app = (CalcApplication) getApplicationContext();

        spinPeriodsPerYear = (Spinner) findViewById(R.id.spinPeriodsPerYear);
        spinFractionDigits = (Spinner) findViewById(R.id.spinFractionDigits);
        checkClearTVM = (CheckBox) findViewById(R.id.checkBox);

        spinPeriodsPerYear.setSelection(app.getVpyIndex());
        spinFractionDigits.setSelection(app.getFractionDigitIndex());
        checkClearTVM.setChecked(app.getClearTVM());
    }

    public void saveOnClick(View v) {
        app.setVpyIndex(spinPeriodsPerYear.getSelectedItemPosition());
        app.setFractionDigitIndex(spinFractionDigits.getSelectedItemPosition());
        app.setClearTVM(checkClearTVM.isChecked());

        finish();
    }

    public void amortOnClick(View v) {
        if (null == amortIntent) {
            amortIntent = new Intent(OptionActivity.this, AmortActivity.class);
        }
        startActivity(amortIntent);
    }

    public void readManualOnClick(View v) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.stonecress.com/accurately"));
        startActivity(browserIntent);
    }
}
