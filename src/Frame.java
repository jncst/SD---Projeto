public class Frame {
    
    public int tag;
    public byte[] data;

    public Frame(int tag, byte[] data) {
        this.tag = tag; 
        //0 - Registo, 
        //1 - login,
        //2 - upload,
        //3 - download,
        //4 - multiupload,
        //5 - multidownload,
        //6 - logout,
        //99 - erro,
        
        this.data = data;
    }
}
