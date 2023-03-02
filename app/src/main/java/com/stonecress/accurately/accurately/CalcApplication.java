package com.stonecress.accurately.accurately;

import android.app.Application;

/**
 * Created by changgull on 4/4/17.
 */

public class CalcApplication extends Application {
    int vpyArray[] = {12, 4, 2, 1};
    int fractionDigitArray[] = {0, 1, 2, 3, 4, 5};

    int vpyIndex = 0;
    int fractionDigitIndex = 2;
    Boolean clearTVM = false;
    String amortSchedule = "Compute PMT before viewing amortization schedule";

    public int getVpy() {
        return vpyArray[vpyIndex];
    }

    public int getVpyIndex() {
        return vpyIndex;
    }

    public void setVpyIndex(int vpyIndex) {
        this.vpyIndex = vpyIndex;
    }

    public int getFractionDigit() {
        return fractionDigitArray[fractionDigitIndex];
    }

    public int getFractionDigitIndex() {
        return fractionDigitIndex;
    }

    public void setFractionDigitIndex(int fractionDigitIndex) {
        this.fractionDigitIndex = fractionDigitIndex;
    }

    public void setClearTVM(Boolean clear) {
        clearTVM = clear;
    }

    public Boolean getClearTVM() {
        return clearTVM;
    }

    public void setAmortSchedule(String amortS) {
        amortSchedule = amortS;
    }

    public String getAmortSchedule() {
        return amortSchedule;
    }
}
