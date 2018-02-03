import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.nary.automata.FA.FiniteAutomaton;
import org.chocosolver.solver.search.limits.FailCounter;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class Main {

    private static final String FILE_SEPARATOR = " ";
    private static final int AMOUNT_OF_PERIODS = 16;

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

            Model model = new Model("Heuristique");
            BoolVar[][] employeesSchedule = model.boolVarMatrix("horaires", numberOfEmployees, AMOUNT_OF_PERIODS);

            BoolVar[][] periodsSchedule = new BoolVar[AMOUNT_OF_PERIODS][numberOfEmployees];
            for (int employeeNumber = 0; employeeNumber < numberOfEmployees; employeeNumber++) {
                for (int periodNumber = 0; periodNumber < AMOUNT_OF_PERIODS; periodNumber++) {
                    periodsSchedule[periodNumber][employeeNumber] = employeesSchedule[employeeNumber][periodNumber];
                }
            }

            FiniteAutomaton finiteAutomaton = new FiniteAutomaton(
                    "0*1{" + minimalAmountOfPeriodTogether + ",}01{" + minimalAmountOfPeriodTogether + ",}0*"
            );
            for (int employeeNumber = 0; employeeNumber < numberOfEmployees; employeeNumber++) {
                model.sum(employeesSchedule[employeeNumber], ">=", minimumPeriodsOfWorkPerEmployee).post();
                model.sum(employeesSchedule[employeeNumber], "<=", maximumPeriodsOfWorkPerEmployee).post();

                model.regular(employeesSchedule[employeeNumber], finiteAutomaton).post();
            }

            IntVar[] numberOfEmployeesPerPeriod = model.intVarArray(AMOUNT_OF_PERIODS, 0, numberOfEmployees);
            IntVar[] lossPerPeriod = model.intVarArray(AMOUNT_OF_PERIODS, 0, numberOfEmployees);

            for (int periodNumber = 0; periodNumber < AMOUNT_OF_PERIODS; periodNumber++) {
                model.sum(periodsSchedule[periodNumber], ">=", requiredAmountOfEmployees[periodNumber]).post();
                model.sum(periodsSchedule[periodNumber], "=", numberOfEmployeesPerPeriod[periodNumber]).post();

                model.ifThenElse(
                        model.arithm(numberOfEmployeesPerPeriod[periodNumber], ">", suggestedAmountOfEmployees[periodNumber]),
                        model.arithm(numberOfEmployeesPerPeriod[periodNumber], "-", model.intVar(suggestedAmountOfEmployees[periodNumber]), "=", lossPerPeriod[periodNumber]),
                        model.arithm(model.intVar(suggestedAmountOfEmployees[periodNumber]), "-", numberOfEmployeesPerPeriod[periodNumber], "=", lossPerPeriod[periodNumber])
                );
            }

            IntVar totalLoss = model.intVar("Perte totale", 0, AMOUNT_OF_PERIODS * numberOfEmployees);
            model.sum(lossPerPeriod, "=", totalLoss).post();

            Solver solver = model.getSolver();

            solver.setGeometricalRestart(2, 2.1, new FailCounter(model, 2), 25000);

            Solution optimalSolution = solver.findOptimalSolution(totalLoss, Model.MINIMIZE);

            Arrays.stream(employeesSchedule).forEach(employeeSchedule -> {
                Arrays.stream(employeeSchedule).forEach(period -> System.out.print(optimalSolution.getIntVal(period)));
                System.out.println();
            });

            System.out.println();
            Arrays.stream(lossPerPeriod).forEach(period -> System.out.print(optimalSolution.getIntVal(period)));
            System.out.print(" : ");
            System.out.println(optimalSolution.getIntVal(totalLoss));

            solver.printStatistics();

        } catch (java.io.IOException exception) {
            System.out.print("Le fichier n'existe pas");
        }
    }
}


