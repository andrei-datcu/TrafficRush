package ro.lupii.trafficrush.objects;

import com.google.common.io.LittleEndianDataOutputStream;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by andrei on 1/24/17.
 */


// TODO ideally this should encapsulate LiitleEndianDataOutputStream or DataOutputStream
// depending on the platform's endianness
final class ExposedDataOutputStream extends LittleEndianDataOutputStream {

    private ExposedByteArrayOutputStream m_wrappedStream;

    public ExposedDataOutputStream(ExposedByteArrayOutputStream out) throws IOException {
        super(out);
        m_wrappedStream = out;
    }

    public ExposedByteArrayOutputStream getWrappedStream() {
        return m_wrappedStream;
    }

    public ByteBuffer toByteBuffer() throws IOException {
        flush();
        ByteBuffer result = ByteBuffer.wrap(m_wrappedStream.getBackingArray(), 0, m_wrappedStream.getBackingArrayLength());
        result.order(ByteOrder.nativeOrder());
        return result;
    }
}
