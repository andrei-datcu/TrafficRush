package ro.lupii.trafficrush.objects;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

/**
 * Created by andrei on 1/24/17.
 */

final class ExposedObjectOutputStream extends ObjectOutputStream {

    private OutputStream m_wrappedStream;

    public ExposedObjectOutputStream(OutputStream out) throws IOException {
        super(out);
        m_wrappedStream = out;
    }

    public OutputStream getWrappedStream() {
        return m_wrappedStream;
    }
}
