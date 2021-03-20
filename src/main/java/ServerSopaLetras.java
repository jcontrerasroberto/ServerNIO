import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerSopaLetras {

    private final int port = 1234;
    private DataOutputStream dos;
    private DataInputStream dis;
    private ObjectInputStream ois;
    private ObjectOutputStream oos;
    private String[] categories = {"Musica", "Animales", "Videojuegos", "Peliculas"};

    public ServerSopaLetras(){

        ServerSocket ss = null;
        System.out.println("Iniciando servidor");

        try {
            ss = new ServerSocket(port);
            ss.setReuseAddress(true);
            System.out.println("Server iniciado, esperando por clientes");

            while (true){
                Socket socketcon = ss.accept();
                System.out.println("Cliente conectado desde " + socketcon.getInetAddress() + " : " + socketcon.getPort());
                dis = new DataInputStream(socketcon.getInputStream());
                dos = new DataOutputStream(socketcon.getOutputStream());
                ois = new ObjectInputStream(socketcon.getInputStream());
                oos = new ObjectOutputStream(socketcon.getOutputStream());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {
        new ServerSopaLetras();
    }


}
