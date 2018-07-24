package client;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;

import client.ClientGrid.WorldInfoBundle;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
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
import parse.SpecParser;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.stage.Window;

public class ClientController {

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
	@FXML
	private Button loginButton;
	@FXML
	private TextField levelField;
	@FXML
	private TextField passwordField;
	@FXML
	private TextField urlField;

	private File curFile; // variable to store a loaded file
	private ClientGrid grid; // canvas
	private Client client;

	protected TextArea rowCritter = new TextArea();
	protected TextArea colCritter = new TextArea();
	protected TextArea numCritters = new TextArea();

	private int stepsPerSecond;

	private int priorVersion; //a version before this(not necessarily right before)
	int updateRate; //Rate at which client gets info
	boolean loggedIn;
	private static String url;

	/**
	 * Runs when user presses login button.
	 * Opens up world window if user is authorized.
	 */
	@FXML
	public void login() {
		String level = levelField.getText();
		String password = passwordField.getText();
		url = urlField.getText();
		try {
			client = new Client(new URL(url));
			if (client.login(level, password)) {
				((Stage) loginButton.getScene().getWindow()).close();
				initializeWorld();
			}
		} catch (MalformedURLException e) {
			urlField.clear();
		} catch (IOException e) {
			urlField.clear();
		}
	}

	/**
	 * Runs when user presses "new world" button. Creates and displays a new world
	 * with randomly places rocks
	 */
	@FXML
	public void newWorld() {
		client.makeWorld(" ");
		finishWorld();
	}

	/**
	 * Enables functionality that requires a world to have been selected. (allows
	 * user to step, start, zoom, etc.) Sets up a hexagonal grid representation of
	 * the world.
	 * 
	 * @param world
	 *            the world to display
	 */
	private void finishWorld() {
		loadCritterButton.setDisable(false);
		startButton.setDisable(false);
		stopButton.setDisable(false);
		stepButton.setDisable(false);
		zoomOutButton.setDisable(false);
		zoomInButton.setDisable(false);
		grid = new ClientGrid(client.getWorld(), infoArea);
		scrollPane.setContent(grid);
		grid.draw(client.getWorld());

		anchorPane.getScene().setOnKeyPressed(new EventHandler<KeyEvent>() {

			// Steps world once when user presses "s"
			@Override
			public void handle(KeyEvent event) {
				if (event.getCode() == KeyCode.S)
					stepWorld();
			}

		});

	}

	/**
	 * Runs when user presses "load world" button. Opens a popup window that allows
	 * the user to choose a world file. Once the user presses submit, if it's a
	 * valid file, this method adds this world to the simulaiton and displays it.
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
					try {
						BufferedReader br = new BufferedReader(new FileReader(curFile));

						String description = "";
						while (br.ready())
							description += br.readLine() + "\n";

						client.makeWorld(description);
						finishWorld();
						br.close();
					} catch (IOException ex) {
						messages.appendText("Bad world file\n");
					}

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
		fc.getExtensionFilters().add(new ExtensionFilter("Text Files", "*.txt"));
		curFile = fc.showOpenDialog(anchorPane.getScene().getWindow());
	}

	/**
	 * Runs when user hits "load critter" button. Produces a popup that allows the
	 * user to choose the file describing the critter, the column and row of the
	 * desired location of the critter and the number of critters. If the user
	 * specifies a column and row, the program only loads 1 critter in that hex.
	 * Otherwise, it loads the number of critters specified(default = 1) in random
	 * hexes. Critters start out facing up.
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

			// Uses info user submitted
			@Override
			public void handle(ActionEvent event) {
				try {
					if (curFile != null) {

						BufferedReader br = new BufferedReader(new FileReader(curFile));

						char c;
						String word = "";
						try {
							while (!Character.isWhitespace((c = (char) br.read())) && c != 65535) {
								word += c;
							}
							
						} catch (IOException e) {
							messages.appendText("Invalid Critter file\n");
						}
							
						String species = br.readLine();
						SpecParser sp = new SpecParser();
						Reader r = sp.parseSpecs(br);
						int[] mem = sp.getAttributes();
						String program = "";
						BufferedReader brPro = new BufferedReader(r);
						while (brPro.ready())
							program = program + brPro.readLine() + "\n";

						if (!rowCritter.getText().isEmpty() && !colCritter.getText().isEmpty()) {

							int row = Integer.parseInt(rowCritter.getText());
							int col = Integer.parseInt(colCritter.getText());

							client.addCritter(species, program, mem, row, col, -1);
							submitButton.getScene().getWindow().hide();

						} else {
							int num = 1;
							if (!numCritters.getText().isEmpty()) {
								num = Integer.parseInt(numCritters.getText());
							}
							client.addCritter(species, program, mem, -1, -1, num);

							submitButton.getScene().getWindow().hide();
						}
						curFile = null;
					} else {
						messages.appendText("No file chosen\n");
					}

					grid.draw(client.getWorld());

				} catch (IOException e) {
					messages.appendText("Couldn't find critter file\n");
				}
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
		inputs.getChildren().addAll(colLabel, colCritter, rowLabel, rowCritter, numLabel, numCritters);
		bottom.getChildren().addAll(submitButton);

		vbox.getChildren().addAll(top, inputs, bottom);
		Scene popupScene = new Scene(vbox, 400, 200);
		((Stage) newWindow).setScene(popupScene);
		((Stage) newWindow).show();
	}

	/**
	 * Runs when user presses "close" from "file" menu. Closes the window containing
	 * the simulation.
	 */
	@FXML
	public void closeWorld() {
		((Stage) anchorPane.getScene().getWindow()).close();
	}

