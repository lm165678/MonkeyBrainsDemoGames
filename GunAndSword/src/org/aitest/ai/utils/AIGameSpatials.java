/**
 * Copyright (c) 2014, jMonkeyEngine All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * Neither the name of 'jMonkeyEngine' nor the names of its contributors may be
 * used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package org.aitest.ai.utils;

import com.jme3.ai.agents.Agent;
import com.jme3.ai.agents.util.control.MonkeyBrainsAppState;
import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.animation.SkeletonControl;
import com.jme3.asset.AssetManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.control.GhostControl;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.CameraNode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.CameraControl;
import com.jme3.scene.shape.Box;
import org.aitest.ai.model.AIModel;

/**
 * Spatials needed for game.
 *
 * @author Tihomir Radosavljević
 * @version 1.0.0
 */
public class AIGameSpatials {

    private AssetManager assetManager;
    private Geometry bulletGeom;

    private AIGameSpatials() {
        assetManager = MonkeyBrainsAppState.getInstance().getApp().getAssetManager();
        
        Box b = new Box(Vector3f.ZERO, 1f, 1f, 1f);
        bulletGeom = new Geometry("Box", b);
        bulletGeom.setLocalScale(0.1f, 0.1f, 0.5f);
        Material material = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        material.setColor("Color", ColorRGBA.Red);
        bulletGeom.setMaterial(material);
        
    }

    public static AIGameSpatials getInstance() {
        return AIGameSpatialHolder.INSTANCE;
    }

    private static class AIGameSpatialHolder {

        private static final AIGameSpatials INSTANCE = new AIGameSpatials();
    }

    /**
     * Attaching camera to agent.
     *
     * @param agent
     * @param cam
     */
    public void attachCameraTo(Agent agent, Camera cam) {
        //create the camera Node
        CameraNode camNode = new CameraNode("Camera Node", cam);
        //this mode means that camera copies the movements of the target
        camNode.setControlDir(CameraControl.ControlDirection.SpatialToCamera);
        //attach the camNode to the target
        ((Node) agent.getSpatial()).attachChild(camNode);
        //move camNode, e.g. behind and above the target
        camNode.setLocalTranslation(new Vector3f(0, 6, -18));
        //rotate the camNode to look at the target
        camNode.lookAt(agent.getSpatial().getLocalTranslation(), Vector3f.UNIT_Y);
        camNode.setLocalTranslation(new Vector3f(0, 12, -22));
        agent.setCamera(cam);
    }

    public void setGameLighting() {
        //setting ambiental lighting
        AmbientLight amb = new AmbientLight();
        amb.setColor(new ColorRGBA(0.7f, 0.8f, 1.0f, 1f));
        MonkeyBrainsAppState.getInstance().getRootNode().addLight(amb);

        //setting directional lighting
        DirectionalLight dl = new DirectionalLight();
        dl.setDirection(new Vector3f(-0.5501984f, -0.6679371f, 0.5011405f));
        dl.setColor(new ColorRGBA(1.0f, 1.0f, 0.7f, 1f));
        MonkeyBrainsAppState.getInstance().getRootNode().addLight(dl);
    }

    public void prepareModel(Agent agent, Node agentNode) {
        //for each part of agent spatial
        for (Spatial spatialPart : agentNode.getChildren()) {
            //make animation
            AnimControl animation = spatialPart.getControl(AnimControl.class);
            //there is no animation and there are more parts
            if (animation == null && spatialPart instanceof Node) {
                prepareModel(agent, (Node) spatialPart);
            } else {
                //there is some animation
                if (animation != null) {
                    //get skeleton control
                    SkeletonControl skeletonControl = spatialPart.getControl(SkeletonControl.class);
                    // PERFORMANCE IS MUCH MUCH BETTER WITH HW SKINNING
                    skeletonControl.setHardwareSkinningPreferred(true);

                    if (((GunAndSwordInventory) agent.getInventory()).getSword().getSpatial() == null) {
                        createSword(agent, skeletonControl, ((GunAndSwordInventory) agent.getInventory()).getSword().getName());
                    }

                    // return animation list?
                    if (!((AIModel) agent.getModel()).getAnimationList().contains(animation)) {
                        AnimChannel aniChannel = animation.createChannel();
                        aniChannel.setAnim("base_stand");
                        ((AIModel) agent.getModel()).getAnimationList().add(animation);
                    }
                }
            }
        }
    }

    public void createSword(Agent agent, SkeletonControl skeletonControl, String name) {
        Node swordModel = new Node(name);

        GhostControl gh = new GhostControl(new BoxCollisionShape(new Vector3f(0.3f, 1f, 0.3f)));
        swordModel.addControl(gh);
        //add sword to physic space
        MonkeyBrainsAppState.getInstance().getApp().getStateManager().getState(BulletAppState.class).getPhysicsSpace().add(gh);

        Node n = skeletonControl.getAttachmentsNode(name);
        n.attachChild(swordModel);
        ((GunAndSwordInventory) agent.getInventory()).getSword().setSpatial(swordModel);
    }

    public Geometry getBulletSpatial() {

        return bulletGeom.clone(false);
    }
}
