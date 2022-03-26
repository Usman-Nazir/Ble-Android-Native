package com.usman.androidble.util;

public class Data {
    public static final int FORMAT_FLOAT = 52;
    public static final int FORMAT_SFLOAT = 50;
    public static final int FORMAT_SINT16 = 34;
    public static final int FORMAT_SINT24 = 35;
    public static final int FORMAT_SINT32 = 36;
    public static final int FORMAT_SINT8 = 33;
    public static final int FORMAT_UINT16 = 18;
    public static final int FORMAT_UINT24 = 19;
    public static final int FORMAT_UINT32 = 20;
    public static final int FORMAT_UINT8 = 17;
    private static char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
    protected byte[] mValue;

    private static float bytesToFloat(byte b, byte b2) {
        double unsignedToSigned = (double) unsignedToSigned(unsignedByteToInt(b) + ((unsignedByteToInt(b2) & 15) << 8), 12);
        double pow = Math.pow(10.0d, (double) unsignedToSigned(unsignedByteToInt(b2) >> 4, 4));
        Double.isNaN(unsignedToSigned);
        return (float) (unsignedToSigned * pow);
    }

    public static Data from(String str) {
        return new Data(str.getBytes());
    }

    public static int getTypeLen(int i) {
        return i & 15;
    }

    public static Data opCode(byte b) {
        return new Data(new byte[]{b});
    }

    private static int unsignedByteToInt(byte b) {
        return b & FlagsParser.UNKNOWN_FLAGS;
    }

    private static long unsignedByteToLong(byte b) {
        return ((long) b) & 255;
    }

    private static int unsignedBytesToInt(byte b, byte b2) {
        return unsignedByteToInt(b) + (unsignedByteToInt(b2) << 8);
    }

    private static long unsignedBytesToLong(byte b, byte b2, byte b3, byte b4) {
        return unsignedByteToLong(b) + (unsignedByteToLong(b2) << 8) + (unsignedByteToLong(b3) << 16) + (unsignedByteToLong(b4) << 24);
    }

    private static int unsignedToSigned(int i, int i2) {
        int i3 = 1 << (i2 - 1);
        return (i & i3) != 0 ? (i3 - (i & (i3 - 1))) * -1 : i;
    }

    private static long unsignedToSigned(long j, int i) {
        int i2 = 1 << (i - 1);
        long j2 = (long) i2;
        return (j & j2) != 0 ? (j2 - (j & ((long) (i2 - 1)))) * -1 : j;
    }

    public Byte getByte(int i) {
        if (i + 1 > size()) {
            return null;
        }
        return Byte.valueOf(this.mValue[i]);
    }

    public Float getFloatValue(int i, int i2) {
        if (getTypeLen(i) + i2 > size()) {
            return null;
        }
        if (i == 50) {
            byte[] bArr = this.mValue;
            int i3 = i2 + 1;
            if (bArr[i3] == 7 && bArr[i2] == -2) {
                return Float.valueOf(Float.POSITIVE_INFINITY);
            }
            byte[] bArr2 = this.mValue;
            if (!(bArr2[i3] == 7 && bArr2[i2] == -1)) {
                byte[] bArr3 = this.mValue;
                if (!(bArr3[i3] == 8 && bArr3[i2] == 0)) {
                    byte[] bArr4 = this.mValue;
                    if (!(bArr4[i3] == 8 && bArr4[i2] == 1)) {
                        byte[] bArr5 = this.mValue;
                        if (bArr5[i3] == 8 && bArr5[i2] == 2) {
                            return Float.valueOf(Float.NEGATIVE_INFINITY);
                        }
                        byte[] bArr6 = this.mValue;
                        return Float.valueOf(bytesToFloat(bArr6[i2], bArr6[i3]));
                    }
                }
            }
            return Float.valueOf(Float.NaN);
        } else if (i != 52) {
            return null;
        } else {
            byte[] bArr7 = this.mValue;
            int i4 = i2 + 3;
            if (bArr7[i4] == 0) {
                int i5 = i2 + 2;
                if (bArr7[i5] != Byte.MAX_VALUE || bArr7[i2 + 1] != -1) {
                    byte[] bArr8 = this.mValue;
                    if (bArr8[i5] == Byte.MIN_VALUE && bArr8[i2 + 1] == 0) {
                        if (bArr8[i2] == 0 || bArr8[i2] == 1) {
                            return Float.valueOf(Float.NaN);
                        }
                        if (bArr8[i2] == 2) {
                            return Float.valueOf(Float.NEGATIVE_INFINITY);
                        }
                    }
                } else if (bArr7[i2] == -2) {
                    return Float.valueOf(Float.POSITIVE_INFINITY);
                } else {
                    if (bArr7[i2] == -1) {
                        return Float.valueOf(Float.NaN);
                    }
                }
            }
            byte[] bArr9 = this.mValue;
            return Float.valueOf(bytesToFloat(bArr9[i2], bArr9[i2 + 1], bArr9[i2 + 2], bArr9[i4]));
        }
    }

