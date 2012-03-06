package org.openkinect.processing;

import java.lang.reflect.Method;
import java.nio.ShortBuffer;

import org.openkinect.freenect.Freenect;
import org.openkinect.freenect.Context;
import org.openkinect.freenect.Device;
import org.openkinect.freenect.VideoFormat;
import org.openkinect.freenect.DepthFormat;

import processing.core.PApplet;
import processing.core.PImage;

public class Kinect extends Thread {
    public static PApplet parent;

    boolean depth_registered = false;
    boolean running = false;

    Context context;
    Device device;

    int w = 640;
    int h = 480;
    RGBImage kimg;// = new KImage();
    DepthImage dimg;

    public Kinect(PApplet parent) {
        this.parent = parent;
    }

    public void start() {
        context = Freenect.createContext();

        if(context.numDevices() < 1) {
            System.err.println("No Kinect devices found.");
            return;
        }

        device = context.openDevice(0);
        device.setDepthFormat(DepthFormat.D11BIT);
        depth_registered = false;
        kimg = new RGBImage(parent);
        dimg = new DepthImage(parent);
        running = true;

        super.start();
    }

    public int[] getRawDepth() {
        ShortBuffer sb = dimg.getRawData();
        int[] depth = new int[w*h];

        // This is inefficent, but I think it's easier for Processing users to have an int array?
        if (sb != null) {
            for (int i = 0; i < depth.length; i++)
                depth[i] = sb.get(i);
        }
        /* Java arrays are automatically initialized to 0,
           so the result is a zero array if sb is null */
        return depth;
    }

    public void tilt(float deg) {
        device.setTiltAngle(deg);
    }

    public void processDepthImage(boolean b) {
        dimg.enableImage(b);
    }

    public void enableDepth(boolean b) {
        if (b) device.startDepth(dimg);
        else device.stopDepth();
    }

    public void enableDepth(boolean b, boolean registered) {
        device.stopDepth();
        if (registered) {
            device.setDepthFormat(DepthFormat.REGISTERED);
        } else {
            device.setDepthFormat(DepthFormat.D11BIT);
        }
        depth_registered = registered;
        enableDepth(b);
    }

    public boolean getDepthRegistered() {
        return depth_registered;
    }

    public void enableRGB(boolean b) {
        device.stopVideo();
        kimg.setIR(!b);
        if(b) {
            device.setVideoFormat(VideoFormat.RGB);
            device.startVideo(kimg);
        }
    }

    public void enableIR(boolean b) {
        device.stopVideo();
        kimg.setIR(b);
        if(b) {
            device.setVideoFormat(VideoFormat.IR_8BIT);
            device.startVideo(kimg);
        }
    }

    public float getVideoFPS() {
        return kimg.getFPS();
    }

    public float getDepthFPS() {
        return dimg.getFPS();
    }

    public PImage getVideoImage() {
        return kimg.img;
    }

    public PImage getDepthImage() {
        return dimg.img;
    }

    /**
     * This method should only be called internally by Thread.start().
     */
    public void run() {
        while (running) {
            try {
                Thread.sleep(2);
            } catch (InterruptedException e) {
            }
        }
    }

    /* Stops the client thread.  You don't really need to do this ever.
     */  
    public void quit() {
        System.err.println("quitting");
        device.close();
        context.shutdown();
        running = false;  // Setting running to false ends the loop in run()
    }
}