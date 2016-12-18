//For storing all logs
package evolutionaryrobotics.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;
import java.util.Vector;
import java.util.zip.GZIPOutputStream;
import simulation.util.Arguments;
import simulation.util.ArgumentsAnnotation;
import evolutionaryrobotics.populations.Population;

import evolutionaryrobotics.evolution.neat.core.NEATPopulation4J;

import evolutionaryrobotics.evolution.neat.ga.core.Chromosome;

public class DiskStorage implements Serializable{
	String outputDirectory;
	Vector<String> argumentsForShowBestIndividual = new Vector<String>();
	Vector<String> argumentsForRestartEvolution = new Vector<String>();
	int population;
	protected final String generationNumberFilename;
	protected final String populationFilename;
	protected final String fitnessLogFilename;
    protected final String fitnessDetailLogFilename;
    protected final String argumentsFilename;
	protected final String showBestFilename;
	protected final String restartFilename;

	//public final String onlineLocationFilename = "_onlineLocation.log";


	protected PrintStream fitnessLog;
	protected PrintStream fitnessDetailLog;
	//protected PrintStream onlineLocationLog;
	

	public DiskStorage(String outputDirectory, int p) {
		this.outputDirectory = outputDirectory;
		this.population = p;
		generationNumberFilename = "_generationnumber" + Integer.toString(population) + "_";
		populationFilename = "population" + Integer.toString(population) + "_";
		fitnessLogFilename = "_fitness"+Integer.toString(population)+".log";
	    fitnessDetailLogFilename = "_fitnessdetail"+ Integer.toString(population)+".log";
		argumentsFilename = "_arguments.conf";
		showBestFilename = "showbest"+ Integer.toString(population);
		restartFilename = "_restartevolution"+ Integer.toString(population)+".conf";
		
	}

	public void start() throws FileNotFoundException {
		if (outputDirectory != null) {
			File f = new File(outputDirectory);
			if (!f.exists())
				if (!f.mkdirs()) {
					throw new RuntimeException(
							"Cannot create output directory: "
									+ outputDirectory);
				}
			File f_best = new File(outputDirectory + "/show_best");
			if (!f_best.exists())
				if (!f_best.mkdirs()) {
					throw new RuntimeException(
							"Cannot create output directory: "
									+ outputDirectory + "/show_best");
				}
			File f_pop = new File(outputDirectory + "/populations");
			if (!f_pop.exists())
				if (!f_pop.mkdirs()) {
					throw new RuntimeException(
							"Cannot create output directory: "
									+ outputDirectory + "/populations");
				}
			openFitnessLog(true);
			openFitnessDetailLog(true);
		}

	}

	public void saveCommandlineArguments(HashMap<String,Arguments> args) throws FileNotFoundException {
		if (outputDirectory != null) {
			//PrintStream onlineLocationFile = openForWriting(outputDirectory + "/" + onlineLocationFilename);
			//onlineLocationFile.println("0\n0\n0");
			//onlineLocationFile.close();
			
			PrintStream argumentsFile = openForWriting(outputDirectory + "/" + argumentsFilename);
			
			
			for(String name : args.keySet()) {
				
				String fullArgument = args.get(name).getCompleteArgumentString();
				String beautifiedArgument = Arguments.beautifyString(fullArgument);
				String save = name+"\n"+beautifiedArgument;
				
				argumentsFile.println(save);
				//------REVER--------
				if (!name.equalsIgnoreCase("--population") && !name.equalsIgnoreCase("--populationa") 
						&& !name.equalsIgnoreCase("--populationb")) {
					argumentsForShowBestIndividual.add(save);
					argumentsForRestartEvolution.add(save);
				}
			}
			argumentsFile.close();
		}
	}
	
	

	public void close() {
		if (outputDirectory != null) {
			fitnessLog.close();
			fitnessDetailLog.close();
			
		}
	}

	public void savePopulation(Population populationA, Population populationB) throws IOException {
		if (outputDirectory != null) {
			updateFitnessLog(populationA, populationB);

			// Save population:
			savePopulationToFile(populationA, "A");
			savePopulationToFile(populationB, "B");

			// Save the generation number
			saveGenerationNumber(populationA, "A");

			// Save the show best file
			saveShowBestFile(populationA, populationB, populationA.getGenerationRandomSeed(),"A","B");

			// Save the show best file
			saveShowCurrentBest(populationA, populationB, populationA.getGenerationRandomSeed(), "A","B");
			saveShowCurrentBest(populationB, populationA, populationB.getGenerationRandomSeed(), "B","A");

			// Save the restart file
			saveRestartFile(populationA, populationB, populationA.getGenerationRandomSeed());

		}
	}
	
