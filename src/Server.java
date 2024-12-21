import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Server {
    private final Map<String,String> login;
    private final Map<String, Byte[]> dados;
    private ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
    private Lock wl = rwl.writeLock();
    private Lock rl = rwl.readLock();

    public void put(String key, byte[] value) {
        wl.lock();
        try {
            data.put(key, value);
        } finally {
            wl.unlock();
        }
    }

    public byte[] get(String key) {
        rl.lock();
        try {
            return data.get(key);
        } finally {
            rl.unlock();
        }
    }

    public void multiPut(Map<String, byte[]> pairs) {
        wl.lock();
        try {
            data.putAll(pairs);
        } finally {
            wl.unlock();
        }
    }

    public Map<String, byte[]> multiGet(Set<String> keys) {
        rl.lock();
        try {
            Map<String, byte[]> result = new HashMap<>();
            for (String key : keys) {
                if (data.containsKey(key)) {
                    result.put(key, data.get(key));
                }
            }
            return result;
        } finally {
            rl.unlock();
        }
    }




    public static void main(String[] args) throws IOException {
        ServerSocket ss = new ServerSocket(12345);
        //TODO: interface aqui
        
            while(true) {
                Socket clientSocket = ss.accept();
                Thread t = new Thread(new Worker(clientSocket));
                t.start();
            }
    }

    public static class Worker implements Runnable {
        Socket socket;
        private ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
        private Lock wl = rwl.writeLock();
        private Lock rl = rwl.readLock();

        public Worker(Socket clientSocket) {      //constructor não tem void
            this.socket = clientSocket;
        }

        @Override
        public void run() {
            try {
                DataInputStream in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
                DataOutputStream out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));

                //TODO: Código lmao

                in.close();
                out.close();
                socket.close();
            }
            catch(Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }
}