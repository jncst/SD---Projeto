import java.io.IOException;
import java.net.Socket;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class Client {
    

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

                case 1:         //!Para funcionar, manda pelo frame primeiro a key + " " + dados em si, como foi feito para login
                                //E não sei se queres que o server mande tags de volta por estas, atualmente n manda

                case 2:         //este só precisa da key no data
                                //devolve um frame com o get e a tag3 (mas erros não estão abrangidos)

                case 3:         //este mandei a especificação no disc mas basicamente, 1 int size map, 1 int size de cada string, 1 int size de cada byte[]

                case 4:         //especificação do array devolvido é a mesma

                case 5:
                    c.send(6, new byte[0]);
                    break;
            }

        } 

        

    }
}
