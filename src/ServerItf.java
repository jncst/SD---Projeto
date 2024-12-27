import java.util.Map;
import java.util.Set;

public interface ServerItf
{
    void put(String key, byte[] value);
    byte[] get(String key);
    void multiPut(Map<String, byte[]> pairs);
        Map<String, byte[]> byteArraytoMap(byte[] data);                            //facilita para o multiput, alivia codigo no server file
    byte[] getWhen(String key, String keyCond, byte[] valueCond);
    Map<String, byte[]> multiGet(Set<String> keys);
        Set<String> bytetoSet(byte[] data);                                         //e este para o multiget
        byte[] multiGetToBytes(Map<String,byte[]> map);                             //temos de transformar o mapa de volta em byte[]
    public Object[] strstrbyteFromByte (byte[] data);                               // Reverte de byte[] para String String Byte[]
    boolean addUser(byte[] data);
    boolean logIn(byte[] data);
}