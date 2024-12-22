import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class Server
{    public static void main(String[] args) throws IOException
    {
        ServerSocket ss = new ServerSocket(12345);
        ServerOps operator = new ServerOps();           //para usar os métodos que estavam no server
        
            while(true)
            {
                Socket clientSocket = ss.accept();
                TaggedConnection c = new TaggedConnection(clientSocket);
                Thread t = new Thread(new Worker(clientSocket, operator, c));
                t.start();
            }
    }

    public static class Worker implements Runnable
    {
        private Socket socket;
        private ServerOps operator;
        private TaggedConnection c;

        public Worker(Socket clientSocket, ServerOps operator, TaggedConnection c)
        {
            this.socket = clientSocket;
            this.operator = operator;
            this.c = c;
        }

        @Override
        public void run()
        {
            try
            {
                //DataInputStream in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
                //DataOutputStream out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));

                Frame f = c.receive();      //recebe os dados que o cliente passa

                switch (f.tag) {
                    case 0:     //* criação de conta
                        if(operator.addUser(f.data))            //chama a função e mete para lá os dados, neste caso só o login
                            c.send(0, null);            //creio que se for só uma confirmação então manda null nos dados
                        else
                            c.send(99, null);           //se dá erro então manda o 99
                        break;

                    case 1:     //* login
                        if(operator.logIn(f.data))
                            c.send(1, null);            //login bem sucedido
                        else
                            c.send(99, null);
                        break;

                    case 2:     //* upload (put)
                        String content = new String(f.data, StandardCharsets.UTF_8);      //transforma o conteudo de byte[] para string
                        String[] info = content.split(" ", 2);              //parte em duas strings para assim sacar a key

                        operator.put(info[1], info[2].getBytes());      //info[1] é a chave, o resto é o conteudo
                                                                        //converti de bytes para string para bytes outra vez, mas foi o que pensei
                        break;

                    case 3:     //* download (get)
                        content = new String(f.data, StandardCharsets.UTF_8);       //aqui n precisa de split
                                                                                    //? content declarado num case funciona neste sem problemas?
                        operator.get(content);

                    default:
                        c.send(99, null);               //só para ter um default, dá erro
                        break;
                }

                //in.close();
                //out.close();
                socket.close();
            }
            catch(Exception e)
            {
                System.out.println(e.getMessage());
            }
        }
    }
}