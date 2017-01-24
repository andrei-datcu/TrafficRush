package ro.lupii.trafficrush.objects;

import java.io.ByteArrayOutputStream;

/**
 * Created by andrei on 1/24/17.
 */

final class ExposedByteArrayOutputStream extends ByteArrayOutputStream {

    public byte[] getBackingArray() {
        return buf;
    }
}
