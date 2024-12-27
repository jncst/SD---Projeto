import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ServerOps implements ServerItf
{
        private Map<String,String> login = new HashMap<>();        //mapa para guardar os logins
        private Map<String, byte[]> dados = new HashMap<>();       //mapa para guardar os dados
        private ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
        private Lock wl = rwl.writeLock();
        private Lock rl = rwl.readLock();

    @Override
    public void put(String key, byte[] value) {
        wl.lock();
        try {
            dados.put(key, value);
            System.out.println("meteu o valor");
        } finally {
            wl.unlock();
        }
    }

    @Override
    public byte[] get(String key) {
        rl.lock();
        try {
            return dados.get(key);
        } finally {
            rl.unlock();
        }
    }

    @Override
    public void multiPut(Map<String, byte[]> pairs) {
        System.out.println("fodasse");
        wl.lock();
        System.out.println("fez lock");
        try {
            System.out.println("multiPut: Inserindo " + pairs.size() + " pares.");
            dados.putAll(pairs);
            System.out.println("multiPut: Inseriu " + pairs.size() + " pares.");
        } finally {
            wl.unlock();
        }
    }

    @Override
public Map<String, byte[]> byteArraytoMap(byte[] data) {
    Map<String, byte[]> result = new HashMap<>();
    // System.out.println("criou o mapa");
    // System.out.println("byteArraytoMap: Dados recebidos: " + Arrays.toString(data));
    ByteBuffer buffer = ByteBuffer.wrap(data);
    // System.out.println("criou o buffer");

    int mapSize = buffer.getInt(); // lê o número de elementos do mapa
    // System.out.println("byteArraytoMap: Número de elementos no mapa: " + mapSize);

    for (int i = 0; i < mapSize; i++) {
        int keyLength = buffer.getInt(); // lê o tamanho da chave
        // System.out.println("byteArraytoMap: Tamanho da chave: " + keyLength);

        byte[] keyBytes = new byte[keyLength]; // lê bytes da chave para array de tamanho certo, converte depois
        // System.out.println("antes do get");
        buffer.get(keyBytes); // guarda os bytes lidos nesse array
        String key = new String(keyBytes, StandardCharsets.UTF_8);
        // System.out.println("byteArraytoMap: Chave lida: " + key);

        int valueLength = buffer.getInt(); // lê o tamanho do array de bytes associado
        // System.out.println("byteArraytoMap: Tamanho do valor: " + valueLength);

        byte[] valueBytes = new byte[valueLength]; // lê os bytes associados e converte para byte[]
        buffer.get(valueBytes);
        result.put(key, valueBytes); // adiciona ao mapa
        // System.out.println("byteArraytoMap: Valor lido para chave " + key + ": " + Arrays.toString(valueBytes));
    }

    return result;
}

    @Override
    public Map<String, byte[]> multiGet(Set<String> keys) {
        rl.lock();
        try {
            Map<String, byte[]> result = new HashMap<>();
            for (String key : keys) {
                if (dados.containsKey(key)) {
                    result.put(key, dados.get(key));
                }
            }
            return result;
        } finally {
            rl.unlock();
        }
    }

        @Override
        public Set<String> bytetoSet(byte[] data)
        {
                Set<String> set = new HashSet<String>();        //tem de ser inicializado propriamente
                ByteBuffer buffer = ByteBuffer.wrap(data);

            int setSize = buffer.getInt();      //nr elementos para o set

            for(int i = 0; i < setSize; i++)
            {
                int keyLength = buffer.getInt();                //lê o tamanho da chave
                byte[] keyBytes = new byte[keyLength];          //mete em array os bytes certos
                buffer.get(keyBytes);
                String key = new String(keyBytes, StandardCharsets.UTF_8);      //lê desse array para string

                set.add(key);       //adiciona ao set
            }

            return set;
        }

        @Override
        public byte[] multiGetToBytes(Map<String,byte[]> map)
        {
            final int[] totalSize = {Integer.BYTES};        //calcula o tamanho necessário para o mapa
                                                            //declarado como array pois forEach requer que as coisas sejam finais, este é workaround
        
            map.forEach((string,array) ->               //loop para calcular o tamanho total
            {
                totalSize[0] += Integer.BYTES;     //tamanho de cada chave
                totalSize[0] += string.getBytes().length;      //comprimento de cada chave
                totalSize[0] += Integer.BYTES;     //tamanho do array
                totalSize[0] += array.length;
            });
        
                //agora armazenar os dados em si
            ByteBuffer buffer = ByteBuffer.allocate(totalSize[0]);     //tamanho serviu para alocar bytes
            buffer.putInt(map.size());      //primeiro número é o tamanho do mapa
            
            map.forEach((string,array) ->
            {
                byte[] stringBytes = string.getBytes();
                buffer.putInt(stringBytes.length);          //primeiro o tamanho da chave
                buffer.put(stringBytes);                    //depois a chave em si
            
                buffer.putInt(array.length);                //mesma coisa, primeiro comprimento
                buffer.put(array);                          //depois array em si
            });
        
            return buffer.array();      //transformar de volta em array
        }

    @Override
    public boolean addUser(byte[] data)
    {
        wl.lock();
        try
        {
            System.out.println("entrou no addUser");
            String content = new String(data, StandardCharsets.UTF_8);      //transforma o conteudo de byte[] para string
            System.out.println(content);
            String[] info = content.split(" ", 2);              //parte em duas strings

            if(login.get(info[0]) == null)
            {
                login.put(info[0], info[1]);        //username é a chave, password é o valor
                return true;
            }
            else
                return false;       //se já existe manda um false e a operação é erro
            
        }
        finally
        {
            wl.unlock();
        }
    }

    @Override
    public boolean logIn(byte[] data)
    {
        rl.lock();
        try
        {
            String content = new String(data, StandardCharsets.UTF_8);      //transforma o conteudo de byte[] para string
            String[] info = content.split(" ", 2);              //parte em duas strings

            if(login.get(info[0]) == info[1])       //verificar se o que está guardado no map é igual ao que foi fornecido
                return true;
            else
                return false;
            
        }
        finally
        {
            rl.unlock();
        }
    }
}