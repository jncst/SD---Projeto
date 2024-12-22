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

            Boolean logout = false;
            try
            {
                //DataInputStream in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
                //DataOutputStream out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));

                while (!logout)
                {
                    Frame f = c.receive();      //recebe os dados que o cliente passa

                    switch (f.tag)
                    {
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
                            byte[] getv = operator.get(content);
                            c.send(3, getv);        //como é get, envia dados

                            break;

                        case 4:     //* multiupload (multiPut)
                            operator.multiPut(operator.byteArraytoMap(f.data));         //complicado mas pega nos dados, transforma em map e passa à função

                            break;

                        case 5:     //* multidownload (multiGet)
                            c.send (5, operator.multiGetToBytes (operator.multiGet (operator.bytetoSet (f.data))));
                                        //confuso mas tipo, primeiro faz byteToSet para transformar no set que queremos
                                        //depois faz a função do multiGet em si
                                        //depois transforma esse map de volta em byte[] para ser enviado como data
                            break;

                        case 6 : // Caso logout
                            logout = true;
                            break ;

                        default:
                            c.send(99, null);               //só para ter um default, dá erro
                            break;
                    }
                }

                socket.close();
            }
            catch(Exception e)
            {
                System.out.println(e.getMessage());
            }
        }
    }
}