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
import org.jgroups.Receiver;
import org.jgroups.ReceiverAdapter;
import org.jgroups.View;

public class RunnableWatcher extends ReceiverAdapter implements Runnable {
    private WatchService watcher;
    private Map<WatchKey, Path> keys;
    JChannel channel;
    String name;

    public RunnableWatcher(String name) throws Exception {
        channel = new JChannel();
        channel.setName(name);
        channel.connect("ClienteServidor");
        channel.setReceiver(this);
        Message msg = new Message(null, "Usuario entrou");
        channel.send(msg);
        this.name = name;
    }

    @Override
    public void run() {
        try {
            watching();
            System.out.println("Terminou canal");
            channel.close();
            return;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
    public void receive(Message msg) {
        if (msg.getObject() instanceof Arquivo && msg.src()!=channel.address()) {
            Arquivo arquivo = msg.getObject();
            if (arquivo.codigo == 100) {
                if (arquivo.diretorio == true) {
                    File newFile = null;
                    newFile = new File("Clientes/"+arquivo.nome); 
                    newFile.mkdirs();
                } else {
                    File newFile = null;
                    newFile = new File("Clientes/"+arquivo.nome); 
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
            }
        }
    }
    public void viewAccepted(View new_view) {
        System.out.println("** view: " + new_view);
    }

    public void watching() throws Exception {
        Path path = Paths.get("Clientes");
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
                            criarDiretorio(child.toFile());
                        } else {
                            File arquivo = new File(child.toString());
                            FileInputStream inputStream = new FileInputStream(arquivo);
                            byte[] bs = new byte[(int) arquivo.length()];
                            inputStream.read(bs);
                            String[] aux2 = child.toString().split("Clientes/");
                            Arquivo aux = new Arquivo(bs, aux2[1], false, 100);
                            Message msg = new Message(null, aux);
                            channel.send(msg);
                            inputStream.close();
                        }
                    } catch (IOException x) {
                        // do something useful
                    }
                } else if (kind == ENTRY_DELETE) {
                    String[] aux2 = child.toString().split("Clientes/");
                    Arquivo aux = new Arquivo(null, aux2[1], false, 300);
                    Message msg = new Message(null, aux);
                    channel.send(msg);
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

    private void registerDirectory(Path dir) throws Exception {
        WatchKey key = dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
        keys.put(key, dir);
    }

    public void criarDiretorio(File diretorio) throws Exception {
        File[] afile = diretorio.listFiles();
        for(int i=0;i<afile.length;i++){
            if(afile[i].isFile()){
                FileInputStream inputStream = new FileInputStream(afile[i]);
                byte[] bs = new byte[(int) afile[i].length()];
                inputStream.read(bs);
                String[] aux2 = afile[i].toString().split("Clientes/");
                Arquivo aux = new Arquivo(bs, aux2[1], false,100);
                Message msg = new Message(null, aux);
                channel.send(msg);
                inputStream.close();
            }else{
                criarDiretorio(afile[i]);
            }
        }
        String[] aux2 = diretorio.toString().split("Clientes/");
        Arquivo aux3 = new Arquivo(null, aux2[1], true,100);
        Message msg = new Message(null, aux3);
        channel.send(msg);
    }

}