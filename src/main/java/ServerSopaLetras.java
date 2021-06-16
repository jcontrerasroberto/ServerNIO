import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;
import java.util.Random;

public class ServerSopaLetras {

    private final int port = 9876;
    private int rows = 16, columns = 16;
    private String[][] matrix = new String[rows][columns];
    private String category;
    private ArrayList<String> actualWords = new ArrayList<String>();
    private String[] categories = {"musica", "animales", "paises", "peliculas"};
    private String[] musica = {
            "microfono",
            "cantante",
            "disco",
            "rock",
            "tango",
            "balada",
            "cumbia",
            "salsa",
            "pop",
            "electronica",
            "baile",
            "notas",
            "guitarra",
            "piano",
            "tambor",
            "bateria",
            "musico",
            "grupo",
            "banda",
            "solista"
    };

    private String[] animales = {
            "oso",
            "panda",
            "pez",
            "camaron",
            "gato",
            "perro",
            "leon",
            "lobo",
            "mariposa",
            "tigre",
            "manada",
            "buho",
            "dinosaurio",
            "elefante",
            "rinoceronte",
            "cocodrilo",
            "lagartija",
            "tortuga",
            "sapo",
            "serpiente"
    };

    private String[] paises = {
            "argentina",
            "brasil",
            "mexico",
            "venezuela",
            "colombia",
            "chile",
            "peru",
            "ecuador",
            "uruguay",
            "bolivia",
            "paraguay",
            "panama",
            "canada",
            "dubai",
            "italia",
            "francia",
            "alemania",
            "suiza",
            "portugal",
            "alaska",
            "china"
    };

    private String[] peliculas = {

            "lallorona",
            "cars",
            "shrek",
            "roma",
            "godzilla",
            "anaconda",
            "anabelle",
            "kingkong",
            "lamonja",
            "pinocho",
            "tomyjerry",
            "soul",
            "titere",
            "exorcista",
            "elpadrino",
            "tiburon",
            "ironman",
            "parasitos",
            "zombi"
    };

