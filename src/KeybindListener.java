/*
    Amos Cabudol
    CSPC-24500, Lewis University
    03/26/2021

    Breakout
    KeybindListener.java
 */

import java.util.HashMap;
import javafx.scene.Scene;
import javafx.event.EventHandler;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;

public class KeybindListener {

    private Scene currentScene = null;
    private HashMap<KeyCode, Boolean> keyStatus;

    private EventHandler<KeyEvent> onKeyPressed;
    private EventHandler<KeyEvent> onKeyReleased;
    private EventHandler<MouseEvent> onMouseExited;

    public KeybindListener(Scene scene) {
        this.keyStatus = new HashMap<>();

        this.onKeyPressed = input -> {
            this.keyStatus.put(input.getCode(), true);
            input.consume();
        };

        this.onKeyReleased = input -> {
            this.keyStatus.put(input.getCode(), false);
            input.consume();
        };

        this.currentScene = scene;
        this.currentScene.setOnKeyPressed(onKeyPressed);
        this.currentScene.setOnKeyReleased(onKeyReleased);
    }

    public boolean isPressed(KeyCode key) {
        return this.keyStatus.getOrDefault(key, false);
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
