import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class Main {

    private static final String FILE_SEPARATOR = " ";

    public static void main(String[] args) {
        try {
            List<String> lines = Files.readAllLines(Paths.get(args[0]));
            String[] parameters = lines.get(0).split(FILE_SEPARATOR);

            int numberOfEmployees = Integer.valueOf(parameters[0]);
            int minimumPeriodsOfWorkPerEmployee = Integer.valueOf(parameters[1]);
            int maximumPeriodsOfWorkPerEmployee = Integer.valueOf(parameters[2]);
            int minimalAmountOfPeriodTogether = Integer.valueOf(parameters[3]);

            int[] requiredAmountOfEmployees = Arrays.stream(lines.get(1).split(FILE_SEPARATOR)).mapToInt(Integer::valueOf).toArray();
            int[] suggestedAmountOfEmployees = Arrays.stream(lines.get(2).split(FILE_SEPARATOR)).mapToInt(Integer::valueOf).toArray();

        } catch (java.io.IOException exception) {
            System.out.print("Le fichier n'existe pas");
        }
    }
}