    public ServerSopaLetras(){

        ServerSocket ss = null;
        System.out.println("Iniciando servidor");

        try {
            ServerSocketChannel s = ServerSocketChannel.open();
            s.configureBlocking(false);
            s.setOption(StandardSocketOptions.SO_REUSEADDR, true);
            InetSocketAddress local = new InetSocketAddress("127.0.0.1",port);
            s.bind(local);
            Selector sel = Selector.open();
            s.register(sel, SelectionKey.OP_ACCEPT);
            while(true){
                int x = sel.select();
                if (x==0) continue;
                Iterator<SelectionKey> iterator = sel.selectedKeys().iterator();
                while (iterator.hasNext()){
                    SelectionKey key = iterator.next();
                    iterator.remove();
                    if (key.isAcceptable()){
                        SocketChannel channel = s.accept();
                        System.out.println("Cliente conectado desde: " + channel.socket().getInetAddress()  + ":" + channel.socket().getPort() );
                        channel.configureBlocking(false);
                        channel.register(sel, SelectionKey.OP_READ|SelectionKey.OP_WRITE);
                        continue;
                    }
                    if (key.isReadable()){
                        SocketChannel channel = (SocketChannel) key.channel();
                        ByteBuffer reader = ByteBuffer.allocate(2000);
                        reader.clear();
                        int n = channel.read(reader);
                        reader.flip();
                        String action = new String(reader.array(), 0, reader.limit());
                        if(action.equals("getCategories")){
                            System.out.println(action);
                            sendCategories(channel);
                        }
                        if(action.matches("category:\\w+")) changeCategory(action, channel);
                        key.interestOps(SelectionKey.OP_READ);
                        continue;
                    }
                }
            }


        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void generateTablero() throws IOException {
        int limit = 15;
        inicializarMatrix();
        Random rand = new Random();
        String[] original_words = null;
        if(category.equals("musica")) original_words = musica;
        if(category.equals("animales")) original_words = animales;
        if(category.equals("paises")) original_words = paises;
        if(category.equals("peliculas")) original_words = peliculas;
        actualWords.clear();
        while(actualWords.size()<limit){
            int seleccionada = rand.nextInt(limit);
            if(! actualWords.contains(original_words[seleccionada])) actualWords.add(original_words[seleccionada]);
        }
        System.out.println(actualWords);

        for(String word : actualWords){
            posicionarPalabra(word);
        }

        printMatrix(matrix);

        fillEmptyCas();
    }

    public void fillEmptyCas(){
        Random rand = new Random();

        for(int x=0; x<rows; x++){
            for (int y=0; y<columns; y++){
                if(matrix[x][y].equals("-")){
                    int asciiVal = rand.nextInt(26) + 65;
                    char letter = (char) asciiVal;
                    matrix[x][y] = ""+letter;
                }
            }
        }

    }

    public void inicializarMatrix(){
        for(int x=0; x<rows; x++){
            for (int y=0; y<columns; y++){
                matrix[x][y] = "-";
            }
        }
    }

    public void posicionarPalabra(String word){
        int limite = 16;
        Random rand = new Random();
        int filapos, colpos;
        boolean stop = false;
        while(true){
            filapos = rand.nextInt(limite);
            colpos = rand.nextInt(limite);

            // Tratar de posicionar palabra horizontalmente hacia la derecha

            if((colpos + word.length() - 1) < limite) {
                if(checkIfFit(word, filapos, colpos, 0, 1)){
                    insertWord(word, filapos, colpos, 0, 1);
                    break;
                }
            }

            // Tratar de posicionar palabra diagonalmente cuarto cuadrante


            if((colpos + word.length() - 1) < limite  && (filapos + word.length() - 1) < limite) {
                if(checkIfFit(word, filapos, colpos, 1, 1)){
                    insertWord(word, filapos, colpos, 1, 1);
                    break;
                }
            }

            // Tratar de posicionar palabra verticalemte descendentemente

            if((filapos + word.length() - 1) < limite ) {
                if(checkIfFit(word, filapos, colpos, 1, 0)){
                    insertWord(word, filapos, colpos, 1, 0);
                    break;
                }
            }

            // Tratar de posicionar palabra diagonalmente tercer cuadrante

            if((colpos - word.length() + 1) >= 0  && (filapos + word.length() - 1) < limite) {
                if(checkIfFit(word, filapos, colpos, 1, -1)){
                    insertWord(word, filapos, colpos, 1, -1);
                    break;
                }
            }

            // Tratar de posicionar palabra horizontalmente hacia la izquierda

            if((colpos - word.length() + 1) >= limite) {
                if(checkIfFit(word, filapos, colpos, 0, -1)){
                    insertWord(word, filapos, colpos, 0, -1);
                    break;
                }
            }

            // Tratar de posicionar palabra diagonalmente segundo cuadrante

            if((colpos - word.length() + 1) >= 0  && (filapos - word.length() + 1) >= 0) {
                if(checkIfFit(word, filapos, colpos, -1, -1)){
                    insertWord(word, filapos, colpos, -1, -1);
                    break;
                }
            }

            // Tratar de posicionar palabra verticalemte ascendentemente

            if((filapos - word.length() + 1) >= 0 ) {
                if(checkIfFit(word, filapos, colpos, -1, 0)){
                    insertWord(word, filapos, colpos, -1, 0);
                    break;
                }
            }

            // Tratar de posicionar palabra diagonalmente primer cuadrante

            if((colpos + word.length() - 1) < limite  && (filapos - word.length() + 1) >= 0) {
                if(checkIfFit(word, filapos, colpos, -1, 1)){
                    insertWord(word, filapos, colpos, -1, 1);
                    break;
                }
            }

        }

    }

    public boolean checkIfFit(String word, int filapos, int colpos, int filaincrement, int colincrement){

        for(int filastep = 0, colstep = 0, times = 0; times < word.length(); times++, filastep+=filaincrement, colstep+=colincrement){
            String valueAt = matrix[filapos+filastep][colpos+colstep];
            if(!(valueAt.equals(String.valueOf(word.charAt(times)).toUpperCase(Locale.ROOT)) || valueAt.equals("-"))) return false;
        }

        return true;
    }

    public void insertWord(String word, int filapos, int colpos, int filaincrement, int colincrement){

        for(int filastep = 0, colstep = 0, times = 0; times < word.length(); times++, filastep+=filaincrement, colstep+=colincrement){
            matrix[filapos+filastep][colpos+colstep] = String.valueOf(word.charAt(times)).toUpperCase(Locale.ROOT);
        }
    }

    public void changeCategory(String action, SocketChannel channel) throws IOException {
        String[] split = action.split(":");
        category = split[1];
        generateTablero();
        AlphabetSoup temp = new AlphabetSoup();
        temp.setCategory(category);
        temp.setActualWords((ArrayList<String>) actualWords.clone());
        temp.setMatrix(matrix.clone());
        temp.setMatrix(temp.getMatrix().clone());
        System.out.println("Enviando:");
        System.out.println(temp.getActualWords());
        printMatrix(temp.getMatrix());
        String matEnv = convertToString(matrix.clone());
        System.out.println(matEnv);
        temp.setLinealMat(matEnv);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        objectOutputStream.writeObject(temp);
        objectOutputStream.flush();
        byte[] bytesCat = byteArrayOutputStream.toByteArray();
        ByteBuffer writer = ByteBuffer.wrap(bytesCat);
        channel.write(writer);
    }

    private String convertToString(String[][] mat) {
        String res = "";
        for(int x=0; x<rows; x++){
            for (int y=0; y<columns; y++){
                res = res + mat[x][y];
            }
        }
        return res;
    }

    public void printMatrix(String[][] rec){
        for(int x=0; x<rows; x++){
            for (int y=0; y<columns; y++){
                System.out.print(rec[x][y] + "\t");
            }
            System.out.println();
        }
    }

    public void sendCategories(SocketChannel channel) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        objectOutputStream.writeObject(categories);
        objectOutputStream.flush();
        byte[] bytesCat = byteArrayOutputStream.toByteArray();
        ByteBuffer writer = ByteBuffer.wrap(bytesCat);
        channel.write(writer);
    }


    public static void main(String[] args) {
        new ServerSopaLetras();
    }

}
