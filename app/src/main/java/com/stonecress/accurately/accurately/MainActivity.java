package com.stonecress.accurately.accurately;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static java.lang.Math.abs;
import static java.lang.Math.log;
import static java.lang.Math.pow;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    Intent optIntent = null;
    CalcApplication app;
    TextView textExpression;
    TextView textOperation;

    String rawText = "0";
    String keyText = "0";
    String oprText = "";
    String tvmText = "";
    String memText = "";
    HashMap<String, Double> vTVM = new HashMap<String, Double>();
    HashMap<String, Double> vMEM = new HashMap<String, Double>();
    Double leftValue = 0.0;
    Boolean hitClearOnce = false;

    List<String> idTVM, idOPER, idMEM, idIDX;
    DecimalFormat expFormatter, rawFormatter, keyFormatter;

    static int maxDigit = 15;
    static int MAX_ITER = 1000;
    static double TOLERANCE = 1E20;
    static double DELTA = 1E-5;
    static double MULT_APR = 0.01;
    static String sSIGN = "+/−";
    static String sPLUS = "+";
    static String sMINUS = "−";
    static String sMULT = "×";
    static String sDIV = "÷";
    static String sidTVM[] = {"N", "I/Y", "PV", "PMT", "FV"};
    static String sidOPER[] = {sPLUS, sMINUS, sMULT, sDIV};
    static String sidMEM[] = {"STO", "RCL"};
    static String sidIDX[] = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9"};

    private static final int[] BUTTON_IDS = {
            R.id.btnC,
            R.id.btnCPT,
            R.id.btnSTO,
            R.id.btnRCL,
            R.id.btnN,
            R.id.btnIY,
            R.id.btnPV,
            R.id.btnPMT,
            R.id.btnFV,
            R.id.btn7,
            R.id.btn8,
            R.id.btn9,
            R.id.btn4,
            R.id.btn5,
            R.id.btn6,
            R.id.btn1,
            R.id.btn2,
            R.id.btn3,
            R.id.btnDIV,
            R.id.btnMULT,
            R.id.btnMINUS,
            R.id.btnPLUS,
            R.id.btn0,
            R.id.btnDOT,
            R.id.btnSIGN,
            R.id.btnEXE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        app = (CalcApplication) getApplicationContext();

        vTVM.put("N", new Double(0.0));
        vTVM.put("I/Y", new Double(0.0));
        vTVM.put("PV", new Double(0.0));
        vTVM.put("PMT", new Double(0.0));
        vTVM.put("FV", new Double(0.0));

        for (int i = 0; i < sidIDX.length; i++) {
            vMEM.put(sidIDX[i], new Double(0.0));
        }

        idTVM = Arrays.asList(sidTVM);
        idOPER = Arrays.asList(sidOPER);
        idMEM = Arrays.asList(sidMEM);
        idIDX = Arrays.asList(sidIDX);

        expFormatter = new DecimalFormat("#,##0" + dupe('0', app.getFractionDigit()));
        rawFormatter = new DecimalFormat("#,##0" + dupe('#', maxDigit));
        keyFormatter = new DecimalFormat("#0" + dupe('#', maxDigit - 3));

        textExpression = (TextView) findViewById(R.id.textExpression);
        textOperation = (TextView) findViewById(R.id.textOperation);
        textExpression.setText("0");
        textOperation.setText("");

        for (int id : BUTTON_IDS) {
            Button button = (Button) findViewById(id);
            button.setOnClickListener(this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        expFormatter = new DecimalFormat("#,##0" + dupe('0', app.getFractionDigit()));
        textExpression.setText(expFormatter.format(Double.valueOf(rawText)));
    }

    private String dupe(char c, int n) {
        if (n <= 0) return "";

        StringBuffer sb = new StringBuffer(".");
        for (int i = 0; i < n; i++) sb.append(c);
        return sb.toString();
    }

    public void optOnClick(View v) {
        if (null == optIntent) {
            optIntent = new Intent(MainActivity.this, OptionActivity.class);
        }
        startActivity(optIntent);
    }

    public void expressN(Double value) {
        textExpression.setText(expFormatter.format(value));
        keyText = "0";
    }

    public void appendN(String digit) {
        String newText;
        Boolean signChanged = false;

        if (sSIGN.contentEquals(digit)) {
            newText = rawText;
            if (newText.charAt(0) == '-') {
                newText = newText.substring(1);
            } else {
                newText = "-" + newText;
            }
            signChanged = true;
        } else {
            newText = keyText + digit;
        }
        try {
            Double value = Double.valueOf(newText);
            if (signChanged || newText.length() < maxDigit) {
                rawText = newText;
                String expText = rawFormatter.format(value);
                if (rawText.contains(".")) {
                    expText += ".";
                    String text = expText.substring(0, expText.indexOf('.'));
                    text += rawText.substring(rawText.indexOf('.'));
                    textExpression.setText(text);
                } else {
                    if (rawText.charAt(0) == '0' && rawText.length() > 1) {
                        rawText = rawText.substring(1);
                    }
                    textExpression.setText(expText);
                }
                keyText = rawText;
            }
        } catch (NumberFormatException e) {
            // gulp;;
        }
    }

    public void onClick(View view) {
        Button button = (Button) view;
        String bt = button.getText().toString();
        int bid = button.getId();
        if (bid != R.id.btnC) hitClearOnce = false;

        // L State
        if (oprText.length() == 0 && tvmText.length() == 0 && memText.length() == 0) {
            textOperation.setText(oprText);
            if (bid == R.id.btnC) {
                rawText = "0";
                keyText = "0";
                textExpression.setText("0");
                if (hitClearOnce && app.getClearTVM()) {
                    for (String key : vTVM.keySet()) {
                        vTVM.put(key, 0.0);
                    }
                    textOperation.setText("TVM cleared");
                    hitClearOnce = false;
                } else {
                    hitClearOnce = true;
                }
            } else if (bid == R.id.btnEXE) {
                expressN(Double.valueOf(rawText));
            } else if (idTVM.contains(bt)) {
                Double value = Double.valueOf(rawText);
                vTVM.put(bt, value);
                expressN(value);
                textOperation.setText(bt + " is set");
            } else if (idOPER.contains(bt)) {
                textOperation.setText(bt);
                oprText = bt;
                leftValue = Double.valueOf(rawText);
                expressN(leftValue);
            } else if (idMEM.contains(bt)) {
                textOperation.setText(bt);
                memText = bt;
            } else if (bid == R.id.btnCPT) {
                textOperation.setText(bt);
                tvmText = bt;
            } else {
                appendN(bt);
            }
        } else if (oprText.length() > 0) {
            if (memText.contentEquals("RCL")) {
                if (bid == R.id.btnC) {
                    memText = "";
                    textOperation.setText(oprText);
                } else if (idIDX.contains(bt)) {
                    Double value = vMEM.get(bt);
                    rawText = keyFormatter.format(value);
                    expressN(value);
                    memText = "";
                    textOperation.setText(oprText);
                } else if (idTVM.contains(bt)) {
                    Double value = vTVM.get(bt);
                    rawText = keyFormatter.format(value);
                    expressN(value);
                    memText = "";
                    textOperation.setText(oprText);
                }
            } else if (bid == R.id.btnC) {
                oprText = "";
                textOperation.setText("");
            } else if (button.getId() == R.id.btnEXE) {
                Double value = leftValue;
                Double rightValue = Double.valueOf(rawText);
                if (oprText.contentEquals(sPLUS)) value += rightValue;
                else if (oprText.contentEquals(sMINUS)) value -= rightValue;
                else if (oprText.contentEquals(sMULT)) value *= rightValue;
                else if (oprText.contentEquals(sDIV)) value /= rightValue;
                expressN(value);
                oprText = "";
                textOperation.setText("");
                rawText = keyFormatter.format(value);
            } else if (bid == R.id.btnRCL) {
                textOperation.setText(oprText + " " + bt);
                memText = bt;
            } else {
                appendN(bt);
            }
        } else if (memText.length() > 0) {
            if (bid == R.id.btnC) {
                memText = "";
                textOperation.setText(oprText);
            } else if (idIDX.contains(bt)) {
                if (memText.contentEquals("STO")) {
                    Double value = Double.valueOf(rawText);
                    vMEM.put(bt, value);
                    expressN(value);
                    memText = "";
                    textOperation.setText(oprText);
                } else if (memText.contentEquals("RCL")) {
                    Double value = vMEM.get(bt);
                    rawText = keyFormatter.format(value);
                    expressN(value);
                    memText = "";
                    textOperation.setText(oprText);
                }
            } else if (idTVM.contains(bt) && memText.contentEquals("RCL")) {
                Double value = vTVM.get(bt);
                rawText = keyFormatter.format(value);
                expressN(value);
                memText = "";
                textOperation.setText(oprText);
            }
        } else if (tvmText.length() > 0) {
            if (bid == R.id.btnC) {
                tvmText = "";
                textOperation.setText("");
            } else if (idTVM.contains(bt)) {
                Double value = 0.0;
                double vpy = new Double(app.getVpy());
                double vn = vTVM.get("N");
                double vapr = vTVM.get("I/Y") * MULT_APR;
                double vpv = vTVM.get("PV");
                double vpmt = vTVM.get("PMT");
                double vfv = vTVM.get("FV");
                double vi = vapr / vpy;

                if (bid == R.id.btnN) {
                    if (vi == 0.0) {
                        value = -(vpv + vfv) / vpmt;
                    } else {
                        double v1 = vpmt - vfv * vi;
                        double v2 = vpmt + vpv * vi;

                        value = log(v1 / v2) / log(1 + vi);
                    }
                } else if (bid == R.id.btnIY) {
                    double v;
                    if (vpmt == 0.0) {
                        v = pow(-vfv / vpv, 1 / vn) - 1;
                    } else {
                        double vd = vpv + vpmt * vn + vfv;
                        if (vd == 0.0) {
                            v = 0.0;
                        } else {
                            double vsign;
                            if (vd < 0.0) vsign = 1.0;
                            else vsign = -1.0;

                            v = DELTA * vsign;
                            double pvn = pow(1 + v, -vn);
                            double vf = vpv + vpmt * (1 - pvn) / v + vfv * pvn;
                            double testfunc = vd / vf;

                            int iter = 0;

                            while (testfunc < TOLERANCE) {
                                double step1 = pow(1 + v, -vn - 1);
                                double step2 = pow(1 + v, -vn);
                                double slope = vpmt * (vn * step1 / v + (step2 - 1) / v / v) - vfv * vn * step1;
                                v = v - vf / slope;
                                vf = vpv + vpmt * (1 - pow(1 + v, -vn)) / v + vfv * pow(1 + v, -vn);
                                if (iter++ > MAX_ITER) break;
                                testfunc = vd / vf;
                            }
                        }
                    }
                    value = vpy * v / MULT_APR;
                } else if (bid == R.id.btnPV) {
                    if (vi == 0.0) {
                        value = -(vfv + vpmt * vn);
                    } else {
                        value = (vpmt / vi - vfv) * pow(1 + vi, -vn) - vpmt / vi;
                    }
                } else if (bid == R.id.btnPMT) {
                    if (vi == 0.0) {
                        value = -(vpv + vfv) / vn;
                    } else {
                        value = -vi * (vpv + (vpv + vfv) / (pow(1 + vi, vn) - 1));
                    }
                    app.amortSchedule = processAmortSchedule(vn, vapr / MULT_APR, vi, vpv, value, vfv);
                } else if (bid == R.id.btnFV) {
                    if (vi == 0.0) {
                        value = -(vpv + vpmt * vn);
                    } else {
                        value = -(vpmt / vi + vpv) * pow(1 + vi, vn) + vpmt / vi;
                    }
                }
                expressN(value);
                if (!value.isNaN() && !value.isInfinite()) {
                    vTVM.put(bt, value);
                }
                tvmText = "";
                textOperation.setText(bt + " =");
                rawText = keyFormatter.format(value);
            }
        }
    }

    public String processAmortSchedule(double vn, double vapr, double vi, double vpv, double vpmt, double vfv) {
        StringBuffer sb = new StringBuffer("Amortization Schedule from last PMT computation:\n");
        sb.append("N: " + String.format("%.0f", vn) + "\n");
        sb.append("APR: " + expFormatter.format(vapr) + "\n");
        sb.append("PV: " + expFormatter.format(vpv) + "\n");
        sb.append("PMT: " + expFormatter.format(vpmt) + "\n");
        sb.append("FV: " + expFormatter.format(vfv) + "\n");
        sb.append("\n");
        sb.append("Period  Interest  Principal    Balance\n");
        Double prevBalance = -vpv;
        for (int i = 1; i <= (int) vn; i++) {
            double balance = 0.0;
            if (vi == 0) {
                balance = -(vpv + vpmt * (double) i);
            } else {
                balance = -(vpmt / vi + vpv) * pow(1 + vi, (double) i) + vpmt / vi;
            }
            double principal = prevBalance - balance;
            double interest = vpmt - principal;

            sb.append(String.format("%5d ", i));
            sb.append(String.format("%10.0f ", abs(interest)));
            sb.append(String.format("%10.0f ", abs(principal)));
            sb.append(String.format("%10.0f\n", abs(balance)));

            prevBalance = balance;
        }

        return sb.toString();
    }
}