	public void savePopulations(Population populationA, Population populationB) throws IOException {
		if (outputDirectory != null) {
			updateFitnessLog(populationA, populationB);

			// Save population:
			savePopulationToFile(populationA, "a");
			savePopulationToFile(populationB, "b");

			// Save the generation number
			saveGenerationNumber(populationA, "a");
			saveGenerationNumber(populationB, "b");

			// Save the show best file
			saveShowBestFile(populationA, populationB, populationA.getGenerationRandomSeed(),"a","b");

			// Save the show best file
			saveShowCurrentBest(populationA, populationB, populationA.getGenerationRandomSeed(), "a","b");

			// Save the restart file
			saveRestartFile(populationA, populationB, populationA.getGenerationRandomSeed());

		}
	}
	

	
	public void updateFitnessOnly(Population population) {
		if (outputDirectory != null) {
			updateFitnessLog(population);
			updateFitnessDetailLog(population); 
		}
	}

	public void savePopulation(Population population)
			throws IOException {
		if (outputDirectory != null) {
			updateFitnessLog(population);
			updateFitnessDetailLog(population);

			// Save population:
			savePopulationToFile(population, "");

			// Save the generation number
			saveGenerationNumber(population, "");

			// Save the show best file
			saveShowBestFile(population, population.getGenerationRandomSeed());

			// Save the show best file
			saveShowCurrentBest(population, population.getGenerationRandomSeed());

			// Save the restart file
			saveRestartFile(population, population.getGenerationRandomSeed());
		}
	}
	
	
	private void openFitnessLog(boolean append) {
		try {
			
			if(fitnessLog != null)
				fitnessLog.close();
			
			fitnessLog = openForWriting(outputDirectory + "/" + fitnessLogFilename, append);
			fitnessLog.println("# Evoluation started on " + new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()));
			fitnessLog.println("# Generation\t   Best \t   Average \t   Worst");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
    
    
    private void openFitnessDetailLog(boolean append) {
        try {
            
            if(fitnessDetailLog != null)
            	fitnessDetailLog.close();
            
            fitnessDetailLog = openForWriting(outputDirectory + "/" + fitnessDetailLogFilename, append);
            fitnessDetailLog.println("# Evoluation started on " + new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()));
            fitnessDetailLog.println("# Generation\t   Individual \t   Fitness \t SecondFitness \t  Size\t	SpecieID");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
	
    
    private void updateFitnessLog(Population populationA, Population populationB) {
		
		if(populationA.getNumberOfCurrentGeneration() == 0) {
			openFitnessLog(false);
		}
	
		fitnessLog
				.printf("\t%3d\t\t%18.10f\t%18.10f\t%18.10f\t%18.10f\t%18.10f\t%18.10f\n",
						populationA.getNumberOfCurrentGeneration(),
						populationA.getHighestFitness(),
						populationA.getAverageFitness(),
						populationA.getLowestFitness(),
						populationB.getHighestFitness(),
						populationB.getAverageFitness(),
						populationB.getLowestFitness());
		fitnessLog.flush();
	}

	private void updateFitnessLog(Population population) {
		if(population.getNumberOfCurrentGeneration() == 0) {
			openFitnessLog(false);
		}
		fitnessLog.printf("\t%3d\t\t%8.3f\t%8.3f\t%8.3f\n",
				population.getNumberOfCurrentGeneration(),
				population.getHighestFitness(), population.getAverageFitness(),
				population.getLowestFitness());
		fitnessLog.flush();
	}
    
   private void updateFitnessDetailLog(Population population) {
        if(population.getNumberOfCurrentGeneration() == 0) {
            openFitnessDetailLog(false);
        }
        
        
        NEATPopulation4J pop = population.getNEATPopulation4J();
        Chromosome[] toppop  = pop.genoTypes();
        
        //System.out.println("Heb ik iets? " + toppop[1].fitness());
        
        
       
        for (int i = 0; i < population.getPopulationSize(); i++){
   
        	if(toppop != null){
        		fitnessDetailLog.printf("\t%3d\t\t%3d\t%8.3f\t%3d\t%3d\t%3d\n",
            			population.getNumberOfCurrentGeneration(),i+1, toppop[i].fitness(), toppop[i].secondFitness(), toppop[i].size(), toppop[i].getSpecieId());
                        // this is not working: chromosomes are in pop (NEATPopulation4J). how to get them?
            	fitnessDetailLog.flush();
        	}
        	
        }
        
        
    }
   
   /*public void removeFileLocation(){
	   File f = new File(outputDirectory + "/" + onlineLocationFilename);
	   f.delete();
   }*/
    
    private void saveShowCurrentBest(Population populationA,
			Population populationB, long randomSeed, String prefixA,
			String prefixB) throws FileNotFoundException {
		PrintStream currentShowBestFile = openForWriting(outputDirectory + "/_"
				+ prefixA + showBestFilename + "_current.conf");

		
		for(String s : argumentsForShowBestIndividual)
			currentShowBestFile.println(s);

			currentShowBestFile.println("--population loada=" + outputDirectory
					+ "/populations/" + prefixA + populationFilename
					+ populationA.getNumberOfCurrentGeneration() + ",loadb="
					+ outputDirectory + "/populations/" + prefixB
					+ populationFilename
					+ populationB.getNumberOfCurrentGeneration()
					+ ",showbestCoevolved");
			currentShowBestFile.println("--random-seed " + randomSeed);
			currentShowBestFile.close();
	}

	private void saveShowCurrentBest(Population population, long randomSeed)
			throws FileNotFoundException {
		PrintStream currentShowBestFile = openForWriting(outputDirectory + "/_"
				+ showBestFilename + "_current.conf");

		for(String s : argumentsForShowBestIndividual)
			currentShowBestFile.println(s);
		
		currentShowBestFile.println("--population load=" + outputDirectory
				+ "/populations/" + populationFilename
				+ population.getNumberOfCurrentGeneration()
				+ ",showbest");
		currentShowBestFile.println("--random-seed " + randomSeed);
		currentShowBestFile.close();
	}

	private void saveRestartFile(Population populationA,
			Population populationB, long randomSeed)
			throws FileNotFoundException {
		PrintStream restartFile = openForWriting(outputDirectory + "/"
				+ restartFilename);

		for(String s : argumentsForRestartEvolution)
			restartFile.println(s);
		
		restartFile.println("--population loada=" + outputDirectory
				+ "/populations/a" + populationFilename
				+ populationA.getNumberOfCurrentGeneration() + ",loadb="
				+ outputDirectory + "/populations/b" + populationFilename
				+ populationB.getNumberOfCurrentGeneration());
		restartFile.println("--random-seed " + randomSeed);
		restartFile.close();
	}

	private void saveRestartFile(Population population, long randomSeed)
			throws FileNotFoundException {
		PrintStream restartFile = openForWriting(outputDirectory + "/"
				+ restartFilename);

		
		for(String s : argumentsForRestartEvolution)
			restartFile.println(s);

		restartFile.println("--population load=" + outputDirectory
				+ "/populations/" + populationFilename
				+ population.getNumberOfCurrentGeneration());
		restartFile.println("--random-seed " + randomSeed);
		restartFile.close();
	}

	private void saveShowBestFile(Population populationA,
			Population populationB, long randomSeed, String prefixA,
			String prefixB)
			throws FileNotFoundException {
		PrintStream showBestFile = openForWriting(outputDirectory
				+ "/show_best/"+ prefixA + showBestFilename
				+ populationA.getNumberOfCurrentGeneration() + ".conf");

		for(String s : argumentsForShowBestIndividual)
			showBestFile.println(s);

		showBestFile.println("--population load" + prefixA + "=" + outputDirectory
				+ "/populations/"+ prefixA + populationFilename
				+ populationB.getNumberOfCurrentGeneration() + ",load" + prefixB + "="
				+ outputDirectory + "/populations/" + prefixB + populationFilename
				+ populationB.getNumberOfCurrentGeneration()
				+ ",showbestCoevolved");
		showBestFile.println("--random-seed " + randomSeed);
		showBestFile.close();

	}

	private void saveShowBestFile(Population population, long randomSeed)
			throws FileNotFoundException {
		PrintStream showBestFile = openForWriting(outputDirectory
				+ "/show_best/" + showBestFilename
				+ population.getNumberOfCurrentGeneration() + ".conf");

		for(String s : argumentsForShowBestIndividual)
			showBestFile.println(s);
		
		showBestFile.println("--population load=" + outputDirectory + "/populations/" + populationFilename + population.getNumberOfCurrentGeneration()+ ",showbest");
		showBestFile.println("--random-seed " + randomSeed);
		showBestFile.close();
	}

	private void savePopulationToFile(Population population, String prefix)
			throws FileNotFoundException, IOException {
		FileOutputStream fileOut = new FileOutputStream(outputDirectory
				+ "/populations/" + prefix + populationFilename
				+ population.getNumberOfCurrentGeneration());
		GZIPOutputStream gzipOut = new GZIPOutputStream(fileOut);
		ObjectOutputStream out = new ObjectOutputStream(gzipOut);
		out.writeObject(population);
		out.close();
	}

	private void saveGenerationNumber(Population population, String prefix)
			throws FileNotFoundException {
		PrintStream generation = openForWriting(outputDirectory + "/"
				+ prefix + generationNumberFilename);
		generation.println(population.getNumberOfCurrentGeneration());
		generation.close();
	}

	protected PrintStream openForWriting(String filename)
			throws FileNotFoundException {
		return new PrintStream(new FileOutputStream(filename));
	}

	protected PrintStream openForWriting(String filename, boolean append)
			throws FileNotFoundException {
		return new PrintStream(new FileOutputStream(filename, append));
	}

	public String getOutputDirectory() {
		return outputDirectory;
	}

}
