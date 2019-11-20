package pacote;

import java.io.Serializable;

public class Arquivo implements Serializable{
    byte[] arquivo;
    String nome;
    boolean diretorio;//false arquivo, true diretorio

    public Arquivo(byte[] arquivo, String nome, boolean diretorio){
        this.arquivo=arquivo;
        this.nome=nome;
        this.diretorio=diretorio;
    }
}