    public Integer getIntValue(int i, int i2) {
        if (getTypeLen(i) + i2 > size()) {
            return null;
        }
        switch (i) {
            case 17:
                return Integer.valueOf(unsignedByteToInt(this.mValue[i2]));
            case 18:
                byte[] bArr = this.mValue;
                return Integer.valueOf(unsignedBytesToInt(bArr[i2], bArr[i2 + 1]));
            case 19:
                byte[] bArr2 = this.mValue;
                return Integer.valueOf(unsignedBytesToInt(bArr2[i2], bArr2[i2 + 1], bArr2[i2 + 2], (byte) 0));
            case 20:
                byte[] bArr3 = this.mValue;
                return Integer.valueOf(unsignedBytesToInt(bArr3[i2], bArr3[i2 + 1], bArr3[i2 + 2], bArr3[i2 + 3]));
            default:
                switch (i) {
                    case 33:
                        return Integer.valueOf(unsignedToSigned(unsignedByteToInt(this.mValue[i2]), 8));
                    case 34:
                        byte[] bArr4 = this.mValue;
                        return Integer.valueOf(unsignedToSigned(unsignedBytesToInt(bArr4[i2], bArr4[i2 + 1]), 16));
                    case 35:
                        byte[] bArr5 = this.mValue;
                        return Integer.valueOf(unsignedToSigned(unsignedBytesToInt(bArr5[i2], bArr5[i2 + 1], bArr5[i2 + 2], (byte) 0), 24));
                    case 36:
                        byte[] bArr6 = this.mValue;
                        return Integer.valueOf(unsignedToSigned(unsignedBytesToInt(bArr6[i2], bArr6[i2 + 1], bArr6[i2 + 2], bArr6[i2 + 3]), 32));
                    default:
                        return null;
                }
        }
    }

    public Long getLongValue(int i, int i2) {
        if (getTypeLen(i) + i2 > size()) {
            return null;
        }
        if (i == 20) {
            byte[] bArr = this.mValue;
            return Long.valueOf(unsignedBytesToLong(bArr[i2], bArr[i2 + 1], bArr[i2 + 2], bArr[i2 + 3]));
        } else if (i != 36) {
            return null;
        } else {
            byte[] bArr2 = this.mValue;
            return Long.valueOf(unsignedToSigned(unsignedBytesToLong(bArr2[i2], bArr2[i2 + 1], bArr2[i2 + 2], bArr2[i2 + 3]), 32));
        }
    }

    public String getStringValue(int i) {
        byte[] bArr = this.mValue;
        if (bArr == null || i > bArr.length) {
            return null;
        }
        byte[] bArr2 = new byte[(bArr.length - i)];
        int i2 = 0;
        while (true) {
            byte[] bArr3 = this.mValue;
            if (i2 == bArr3.length - i) {
                return new String(bArr2);
            }
            bArr2[i2] = bArr3[i + i2];
            i2++;
        }
    }

    public byte[] getValue() {
        return this.mValue;
    }

    public int size() {
        byte[] bArr = this.mValue;
        if (bArr != null) {
            return bArr.length;
        }
        return 0;
    }

    public String toString() {
        if (size() == 0) {
            return "";
        }
        char[] cArr = new char[((this.mValue.length * 3) - 1)];
        int i = 0;
        while (true) {
            byte[] bArr = this.mValue;
            if (i < bArr.length) {
                int i2 = bArr[i] & FlagsParser.UNKNOWN_FLAGS;
                int i3 = i * 3;
                char[] cArr2 = HEX_ARRAY;
                cArr[i3] = cArr2[i2 >>> 4];
                cArr[i3 + 1] = cArr2[i2 & 15];
                if (i != bArr.length - 1) {
                    cArr[i3 + 2] = '-';
                }
                i++;
            } else {
                return "(0x) " + new String(cArr);
            }
        }
    }

    private static int unsignedBytesToInt(byte b, byte b2, byte b3, byte b4) {
        return unsignedByteToInt(b) + (unsignedByteToInt(b2) << 8) + (unsignedByteToInt(b3) << 16) + (unsignedByteToInt(b4) << 24);
    }

    public Data(byte[] bArr) {
        this.mValue = bArr;
    }

    private static float bytesToFloat(byte b, byte b2, byte b3, byte b4) {
        double unsignedToSigned = (double) unsignedToSigned(unsignedByteToInt(b) + (unsignedByteToInt(b2) << 8) + (unsignedByteToInt(b3) << 16), 24);
        double pow = Math.pow(10.0d, (double) b4);
        Double.isNaN(unsignedToSigned);
        return (float) (unsignedToSigned * pow);
    }

}
