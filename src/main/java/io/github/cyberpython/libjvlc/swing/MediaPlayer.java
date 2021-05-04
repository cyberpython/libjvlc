package io.github.cyberpython.libjvlc.swing;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Rectangle2D;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.MouseInputAdapter;

import org.apache.commons.lang3.SystemUtils;

import io.github.cyberpython.libjvlc.swing.VlcVideoView.PlaybackState;
import io.github.cyberpython.libjvlc.swing.VlcVideoView.VlcVideoViewListener;

/**
 * Sample media player Swing component built on top of {@linkplain VlcVideoView}.
 */
public class MediaPlayer extends JPanel {

    private static class SeekBar extends JComponent {

        private static final int HEIGHT = 5;

        private VlcVideoView view;

        public SeekBar(VlcVideoView view){
            super();
            this.view = view;
            setBackground(new Color(134, 135, 135));
            addMouseMotionListener(new MouseInputAdapter(){
                @Override
                public void mouseMoved(MouseEvent e) {
                    if(view.isLoaded()){
                        int mouseX = e.getX();
                        long totalDuration = view.getDuration() / 1000;
                        long timeAtCursor = Math.min((int)Math.round(((double)mouseX / (double)getWidth()) * totalDuration), totalDuration);
                        timeAtCursor = Math.max(0, timeAtCursor);
                        setToolTipText(String.format("%02d:%02d:%02d", timeAtCursor / 3600, (timeAtCursor % 3600) / 60, (timeAtCursor % 60)));
                    }else {
                        setToolTipText(null);
                    }
                }
            });
            addMouseListener(new MouseInputAdapter(){
                @Override
                public void mouseReleased(MouseEvent e) {
                    if(view.isLoaded()){
                        if(e.getButton() == MouseEvent.BUTTON1){
                            int mouseX = e.getX();
                            int progressBarVal = Math.min((int)Math.round(((double)mouseX / (double)getWidth()) * (view.getDuration()/1000)), (int)view.getDuration()/1000);
                            progressBarVal = Math.max(0, progressBarVal);
                            view.setTime(progressBarVal * 1000, true);
                            repaint();
                        }
                    }
                }
            });

            view.addListener(new VlcVideoViewListener(){

                @Override
                public void mediaPositionChanged(long position, long duration) {
                    repaint();
                }

                @Override
                public void playbackStateChanged(PlaybackState newState) {
                    repaint();
                }

                @Override
                public void fullscreenChanged(boolean isFullscreen) {
                    repaint();
                }

                @Override
                public void volumeChanged(int newVolume) {
                    // Do nothing
                }
                
            });
        }

        @Override
        public Dimension getMaximumSize() {
            return new Dimension((int)super.getMaximumSize().getWidth(), HEIGHT);
        }

        @Override
        public Dimension getMinimumSize() {
            return new Dimension((int)super.getMinimumSize().getWidth(), HEIGHT);
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension((int)super.getPreferredSize().getWidth(), HEIGHT);
        }

