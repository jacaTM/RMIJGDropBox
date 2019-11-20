package pacote;

import java.io.Serializable;

public class Arquivo implements Serializable{
    byte[] arquivo;
    String nome;

    public Arquivo(byte[] arquivo, String nome){
        this.arquivo=arquivo;
        this.nome=nome;
    }
}