package com.luzianu.io;

import com.luzianu.database.IntDoublePair;
import com.luzianu.database.TimingPoint;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

public class Input {

    public static void writeInt(OutputStream os, int value) throws IOException {
        byte[] bytes = new byte[4];
        bytes[3] = (byte) ((value >> 24) & 0xFF);
        bytes[2] = (byte) ((value >> 16) & 0xFF);
        bytes[1] = (byte) ((value >> 8) & 0xFF);
        bytes[0] = (byte) ((value << 0) & 0xFF);
        os.write(bytes);
    }

    public static void writeString(OutputStream os, String value) throws IOException {
        os.write(11);
        writeUnsignedLeb128(os, value.getBytes(StandardCharsets.UTF_8).length);
        os.write(value.getBytes(StandardCharsets.UTF_8));
    }

    public static void writeUnsignedLeb128(OutputStream os, int value) throws IOException {
        int remaining = value >>> 7;
        while (remaining != 0) {
            os.write(((value & 0x7f) | 0x80));
            value = remaining;
            remaining >>>= 7;
        }
        os.write((value & 0x7f));
    }

    public static int readInt(InputStream is) throws IOException {
        byte[] bytes = new byte[4];
        is.read(bytes);
        return ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).getInt();
    }

    public static String readString(InputStream is) throws IOException {
        // Has three parts; a single byte which will be either 0x00, indicating that the next two parts are not present,
        // or 0x0b (decimal 11), indicating that the next two parts are present.
        // If it is 0x0b, there will then be a ULEB128, representing the byte length of the following string,
        // and then the string itself, encoded in UTF-8.

        switch (is.read()) {
            case 0:
                return "";
            //System.err.println("STRINGISZERO");
            //is.read();
            //is.read();
            case 11:
                int len = readUnsignedLeb128(is);
                byte[] bytes = new byte[len];
                is.read(bytes);
                return new String(bytes, StandardCharsets.UTF_8);
            default:
                throw new IOException("peppy send help");

        }
    }

    public static int readUnsignedLeb128(InputStream is) throws IOException {
        int result = 0;
        int cur;
        int count = 0;
        do {
            cur = is.read() & 0xff;
            result |= (cur & 0x7f) << (count * 7);
            count++;
        } while (((cur & 0x80) == 0x80) && count < 5);
        if ((cur & 0x80) == 0x80) {
            throw new IOException("invalid LEB128 sequence");
        }
        return result;
    }

    public static void skipDateTime(InputStream is) throws IOException {
        is.read(new byte[8]);
    }

    public static boolean readBoolean(InputStream is) throws IOException {
        byte[] bytes = new byte[1];
        is.read(bytes);
        return bytes[0] != 0;
    }

    public static byte readByte(InputStream is) throws IOException {
        byte[] bytes = new byte[1];
        is.read(bytes);
        return ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).get();
    }

    public static short readShort(InputStream is) throws IOException {
        byte[] bytes = new byte[2];
        is.read(bytes);
        return ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).getShort();
    }

    public static long readLong(InputStream is) throws IOException {
        byte[] bytes = new byte[8];
        is.read(bytes);
        return ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).getLong();
    }

    public static float readSingle(InputStream is) throws IOException {
        byte[] bytes = new byte[4];
        is.read(bytes);
        return ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).getFloat();
    }

    public static double readDouble(InputStream is) throws IOException {
        byte[] bytes = new byte[8];
        is.read(bytes);
        return ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).getDouble();
    }

    public static IntDoublePair readIntDoublePair(InputStream is) throws IOException {
        byte[] bytes = new byte[14];
        is.read(bytes);
        return new IntDoublePair(bytes);
    }

    public static TimingPoint readTimingPoint(InputStream is) throws IOException {
        byte[] bytes = new byte[17];
        is.read(bytes);
        return new TimingPoint(bytes);
    }
}
