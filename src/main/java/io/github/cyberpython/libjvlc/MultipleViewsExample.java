package io.github.cyberpython.libjvlc;

import java.awt.GridLayout;
import java.io.File;

import javax.swing.JFrame;
import javax.swing.JPanel;

import io.github.cyberpython.libjvlc.swing.VlcVideoView;

public class MultipleViewsExample {
    public static void main(String[] args) {

        System.setProperty("VLC_VERBOSE", "0"); // disable libvlc's verbose output

        JFrame frame1 = new JFrame("VLC Demo (MP4 over HTTP & MP4 local)");
        frame1.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame1.setSize(800, 600);
        JPanel p = new JPanel();

        VlcVideoView view1 = new VlcVideoView();
        VlcVideoView view2 = new VlcVideoView();
        VlcVideoView view3 = new VlcVideoView();
        VlcVideoView view4 = new VlcVideoView();

        p.setLayout(new GridLayout(2, 2));
        p.add(view1);
        p.add(view2);
        p.add(view3);
        p.add(view4);
        frame1.setContentPane(p);

        frame1.setVisible(true);

        // try{
            view1.load(new File("test.mkv").toURI(), new String[]{"--no-xlib"});
            view2.load(new File("test.mkv").toURI(), new String[]{"--no-xlib"});
            // view2.load(new URI("https://www.scytalys.com/wp-content/uploads/2020/02/SCYTALYS-Corporate-Video-V7.mp4"));
            view3.load(new File("test.mkv").toURI(), new String[]{"--no-xlib"});
            view4.load(new File("test.mkv").toURI(), new String[]{"--no-xlib"});
            // view4.load(new URI("udp://@227.4.4.4:1234"));
            

            view1.play();
            view2.play();
            view3.play();
            view4.play();

        
        // } catch(URISyntaxException use){
        //     use.printStackTrace();
        // }
        
        Runtime.getRuntime().addShutdownHook(new Thread(
            new Runnable(){
                public void run() {
                    view1.close();
                    view2.close();
                    view3.close();
                    view4.close();
                };
            }
        ));
    }
}
