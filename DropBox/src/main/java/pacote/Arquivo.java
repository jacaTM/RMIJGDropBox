package pacote;

import java.io.Serializable;

public class Arquivo implements Serializable{
    byte[] arquivo;
    String nome;
    boolean diretorio;//false arquivo, true diretorio
    int codigo; //100-create, 200-modify, 300-delete

    public Arquivo(byte[] arquivo, String nome, boolean diretorio,int codigo){
        this.arquivo=arquivo;
        this.nome=nome;
        this.diretorio=diretorio;
        this.codigo=codigo;
    }
}