package com.zhijia.hotelpad;

/**
 * Created by rxnh8 on 2018/5/23.
 */

public class SpinnerItem {
    private String ID = "";
    private String Value = "";

    public SpinnerItem () {
        ID = "";
        Value = "";
    }

    public SpinnerItem (String _ID, String _Value) {
        ID = _ID;
        Value = _Value;
    }

    @Override
    public String toString() {
        return Value;
    }

    public String GetID() {
        return ID;
    }

    public String GetValue() {
        return Value;
    }
}
