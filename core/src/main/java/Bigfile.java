import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

public class Bigfile {

    public static void main(String[] args) throws IOException {
        Random random = new Random();

        String alphabet = "abcdefghijklmnopqrstuvwxyz" +
                "ABCDEFGHIJKLMNOPQRSTUVWXYZ" +
                "абвгдежзиклмнопрстуфхцчшщьыъэюя" +
                "АБВГДЕЖЗИКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯ" +
                "1234567890" +
                "!@#$%^&*()_+{}<>?|/ ";

        File file = new File("/home/pacman/downloads/big7");
        file.createNewFile();
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));

        for (int i = 0; i < Integer.MAX_VALUE /64; i++) {
            char randomChar = alphabet.charAt(random.nextInt(alphabet.length()));
            writer.write(randomChar);
        }

        int[] ints = new int[0];


    }
}
