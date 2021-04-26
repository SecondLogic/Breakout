/*
    Amos Cabudol
    CSPC-24500, Lewis University
    03/26/2021

    Breakout
    Game/KeybindListener.java
 */

package Game;

import Structures.Vector2;

import java.util.HashMap;

import javafx.scene.Scene;
import javafx.event.EventHandler;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;

public class InputListener {

    private Scene currentScene = null;
    private HashMap<KeyCode, Boolean> keyStatus;

    private EventHandler<KeyEvent> onKeyPressed;
    private EventHandler<KeyEvent> onKeyReleased;
    private EventHandler<MouseEvent> onMouseMoved;

    private Vector2 mouseLocation;

    public InputListener(Scene scene) {
        this.keyStatus = new HashMap<>();

        this.mouseLocation = Vector2.ZERO;

        this.onKeyPressed = input -> {
            this.keyStatus.put(input.getCode(), true);
            input.consume();
        };

        this.onKeyReleased = input -> {
            this.keyStatus.put(input.getCode(), false);
            input.consume();
        };

        this.onMouseMoved = input -> {
            this.mouseLocation = new Vector2(input.getX(), input.getY());
            input.consume();
        };

        this.currentScene = scene;
        this.currentScene.setOnKeyPressed(onKeyPressed);
        this.currentScene.setOnKeyReleased(onKeyReleased);
        this.currentScene.setOnMouseMoved(onMouseMoved);
    }

    public boolean isPressed(KeyCode key) {
        return this.keyStatus.getOrDefault(key, false);
    }

    public Vector2 getMouseLocation() {
        return this.mouseLocation;
    }

    public void setScene(Scene scene) {
        if (!this.currentScene.equals(scene)) {
            if (!this.currentScene.equals(null)) {
                this.currentScene.setOnKeyPressed(null);
                this.currentScene.setOnKeyReleased(null);
            }

            this.currentScene = scene;
            this.currentScene.setOnKeyPressed(this.onKeyPressed);
            this.currentScene.setOnKeyReleased(this.onKeyReleased);
        }
    }
}
