/*
    Amos Cabudol
    CSPC-24500, Lewis University
    04/05/2021

    Breakout
    Game/BreakoutBall.java
 */

package Game;

import Simulation.SimulatedCircle;
import Simulation.SimulatedShape;
import Structures.Vector2;

public class BreakoutBall extends SimulatedCircle {

    private double speed;
    private Vector2 defaultBallPos;

    private BreakoutRoom room;
    private SimulatedShape lastCollided;

    public BreakoutBall(double radius, Vector2 position, BreakoutRoom room) {
        super(radius, position);

        this.room = room;
        BreakoutRoomSettings settings = this.room.getSettings();

        this.speed = settings.getDefaultBallSpeed();
        this.lastCollided = null;
        this.defaultBallPos = position;

        // Ball collision resolution
        this.setOnCollide(collision -> {
            incrementBallSpeed(collision.collidedShape);
            if (collision.collidedShape instanceof BreakoutPaddle) {
                BreakoutPaddle paddle = (BreakoutPaddle) collision.collidedShape;
                double angleAlpha = (this.getPosition().x - paddle.getPosition().x) / (paddle.getSize().x / 2);
                double angle = -90 + Math.sin(angleAlpha) * settings.getMinBallDeflection() + angleAlpha * (settings.getMaxBallDeflection() - settings.getMinBallDeflection());
                this.setVelocity(Vector2.rotationToVector(angle).product(this.speed));
            }
            else {
                this.setVelocity(this.getVelocity().reflect(collision.collisionAxis.normal()));

                // Clamp ball deflection
                if (this.velocity.y == 0 || Math.abs(Math.toDegrees((Math.atan(this.velocity.x / this.velocity.y)))) > this.room.getSettings().getMaxBallDeflection() + 1) {
                    double ySign = Math.signum(this.velocity.y);
                    if (ySign == 0) {
                        ySign = 1;
                    }
                    Vector2 deflection = Vector2.rotationToVector(-90 - this.room.getSettings().getMaxBallDeflection());
                    this.setVelocity(new Vector2(Math.abs(deflection.x) * Math.signum(this.velocity.x) * speed, Math.abs(deflection.y) * ySign * speed));
                }
            }
            this.moveTo(this.getPosition().sum(this.getVelocity().product(collision.leftOverTime)));
        });

        this.setVelocity(Vector2.rotationToVector(-90 - settings.getMaxBallDeflection() + Math.random() * settings.getMaxBallDeflection() * 2).product(this.speed));
        this.setAnchored(false);
        this.room.add(this);
    }

    public void incrementBallSpeed(SimulatedShape collided) {
        if (collided != lastCollided) {
            speed += this.room.getSettings().getBallSpeedIncrement();
            lastCollided = collided;
        }
    }

    public void reset() {
        BreakoutRoomSettings settings = this.room.getSettings();
        this.speed = settings.getDefaultBallSpeed();
        this.setVelocity(Vector2.rotationToVector(-90 - settings.getMaxBallDeflection() + Math.random() * settings.getMaxBallDeflection() * 2).product(this.speed));
    }

    @Override
    public void moveTo(Vector2 position) {
        super.moveTo(position);
        if (this.position.y > this.room.getSettings().sceneDimensions.y) {
            this.room.remove(this);
        }
    }
}
