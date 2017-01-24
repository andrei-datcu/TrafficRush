package ro.lupii.trafficrush.objects;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by andrei on 1/24/17.
 */

final class ExposedDataOutputStream extends DataOutputStream {

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
        return ByteBuffer.wrap(m_wrappedStream.getBackingArray(), 0, m_wrappedStream.getBackingArrayLength());
    }
}
