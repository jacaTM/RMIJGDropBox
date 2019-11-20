package pacote;

import java.io.File;
import java.io.FileInputStream;
import java.util.Scanner;

import com.google.gson.Gson;

import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.View;

public class Cliente extends ReceiverAdapter{
    JChannel channel;
    public void await(String name) {
        try {
            channel = new JChannel();
            channel.setName(name);
            channel.connect("ClienteServidor");
            eventLoop(name);
            channel.close();
        }
        catch(Exception e) {
        }
    }
    public void receive(Message msg) {
        System.out.println(msg.getObject().toString());
    }
    public void viewAccepted(View new_view) {
        System.out.println("** view: " + new_view);
    }

    public void eventLoop(String name) throws Exception {
        while(true){
            System.out.println("Digite o caminho do arquivo");
            Scanner sc = new Scanner(System.in);
            String file = sc.nextLine();
            File arquivo = new File(file);
            if(arquivo.exists()){
                FileInputStream inputStream = new FileInputStream(arquivo);
                byte[] bs = new byte[(int) arquivo.length()];
                inputStream.read(bs);
                Arquivo aux = new Arquivo(bs,arquivo.getName());
                Message msg = new Message(null, aux);
                channel.send(msg);
                inputStream.close();
                sc.close();
            }else{
                System.out.println("Arquivo não existe");
            }
        }
    }
    public static void main(String[] args) {
        System.out.println("Qual seu nome?");
        Scanner sc = new Scanner(System.in);
        String name = sc.nextLine();
        Cliente cliente = new Cliente();
        cliente.await(name);
    }
}