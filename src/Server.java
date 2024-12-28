import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class Server
{

    private static final int MAX_CLIENTS = 1;
    private static final AtomicInteger clientCount = new AtomicInteger(1);//tem que se meter mais um do que se realmente tem (p.e. 1 para quando comeca com 0)
    public static void main(String[] args) throws IOException
    {
        ServerSocket ss = new ServerSocket(12345);
        ServerOps operator = new ServerOps();           //para usar os métodos que estavam no server

        while(true)
        {
            Socket clientSocket = ss.accept();
            if (clientCount.get() <= MAX_CLIENTS) {
                clientCount.incrementAndGet();
                TaggedConnection c = new TaggedConnection(clientSocket);
                c.send(0,"Conexão aceita".getBytes(StandardCharsets.UTF_8));
                Thread t = new Thread(new Worker(clientSocket, operator, c));
                t.start();
            } else {
                TaggedConnection c = new TaggedConnection(clientSocket);
                c.send(99, "Número máximo de clientes atingido".getBytes(StandardCharsets.UTF_8)); // Envia mensagem de erro ao cliente
                clientSocket.close(); // Fecha a conexão se o limite de clientes for atingido
                System.out.println("Client refused: too many clients.");
            }
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
                System.out.println("entrou no worker");
                while (!logout)
                {
                    Frame f = c.receive();      //recebe os dados que o cliente passa

                    switch (f.tag)
                    {
                        case 0:     //* criação de conta
                            System.out.println("entrou no case 0");
                            if(operator.addUser(f.data)){

                                System.out.println("fez o registo");           //chama a função e mete para lá os dados, neste caso só o login
                                c.send(0, null);            //creio que se for só uma confirmação então manda null nos dados
                                System.out.println("enviou o 0");
                            }
                            else
                                c.send(99, null);           //se dá erro então manda o 99
                            break;

                        case 1:     //* login
                            System.out.println("entrou no case 1");
                            if(operator.logIn(f.data)) {

                                System.out.println("fez o login");           //mesma coisa, chama a função e mete para lá os dados
                                c.send(1, null);            //login bem sucedido
                                System.out.println("enviou o 1");}
                            else
                                c.send(99, null);
                            break;

                        case 2:     //* upload (put)
                            System.out.println("entrou no case 2");
                            String content = new String(f.data, StandardCharsets.UTF_8);      //transforma o conteudo de byte[] para string
                            String[] info = content.split(" ", 2);              //parte em duas strings para assim sacar a key

                            operator.put(info[0], info[1].getBytes());      //info[1] é a chave, o resto é o conteudo
                                                                            //converti de bytes para string para bytes outra vez, mas foi o que pensei
                            break;

                        case 3:     //* download (get)
                            System.out.println("entrou no case 3");
                            content = new String(f.data, StandardCharsets.UTF_8);       //aqui n precisa de split
                                                                                        //? content declarado num case funciona neste sem problemas?
                            byte[] getv = operator.get(content);
                            System.out.println("fez o get");
                            c.send(3, getv);        //como é get, envia dados
                            System.out.println("enviou o 3");
                            break;

                        case 4:     //* multiupload (multiPut)
                            System.out.println("entrou no case 4");
                            if(f.data == null) {
                                System.out.println("não tem dados");
                                c.send(99, null);           //se não tiver dados, manda erro
                                break;
                            }
                            System.out.println("multiPut: Dados recebidos não são nulos.");
                            Map<String, byte[]> pairs = operator.byteArraytoMap(f.data);
                            System.out.println("multiPut: Dados convertidos para mapa com " + pairs.size() + " pares.");
                            operator.multiPut(operator.byteArraytoMap(f.data));         //complicado mas pega nos dados, transforma em map e passa à função
                            System.out.println("fez o multiPut");
                            break;

                        case 5:     //* multidownload (multiGet)
                            System.out.println("entrou no case 5");
                            c.send (5, operator.multiGetToBytes (operator.multiGet (operator.bytetoSet (f.data))));
                            System.out.println("enviou o 5");
                                        //confuso mas tipo, primeiro faz byteToSet para transformar no set que queremos
                                        //depois faz a função do multiGet em si
                                        //depois transforma esse map de volta em byte[] para ser enviado como data
                            break;

                        case 6 : // Caso logout
                            System.out.println("entrou no case 6");
                            logout = true;
                            break ;

                        case 7 : // caso de conditional get
                            System.out.println ("entrou no caso 7");
                            Thread t = new Thread (() ->
                                {
                                    try
                                    {
                                        Object[] a = operator.strstrbyteFromByte (f.data);
                                        c.send (7, operator.getWhen ((String) a[0], (String) a[1], (byte[]) a[2]));
                                    }
                                    catch (IOException e)
                                    {
                                        System.out.println ("Error: Couldn't send response message back to client");
                                    }

                                }
                            );
                            t.start();
                            System.out.println("passei do ponto que parava");
                            break;

                        default:
                            c.send(99, null);               //só para ter um default, dá erro
                            break;
                    }
                }
                socket.close();
            } catch (Exception e) {
                System.out.println(e.getMessage());
            } finally {
                clientCount.decrementAndGet();
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}