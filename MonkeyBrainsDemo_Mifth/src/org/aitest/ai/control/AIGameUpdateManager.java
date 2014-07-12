package org.aitest.ai.control;

import com.jme3.app.state.AbstractAppState;

/**
 * Class for controling that all updates are in 60fps.
 *
 * @author mifth
 */
public class AIGameUpdateManager extends AbstractAppState {

    private long lastFrame = System.nanoTime();
    private float currentTpf = 0f;
    private boolean update = false;
    // It's about 61fps as VSync can use less than 60fps for some frames.
    private final static double FRAMERATE = 0.0163;

    public boolean IsUpdate() {
        return update;
    }

    public float getCurrentTpf() {
        return currentTpf;
    }

    public void setCurrentTpf(float currentTpf) {
        this.currentTpf = currentTpf;
    }

    @Override
    public void update(float tpf) {
        // Use our own tpf calculation in case frame rate is
        // running away making this tpf unstable
        long time = System.nanoTime();
        long delta = time - lastFrame;
        double seconds = (delta / 1000000000.0);
        // Clamp frame time to no bigger than a certain amount 60fps
        if (seconds >= FRAMERATE) {
            lastFrame = time;
            update = true;
            currentTpf = (float) seconds;
            // Clamp to 3 seconds
            if (currentTpf > 3f) {
                currentTpf = (float) FRAMERATE;
            }
        } else {
            update = false;
        }
    }
}