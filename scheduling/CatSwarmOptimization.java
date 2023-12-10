package org.workflowsim.examples.scheduling;
import java.util.Arrays;
import java.util.Random;

public class CatSwarmOptimization {
    private int populationSize;
    private int dimension;
    private int maxIterations;
    private double[][] population;
    private double[] fitnessValues;
    private double[] bestSolution;
    private double bestFitness;

    public CatSwarmOptimization(int populationSize, int dimension, int maxIterations) {
        this.populationSize = populationSize;
        this.dimension = dimension;
        this.maxIterations = maxIterations;
        this.population = new double[populationSize][dimension];
        this.fitnessValues = new double[populationSize];
        this.bestSolution = new double[dimension];
        this.bestFitness = Double.MAX_VALUE; // Initial best fitness set to maximum value
    }

    public void initializePopulation() {
        Random rand = new Random();
        for (int i = 0; i < populationSize; i++) {
            for (int j = 0; j < dimension; j++) {
                population[i][j] = rand.nextDouble() * 10; // Adjust the range according to the problem
            }
            fitnessValues[i] = evaluateFitness(population[i]);
            if (fitnessValues[i] < bestFitness) {
                bestFitness = fitnessValues[i];
                bestSolution = Arrays.copyOf(population[i], dimension);
            }
        }
    }

    public void catSwarmOptimization() {
        initializePopulation();
        Random rand = new Random();
        for (int iter = 0; iter < maxIterations; iter++) {
            for (int i = 0; i < populationSize; i++) {
                // Update cat's position based on its movement
                double[] newPosition = moveCat(population[i], bestSolution, rand);
                
                // Evaluate the new position
                double newFitness = evaluateFitness(newPosition);

                // Update the population if the new position is better
                if (newFitness < fitnessValues[i]) {
                    population[i] = Arrays.copyOf(newPosition, dimension);
                    fitnessValues[i] = newFitness;
                    if (newFitness < bestFitness) {
                        bestFitness = newFitness;
                        bestSolution = Arrays.copyOf(newPosition, dimension);
                    }
                }
            }
        }
    }

    // Movement of a cat
    private double[] moveCat(double[] currentPosition, double[] bestPosition, Random rand) {
        double[] newPosition = new double[dimension];
        double alpha = 0.1; // Step size factor (can be adjusted)

        for (int i = 0; i < dimension; i++) {
            double r1 = rand.nextDouble();
            double r2 = rand.nextDouble();

            // Update position using the Cat Swarm Optimization equation
            newPosition[i] = currentPosition[i] + alpha * r1 * (bestPosition[i] - currentPosition[i])
                                + alpha * r2 * (bestSolution[i] - currentPosition[i]);
        }
        return newPosition;
    }

    // Evaluate fitness (to be replaced with the problem-specific fitness function)
    private double evaluateFitness(double[] solution) {
        // Example fitness function: Sphere function
        double sum = 0;
        for (double x : solution) {
            sum += x * x;
        }
        return sum;
    }

    public double[] getBestSolution() {
        return bestSolution;
    }

    public double getBestFitness() {
        return bestFitness;
    }

    public static void main(String[] args) {
        int populationSize = 20;
        int dimension = 5;
        int maxIterations = 100;

        CatSwarmOptimization cso = new CatSwarmOptimization(populationSize, dimension, maxIterations);
        cso.catSwarmOptimization();

        System.out.println("Best Solution: " + Arrays.toString(cso.getBestSolution()));
        System.out.println("Best Fitness: " + cso.getBestFitness());
    }
}