	/**
	 * Runs when user clicks logout(File menu).
	 * Opens the login screen.
	 */
	@FXML
	public void logout() {
		((Stage) anchorPane.getScene().getWindow()).close();
		synchronized (this) {
			loggedIn = false;
		}
		initializeLogin(false);
	}

	/**
	 * Runs when user presses "step" or when user presses "s". Advances the world by
	 * 1 time step and draws the updated world.
	 */
	@FXML
	public void stepWorld() {
		client.step(1);
		timeField.clear();
		WorldInfoBundle world = client.getWorld();
		timeField.appendText(world.current_timestep + "");
		critterCount.clear();
		critterCount.appendText(world.population + "");
		grid.draw(world);
	}

	/**
	 * Stops the simulation from running automatically.
	 */
	@FXML
	public void stopWorld() {
		client.changeRate(0);
	}

	/**
	 * Makes the simulation run automatically. It runs at the number of steps per
	 * second that the user chose with the scroll bar, but only displays updates at
	 * a maximum rate of 30 steps per second.
	 */
	@FXML
	public void startWorld() {
		client.changeRate(stepsPerSecond);
	}

	/**
	 * Runs when user hits + button. Zooms into the world.
	 */
	public void zoomIn() {
		grid.rescale(1);
		grid.draw(client.getWorld());
	}

	/**
	 * Runs when user hits - button. Zooms out of world.
	 */
	public void zoomOut() {
		grid.rescale(-1);
		grid.draw(client.getWorld());
	}

	/**
	 * Initializes text fields that only take in numbers. Doesn't allow the user to
	 * type letters. (ex, rowCritter, colCritter, numCritter)
	 * 
	 * @param ta
	 *            The text field to initialize
	 */
	public void initNumTextArea(TextArea ta) {
		ta.setMaxSize(25, 15);

		ta.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				if (!newValue.matches("\\d*")) {
					ta.setText(newValue.replaceAll("[^\\d]", ""));
				}
			}
		});
	}

	/**
	 * Sets the value of steps per second that the world does. Prints this under the
	 * slider in the GUI.
	 */
	void updateFPS(int value) {
		stepsPerSecond = value;
		client.changeRate(stepsPerSecond);

		fpsArea.clear();
		fpsArea.appendText(stepsPerSecond + "");
	}

	public void initializeWorld() {
		try {
			URL r = getClass().getResource("controller.fxml");
			if (r == null)
				throw new Exception("No FXML resource found.");
			Scene scene = new Scene(FXMLLoader.load(r));
			Stage stage = new Stage();
			stage.setTitle("Critter World");
			stage.setScene(scene);
			stage.sizeToScene();
			stage.show();
		} catch (Exception e) {
			loggedIn = false;
		}
	}

	/**
	 * 
	 * @param first
	 *            if this is the first time a login screen is being shown
	 */
	public void initializeLogin(boolean first) {
		if (!first) {
			try {
				URL r = getClass().getResource("login.fxml");
				if (r == null)
					throw new Exception("No FXML resource found.");
				Scene scene = new Scene(FXMLLoader.load(r));
				Stage stage = new Stage();
				stage.setTitle("Login");
				stage.setScene(scene);
				stage.sizeToScene();
				stage.show();

			} catch (Exception e) {
				System.out.println(e.getMessage());
				e.printStackTrace();
			}
		}

	}

	/**
	 * Initializes variables corresponding to user inputs. Disables functionality
	 * that cannot be used without a world.
	 */
	@FXML
	public void initialize() {

		try {
			if (url != null)
				client = new Client(new URL(url));
			if (loginButton == null) {

				fpsSlider.valueProperty().addListener((obs, oldValue, newValue) -> updateFPS(newValue.intValue()));

				initNumTextArea(rowCritter);
				initNumTextArea(colCritter);
				initNumTextArea(numCritters);
				stepsPerSecond = 1;
				fpsArea.appendText(stepsPerSecond + "");
				updateRate = 33;
				loggedIn = true;

				client.connectMessages(messages);

				// Update display
				// Advancing time in the background

				Thread updateWorld = new Thread() {
					public void run() {
						int time = 1000 / updateRate;
						while (loggedIn) {
							WorldInfoBundle worldInfo = client.getWorld(priorVersion);

							if (worldInfo == null) {
								messages.appendText("No world\n");
								loadCritterButton.setDisable(true);
								startButton.setDisable(true);
								stopButton.setDisable(true);
								stepButton.setDisable(true);
								zoomOutButton.setDisable(true);
								zoomInButton.setDisable(true);
							} else {
								if(grid == null) {
									Platform.runLater(new Runnable() {
										@Override
										public void run() {
											finishWorld();
										}
									});
								}

								// somebody else loaded world
								if (startButton == null) {
									loadCritterButton.setDisable(false);
									startButton.setDisable(false);
									stopButton.setDisable(false);
									stepButton.setDisable(false);
									zoomOutButton.setDisable(false);
									zoomInButton.setDisable(false);
								}

								priorVersion = worldInfo.current_version_number;

								// Draws world in applicaiton thread
								Platform.runLater(new Runnable() {
									@Override
									public void run() {
										grid.draw(worldInfo);
										timeField.clear();
										timeField.appendText(worldInfo.current_timestep + "");
										critterCount.clear();
										critterCount.appendText(worldInfo.population + "");
									}

								});

							}

							try {
								sleep(time);
							} catch (InterruptedException e) {
								System.out.println("Problem sleeping");
							}
						}
					}

				};

				updateWorld.start();
			}
		} catch (MalformedURLException e) {
			messages.appendText("Bad url");
			e.printStackTrace();
		} catch (IOException e) {
			messages.appendText("Bad url");
		}
	}

}
