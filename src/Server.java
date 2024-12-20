import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server
{
    public static void main(String[] args) throws IOException
    {
        ServerSocket ss = new ServerSocket(12345);
        //TODO: interface aqui
        
            while(true)
            {
                Socket clientSocket = ss.accept();
                Thread t = new Thread(new Worker(clientSocket));
                t.start();
            }
    }

    public static class Worker implements Runnable
    {
        Socket socket;

        public Worker(Socket clientSocket)      //constructor não tem void
        {
            this.socket = clientSocket;
        }

        @Override
        public void run()
        {
            try
            {
                DataInputStream in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
                DataOutputStream out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));

                //TODO: Código lmao

                in.close();
                out.close();
                socket.close();
            }
            catch(Exception e)
            {
                System.out.println(e.getMessage());
            }
        }
    }
}