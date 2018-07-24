package mvc;

import java.io.File;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.stage.Window;
import simulator.Simulator;
import simulator.SimulatorImpl;

/**
 * Handles all of the user input from the GUI.
 */
public class Controller {
	
	@FXML
	private AnchorPane anchorPane;
	@FXML
	private MenuBar menuBar;
	@FXML
	private Button loadCritterButton;
	@FXML
	private TextField fpsArea;
	@FXML
	private Slider fpsSlider;
	@FXML
	private Button stepButton;
	@FXML
	private Button startButton;
	@FXML
	private Button stopButton;
	@FXML
	private TextField timeField;
	@FXML
	private TextField critterCount;
	@FXML
	private Button zoomOutButton;
	@FXML
	private Button zoomInButton;
	@FXML
	private ScrollPane scrollPane;
	@FXML
	private TextArea infoArea;
	@FXML
	private TextArea messages;
	@FXML
	private Canvas canvas;
	@FXML
	private FileChooser fc;
	
	private Simulator world;
	private File curFile; //variable to store a loaded file
	private HexGrid grid; //canvas
	private boolean running; //true is simulaiton is automatic
	private int changes; //updates that have not been drawn
	
	protected TextArea rowCritter = new TextArea();
	protected TextArea colCritter = new TextArea();
	protected TextArea numCritters = new TextArea();
	
	private int stepsPerSecond;
	
	/**
	 * Runs when user presses "new world" button.
	 * Creates and displays a new world
	 * with randomly places rocks
	 */
	@FXML
	public void newWorld() {
		world = new SimulatorImpl();
		finishWorld(world);
	}
	
	/**
	 * Enables functionality that requires a world to have been selected.
	 * (allows user to step, start, zoom, etc.)
	 * Sets up a hexagonal grid representation of the world.
	 * @param world
	 * 			the world to display
	 */
	private void finishWorld(Simulator world) {
		loadCritterButton.setDisable(false);
		startButton.setDisable(false);
		stepButton.setDisable(false);
		zoomOutButton.setDisable(false);
		zoomInButton.setDisable(false);
		grid = new HexGrid(world, infoArea);
		scrollPane.setContent(grid);
		grid.draw();
		
		anchorPane.getScene().setOnKeyPressed(new EventHandler<KeyEvent>() {
			
			//Steps world once when user presses "s"
			@Override
			public void handle(KeyEvent event) {
				if (event.getCode() == KeyCode.S)
					if (world != null)
						stepWorld();
			}
			
		});
	}
	
