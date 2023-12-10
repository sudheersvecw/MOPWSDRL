/**
 * Copyright 2012-2013 University Of Southern California
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.workflowsim.scheduling;
import java.util.Arrays;
import java.util.Random;
import java.io.File;
import java.lang.reflect.Array;
import java.util.Calendar;
import java.util.List;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.core.CloudSim;
import org.workflowsim.CondorVM;
import org.workflowsim.WorkflowDatacenter;
import org.workflowsim.Job;
import org.workflowsim.WorkflowEngine;
import org.workflowsim.WorkflowPlanner;
import org.workflowsim.examples.planning.DHEFTPlanningAlgorithmExample1;
import org.workflowsim.utils.ClusteringParameters;
import org.workflowsim.utils.OverheadParameters;
import org.workflowsim.utils.Parameters;
import org.workflowsim.utils.ReplicaCatalog;

public class ACOS extends DHEFTPlanningAlgorithmExample1 {
    static int currentvm=0;
    static int numOfVMs = 50; // Number of VMs
    static int numOfAnts = 100; // Number of tasks
    static int maxIterations = 100; // Maximum number of iterations
    static double alpha = 1.0; // Alpha parameter
    static double beta = 2.0; // Beta parameter
    static double evaporationRate = 0.5; // Evaporation rate
    static double pheromone[][] = new double[numOfVMs+10][numOfVMs+10]; // Pheromone matrix
    static double availableresource[][] = new double[numOfVMs+10][numOfVMs+10]; // Distance matrix
    static Ant ants[] = new Ant[numOfAnts]; // Array of ants

    static class Ant {
        int schedule[] = new int[numOfVMs];
        boolean visited[] = new boolean[numOfVMs];
        int totalAvailable = 0;
    }

    public static void main(String[] args) {
        initialize(); // Initialize pheromone and distance matrices
try {
        String daxPath = "/Users/weiweich/NetBeansProjects/WorkflowSim-1.0/config/dax/Montage_100.xml";
        Parameters.SchedulingAlgorithm sch_method = Parameters.SchedulingAlgorithm.ACOS;
        Parameters.PlanningAlgorithm pln_method = Parameters.PlanningAlgorithm.INVALID;
        ReplicaCatalog.FileSystem file_system = ReplicaCatalog.FileSystem.LOCAL;

        /**
         * No overheads
         */
        
        Ant bestAnt = getBestAnt();
        OverheadParameters op = new OverheadParameters(0, null, null, null, null, 0);

        File daxFile = new File(daxPath);
        if (!daxFile.exists()) {
            Log.printLine("Warning: Please replace daxPath with the physical path in your working environment!");
            return;
        }
        ClusteringParameters.ClusteringMethod method = ClusteringParameters.ClusteringMethod.NONE;
        ClusteringParameters cp = new ClusteringParameters(0, 0, method, null);

        /**
         * Initialize static parameters
         */
        Parameters.init(numOfVMs, daxPath, null,
                null, op, cp, sch_method, pln_method,
                null, 0);
        ReplicaCatalog.init(file_system);

        // before creating any entities.
        int num_user = 1;   // number of grid users
        Calendar calendar = Calendar.getInstance();
        boolean trace_flag = false;  // mean trace events

        // Initialize the CloudSim library
        CloudSim.init(num_user, calendar, trace_flag);

        WorkflowDatacenter datacenter0 = createDatacenter("Datacenter_0");

        /**
         * Create a WorkflowPlanner with one schedulers.
         */
        WorkflowPlanner wfPlanner = new WorkflowPlanner("planner_0", 1);
        /**
         * Create a WorkflowEngine.
         */
        WorkflowEngine wfEngine = wfPlanner.getWorkflowEngine();
        /**
         * Create a list of VMs.The userId of a vm is basically the id of
         * the scheduler that controls this vm.
         */
        
        List<CondorVM> vmlist0 = createVM(wfEngine.getSchedulerId(0), (int)Array.get(bestAnt.schedule,1));
        //currentvm=2;
        /**
         * Submits this list of vms to this WorkflowEngine.
         */
        wfEngine.submitVmList(vmlist0, 0);

        /**
         * Binds the data centers with the scheduler.
         */
        wfEngine.bindSchedulerDatacenter(datacenter0.getId(), 0);

        CloudSim.startSimulation();
        List<Job> outputList0 = wfEngine.getJobsReceivedList();
        CloudSim.stopSimulation();
        printJobList(outputList0);
    } catch (Exception e) {
        Log.printLine("The simulation has been terminated due to an unexpected error");
    }
        
        for (int i = 0; i < maxIterations; i++) {
            generateAnts(); // Generate ant tours
            updatePheromone(); // Update pheromone levels
        }

        // Display the best tour found by the ants
        Ant bestAnt = getBestAnt();
        System.out.println("Best VM secheduled: " + (int)(Array.get(bestAnt.schedule, 2)));
        
    }

    

	// Initialize pheromone and distance matrices
    static void initialize() {
        Random rand = new Random();
        try {
        for (int i = 0; i < numOfVMs; i++) {
            for (int j = 0; j < numOfVMs; j++) {
                if (i != j) {
                    pheromone[i][j] = 1.0; // Initial pheromone level
                    availableresource[i][j] = rand.nextInt(100) + 1; // Random availability of VMs
                }
            }
        }
        }
        
    
        catch(Exception e) {
        }
    }
    

    // Generate ants
    static void generateAnts() {
    	try {
        for (int k = 0; k < numOfAnts; k++) {
            Ant ant = new Ant();
            ant.schedule[0] = 0; // Start from VM 0
            ant.visited[0] = true;

            for (int i = 1; i < numOfVMs; i++) {
                int nextVM = selectNextVM(ant);
                ant.schedule[i] = nextVM;
                ant.visited[nextVM] = true;
                ant.totalAvailable += availableresource[ant.schedule[i - 1]][ant.schedule[i]];
            }

            // Return to the starting VM
            ant.totalAvailable += availableresource[ant.schedule[numOfVMs - 1]][ant.schedule[0]];
            ants[k] = ant;
        }
    	}
    	catch(Exception e) {}
    }

    // Select the next city for an ant
    static int selectNextVM(Ant ant) {
        //int currentVM = ant.totalAvailable[ant.totalAvailable == 0 ? 0 : ant.totalAvailable / (numOfVMs - 1)];
    	int currentVM = ant.schedule[ant.totalAvailable == 0 ? 0 : ant.totalAvailable/ (numOfVMs - 1)];
    	double[] probabilities = new double[numOfVMs];
        double totalProbability = 0.0;

        for (int i = 0; i < numOfVMs; i++) {
            if (!ant.visited[i]) {
                probabilities[i] = Math.pow(pheromone[currentVM][i], alpha) * Math.pow(1.0 / availableresource[currentVM][i], beta);
                totalProbability += probabilities[i];
            }
        }

        double rand = Math.random() * totalProbability;
        double sum = 0.0;

        for (int i = 0; i < numOfVMs; i++) {
            if (!ant.visited[i]) {
                sum += probabilities[i];
                if (sum >= rand) {
                    return i;
                }
            }
        }
        return -1;
    }

    // Update pheromone levels
    static void updatePheromone() {
        for (int i = 0; i < numOfVMs; i++) {
            for (int j = 0; j < numOfVMs; j++) {
                if (i != j) {
                    pheromone[i][j] *= (1.0 - evaporationRate); // Evaporation
                    for (Ant ant : ants) {
                        pheromone[i][j] += (ant.visited[i] && ant.visited[j]) ? (1.0 / 2) : 0.0;
                    }
                }
            }
        }
    }

    // Get the best ant (VM with the more resource)
    static Ant getBestAnt() {
        Ant bestAnt = ants[0];
        for (Ant ant : ants) {
            if (ant.totalAvailable < bestAnt.totalAvailable) {
                bestAnt = ant;
              
            }
        }
        return bestAnt;
    }
}
