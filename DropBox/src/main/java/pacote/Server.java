package pacote;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.View;

public class Server extends ReceiverAdapter {
    static String name;
    JChannel channel;
    public Server() throws Exception {
        Random r = new Random();
        name = "Server" + (r.nextInt(65536) - 32768);
        File fr = new File("Servidores/"+name);
        fr.mkdirs();
        channel = new JChannel();
        channel.setName(name);
        channel.connect("ClienteServidor");
        Message msg = new Message(null,"Me enviem os arquivos");
        channel.send(null,msg);
    }

    public void raise(String name) {
        try {
            channel.setReceiver(this);
        } catch (Exception e) {
        }
    }

    public void receive(Message msg) {
        if(msg.getObject().toString().equals("Me enviem os arquivos")){
            
        }else{
            String[] aux = msg.getObject().toString().split(" from ");
            File arquivo = new File(aux[0]);
            File newFile = new File("Servidores/"+name+"/"+aux[1]+"/"+arquivo.getName());
            File diretorio = new File("Servidores/"+name+"/"+aux[1]);
            diretorio.mkdirs();
            try {
                FileInputStream inputStream = new FileInputStream(arquivo);
                FileOutputStream outputStream = new FileOutputStream(newFile);
                byte[] bs = new byte[(int) arquivo.length()];
                int line = 0;
                while((line=inputStream.read(bs)) != -1){
                    outputStream.write(bs, 0, line);
                }
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public void viewAccepted(View new_view) {
        System.out.println("** view: " + new_view);
    }

    public static void main(String[] args) throws Exception {
        Server servidor = new Server();
        servidor.raise(name);
    }
}
