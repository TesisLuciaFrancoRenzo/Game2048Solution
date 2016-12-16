package org.hol.game2048;

import ar.edu.unrc.coeus.tdlearning.interfaces.IAction;
import ar.edu.unrc.coeus.tdlearning.learning.ELearningStyle;
import ar.edu.unrc.coeus.tdlearning.learning.TDLambdaLearning;
import ar.edu.unrc.coeus.tdlearning.training.ntuple.NTupleSystem;
import ar.edu.unrc.coeus.tdlearning.training.ntuple.SamplePointValue;
import ar.edu.unrc.coeus.tdlearning.utils.FunctionUtils;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * JAVAONE 2014 - Create the Game 2048 with Java 8 and JavaFX [HOL3244]
 *
 * @authors bruno.borges@oracle.com @brunoborges & pereda@eii.uva.es @jperedadnr
 */
public
class Game2048
        extends Application {

    /**
     *
     */
    public static final int STEP = 45;
    private boolean      computeParallelBestPossibleAction;
    private GameManager  gameManager;
    private NTupleSystem nTupleSystem;

    /**
     * @param args the command line arguments
     */
    public static
    void main( String[] args ) {
        launch(args);
    }

    @Override
    public
    void init() {
        // TO-DO: Step 10. Load font when css is enabled
        if ( STEP >= 10 ) {
            Font.loadFont(Game2048.class.getResource("ClearSans-Bold.ttf").toExternalForm(), 10.0);
            InputStream                perceptronFile            = getClass().getResourceAsStream("/org/hol/game2048/trainedntuplas/Best.ser");
            Function< Double, Double > activationFunction        = FunctionUtils.LINEAR;
            Function< Double, Double > derivedActivationFunction = FunctionUtils.LINEAR_DERIVED;
            boolean                    concurrency               = false;
            int                        maxTile                   = 15;

            int[] nTuplesLength = new int[17];
            for ( int i = 0; i < 17; i++ ) {
                nTuplesLength[i] = 4;
            }

            ArrayList< SamplePointValue > allSamplePointPossibleValues = new ArrayList<>();
            allSamplePointPossibleValues.add(new SimpleTile());
            for ( int i = 1; i <= maxTile; i++ ) {
                allSamplePointPossibleValues.add(new SimpleTile((int) Math.pow(2, i)));
            }
            nTupleSystem = new NTupleSystem(allSamplePointPossibleValues, nTuplesLength, activationFunction, derivedActivationFunction, concurrency);
            try {
                nTupleSystem.load(perceptronFile);
            } catch ( IOException | ClassNotFoundException ex ) {
                Logger.getLogger(Game2048.class.getName()).log(Level.SEVERE, null, ex);
            }

            computeParallelBestPossibleAction = false;
        }
    }

    @Override
    public
    void start( Stage primaryStage ) {

        StackPane root = new StackPane();
        // TO-DO: Step 1. Add gameManager to root
        if ( STEP >= 1 ) {
            gameManager = new GameManager(nTupleSystem);
            root.getChildren().add(gameManager);
        }
        Scene scene = new Scene(root, 600, 700);
        // TO-DO: Step 10. Load css
        if ( STEP >= 10 ) {
            scene.getStylesheets().add(Game2048.class.getResource("game.css").toExternalForm());
            root.getStyleClass().addAll("game-root");
        }
        // TO-DO: Step 14. enable arrow keys to move the tiles
        if ( Game2048.STEP >= 14 ) {
            scene.setOnKeyPressed(ke -> {
                KeyCode keyCode = ke.getCode();
                if ( keyCode.isArrowKey() ) {
                    Direction dir = Direction.valueFor(keyCode);
                    gameManager.move(dir);
                } else if ( keyCode == KeyCode.SPACE && gameManager.getNTupleBoard().isCanMove() ) {
                    List< IAction > possibleActions = gameManager.listAllPossibleActions(gameManager.getNTupleBoard());
                    Direction bestAction = (Direction) TDLambdaLearning.computeBestPossibleAction(gameManager,
                            ELearningStyle.afterState,
                            gameManager.getNTupleBoard(),
                            possibleActions,
                            null,
                            computeParallelBestPossibleAction, null).getAction();
                    gameManager.move(bestAction);
                }
            });
        }

        primaryStage.setTitle("2048FX");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

}
