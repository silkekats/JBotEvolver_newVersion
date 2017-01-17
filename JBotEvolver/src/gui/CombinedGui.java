package gui;

import evolutionaryrobotics.JBotEvolver;
import gui.configuration.ConfigurationGui;
import gui.evolution.EvolutionGui;

import java.util.HashMap;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import simulation.util.Arguments;

public class CombinedGui extends JFrame {
	
	private JTabbedPane tabbedPane;
	private EvolutionGui evo;
	
	public CombinedGui(String[] args) {
		
		super("JBotEvolver");
		
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException
				| IllegalAccessException | UnsupportedLookAndFeelException e1) {
			e1.printStackTrace();
		}
		
		tabbedPane = new JTabbedPane(); 
		
		JBotEvolver jbot = null;
		try {
			jbot = new JBotEvolver(args);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		evo = new EvolutionGui(jbot);
		
		tabbedPane.addTab("Configuration", new ConfigurationGui(jbot, this));
		tabbedPane.addTab("Evolution", evo);
		tabbedPane.addTab("Results", Gui.getGui(jbot, jbot.getArguments().get("--gui")));
		
		add(tabbedPane);

		setSize(1100,680);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setVisible(true);
	}
	
	public void switchGUIs(HashMap<String, Arguments> resultArgsCopy) {
		evo.init(new JBotEvolver(resultArgsCopy, Long.parseLong(resultArgsCopy.get("--random-seed").getCompleteArgumentString())));
		tabbedPane.setSelectedIndex(1);
		evo.executeEvolution();
	}
	
	public CombinedGui() {
		this(new String[]{"--gui","classname=ResultViewerGui,renderer=(classname=TwoDRenderer))"});
	}
	
	public static void main(String[] args) {
		new CombinedGui();
	}
}