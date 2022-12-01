package com.chocolate.puzhle2.Utils;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by mahdi on 10/3/15.
 */
public class ParcelableStringArr implements Parcelable {
    private String[] strings;

    public ParcelableStringArr(String[] strings) {
        this.strings = strings;
    }

    protected ParcelableStringArr(Parcel in) {
        strings = in.createStringArray();
    }

    public static final Creator<ParcelableStringArr> CREATOR = new Creator<ParcelableStringArr>() {
        @Override
        public ParcelableStringArr createFromParcel(Parcel in) {
            return new ParcelableStringArr(in);
        }

        @Override
        public ParcelableStringArr[] newArray(int size) {
            return new ParcelableStringArr[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeStringArray(strings);
    }
}
