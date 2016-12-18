package evolutionaryrobotics.evolution.odneat.genotypePhenotypeMapping;


import java.io.Serializable;
import java.lang.reflect.Constructor;

//import org.encog.ml.MLMethod;

import evolutionaryrobotics.evolution.odneat.geneticcomponents.ODNEATGenome;
import simulation.util.Arguments;

public class GPMapping<G,C> implements Serializable {

	//little hack
	protected final boolean SERIALISE_CONTROLLERS;
	protected static String outputDirectory;
	
	public static void setOutputDirectory(String out){
		GPMapping.outputDirectory = out;
	}
	
	public GPMapping(Arguments args){
		SERIALISE_CONTROLLERS = args.getArgumentAsIntOrSetDefault("serialisecontrollers", 1) == 1;
	}

	public static GPMapping getGPMapping(Arguments arguments){
		if (!arguments.getArgumentIsDefined("classname"))
			throw new RuntimeException("GPMapping 'classname' not defined: "+arguments.toString());

		String evaluationName = arguments.getArgumentAsString("classname");

		try {
			Constructor<?>[] constructors = Class.forName(evaluationName).getDeclaredConstructors();
			for (Constructor<?> constructor : constructors) {
				Class<?>[] params = constructor.getParameterTypes();
				if (params.length == 1 && params[0] == Arguments.class) {
					return (GPMapping) constructor.newInstance(arguments);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
		throw new RuntimeException("Unknown mapping: " + evaluationName);
	}

	
	public C decode(G genome) {
		return null;
	}
		
	public G encode(C p) {
		return null;
	}
		
		
}

