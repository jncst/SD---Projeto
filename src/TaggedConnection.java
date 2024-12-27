import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.net.Socket;
import java.io.IOException;

public class TaggedConnection {
    
    private DataInputStream in;
    private DataOutputStream out;
    private final Lock read_lock = new ReentrantLock();
    private final Lock write_lock = new ReentrantLock();

    public TaggedConnection(Socket s) throws IOException {
        in = new DataInputStream(s.getInputStream());
        out = new DataOutputStream(s.getOutputStream());
    }

    public void send(int tag, byte[] data) throws IOException {
        write_lock.lock();
        try {
            this.out.writeInt(tag);
            if (data != null) {
                this.out.writeInt(data.length);
                this.out.write(data);
            }
            else {
                this.out.writeInt(0);
            }
            this.out.flush();
        } finally {
            write_lock.unlock();
        }
    }

    public Frame receive() throws IOException {
        read_lock.lock();
        try {
            int tag = this.in.readInt();
            int length = this.in.readInt();
            byte[] data = new byte[length];
            this.in.readFully(data);
            return new Frame(tag, data);
        } finally {
            read_lock.unlock();
        }
    }

    public void close() throws IOException {
        this.in.close();
        this.out.close();
    }
}
