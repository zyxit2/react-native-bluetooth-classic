package kjd.reactnative.bluetooth.conn;

import android.bluetooth.BluetoothSocket;
import android.util.Base64;
import android.util.Log;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * Implements {@link DeviceConnection} providing direct write and reading of byte[] data.   When
 * writing data to the device, the byte[] is transferred as is with no extra encoding or
 * manipulation.
 * <p>
 * When data is received, it's written to the input buffer or transferred as is.  At this point
 * there is no concept of a deliminator, if required we can add one in later where we just look
 * for matching bytes.
 *
 * @author kendavidson
 *
 */
public class ByteArrayDeviceConnectionImpl extends AbstractDeviceConnection {

    /**
     * The buffer in which data is stored.
     */
    // private final ByteBuffer mBuffer;
    private byte[] mBuffer;

    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
    
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

    /**
     * Creates a new {@link AbstractDeviceConnection} to the provided NativeDevice, using the provided
     * Properties.
     *
     * @param socket
     * @param properties
     */
    public ByteArrayDeviceConnectionImpl(BluetoothSocket socket, Properties properties) throws IOException {
        super(socket, properties);

        int bufferSize = StandardOption.READ_SIZE.get(mProperties);
        // this.mBuffer = ByteBuffer.allocate(bufferSize);
        this.mBuffer = new byte[bufferSize];
    }

    @Override
    protected void receivedData(byte[] bytes) {
        Log.d(this.getClass().getSimpleName(),
                String.format("Received %d bytes from device %s", bytes.length, getDevice().getAddress()));
        // mBuffer.clear();
        // mBuffer.put(bytes);
        mBuffer = bytes;

        if (mOnDataReceived != null) {
            Log.d(this.getClass().getSimpleName(),
                    "BluetoothEvent.READ listener is registered, providing data");
            mOnDataReceived.accept(getDevice(), read());
        } else {
            Log.d(this.getClass().getSimpleName(),
                    "No BluetoothEvent.READ listeners are registered, storing in buffer");
        }
    }

    /**
     * Provides the number of full messages (delimiters) available within the buffer.
     *
     * @return the number of messages available
     */
    @Override
    public int available() {
        // return mBuffer.array().length;
        return mBuffer.length;
    }

    @Override
    public synchronized boolean clear() {
        // mBuffer.clear();
        mBuffer = new byte[1024];
        return true;
    }

    /**
     * Reads the full ByteBuffer into a Base64 encoded String for transfer back to React Native
     * bridge.
     *
     * @return the next message from the buffer
     * @throws IOException if an error occurs during reading
     */
    @Override
    public String read() {
        // Sending as HEX string
        String message = bytesToHex(mBuffer);
        // String message = new String(mBuffer.array(), StandardCharsets.ISO_8859_1);
        // String message = new String(mBuffer, StandardCharsets.ISO_8859_1);
        clear();

        return message;
    }

}
