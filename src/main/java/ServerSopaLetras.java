import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;

public class ServerSopaLetras {

    private final int port = 9876;
    private int rows = 16, columns = 16;
    private ObjectInputStream ois;
    private ObjectOutputStream oos;
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
            ss = new ServerSocket(port);
            ss.setReuseAddress(true);
            System.out.println("Server iniciado, esperando por clientes");

            while (true){
                Socket socketcon = ss.accept();
                System.out.println("Cliente conectado desde " + socketcon.getInetAddress() + " : " + socketcon.getPort());
                oos = new ObjectOutputStream(socketcon.getOutputStream());
                oos.flush();
                ois = new ObjectInputStream(socketcon.getInputStream());


                while(true){
                    String action = ois.readUTF();
                    if(action.equals("getCategories")) sendCategories();
                    if(action.matches("category:\\w+")) changeCategory(action);
                    if(action.equals("exit")) break;
                }

                oos.close();
                ois.close();
                socketcon.close();

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

    public void changeCategory(String action) throws IOException {
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
        sendObject(temp);
        sendObject(matrix.clone());

        oos.writeUTF(matEnv);
        oos.flush();
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

    public void sendCategories() throws IOException {
        sendObject(categories);
    }


    public void sendObject(Object toSend) throws IOException {
        oos.writeObject(toSend);
        oos.flush();
    }

    public static void main(String[] args) {
        new ServerSopaLetras();
    }

}