        @Override
        public void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            Stroke stroke = new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER, 1.0f);
            g2d.setStroke(stroke);
            float w = getWidth();
            float h = getHeight();
            g2d.setPaint(getBackground());
            g2d.fill(new Rectangle2D.Float(0,0,w,h));
            if(view.isLoaded() && view.getDuration() > 0){
                    g2d.setPaint(Color.RED);
                    float total = view.getDuration() / 1000;
                    float current = view.getTime() / 1000;
                    float rw = current * w / total;
                    g2d.fill(new Rectangle2D.Float(0,0,rw,h));
            }
            g2d.dispose();
        }

    }

    

    private static class MediaStatusLabel extends JLabel {


        private VlcVideoView view;

        public MediaStatusLabel(VlcVideoView view) {
            this.view = view;
            setForeground(Color.WHITE);
            setBackground(Color.BLACK);
            setText("--:--:-- / --:--:--");
            view.addListener(new VlcVideoViewListener(){

                @Override
                public void mediaPositionChanged(long position, long duration) {
                    updateMediaStatusIndicator();
                }

                @Override
                public void playbackStateChanged(PlaybackState newState) {
                    updateMediaStatusIndicator();
                }

                @Override
                public void fullscreenChanged(boolean isFullscreen) {
                    // Do nothing
                }

                @Override
                public void volumeChanged(int newVolume) {
                    // Do nothing
                }
                
            });
        }

        private void updateMediaStatusIndicator(){
            if(view.isLoaded()){
                if(view.isPaused()){
                    setText("Paused");
                } else {
                    long currentMediaTime = view.getTime() / 1000;
                    long duration = view.getDuration()/ 1000;
                    if(duration == 0L){
                        setText(String.format("%02d:%02d:%02d / --:--:--", currentMediaTime / 3600, (currentMediaTime % 3600) / 60, (currentMediaTime % 60)));
                    } else {
                        setText(String.format("%02d:%02d:%02d / %02d:%02d:%02d", currentMediaTime / 3600, (currentMediaTime % 3600) / 60, (currentMediaTime % 60), duration / 3600, (duration % 3600) / 60, (duration % 60)));
                    }
                }
            } else {
                setText("--:--:-- / --:--:--");
            }
        }

    }

    private VlcVideoView vlcView;

    public MediaPlayer() {
        setBackground(Color.BLACK);
        setLayout(new GridBagLayout());

        
        vlcView = new VlcVideoView();
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.gridheight = 1;
        c.gridwidth = 1;
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1;
        c.weighty = 1;
        c.insets = new Insets(0,0,0,0);
        add(vlcView, c);

        JPanel mediaControls = new JPanel();
        mediaControls.setBackground(Color.BLACK);
        mediaControls.setLayout(new GridBagLayout());
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 1;
        c.gridheight = 1;
        c.gridwidth = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.weighty = 0;
        c.anchor = GridBagConstraints.CENTER;
        c.insets = new Insets(0,0,0,0);
        add(mediaControls, c);


        SeekBar mediaPositionControl = new SeekBar(vlcView);
        c = new GridBagConstraints();

        c.gridx = 0;
        c.gridy = 0;
        c.gridheight = 1;
        c.gridwidth = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.weighty = 0;
        c.anchor = GridBagConstraints.CENTER;
        c.insets = new Insets(0,10,10,10);
        mediaControls.add(mediaPositionControl, c);

        JLabel mediaStatusIndicator = new MediaStatusLabel(vlcView);
        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 0;
        c.gridheight = 1;
        c.gridwidth = 1;
        c.fill = GridBagConstraints.NONE;
        c.weightx = 0;
        c.weighty = 0;
        c.anchor = GridBagConstraints.CENTER;
        c.insets = new Insets(0,0,10,10);
        mediaControls.add(mediaStatusIndicator, c);
        
        

        vlcView.addListener(new VlcVideoViewListener(){

            @Override
            public void mediaPositionChanged(long position, long duration) {
                // Do nothing
            }

            @Override
            public void playbackStateChanged(PlaybackState newState) {
                if(VlcVideoView.PlaybackState.END_REACHED.equals(newState)){
                    vlcView.close();
                }
            }

            @Override
            public void fullscreenChanged(boolean isFullscreen) {
                // Do nothing
            }

            @Override
            public void volumeChanged(int newVolume) {
                // Do nothing
            }
            
        });

        MouseInputAdapter mia = new MouseInputAdapter(){
            @Override
            public void mouseClicked(MouseEvent e) {
                if(e.getButton() == MouseEvent.BUTTON1){
                    if(e.getClickCount() == 2 && !e.isConsumed()){
                        vlcView.toggleFullScreen();
                        e.consume();
                    } 
                    if(e.getClickCount() == 1 && !e.isConsumed()){
                        vlcView.togglePause();
                        e.consume();
                    }
                }
            }

            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                int volumeChange = -1 * e.getWheelRotation() * 10;
                int newVolume = vlcView.getVolume() + volumeChange;
                if(newVolume < 0){
                    newVolume = 0;
                }
                if(newVolume > 100){
                    newVolume = 100;
                }
                vlcView.setVolume(newVolume);
            }
            
        };
        vlcView.addMouseListener(mia);
        vlcView.addMouseWheelListener(mia);

        vlcView.addKeyListener(new KeyListener(){

            @Override
            public void keyTyped(KeyEvent e) {
                // Do nothing
            }

            @Override
            public void keyPressed(KeyEvent e) {
                switch(e.getKeyCode()){
                    case KeyEvent.VK_ESCAPE:
                        vlcView.exitFullScreen();
                        break;
                    case KeyEvent.VK_SPACE:
                        vlcView.togglePause();
                        break;
                    default: break;
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                // Do nothing
            }

        });

    }

    @Override
    public void requestFocus() {
        vlcView.requestFocus();
    }

    public void load(URI mediaUri, String[] vlcLibArgs){
        vlcView.load(mediaUri, vlcLibArgs);
    }

    public void play(){
        vlcView.play();
    }

    public void close(){
        vlcView.close();
    }

    public void stop(){
        vlcView.stop();
    }

    public void togglePause(){
        vlcView.togglePause();
    }

    public void toggleFullScreen(){
        vlcView.toggleFullScreen();
    }

    public static void main(String[] args) {

        JFrame frame1 = new JFrame("LibVLC Swing Demo");
        frame1.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame1.setSize(800, 600);
        MediaPlayer player = new MediaPlayer();

        frame1.setContentPane(player);
        
        frame1.setVisible(true);

        player.requestFocus();


        try{
            if(SystemUtils.IS_OS_LINUX){
                player.load(new URI(args[0]), new String[]{"--no-xlib"});
            } else {
                player.load(new URI(args[0]), null);
            }

            player.play();
            
            Runtime.getRuntime().addShutdownHook(new Thread(
                new Runnable(){
                    public void run() {
                        player.close();
                    };
                }
            ));
        }catch(URISyntaxException use){
            use.printStackTrace();
        }
    }
}
