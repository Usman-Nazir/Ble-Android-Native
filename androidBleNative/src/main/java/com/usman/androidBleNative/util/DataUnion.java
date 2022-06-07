package com.usman.androidBleNative.util;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class DataUnion {
    private static final String TAG = "DataUnion";
    private List<Data> mData;
    private int mSelected;
    private byte[] mServiceUuid;
    private int mStartIndex;

    public static final int FORMAT_FLOAT = 52;
    public static final int FORMAT_SFLOAT = 50;
    public static final int FORMAT_SINT16 = 34;
    public static final int FORMAT_SINT32 = 36;
    public static final int FORMAT_SINT8 = 33;
    public static final int FORMAT_UINT16 = 18;
    public static final int FORMAT_UINT32 = 20;
    public static final int FORMAT_UINT8 = 17;
    public float divider;
    public int format;
    public String key;
    public boolean showAsTab;
    public String uri;
    public String value;

    public static class Data implements Parcelable {
        public static final Creator<Data> CREATOR = new Creator<Data>() {
            /* class p139no.nordicsemi.android.mcp.ble.model.DataUnion.Data.C25531 */

            @Override // android.os.Parcelable.Creator
            public Data createFromParcel(Parcel parcel) {
                return new Data(parcel);
            }

            @Override // android.os.Parcelable.Creator
            public Data[] newArray(int i) {
                return new Data[i];
            }
        };
        public static final int FORMAT_FLOAT = 52;
        public static final int FORMAT_SFLOAT = 50;
        public static final int FORMAT_SINT16 = 34;
        public static final int FORMAT_SINT32 = 36;
        public static final int FORMAT_SINT8 = 33;
        public static final int FORMAT_UINT16 = 18;
        public static final int FORMAT_UINT32 = 20;
        public static final int FORMAT_UINT8 = 17;
        public float divider;
        public int format;
        public String key;
        public boolean showAsTab;
        public String uri;
        public String value;

        public int describeContents() {
            return 0;
        }

        public String toString() {
            return this.key + ": " + this.value;
        }

        public void writeToParcel(Parcel parcel, int i) {
            parcel.writeString(this.key);
            parcel.writeString(this.value);
            parcel.writeString(this.uri);
            parcel.writeInt(this.showAsTab ? 1 : 0);
            parcel.writeInt(this.format);
            parcel.writeFloat(this.divider);
        }

        public Data(String str, String str2) {
            this(str, str2, 0, 1.0f);
            this.showAsTab = false;
        }

        public Data(String str, String str2, int i) {
            this(str, str2, i, 1.0f);
        }

        public Data(String str, String str2, int i, float f) {
            this.key = str;
            this.value = str2;
            this.format = i;
            this.divider = f;
            this.showAsTab = true;
        }

        private Data(Parcel parcel) {
            this.key = parcel.readString();
            this.value = parcel.readString();
            this.uri = parcel.readString();
            this.showAsTab = parcel.readInt() != 1 ? false : true;
            this.format = parcel.readInt();
            this.divider = parcel.readFloat();
        }
    }

    private static float bytesToFloat(byte b, byte b2) {
        double unsignedToSigned = (double) unsignedToSigned(unsignedByteToInt(b) + ((unsignedByteToInt(b2) & 15) << 8), 12);
        double pow = Math.pow(10.0d, (double) unsignedToSigned(unsignedByteToInt(b2) >> 4, 4));
        Double.isNaN(unsignedToSigned);
        return (float) (unsignedToSigned * pow);
    }

    private static int getTypeLen(int i) {
        return i & 15;
    }

    private static int unsignedByteToInt(byte b) {
        return b & FlagsParser.UNKNOWN_FLAGS;
    }

    private static int unsignedBytesToInt(byte b, byte b2) {
        return unsignedByteToInt(b) + (unsignedByteToInt(b2) << 8);
    }

    private static int unsignedToSigned(int i, int i2) {
        int i3 = 1 << (i2 - 1);
        return (i & i3) != 0 ? (i3 - (i & (i3 - 1))) * -1 : i;
    }

    public void addData(String str, String str2) {
        this.mData.add(new Data(str, str2));
    }

    public DataUnion clear() {
        this.mSelected = 0;
        this.mServiceUuid = null;
        this.mStartIndex = 0;
        this.mData.clear();
        return this;
    }

    public int describeContents() {
        return 0;
    }

    public Data getData(int i) {
        if (i < this.mData.size()) {
            return this.mData.get(i);
        }
        return this.mData.get(0);
    }

    public String[] getKeys() {
        List<Data> list = this.mData;
        String[] strArr = new String[list.size()];
        for (int i = 0; i < list.size(); i++) {
            strArr[i] = list.get(i).key;
        }
        return strArr;
    }

    public Data getSelectedData() {
        return this.mData.get(getSelectedIndex());
    }

    public int getSelectedIndex() {
        if (this.mData.size() == 0) {
            Log.e(TAG, "Empty data!");
        }
        if (this.mSelected >= this.mData.size()) {
            this.mSelected = 0;
        }
        return this.mSelected;
    }

    public Float getSelectedValueFromPacket(byte[] bArr) {
        byte[] bArr2 = this.mServiceUuid;
        int i = this.mStartIndex;
        int i2 = getSelectedData().format;
        float f = getSelectedData().divider;
        if (bArr2 == null || bArr == null || bArr.length < bArr2.length + i + getTypeLen(i2)) {
            return null;
        }
        for (int i3 = 0; i3 < bArr2.length; i3++) {
            if (bArr2[i3] != bArr[i + i3]) {
                return null;
            }
        }
        int length = i + bArr2.length;
        if (i2 == 17) {
            return Float.valueOf(((float) unsignedByteToInt(bArr[length])) * f);
        }
        if (i2 == 18) {
            return Float.valueOf(((float) unsignedBytesToInt(bArr[length], bArr[length + 1])) * f);
        }
        if (i2 == 20) {
            return Float.valueOf(((float) (((long) unsignedBytesToInt(bArr[length], bArr[length + 1], bArr[length + 2], bArr[length + 3])) & 4294967295L)) * f);
        }
        if (i2 == 36) {
            return Float.valueOf(((float) unsignedToSigned(unsignedBytesToInt(bArr[length], bArr[length + 1], bArr[length + 2], bArr[length + 3]), 32)) * f);
        }
        if (i2 == 50) {
            return Float.valueOf(bytesToFloat(bArr[length], bArr[length + 1]) * f);
        }
        if (i2 == 52) {
            return Float.valueOf(bytesToFloat(bArr[length], bArr[length + 1], bArr[length + 2], bArr[length + 3]) * f);
        }
        if (i2 == 33) {
            return Float.valueOf(((float) unsignedToSigned(unsignedByteToInt(bArr[length]), 8)) * f);
        }
        if (i2 != 34) {
            return null;
        }
        return Float.valueOf(((float) unsignedToSigned(unsignedBytesToInt(bArr[length], bArr[length + 1]), 16)) * f);
    }

    public boolean isMultiple() {
        return this.mData.size() > 1;
    }

    public void setSelected(int i) {
        if (i < this.mData.size()) {
            this.mSelected = i;
        }
    }

    public void setServiceUuid(byte[] bArr, int i, int i2) {
        byte[] bArr2 = this.mServiceUuid;
        if (bArr2 == null || bArr2.length != i2) {
            bArr2 = new byte[i2];
            this.mServiceUuid = bArr2;
        }
        System.arraycopy(bArr, i, bArr2, 0, i2);
        this.mStartIndex = i;
    }

    public int size() {
        return this.mData.size();
    }

    public String toString() {
        return getSelectedData().toString();
    }

    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeList(this.mData);
        parcel.writeInt(this.mSelected);
        parcel.writeByteArray(this.mServiceUuid);
        parcel.writeInt(this.mStartIndex);
    }

    DataUnion() {
        this.mData = new ArrayList();
        this.mSelected = 0;
        this.mServiceUuid = null;
    }

    private static int unsignedBytesToInt(byte b, byte b2, byte b3, byte b4) {
        return unsignedByteToInt(b) + (unsignedByteToInt(b2) << 8) + (unsignedByteToInt(b3) << 16) + (unsignedByteToInt(b4) << 24);
    }

    public void addData(String str, String str2, int i) {
        this.mData.add(new Data(str, str2, i));
    }

    public void addData(String str, String str2, int i, float f) {
        this.mData.add(new Data(str, str2, i, f));
    }

    public void addData(String str, String str2, String str3) {
        Data data = new Data(str, str2);
        data.uri = str3;
        this.mData.add(data);
    }

    private DataUnion(Parcel parcel) {
        ArrayList arrayList = new ArrayList();
        this.mData = arrayList;
        this.mSelected = 0;
        this.mServiceUuid = null;
        parcel.readList(arrayList, Data.class.getClassLoader());
        this.mSelected = parcel.readInt();
        this.mServiceUuid = parcel.createByteArray();
        this.mStartIndex = parcel.readInt();
    }

    private static float bytesToFloat(byte b, byte b2, byte b3, byte b4) {
        double unsignedToSigned = (double) unsignedToSigned(unsignedByteToInt(b) + (unsignedByteToInt(b2) << 8) + (unsignedByteToInt(b3) << 16), 24);
        double pow = Math.pow(10.0d, (double) b4);
        Double.isNaN(unsignedToSigned);
        return (float) (unsignedToSigned * pow);
    }



}
