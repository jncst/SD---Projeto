import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class Client {
    
    private void handleServerResponse(TaggedConnection c) {
    Thread t = new Thread(() -> {
        Frame f;
        ServerOps so = new ServerOps();
        try {
            f = c.receive(); // Recebe o Frame do servidor
            switch (f.tag) {
                case 4: // Caso de resposta específica para operação 4
                    System.out.println("Resposta para operação 4:");
                    System.out.println(new String(f.data, StandardCharsets.UTF_8));
                    break;

                case 5: // Caso de multiGet ou outra operação
                    System.out.println("Resposta para operação 5:");
                    Map<String, byte[]> resultMap = so.byteArraytoMap(f.data);
                    resultMap.forEach((key, value) -> 
                        System.out.println("Chave: " + key + " | Valor: " + new String(value, StandardCharsets.UTF_8))
                    );
                    break;

                case 6: // Exemplo para um novo caso
                    System.out.println("Resposta para operação 6:");
                    // Lógica para caso 6
                    break;

                default: // Caso de erro ou tag não reconhecida
                    System.err.println("Tag desconhecida recebida: " + f.tag);
                    System.err.println("Dados: " + new String(f.data, StandardCharsets.UTF_8));
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    });
    t.start();
    }

    public static void main(String[] args) throws IOException {
        
        Socket s = new Socket("localhost", 12345);
        TaggedConnection c = new TaggedConnection(s);

        BufferedReader scan = new BufferedReader (new InputStreamReader (System.in));

        System.out.println("Bem vindo ao Portal das Bananas:\n"
        + "Digite o número correspondente a operação desejada:\n"
        + "1 - Login\n"
        + "2 - Registar novo usuário\n");

        int escolha = Integer.parseInt(scan.readLine());
        
        Boolean sucesso = false;

        while (!sucesso) {
            if (escolha == 1) {
                System.out.println("Digite o seu nome de usuário:");
                String username = scan.readLine();
                System.out.println("Digite a sua senha:");
                String password = scan.readLine();
                byte[] data = (username + " " + password).getBytes();
                c.send(1, data);
                Frame f = c.receive();
                if (f.tag == 1) {
                    System.out.println("Login efetuado com sucesso!");
                } else if (f.tag == 99) {
                    System.out.println("Usuário ou senha incorretos.");
                }
            } else if (escolha == 2) {
                System.out.println("Digite o seu nome de usuário:");
                String username = scan.readLine();
                System.out.println("Digite a sua senha:");
                String password = scan.readLine();
                byte[] data = (username + " " + password).getBytes();
                c.send(0, data);
                Frame f = c.receive();
                if (f.tag == 0) {
                    System.out.println("Usuário registrado com sucesso!");
                } else if (f.tag == 99) {
                    System.out.println("Usuário já existente.");
                }
            }
        }

        while (true) {

            System.out.println("Digite o número correspondente a operação desejada:\n"
            + "1 - Upload\n"                //tag2
            + "2 - Download\n"              //tag3
            + "3 - Multiupload\n"           //tag4
            + "4 - Multidownload\n"         //tag5
            + "5 - Logout\n");              //tag6

            escolha = Integer.parseInt(scan.readLine());

            switch(escolha) {

                case 1:
                    
                    System.out.println("Digite a chave:");
                    String key = scan.readLine();
                    System.out.println("Digite o conteúdo:");
                    String content = scan.readLine();
                    byte[] data = (key + " " + content).getBytes();
                    c.send(2, data);
                    break;            
                                //!Para funcionar, manda pelo frame primeiro a key + " " + dados em si, como foi feito para login
                                //E não sei se queres que o server mande tags de volta por estas, atualmente n manda

                case 2:         
                    System.out.println("Digite a chave:");  
                    key = scan.readLine();
                    data = key.getBytes();
                    c.send(3, data);
                    //thread para receber a resposta
                    // Thread t = new Thread(() -> {
                    //     Frame f;
                    //     try {
                    //         f = c.receive();
                    //         if (f.tag == 3) {
                    //             System.out.println(new String(f.data));
                    //         }
                    //     } catch (IOException e) {
                    //         e.printStackTrace();
                    //     }
                    // });              
                    // t.start();
                    handleServerResponse(c);
                    break;
                                //este só precisa da key no data
                                //devolve um frame com o get e a tag3 (mas erros não estão abrangidos)

                case 3:         //este mandei a especificação no disc mas basicamente, 1 int size map, 1 int size de cada string, 1 int size de cada byte[]
                    System.out.println("Digite o número de chaves:");
                    int n = Integer.parseInt(scan.readLine());
                    byte[] map = new byte[0];
                    for (int i = 0; i < n; i++) {
                        System.out.println("Digite a chave:");
                        key = scan.readLine();
                        System.out.println("Digite o conteúdo:");
                        content = scan.readLine();
                        byte[] entry = (key + " " + content).getBytes();
                        byte[] temp = new byte[map.length + entry.length];
                        System.arraycopy(map, 0, temp, 0, map.length);
                        System.arraycopy(entry, 0, temp, map.length, entry.length);
                        map = temp;
                    }
                    c.send(4, map);
                    break;
                                //este é um pouco mais complicado, mas basicamente é um loop que vai pedindo chaves e conteúdos
                                //e vai adicionando ao array de bytes, que é o que é mandado
                                //não sei se queres que o server mande tags de volta por estas, atualmente n manda
                case 4:         //especificação do array devolvido é a mesma

                    System.out.println("Digite o número de chaves:");
                    n = Integer.parseInt(scan.readLine());
                    map = new byte[0];
                    for (int i = 0; i < n; i++) {
                        System.out.println("Digite a chave:");
                        key = scan.readLine();
                        byte[] entry = key.getBytes();
                        byte[] temp = new byte[map.length + entry.length];
                        System.arraycopy(map, 0, temp, 0, map.length);
                        System.arraycopy(entry, 0, temp, map.length, entry.length);
                        map = temp;
                    }
                    c.send(5, map);
                    //thread para receber a resposta
                    // t = new Thread(() -> {
                    //     Frame f;
                    //     try {
                    //         f = c.receive();
                    //         if (f.tag == 5) {

                    //             System.out.println(new String(f.data));
                    //         }
                    //         System.out.println(new String(f.data));
                    //     } catch (IOException e) {
                    //         e.printStackTrace();
                    //     }
                    // });              
                    // t.start();
                    break;
                                //este é um pouco mais complicado, mas basicamente é um loop que vai pedindo chaves
                                //e vai adicionando ao array de bytes, que é o que é mandado
                                //devolve um frame com o get e a tag5 (mas erros não estão abrangidos)

                case 5:
                    c.send(6, new byte[0]);
                    break;
            }

        } 

        

    }
}
