//Copyright (c) 2014, Jesús Martín Berlanga. All rights reserved. Distributed under the BSD licence. Read "com/jme3/ai/license.txt".
package steeringDemos.demos;

import com.jme3.ai.agents.Agent;
import com.jme3.ai.agents.behaviors.npc.SimpleMainBehavior;
import com.jme3.ai.agents.behaviors.npc.steering.SeekBehavior;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import steeringDemos.BasicDemo;
import steeringDemos.control.CustomSteerControl;

/**
 * Seek Demo
 *
 * @author Jesús Martín Berlanga
 * @version 1.0
 */
public class SeekDemo extends BasicDemo {

    private Agent target;
    private SeekBehavior targetMove;
    Vector3f[] locations = new Vector3f[]{
        new Vector3f(7, 0, 0),
        new Vector3f(0, 7, 0),
        new Vector3f(0, 0, 7)
    };
    private int currentFocus = -1;

    public static void main(String[] args) {
        SeekDemo app = new SeekDemo();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        this.steerControl = new CustomSteerControl(9, 1);
        this.steerControl.setCameraSettings(getCamera());
        this.steerControl.setFlyCameraSettings(getFlyByCamera());

        //defining rootNode for aiAppState processing
        aiAppState.setApp(this);
        aiAppState.setGameControl(new CustomSteerControl(5f));

        target = this.createBoid("Target", this.targetColor, 0.11f);

        for (Vector3f loc : this.locations) {
            this.createSphereHelper("Sphere " + loc.toString(), ColorRGBA.Yellow, 0.05f, loc);
        }

        aiAppState.addAgent(target); //Add the target to the aiAppState
        aiAppState.getGameControl().spawn(target, Vector3f.ZERO);
        this.setStats(
                target,
                this.targetMoveSpeed,
                this.targetRotationSpeed,
                this.targetMass,
                this.targetMaxForce);

        SimpleMainBehavior targetMainBehaviour = new SimpleMainBehavior(target);

        this.targetMove = new SeekBehavior(target, this.locations[0]);
        this.currentFocus = 0;
        targetMainBehaviour.addBehavior(this.targetMove);

        target.setMainBehaviour(targetMainBehaviour);

        aiAppState.start();
    }

    @Override
    public void simpleUpdate(float tpf) {
        aiAppState.update(tpf);

        if (this.target.distanceFromPosition(this.locations[this.currentFocus]) < 0.01f) {
            this.currentFocus++;

            if (this.currentFocus > this.locations.length - 1) {
                this.currentFocus = 0;
            }

            this.targetMove.setSeekingPosition(this.locations[this.currentFocus]);
        }
    }
}