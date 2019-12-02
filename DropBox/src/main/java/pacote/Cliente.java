package pacote;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.WatchEvent;

import static java.nio.file.StandardWatchEventKinds.*;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.View;

public class Cliente extends ReceiverAdapter {
    String name;

    public Cliente(String name) {
        this.name = name;
    }

    public void await(Scanner sc) {
        try {
            RunnableWatcher rnb = new RunnableWatcher(name);
            Thread t1 = new Thread(rnb);
            t1.start();
            while(true){
                System.out.println("Digite logout quando quiser deslogar");
                System.out.println("Digite sair quando quiser sair");
                String escolha=sc.nextLine();
                if(escolha.equals("logout")){
                    this.name = "";
                    t1.interrupt();
                    File diretorio = new File("Clientes");
                    deletarPasta(diretorio);
                    main(new String[0]);

                }else if(escolha.equals("sair")){
                    t1.interrupt();
                    File diretorio = new File("Clientes");
                    deletarPasta(diretorio);
                    System.exit(0);
                }else{
                    System.out.println("Entrada errada");
                }
            }
            
        } catch (Exception e) {
        }
    }

    

    public void deletarPasta(File diretorio){
        File[] afile = diretorio.listFiles();
        for(int i=0;i<afile.length;i++){
            if(afile[i].isFile()){
                afile[i].delete();
            }else{
                deletarPasta(afile[i]);
            }
        }
        diretorio.delete();
    }
    

    
    public static void main(String[] args) {
        System.out.println("Qual seu nome?");
        Scanner sc = new Scanner(System.in);
        String name = sc.nextLine();
        File cl = new File("Clientes");
        if(cl.mkdirs()){
            System.out.println("Diretorio criado");
        }
        Cliente cliente = new Cliente(name);
        cliente.await(sc);
        sc.close();
    }
}