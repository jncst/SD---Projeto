import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ServerOps implements ServerItf
{
        private Map<String,String> login;       //isto vai ser atualizado aonde?
        private Map<String, byte[]> dados;
        private ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
        private Lock wl = rwl.writeLock();
        private Lock rl = rwl.readLock();

    @Override
    public void put(String key, byte[] value) {
        wl.lock();
        try {
            dados.put(key, value);
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
        wl.lock();
        try {
            dados.putAll(pairs);
        } finally {
            wl.unlock();
        }
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
    public boolean addUser(byte[] data)
    {
        wl.lock();
        try
        {
            String content = new String(data, StandardCharsets.UTF_8);      //transforma o conteudo de byte[] para string
            String[] info = content.split(" ", 2);              //parte em duas strings

            if(login.get(info[1]) == null)
            {
                login.put(info[1], info[2]);        //username é a chave, password é o valor
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

            if(login.get(info[1]) == info[2])       //verificar se o que está guardado no map é igual ao que foi fornecido
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