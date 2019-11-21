package pacote;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

import org.jgroups.Address;
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
        File fr = new File("Servidores/" + name);
        fr.mkdirs();
        channel = new JChannel();
        channel.setName(name);
        channel.connect("ClienteServidor");
        Message msg = new Message(null, "Me enviem os arquivos");
        channel.send(msg);
    }

    public void raise(String name) {
        try {
            channel.setReceiver(this);
        } catch (Exception e) {
        }
    }

    public void receive(Message msg) {
        if (msg.getObject() instanceof Arquivo) {
            Arquivo arquivo = msg.getObject();
            if (arquivo.codigo == 100) {
                if (arquivo.diretorio == true) {
                    File diretorio = new File("Servidores/" + name + "/" + msg.src() + "/" + arquivo.nome);
                    diretorio.mkdirs();
                } else {
                    File newFile = new File("Servidores/" + name + "/" + msg.src() + "/" + arquivo.nome);
                    File diretorio = new File(newFile.getParent());
                    diretorio.mkdirs();
                    try {
                        FileOutputStream outputStream = new FileOutputStream(newFile);
                        byte[] bs = arquivo.arquivo;
                        outputStream.write(bs, 0, bs.length);
                        outputStream.close();
                    } catch (FileNotFoundException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            } else if (arquivo.codigo == 300) {
                File newFile = new File("Servidores/" + name + "/" + msg.src() + "/" + arquivo.nome);
                if (newFile.isFile())
                    newFile.delete();
                else {
                    try {
                        deleteDiretorio(new File("Servidores/" + name + "/" + msg.src() + "/" + arquivo.nome),
                                msg.src());
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    }
            }
        }
    }

    public void deleteDiretorio(File diretorio, Address source) throws Exception {
        File[] afile = diretorio.listFiles();
        for(int i=0;i<afile.length;i++){
            if(afile[i].isFile()){
                afile[i].delete();
            }else{
                deleteDiretorio(afile[i], source);
            }
        }
        diretorio.delete();
    }

    public void viewAccepted(View new_view) {
        System.out.println("** view: " + new_view);
    }

    public static void main(String[] args) throws Exception {
        Server servidor = new Server();
        servidor.raise(name);
    }
}
