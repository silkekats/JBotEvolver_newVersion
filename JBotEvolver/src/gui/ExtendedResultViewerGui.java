package gui;



import evolutionaryrobotics.JBotEvolver;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;
import evolutionaryrobotics.neuralnetworks.NeuralNetworkController;
import evolutionaryrobotics.populations.Population;
import gui.EnvironmentKeyDispatcher;
import gui.Gui;
import gui.renderer.Renderer;
import gui.util.Editor;
import gui.util.GraphPlotter;
import gui.util.GraphViz;
import gui.util.NetworkViewer;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FilenameFilter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Vector;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import simulation.JBotSim;
import simulation.Simulator;
import simulation.Updatable;
import simulation.util.Arguments;

public class ExtendedResultViewerGui extends Gui {

	private JFrame      frame;
	private JTextField  simulationTimeTextField;
	private JTextField  controlStepTextField;	
	private JTextField  fitnessTextField;
    private JTextField  secondFitnessTextField;
	private JTextField  shiftTextField;

	private JTextField  controlStepTimeTextField;
	private JTextField  rendererTimeTextField;

	private NetworkViewer networkViewer = new NetworkViewer();

	private int         sleepBetweenControlSteps = 10;

	JPanel treeWrapper;

	private JButton     startButton = new JButton("Start");
	private JButton     quitButton  = new JButton("Quit");
	private JButton     pauseButton = new JButton("Pause");
	private JButton		plotButton = new JButton("Plot Graph");
	private JButton		shiftButton = new JButton("Set");

	private JSlider playPosition = new JSlider(0,100);
	private JSlider sleepSlider = new JSlider(0,100);

	private JEditorPane extraArguments = new JEditorPane();

	private FileTree fileTree;
	private JTextField currentFileTextField = new JTextField(18);
	private JButton	loadButton	= new JButton("Load");
	private JButton editButton = new JButton("Edit");
	private JButton plotFitnessButton = new JButton("Plot Fitness");

	static final int RUN         = 2;
	static final int PAUSED      = 3;
	static final int STOPPED     = 4;	
	static final int ENDED       = 5;

	private int position_shift = 10;
	private int simulateUntil = 0;

	private int simulationState = STOPPED;

	private Renderer renderer;
	protected Simulator simulator;
	private JBotEvolver jBotEvolver;
	private EvaluationFunction evaluationFunction;

	private boolean readyToSkip = true;

	private Thread worker;

	private GraphViz graphViz = null;
	private boolean showNeuralNetwork = false;
	private JCheckBox neuralNetworkCheckbox;
	private JCheckBox neuralNetworkViewerCheckbox;

	private EnvironmentKeyDispatcher dispatcher;

