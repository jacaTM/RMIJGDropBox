package pacote;

import java.io.File;
import java.io.FileInputStream;
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
    JChannel channel;
    private WatchService watcher;
    private Map<WatchKey, Path> keys;
    String name;

    public Cliente(String name) {
        this.name = name;
    }

    public void await() {
        try {
            channel = new JChannel();
            channel.setName(name);
            channel.connect("ClienteServidor");
            watching();
            channel.close();
        } catch (Exception e) {
        }
    }

    public void receive(Message msg) {
        System.out.println(msg.getObject().toString());
    }

    public void viewAccepted(View new_view) {
        System.out.println("** view: " + new_view);
    }

    public void eventLoop(String name) throws Exception {
        while (true) {
            System.out.println("Digite o caminho do arquivo");
            Scanner sc = new Scanner(System.in);
            String file = sc.nextLine();
            File arquivo = new File(file);
            if (arquivo.exists()) {
                FileInputStream inputStream = new FileInputStream(arquivo);
                byte[] bs = new byte[(int) arquivo.length()];
                inputStream.read(bs);
                Arquivo aux = new Arquivo(bs, arquivo.getName(), false);
                Message msg = new Message(null, aux);
                channel.send(msg);
                inputStream.close();
                sc.close();
            } else {
                System.out.println("Arquivo não existe");
            }
        }
    }

    public void watching() throws Exception {
        Path path = Paths.get("Clientes/" + name);
        watcher = FileSystems.getDefault().newWatchService();
        keys = new HashMap<WatchKey, Path>();
        walkAndRegisterDirectories(path);
        while (true) {
            WatchKey key;
            try {
                key = watcher.take();
            } catch (InterruptedException x) {
                return;
            }
            Path dir = keys.get(key);
            if (dir == null) {
                System.err.println("WatchKey not recognized!!");
                continue;
            }
            for (WatchEvent<?> event : key.pollEvents()) {
                @SuppressWarnings("rawtypes")
                WatchEvent.Kind kind = event.kind();

                // Context for directory entry event is the file name of entry
                @SuppressWarnings("unchecked")
                Path caminho = ((WatchEvent<Path>) event).context();
                Path child = dir.resolve(caminho);
                System.out.format("%s: %s\n", event.kind().name(), child);

                // if directory is created, and watching recursively, then register it and its
                // sub-directories
                if (kind == ENTRY_CREATE) {
                    try {
                        if (Files.isDirectory(child)) {
                            walkAndRegisterDirectories(child);
                        }
                    } catch (IOException x) {
                        // do something useful
                    }
                }
            }

            // reset key and remove from set if directory no longer accessible
            boolean valid = key.reset();
            if (!valid) {
                keys.remove(key);

                // all directories are inaccessible
                if (keys.isEmpty()) {
                    break;
                }
            }
        }
    }

    private void walkAndRegisterDirectories(final Path start) throws IOException {
        // register directory and sub-directories
        Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                try {
                    registerDirectory(dir);
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private void registerDirectory(Path dir) throws Exception 
    {
        WatchKey key = dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
        keys.put(key, dir);
        if(!dir.toString().equals("Clientes/"+name)){
            String[] aux = dir.toString().split("Clientes/"+name+"/");
            Arquivo arquivoAux = new Arquivo(null,aux[1],true);
            Message msg = new Message(null, arquivoAux);
            channel.send(msg);
        }
    }
    public static void main(String[] args) {
        System.out.println("Qual seu nome?");
        Scanner sc = new Scanner(System.in);
        String name = sc.nextLine();
        File cl = new File("Clientes/"+name);
        if(cl.mkdirs()){
            System.out.println("Diretorio criado");
        }else System.out.println("Usuario existente");
        Cliente cliente = new Cliente(name);
        cliente.await();
    }
}