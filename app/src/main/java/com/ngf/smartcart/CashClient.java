package com.ngf.smartcart;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by Roni on 08/04/2015.
 */
public class CashClient {
    private Hashtable<Integer, Integer> mProducts = new Hashtable<>();

    public CashClient() {

    }

    public void clear() {
        mProducts.clear();
    }

    public void add(int id, int count) {
        Integer current = mProducts.get(id);

        if (current == null) {
            current = 0;
        }

        mProducts.put(id, current + count);
    }

    public double send(String host, int port) throws Exception {
        InetAddress addr = InetAddress.getByName(host);

        Socket socket = new Socket(addr, port);

        try {
            OutputStream os = socket.getOutputStream();
            InputStream is = socket.getInputStream();

            writeInt(os, mProducts.size());

            Iterator<Hashtable.Entry<Integer, Integer>> it = mProducts.entrySet().iterator();

            while (it.hasNext()) {
                Hashtable.Entry<Integer, Integer> entry = it.next();

                writeInt(os, entry.getKey());
                writeInt(os, entry.getValue());
            }

            return readDouble(is);
        }
        finally {
            socket.close();
        }
    }

    static void writeInt(OutputStream os, int value) throws IOException {
        byte[] buf = new byte[4];

        buf[0] = (byte)(value & 0xFF);
        buf[1] = (byte)((value >>> 8) & 0xFF);
        buf[2] = (byte)((value >>> 16) & 0xFF);
        buf[3] = (byte)((value >>> 24) & 0xFF);

        os.write(buf);
    }

    static double readDouble(InputStream is) throws IOException {
        byte[] buf = new byte[8];
        int total = 0;

        while (total < 8)
        {
            total += is.read(buf, total, 8 - total);
        }

        // Convert to big endian
        for (int i = 0; i < 4; i++)
        {
            byte temp = buf[i];
            buf[i] = buf[7 - i];
            buf[7 - i] = temp;
        }

        return ByteBuffer.wrap(buf).getDouble();
    }
}