	public ExtendedResultViewerGui(JBotSim jBotEvolver, Arguments args) {
		super(jBotEvolver,args);
		this.jBotEvolver = (JBotEvolver)jBotEvolver;

		if(args.getArgumentIsDefined("renderer"))
			createRenderer(new Arguments(args.getArgumentAsString("renderer")));

		frame = new JFrame("Result Viewer");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(1200, 750);

		frame.getContentPane().add(initBottomPanel(), BorderLayout.SOUTH);	
		frame.getContentPane().add(initRightWrapperPanel(), BorderLayout.EAST);
		frame.getContentPane().add(initLeftWrapperPanel(), BorderLayout.WEST);

		initActions();
		initListeners();

		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

	private JPanel initLeftWrapperPanel() {

		treeWrapper = new JPanel();
		treeWrapper.setPreferredSize(new Dimension(250,100));

		fileTree = new FileTree(new File("."));

		JPanel argumentsPanel = new JPanel(new BorderLayout());
		argumentsPanel.setPreferredSize(new Dimension(230,100));
		argumentsPanel.add(new JLabel("Extra arguments"),BorderLayout.NORTH);
		argumentsPanel.add(new JScrollPane(extraArguments),BorderLayout.CENTER);

		extraArguments.getInputMap().put(KeyStroke.getKeyStroke("control LEFT"), "none");
		extraArguments.getInputMap().put(KeyStroke.getKeyStroke("control RIGHT"), "none");

		treeWrapper.add(currentFileTextField);
		treeWrapper.add(fileTree);
		treeWrapper.add(editButton);
		treeWrapper.add(loadButton);
		treeWrapper.add(argumentsPanel);
		treeWrapper.add(plotFitnessButton);

		treeWrapper.setBorder(BorderFactory.createTitledBorder("Experiments"));

		return treeWrapper;
	}

	private JPanel initRightWrapperPanel() {

		JPanel sideTopPanel = new JPanel();
		sideTopPanel.setLayout(new GridLayout(12,1));
		sideTopPanel.add(startButton);
		sideTopPanel.add(pauseButton);
		sideTopPanel.add(quitButton);
		sideTopPanel.add(plotButton);

		sideTopPanel.add(new JLabel(" Sleep between control steps (ms)"));

		sleepSlider.setMajorTickSpacing(20);
		sleepSlider.setMinorTickSpacing(5);
		sleepSlider.setPaintTicks(true);
		sleepSlider.setPaintLabels(true);
		sleepSlider.setValue(10);

		sideTopPanel.add(sleepSlider);

		sideTopPanel.add(new JLabel(" Play position (%)"));
		playPosition.setMajorTickSpacing(20);
		playPosition.setMinorTickSpacing(5);
		playPosition.setPaintTicks(true);
		playPosition.setPaintLabels(true);
		playPosition.setValue(0);
		sideTopPanel.add(playPosition);

		sideTopPanel.add(new JLabel(" Number of steps to shift (ctrl+arrows)"));
		JPanel buttonPanel = new JPanel();
		shiftTextField = new JTextField(5);
		shiftTextField.setText(""+position_shift);
		buttonPanel.add(shiftTextField);
		buttonPanel.add(shiftButton);
		sideTopPanel.add(buttonPanel);

		neuralNetworkCheckbox = new JCheckBox("Show Neural Network");
		sideTopPanel.add(neuralNetworkCheckbox);

		neuralNetworkViewerCheckbox = new JCheckBox("Show Neural Network #2");
		sideTopPanel.add(neuralNetworkViewerCheckbox);

		JPanel sideWrapperPanel = new JPanel();
		sideWrapperPanel.setLayout(new BorderLayout());
		sideWrapperPanel.add(sideTopPanel, BorderLayout.NORTH);

		sideWrapperPanel.setBorder(BorderFactory.createTitledBorder("Controls"));

		return sideWrapperPanel;
	}

	private JPanel initBottomPanel() {

		JPanel bottomPanel   = new JPanel();

		bottomPanel.add(new JLabel("Simulation time: "));
		simulationTimeTextField = new JTextField("N/A");
		simulationTimeTextField.setPreferredSize(new Dimension(50, 20));
		simulationTimeTextField.setHorizontalAlignment(JTextField.RIGHT);
		bottomPanel.add(simulationTimeTextField);

		bottomPanel.add(new JLabel("Control step: "));
		controlStepTextField = new JTextField("N/A");
		controlStepTextField.setPreferredSize(new Dimension(50, 20));
		controlStepTextField.setHorizontalAlignment(JTextField.RIGHT);
		bottomPanel.add(controlStepTextField);

		bottomPanel.add(new JLabel("Fitness: "));
		fitnessTextField = new JTextField("N/A");
		fitnessTextField.setPreferredSize(new Dimension(100, 20));
		fitnessTextField.setHorizontalAlignment(JTextField.RIGHT);
		bottomPanel.add(fitnessTextField);
        
        bottomPanel.add(new JLabel("Second Fitness: "));
        secondFitnessTextField = new JTextField("N/A");
        secondFitnessTextField.setPreferredSize(new Dimension(100, 20));
        secondFitnessTextField.setHorizontalAlignment(JTextField.RIGHT);
        bottomPanel.add(secondFitnessTextField);

		bottomPanel.setBorder(BorderFactory.createTitledBorder("Status"));

		return bottomPanel;
	}

	private void initActions() {

		shiftTextField.getInputMap().put(KeyStroke.getKeyStroke("control LEFT"), "none");
		shiftTextField.getInputMap().put(KeyStroke.getKeyStroke("control RIGHT"), "none");
		shiftTextField.getInputMap().put(KeyStroke.getKeyStroke("meta LEFT"), "none");;
		shiftTextField.getInputMap().put(KeyStroke.getKeyStroke("meta RIGHT"), "none");

		((JComponent) frame.getContentPane()).getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("control ENTER"), "control ENTER");
		((JComponent) frame.getContentPane()).getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("meta ENTER"), "control ENTER");
		((JComponent) frame.getContentPane()).getActionMap().put("control ENTER", new AbstractAction(){  
			private static final long serialVersionUID = 1L;
			public void actionPerformed(ActionEvent evt) {  
				loadCurrentFile();
			}
		});