	/**
	 * Runs when user presses "load world" button.
	 * Opens a popup window that allows the user to choose a world file.
	 * Once the user presses submit, if it's a valid file, this method
	 * adds this world to the simulaiton and displays it.
	 */
	@FXML
	public void loadWorld() {
		Window newWindow = new Stage();
		
		Button chooseFileButton = new Button();
		chooseFileButton.setText("Choose file");
		
		chooseFileButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				handleChooseFile();
			}
		});
		
		Button submitButton = new Button();
		submitButton.setText("Submit");
		submitButton.setOnAction(new EventHandler<ActionEvent>() {
			
			@Override
			public void handle(ActionEvent e) {
				if (curFile != null) {
					world = new SimulatorImpl(curFile.toString());
					if (world.getWorld() == null) {
						messages.appendText("Not a valid world\n");
						return;
					}
					finishWorld(world);
					submitButton.getScene().getWindow().hide();
					curFile = null;
				} else {
					messages.appendText("No file chosen\n");
				}
			}
			
		});
		
		Popup popup = new Popup();
		popup.getContent().addAll(chooseFileButton);
		
		popup.sizeToScene();
		popup.setX(1000);
		popup.setY(800);
		popup.show(newWindow);
		
		VBox layout = new VBox(10);
		HBox top = new HBox();
		top.setStyle("-fx-background-color: cornsilk; -fx-padding: 10;");
		HBox bottom = new HBox();
		bottom.setStyle("-fx-background-color: cornsilk; -fx-padding: 10;");
		top.getChildren().addAll(chooseFileButton);
		bottom.getChildren().addAll(submitButton);
		layout.getChildren().addAll(top, bottom);
		Scene popupScene = new Scene(layout, 400, 120);
		((Stage) newWindow).setScene(popupScene);
		((Stage) newWindow).show();
	}
	
	/**
	 * Allows a user to choose a file from their file explorer.
	 */
	public void handleChooseFile() {
		fc = new FileChooser();
		fc.getExtensionFilters()
				.add(new ExtensionFilter("Text Files", "*.txt"));
		curFile = fc.showOpenDialog(anchorPane.getScene().getWindow());
	}
	
	/**
	 * Runs when user hits "load critter" button.
	 * Produces a popup that allows the user to choose the file
	 * describing the critter, the column and row of the desired
	 * location of the critter and the number of critters.
	 * If the user specifies a column and row, the program only loads 1 critter
	 * in that hex.  Otherwise, it loads the number of critters specified(default = 1)
	 * in random hexes. Critters start out facing up.
	 */
	@FXML
	public void loadCritter() {
		Window newWindow = new Stage();
		
		Button chooseFileButton = new Button();
		chooseFileButton.setText("Choose file");
		
		chooseFileButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				handleChooseFile();
			}
		});
		
		Button submitButton = new Button();
		submitButton.setText("Submit");
		submitButton.setOnAction(new EventHandler<ActionEvent>() {
			
			//Uses info user submitted
			@Override
			public void handle(ActionEvent event) {
				
				if (curFile != null) {
					
					if (!rowCritter.getText().isEmpty()
							&& !colCritter.getText().isEmpty()) {
						
						int row = Integer.parseInt(rowCritter.getText());
						int col = Integer.parseInt(colCritter.getText());
						if (!world.isInBounds(col, row))
							messages.appendText("That is not a valid hex\n");
						else {
							// faces forward
							if (!world.loadCritter(curFile.toString(), col, row,
									0)) {
								messages.appendText("Could not load critter\n");
								messages.appendText(
										"Invalid hex or invalid program\n");
							}
							submitButton.getScene().getWindow().hide();
						}
					} else {
						int num = 1;
						if (!numCritters.getText().isEmpty()) {
							num = Integer.parseInt(numCritters.getText());
						}
						int notLoaded = 0;
						for (int i = 0; i < num; i++)
							if (!world.loadCritter(curFile.toString())) {
								notLoaded++;
							}
						messages.appendText((num - notLoaded) + "/" + num
								+ " critters were loaded.\n");
						if (notLoaded > 0)
							messages.appendText(
									"Occupied hex or invalid program\n");
						
						submitButton.getScene().getWindow().hide();
					}
					curFile = null;
				} else {
					messages.appendText("No file chosen\n");
				}
				
				grid.draw();
			}
			
		});
		
		Popup popup = new Popup();
		popup.getContent().addAll(chooseFileButton);
		
		popup.sizeToScene();
		popup.setX(1000);
		popup.setY(800);
		popup.show(newWindow);
		
		VBox vbox = new VBox(10);
		HBox top = new HBox(10);
		HBox inputs = new HBox(10);
		HBox bottom = new HBox(10);
		top.setStyle("-fx-background-color: cornsilk; -fx-padding: 10;");
		inputs.setStyle("-fx-background-color: cornsilk; -fx-padding: 10;");
		bottom.setStyle("-fx-background-color: cornsilk; -fx-padding: 10;");
		
		top.getChildren().addAll(chooseFileButton);
		Label colLabel = new Label("Col: ");
		Label rowLabel = new Label("Row: ");
		Label numLabel = new Label("# of critters: ");
		inputs.getChildren().addAll(colLabel, colCritter, rowLabel, rowCritter,
				numLabel, numCritters);
		bottom.getChildren().addAll(submitButton);
		
		vbox.getChildren().addAll(top, inputs, bottom);
		Scene popupScene = new Scene(vbox, 400, 200);
		((Stage) newWindow).setScene(popupScene);
		((Stage) newWindow).show();
	}
	
	/**
	 * Runs when user presses "close" from "file" menu.
	 * Closes the window containing the simulation.
	 */
	@FXML
	public void closeWorld() {
		((Stage) anchorPane.getScene().getWindow()).close();
	}
	
	/**
	 * Runs when user presses "step" or when user presses "s".
	 * Advances the world by 1 time step and draws the updated world.
	 */
	@FXML
	public void stepWorld() {
		world.advanceTime(1);
		timeField.clear();
		timeField.appendText(world.getTimeStep() + "");
		critterCount.clear();
		critterCount.appendText(world.numCritters() + "");
		synchronized (this) {
			changes++;
		}
		grid.draw();
	}
	
	/**
	 * Stops the simulation from running automatically.
	 */
	@FXML
	public void stopWorld() {
		running = false;
		stopButton.setDisable(true);
		startButton.setDisable(false);
		loadCritterButton.setDisable(false);
	}
	
	/**
	 * Makes the simulation run automatically.
	 * It runs at the number of steps per second that the user
	 * chose with the scroll bar, but only displays updates
	 * at a maximum rate of 30 steps per second.
	 */
	@FXML
	public void startWorld() {
		running = true;
		startButton.setDisable(true);
		loadCritterButton.setDisable(true);
		stopButton.setDisable(false);
		
		// Advancing time in the background
		Thread advanceTime = new Thread() {
			public void run() {
				
				while (running) {
					if (stepsPerSecond != 0) {
						int time = 1000 / stepsPerSecond;
						world.advanceTime(1);
						synchronized (this) {
							changes++;
						}
						//Draws world in applicaiton thread
						Platform.runLater(new Runnable() {
							@Override
							public void run() {
								try {
									sleep(33);
								} catch (InterruptedException e) {
									messages.appendText("Problem Sleeping");
								}
								if (changes > 0) {
									timeField.clear();
									timeField.appendText(
											world.getTimeStep() + "");
									critterCount.clear();
									critterCount.appendText(
											world.numCritters() + "");
									grid.draw();
									synchronized (this) {
										changes = 0;
									}
								}
							}
						});
						
						try {
							sleep(time);
						} catch (InterruptedException e) {
							System.out.println("Problem sleeping");
						}
					}
				}
			}
		};
		
		advanceTime.start();
		
	}
	
	/**
	 * Runs when user hits + button.
	 * Zooms into the world.
	 */
	public void zoomIn() {
		grid.rescale(1);
		grid.draw();
	}
	
	/**
	 * Runs when user hits - button.
	 * Zooms out of world.
	 */
	public void zoomOut() {
		grid.rescale(-1);
		grid.draw();
	}
	
	/**
	 * Initializes text fields that only take in numbers.
	 * Doesn't allow the user to type letters.
	 * (ex, rowCritter, colCritter, numCritter)
	 * @param ta 
	 * 		The text field to initialize
	 */
	public void initNumTextArea(TextArea ta) {
		ta.setMaxSize(25, 15);
		
		ta.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable,
					String oldValue, String newValue) {
				if (!newValue.matches("\\d*")) {
					ta.setText(newValue.replaceAll("[^\\d]", ""));
				}
			}
		});
	}
	
	/**
	 * Sets the value of steps per second that the world does.
	 * Prints this under the slider in the GUI.
	 */
	void updateFPS(int value) {
		stepsPerSecond = value;
		fpsArea.clear();
		fpsArea.appendText(stepsPerSecond + "");
	}
	
	
	/**
	 * Initializes variables corresponding to user inputs.
	 * Disables functionality that cannot be used without a world.
	 */
	@FXML
	public void initialize() {
		loadCritterButton.setDisable(true);
		startButton.setDisable(true);
		stopButton.setDisable(true);
		stepButton.setDisable(true);
		running = false;
		
		fpsSlider.valueProperty().addListener(
				(obs, oldValue, newValue) -> updateFPS(newValue.intValue()));
		
		zoomOutButton.setDisable(true);
		zoomInButton.setDisable(true);
		
		initNumTextArea(rowCritter);
		initNumTextArea(colCritter);
		initNumTextArea(numCritters);
		stepsPerSecond = 1;
		fpsArea.appendText(stepsPerSecond + "");
		changes = 0;
		
	}
}