		((JComponent) frame.getContentPane()).getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("control P"), "control P");
		((JComponent) frame.getContentPane()).getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("meta P"), "control P");
		((JComponent) frame.getContentPane()).getActionMap().put("control P", new AbstractAction(){  
			private static final long serialVersionUID = 1L;
			public void actionPerformed(ActionEvent evt) {  
				pauseButton();
			}
		});

		((JComponent) frame.getContentPane()).getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("control S"), "control S");  
		((JComponent) frame.getContentPane()).getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("meta S"), "control S");
		((JComponent) frame.getContentPane()).getActionMap().put("control S", new AbstractAction(){  
			private static final long serialVersionUID = 1L;
			public void actionPerformed(ActionEvent evt) {  
				startButton();
			}
		});

		((JComponent) frame.getContentPane()).getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("control LEFT"), "control LEFT");
		((JComponent) frame.getContentPane()).getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("meta LEFT"), "control LEFT");
		((JComponent) frame.getContentPane()).getActionMap().put("control LEFT", new AbstractAction(){  
			private static final long serialVersionUID = 1L;
			public void actionPerformed(ActionEvent evt) {  
				shiftSimulationBy(-position_shift,false);
			}
		});

		((JComponent) frame.getContentPane()).getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("alt LEFT"), "alt LEFT");
		((JComponent) frame.getContentPane()).getActionMap().put("alt LEFT", new AbstractAction(){  
			private static final long serialVersionUID = 1L;
			public void actionPerformed(ActionEvent evt) {  
				renderer.moveLeft();
			}
		});
		((JComponent) frame.getContentPane()).getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("alt RIGHT"), "alt RIGHT");
		((JComponent) frame.getContentPane()).getActionMap().put("alt RIGHT", new AbstractAction(){  
			private static final long serialVersionUID = 1L;
			public void actionPerformed(ActionEvent evt) {  
				renderer.moveRight();
			}
		});
		((JComponent) frame.getContentPane()).getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("alt UP"), "alt UP");
		((JComponent) frame.getContentPane()).getActionMap().put("alt UP", new AbstractAction(){  
			private static final long serialVersionUID = 1L;
			public void actionPerformed(ActionEvent evt) {  
				renderer.moveUp();
			}
		});
		((JComponent) frame.getContentPane()).getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("alt DOWN"), "alt DOWN");
		((JComponent) frame.getContentPane()).getActionMap().put("alt DOWN", new AbstractAction(){  
			private static final long serialVersionUID = 1L;
			public void actionPerformed(ActionEvent evt) {  
				renderer.moveDown();
			}
		});

		((JComponent) frame.getContentPane()).getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("control RIGHT"), "control RIGHT");
		((JComponent) frame.getContentPane()).getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("meta RIGHT"), "control RIGHT");
		((JComponent) frame.getContentPane()).getActionMap().put("control RIGHT", new AbstractAction(){  
			private static final long serialVersionUID = 1L;
			public void actionPerformed(ActionEvent evt) {  
				shiftSimulationBy(position_shift,false);
			}
		});

		((JComponent) frame.getContentPane()).getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke('+'), "+");  
		((JComponent) frame.getContentPane()).getActionMap().put("+", new AbstractAction(){  
			private static final long serialVersionUID = 1L;
			public void actionPerformed(ActionEvent evt) {  
				renderer.zoomIn();
				renderer.drawFrame();
			}
		});  

		((JComponent) frame.getContentPane()).getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke('-'), "-");  
		((JComponent) frame.getContentPane()).getActionMap().put("-", new AbstractAction(){  
			private static final long serialVersionUID = 1L;
			public void actionPerformed(ActionEvent evt) {  
				renderer.zoomOut();
				renderer.drawFrame();
			}
		});  

		((JComponent) frame.getContentPane()).getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke('*'), "*");  
		((JComponent) frame.getContentPane()).getActionMap().put("*", new AbstractAction(){  
			private static final long serialVersionUID = 1L;
			public void actionPerformed(ActionEvent evt) {  
				renderer.resetZoom();
				renderer.drawFrame();
			}
		});  
	}

	private void initListeners() {

		sleepSlider.addMouseListener(new MouseListener() {
			public void mouseReleased(MouseEvent arg0) {
				sleepBetweenControlSteps = sleepSlider.getValue();
			}
			public void mousePressed(MouseEvent arg0) {}
			public void mouseExited(MouseEvent arg0) {}
			public void mouseEntered(MouseEvent arg0) {}
			public void mouseClicked(MouseEvent arg0) {}
		});

		startButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				startButton();
			} 		
		});

		quitButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				System.exit(0);
			} 		
		});

		pauseButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				pauseButton();
			} 		
		});

		loadButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(worker != null)
					worker.interrupt();
				loadCurrentFile();
			} 		
		});

		playPosition.addMouseListener(new MouseListener() {
			public void mouseReleased(MouseEvent arg0) {
				shiftSimulationBy(playPosition.getValue(),true);
			}
			public void mousePressed(MouseEvent arg0) {
				simulationState = PAUSED;
			}
			public void mouseExited(MouseEvent arg0) {}
			public void mouseEntered(MouseEvent arg0) {}
			public void mouseClicked(MouseEvent arg0) {}
		});

		currentFileTextField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				loadCurrentFile();
			}
		});

		editButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				new Editor(currentFileTextField.getText());
			}
		});

		plotButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(validFile(currentFileTextField.getText())) {
					if(simulator != null)
						simulator.stopSimulation();
					try{
						simulationState = PAUSED;
						jBotEvolver.loadFile(currentFileTextField.getText(), extraArguments.getText());
						new GraphPlotter(jBotEvolver,loadSimulator());
					}catch(Exception e){
						e.printStackTrace();
					}
				}
			}
		});

		shiftButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				try{
					int value = Integer.parseInt(shiftTextField.getText());
					position_shift = value;
				}catch(Exception e) {}
			}
		});

		shiftTextField.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				try{
					int value = Integer.parseInt(shiftTextField.getText());
					position_shift = value;
				}catch(Exception e) {}				
			}
		});

		plotFitnessButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				plotFitness();
			}
		});

		neuralNetworkCheckbox.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				JCheckBox check = (JCheckBox)arg0.getSource();
				showNeuralNetwork = check.isSelected();
			}
		});

		neuralNetworkViewerCheckbox.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				JCheckBox check = (JCheckBox)arg0.getSource();
				networkViewer.setVisible(check.isSelected());
			}
		});
	}

	private void plotFitness() {
		File f = new File(currentFileTextField.getText().trim());
		final String mainFolder = f.isDirectory() ? f.getAbsolutePath() : f.getParent();

		Thread t = new Thread( new Runnable(){
			public void run(){
				new GraphPlotter(getFitnessFiles(mainFolder).split("###"));
			}
		});
		t.start();
	}

	private String getFitnessFiles(String folder) {

		File f = new File(folder+"/_fitness.log");

		try {
			if(f.exists()){
				return f.getAbsolutePath();
			} else {
				String[] directories = (new File(folder)).list(new FilenameFilter() {
					@Override
					public boolean accept(File dir, String name) {
						return (new File(dir,name)).isDirectory();
					}
				});
				String result = "";
				if(directories != null){
					for(String dir : directories){
						String dirResult = getFitnessFiles(folder+"/"+dir);
						if(!dirResult.isEmpty())
							result+=dirResult+"###";
					}
				}
				return result;
			}
		}catch(Exception e ){
			e.printStackTrace();
		}
		return "";
	}

	public void dispose() {
		frame.setVisible(false);
	}

	private void startButton() {
		simulationState = RUN;
		simulateUntil = 0;
		readyToSkip = true;
		if(simulator != null && simulator.simulationFinished())
			loadCurrentFile();
	}

	private void pauseButton() {
		if (simulationState == RUN)
			simulationState = PAUSED;
		else
			simulationState = RUN;
	}

	private void shiftSimulationBy(int value, boolean percentage) {

		try {
			if(readyToSkip || (simulator != null && simulator.simulationFinished())) {
				int maxSteps = simulator.getEnvironment().getSteps();

				if(!percentage) {
					double realStep = simulator.getTime()-1;
					value+=realStep;
					value = value > maxSteps ? maxSteps : (value < 1 ? 1 : value);
					simulateUntil = value;
				}else {
					value = value > 100 ? 100 : (value < 1 ? 1 : value);
					double percent = value/100.0;
					int targetStep = (int)(percent*maxSteps);
					simulateUntil = targetStep;
				}
				simulationState = PAUSED;
				loadCurrentFile();
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	public synchronized void update(Simulator simulator) {

		if(simulateUntil == 0) {

			readyToSkip = true;

			if(dispatcher != null)
				KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(dispatcher);
			dispatcher = new EnvironmentKeyDispatcher(simulator);
			KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(dispatcher);

			if(showNeuralNetwork && graphViz == null) {
				NeuralNetworkController nn = (NeuralNetworkController)simulator.getEnvironment().getRobots().get(0).getController();
				graphViz = new GraphViz(nn.getNeuralNetwork());
			}
			if(showNeuralNetwork)
				graphViz.show();

			updateStatus();

			try {
				if (sleepBetweenControlSteps > 0)
					Thread.sleep(sleepBetweenControlSteps);

				while(simulationState != RUN)
					Thread.sleep(10);
			} catch(Exception e) {}

			if (simulationState == RUN) {
				if(showNeuralNetwork)
					graphViz.changeNeuralNetwork(((NeuralNetworkController)simulator.getEnvironment().getRobots().get(0).getController()).getNeuralNetwork());
			}
		}else if(simulateUntil <= simulator.getTime()){
			simulateUntil = 0;
			updateStatus();
			simulationState = PAUSED;
			readyToSkip = true;
		}else
			readyToSkip = false;
	}

	private void updateStatus() {
		simulationTimeTextField.setText(String.format("%6.2fs", simulator.getTime() * simulator.getTimeDelta()));
		controlStepTextField.setText("" + simulator.getTime().intValue());
		fitnessTextField.setText(String.format("%12.6f", evaluationFunction.getFitness()));
        secondFitnessTextField.setText(String.format("%3d", evaluationFunction.getSecondFitness()));
		updatePlaySlider(simulator.getTime(), simulator.getEnvironment().getSteps());
		renderer.drawFrame();
	}

	private void updatePlaySlider(double step, double maxSteps) {
		int value = (int)(step/maxSteps*100);
		playPosition.setValue(value);
	}

	private boolean validFile(String filename) {
		File f = new File(filename);
		return f.exists() && !f.isDirectory();
	}

	private void loadCurrentFile() {
		String filename = currentFileTextField.getText();

		try{
			if(validFile(filename)) {
				jBotEvolver.loadFile(filename,extraArguments.getText());
				simulator = loadSimulator();

				if(simulateUntil == 0)
					playPosition.setValue(0);

				launchSimulation();
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	public class FileTree extends JPanel {

		private static final long serialVersionUID = 1L;
		private String currentFilename = "";
		private JTree tree;
		private DefaultTreeModel model;

		public FileTree(File dir) {
			setLayout(new BorderLayout());

			DefaultMutableTreeNode nodes = addNodes(null, dir);

			model = new DefaultTreeModel(nodes);
			tree = new JTree(model);

			addTree();
		}

		private void addTree() {

			tree.getInputMap().put(KeyStroke.getKeyStroke("control LEFT"), "none");
			tree.getInputMap().put(KeyStroke.getKeyStroke("control RIGHT"), "none");
			tree.getInputMap().put(KeyStroke.getKeyStroke("meta LEFT"), "none");
			tree.getInputMap().put(KeyStroke.getKeyStroke("meta RIGHT"), "none");

			tree.addTreeSelectionListener(new TreeSelectionListener() {
				public void valueChanged(TreeSelectionEvent e) {
					TreePath tp = e.getPath();

					String filename = "";

					for(int i = 0 ; i < tp.getPathCount() ; i++) {
						filename+= tp.getPathComponent(i);
						if(i != tp.getPathCount()-1)
							filename+="/";
					}

					File f = new File(filename);

					if(f.exists()){
						currentFilename = filename;
						if(!currentFileTextField.getText().equals(filename))
							currentFileTextField.setText(filename);
					}
				}
			});

			tree.addMouseListener(new MouseListener() {
				public void mousePressed(MouseEvent arg0) {
					if(arg0.getClickCount() == 2) {
						File f = (new File(currentFileTextField.getText()));
						if(f.isDirectory())
							changeDirectory(f.getAbsolutePath());
						else
							loadCurrentFile();
					}						
				}
				public void mouseReleased(MouseEvent arg0) {}
				public void mouseExited(MouseEvent arg0) {}
				public void mouseEntered(MouseEvent arg0) {}
				public void mouseClicked(MouseEvent arg0) {}
			});

			JScrollPane scroll = new JScrollPane(tree);
			scroll.setPreferredSize(new Dimension(230,300));

			removeAll();
			add(BorderLayout.CENTER, scroll);
		}

		public void changeDirectory(String dir) {
			File f = new File(dir);
			try {
				String filename = f.getCanonicalPath();
				DefaultMutableTreeNode nodes = addNodes(null, new File(filename));
				tree = new JTree(nodes);
				addTree();
				treeWrapper.revalidate();
				frame.invalidate();
			}catch(Exception e){e.printStackTrace();}
		}

		public String getCurrentFilename() {
			return currentFilename;
		}

		private DefaultMutableTreeNode addNodes(DefaultMutableTreeNode curTop, File dir) {

			String curPath = dir.getAbsolutePath();
			DefaultMutableTreeNode curDir = new DefaultMutableTreeNode(dir.getAbsolutePath());

			if(curTop != null)
				curDir = curTop;

			Vector<String> ol = new Vector<String>();
			String[] tmp = dir.list();
			for (int i = 0; i < tmp.length; i++)
				ol.addElement(tmp[i]);
			Collections.sort(ol, String.CASE_INSENSITIVE_ORDER);

			curDir.add(new DefaultMutableTreeNode(".."));

			for (int i = 0; i < ol.size(); i++) {
				String thisObject = (String) ol.elementAt(i);
				String newPath = curPath + File.separator + thisObject;
				File f = new File(newPath);

				if(!f.getName().startsWith(".")) {
					DefaultMutableTreeNode DefaultMutableTreeNode = new DefaultMutableTreeNode(f.getName());
					if(f.isDirectory())
						DefaultMutableTreeNode.add(new DefaultMutableTreeNode());
					curDir.add(DefaultMutableTreeNode);
				}
			}
			return curDir;
		}
	}

	private void launchSimulation() {

		if(worker != null)
			worker.interrupt();

		worker = new Thread(new SimulationRunner(simulator));
		worker.start();
	}

	private void createRenderer(Arguments args) {
		if(args.getArgumentIsDefined("classname"))
			this.renderer = Renderer.getRenderer(args);
	}


	public Simulator loadSimulator() {

		if(simulator != null)
			simulator.stopSimulation();

		HashMap<String,Arguments> args = jBotEvolver.getArguments();

		if(renderer != null)
			frame.getContentPane().remove(renderer);

		if(args.get("--gui") != null && args.get("--gui").getArgumentIsDefined("renderer"))
			createRenderer(new Arguments(args.get("--gui").getArgumentAsString("renderer")));

		Simulator simulator = jBotEvolver.createSimulator();

		if(args.get("--objective1") == null){
			evaluationFunction = jBotEvolver.getSpecificEvaluationFunction(getName());
		}
		else {
			evaluationFunction = EvaluationFunction.getEvaluationFunction(args.get("--objective1"));
		}

		simulator.addCallback(evaluationFunction);

		if(networkViewer.isVisible())
			simulator.addCallback(networkViewer);

		jBotEvolver.setupBestIndividual(simulator);

		simulator.addCallback((Updatable) this);
		simulator.setupEnvironment();

		for(Updatable up : simulator.getCallbacks()) {
			if(up instanceof EvaluationFunction) {
				this.evaluationFunction = (EvaluationFunction)up;
				break;
			}
		}

		if (renderer != null) {
			renderer.enableInputMethods(true);
			renderer.setSimulator(simulator);
			frame.getContentPane().add(renderer);
			if(simulateUntil == 0)
				renderer.drawFrame();
			frame.validate();
		}

		if(simulateUntil == 0) {
			simulationTimeTextField.setText("0");
			controlStepTextField.setText("0");
			fitnessTextField.setText("0");
		}
        
        if(simulateUntil == 0) {
            simulationTimeTextField.setText("0");
            controlStepTextField.setText("0");
            secondFitnessTextField.setText("0");
        }

		KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new EnvironmentKeyDispatcher(simulator));

		return simulator;
	}

	//TODO: TBC
	/*public Simulator loadSimulator(){
		/**
	 * gui stuff
	 */
	/*if(simulator != null)
			simulator.stopSimulation();

		HashMap<String,Arguments> args = jBotEvolver.getArguments();

		if(renderer != null)
			frame.getContentPane().remove(renderer);

		if(args.get("--gui") != null && args.get("--gui").getArgumentIsDefined("renderer"))
			createRenderer(new Arguments(args.get("--gui").getArgumentAsString("renderer")));

		Population p = jBotEvolver.getPopulation();


		return simulator;
	}*/



	public class SimulationRunner implements Runnable {
		private Simulator sim;
		public SimulationRunner(Simulator sim) {
			this.sim = sim;
		}
		@Override
		public void run() {
			sim.simulate();
		}
	}
}

