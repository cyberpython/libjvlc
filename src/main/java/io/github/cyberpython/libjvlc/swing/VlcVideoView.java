package io.github.cyberpython.libjvlc.swing;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Container;
import java.awt.Window;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

import org.apache.commons.lang3.SystemUtils;

import io.github.cyberpython.libjvlc.VlcLibrary;
import io.github.cyberpython.libjvlc.VlcLibrary.libvlc_event_t;

/**
 * Swing component that wraps a {@linkplain java.awt.Canvas} and uses libvlc
 * to render video on it.
 */
public final class VlcVideoView extends JPanel {

  public static enum PlaybackState {
    NO_MEDIA, PLAYING, STOPPED, PAUSED, END_REACHED;
  }
  public static interface VlcVideoViewListener {
    
    public void mediaPositionChanged(long position, long duration);
    
    /**
     * Get notified about a change in the playback state.
     * @param newState The new playback state. If 
     *                 {@linkplain PlaybackState#END_REACHED}, then apps need to
     *                 call {@linkplain VlcVideoView#close()} and then
     *                 {@linkplain VlcVideoView#load()} again.
     */
    public void playbackStateChanged(PlaybackState newState);
    
    public void fullscreenChanged(boolean isFullscreen);

    public void volumeChanged(int newVolume);

  }

  private Canvas videoCanvas;
  private PointerByReference vlcInstance;
  private PointerByReference media;
  private PointerByReference mediaPlayer;
  private PointerByReference eventManager;
  private VlcLibrary vlc;
  private boolean loaded;
  private long duration;
  private boolean paused;
  private boolean fullscreen;

  private Set<VlcVideoViewListener> listeners;

  static {
    System.setProperty("VLC_VERBOSE", "0"); // disable libvlc's verbose output
  }

  public VlcVideoView() {
    super();
    this.setBackground(Color.BLACK);
    this.vlc = VlcLibrary.INSTANCE;
    this.videoCanvas = new Canvas();
    this.setLayout(new BorderLayout());
    this.add(videoCanvas);
    this.duration = 0;
    this.loaded = false;
    this.paused = false;
    this.fullscreen = false;
    this.listeners = new HashSet<>();
  }

  private static boolean isLocalFile(URI uri) {
    try {
      return uri.toURL().getProtocol().equals("file");
    } catch (MalformedURLException mue) {
      return false;
    }
  }

  private void releaseResources() {
    this.duration = 0;
    notifyListenersMediaPositionChanged(0, 0);
    if (media != null) {
      vlc.libvlc_media_release(media);
      media = null;
    }
    if (mediaPlayer != null) {
      vlc.libvlc_media_player_release(mediaPlayer);
      mediaPlayer = null;
    }
    if (vlcInstance != null) {
      vlc.libvlc_release(vlcInstance);
      vlcInstance = null;
    }
    notifyListenersPlaybackStateChanged(PlaybackState.NO_MEDIA);
  }

  /**
   * Loads the media from the given URI.
   * 
   * This operation currently blocks while trying to load and parse the media
   * and its information (may be a lengthy operation for media over slow 
   * networks).
   * 
   * WARNING: The component must be visible before calling this!
   * 
   * @return True if the load operation is successful, false otherwise.
   */
  public synchronized boolean load(URI mediaUri, String[] vlcLibArgs) {
    if (!isLoaded()) {

      vlcInstance = vlc.libvlc_new(vlcLibArgs == null ? 0 : vlcLibArgs.length, vlcLibArgs);
      if (vlcInstance == null) {
        return false;
      }

      PointerByReference media;

      if (isLocalFile(mediaUri)) {
        media = vlc.libvlc_media_new_path(vlcInstance, new File(mediaUri).getAbsolutePath());
      } else {
        media = vlc.libvlc_media_new_location(vlcInstance, mediaUri.toString());
      }

      if (media == null) {
        releaseResources();
        return false;
      }
      
      // Parse the media to get the duration:
      // TODO: Check libvlc version and if >= 3.0.0 call libvlc_media_parse_with_options
      vlc.libvlc_media_parse(media);
      this.duration = vlc.libvlc_media_get_duration(media);

      mediaPlayer = vlc.libvlc_media_player_new_from_media(media);

      if (mediaPlayer == null) {
        releaseResources();
        return false;
      }

      eventManager = vlc.libvlc_media_player_event_manager(mediaPlayer);

      final int libvlc_MediaPlayerEndReached = 265;

      vlc.libvlc_event_attach(eventManager, libvlc_MediaPlayerEndReached, new VlcLibrary.libvlc_callback_t(){

        @Override
        public void apply(libvlc_event_t p_event, Pointer p_data) {
          SwingUtilities.invokeLater(new Runnable(){
            @Override
            public void run() {
              notifyListenersPlaybackStateChanged(PlaybackState.END_REACHED);
            }
          });
        }
        
      }, null);


      final int libvlc_MediaPlayerTimeChanged = 267;

      vlc.libvlc_event_attach(eventManager, libvlc_MediaPlayerTimeChanged, new VlcLibrary.libvlc_callback_t(){

        @Override
        public void apply(libvlc_event_t p_event, Pointer p_data) {
          SwingUtilities.invokeLater(new Runnable(){
            @Override
            public void run() {
              notifyListenersMediaPositionChanged(getTime(), getDuration());
            }
          });
        }
        
      }, null);

      this.paused = false;

      // Disable VLC mouse input handling so that it can be intercepted by Swing / AWT:
      vlc.libvlc_video_set_mouse_input(mediaPlayer, 0);
      // Disable VLC keyboard input handling so that it can be intercepted by Swing / AWT:
      vlc.libvlc_video_set_key_input(mediaPlayer, 0);

      if (SystemUtils.IS_OS_WINDOWS) {
        vlc.libvlc_media_player_set_hwnd(mediaPlayer, Native.getComponentPointer(videoCanvas));
      } else if (SystemUtils.IS_OS_LINUX) {
        vlc.libvlc_media_player_set_xwindow(mediaPlayer, (int) Native.getComponentID(videoCanvas));
      } else {
        releaseResources();
        // TODO: consider throwing exception / logging the error
        return false;
      }
      
      SwingUtilities.invokeLater(new Runnable(){
        @Override
        public void run() {
          notifyListenersMediaPositionChanged(0, getDuration());
          notifyListenersPlaybackStateChanged(PlaybackState.STOPPED);
        }
      });

      setLoaded(true);

      return true;
    }
    return false;
  }

  public synchronized boolean isLoaded() {
    return loaded;
  }

  private synchronized void setLoaded(boolean loaded) {
    this.loaded = loaded;
  }

  /**
   * Starts playback.
   */
  public synchronized void play() {
    if (isLoaded()) {
      this.paused = false;
      vlc.libvlc_media_player_play(mediaPlayer);
      SwingUtilities.invokeLater(new Runnable(){
        @Override
        public void run() {
          notifyListenersPlaybackStateChanged(PlaybackState.PLAYING);
        }
      });
    }
  }

  /**
   * Stops playback.
   */
  public synchronized void stop() {
    if (isLoaded()) {
      this.paused = false;
      vlc.libvlc_media_player_stop(mediaPlayer);
      SwingUtilities.invokeLater(new Runnable(){
        @Override
        public void run() {
          notifyListenersPlaybackStateChanged(PlaybackState.STOPPED);
        }
      });
    }
  }

  /**
   * Toggles playback pause.
   */
  public synchronized void togglePause() {
    if (isLoaded()) {
      this.paused = !this.paused;
      vlc.libvlc_media_player_set_pause(mediaPlayer, paused?1:0);
      SwingUtilities.invokeLater(new Runnable(){
        @Override
        public void run() {
          notifyListenersPlaybackStateChanged(paused ? PlaybackState.PAUSED : PlaybackState.PLAYING);
        }
      });
    }
  }

  /**
   * Returns the playback's paused status.
   * @return True if playbacl is paused, false otherwise.
   */
  public synchronized boolean isPaused() {
    return paused;
  }

  /**
   * Stops playback and releases any resources associated to the loaded media.
   */
  public synchronized void close() {
    if (isLoaded()) {
      vlc.libvlc_media_player_stop(mediaPlayer);
      notifyListenersPlaybackStateChanged(PlaybackState.STOPPED);
      this.paused = false;
      setLoaded(false);
      releaseResources();
    }
  }

  /**
   * Sets the audio volume level
   * @param volume The volume level as a percentage (0 = mute, 100 = max)
   */
  public synchronized void setVolume(int volume){
    if(isLoaded()){
      vlc.libvlc_audio_set_volume(mediaPlayer, volume);
      notifyListenersVolumeChanged(volume);
    }
  }

  /**
   * Increases the audio volume level by 10%. 
   * Has no effect if volume level is already at 100%.
   */
  public synchronized void volumeUp(){
    if(isLoaded()){
      int volume = vlc.libvlc_audio_get_volume(mediaPlayer);
      volume = Math.min(volume+10, 100);
      vlc.libvlc_audio_set_volume(mediaPlayer, volume);
      notifyListenersVolumeChanged(volume);
    }
  }

  /**
   * Decreases the audio volume level by 10%. 
   * Has no effect if volume level is already at 0%.
   */
  public synchronized void volumeDown(){
    if(isLoaded()){
      int volume = vlc.libvlc_audio_get_volume(mediaPlayer);
      volume = Math.max(volume-10, 0);
      vlc.libvlc_audio_set_volume(mediaPlayer, volume);
      notifyListenersVolumeChanged(volume);
    }
  }

  /**
   * Returns the audio volume percentage (0 = mute, 100 = max)
   * @return The audio volume level. 0 if no media have been loaded.
   */
  public synchronized int getVolume(){
    if(isLoaded()){
      return vlc.libvlc_audio_get_volume(mediaPlayer);
    }
    return 0;
  }

  /**
   * Returns the currently loaded media duration.
   * 
   * @return The duration in milliseconds or 0 if no media have been loaded.
   */
  public synchronized long getDuration(){
    if(isLoaded()){
      return this.duration;
    }
    return 0;
  }

  /**
   * Returns the currently loaded media time.
   * 
   * @return The time in milliseconds or 0 if no media have been loaded.
   */
  public synchronized long getTime(){
    if(isLoaded()){
      return vlc.libvlc_media_player_get_time(mediaPlayer);
    }
    return 0;
  }

  /**
   * Sets the currently loaded media time.
   * 
   * Only has an effect if media have been loaded.
   * 
   * @param time The media time in milliseconds.
   */
  public synchronized void setTime(long time, boolean fast){
    if(isLoaded()){
      vlc.libvlc_media_player_set_time(mediaPlayer, time);
      SwingUtilities.invokeLater(new Runnable(){
        @Override
        public void run() {
          notifyListenersMediaPositionChanged(getTime(), getDuration());
        }
      });
    }
  }

  /**
   * Toggles fullscreen video playback.
   */
  public synchronized void toggleFullScreen(){
    Container topLevel  = getTopLevelAncestor();
    if(topLevel instanceof Window){
      Window window = (Window) topLevel;
      if(fullscreen){
        getGraphicsConfiguration().getDevice().setFullScreenWindow(null);
        fullscreen = false;
        notifyListenersFullscreenChanged(false);
      } else {
        getGraphicsConfiguration().getDevice().setFullScreenWindow(window);
        fullscreen = true;
        notifyListenersFullscreenChanged(true);
      }
    }
  }

  /**
   * Exits fullscreen video (if already in fullscreen mode).
   */
  public synchronized void exitFullScreen(){
    if(fullscreen){
      getGraphicsConfiguration().getDevice().setFullScreenWindow(null);
      fullscreen = false;
      notifyListenersFullscreenChanged(false);
    }
  }

  public synchronized void addListener(VlcVideoViewListener listener){
    this.listeners.add(listener);
  }

  public synchronized void removeListener(VlcVideoViewListener listener){
    this.listeners.remove(listener);
  }

  public synchronized void clearListeners(){
    this.listeners.clear();
  }

  private synchronized void notifyListenersMediaPositionChanged(long position, long duration){
    for(VlcVideoViewListener l : listeners){
      l.mediaPositionChanged(position, duration);
    }
  }

  private synchronized void notifyListenersPlaybackStateChanged(PlaybackState newPlaybackState){
    for(VlcVideoViewListener l : listeners){
      l.playbackStateChanged(newPlaybackState);
    }
  }

  private synchronized void notifyListenersVolumeChanged(int newVolume){
    for(VlcVideoViewListener l : listeners){
      l.volumeChanged(newVolume);
    }
  }

  private synchronized void notifyListenersFullscreenChanged(boolean isFullscreen){
    for(VlcVideoViewListener l : listeners){
      l.fullscreenChanged(isFullscreen);
    }
  }

  @Override
  public void requestFocus() {
    super.requestFocus();
    videoCanvas.requestFocus();
  }

  @Override
  public synchronized void addMouseListener(MouseListener l) {
    videoCanvas.addMouseListener(l);
  }

  @Override
  public synchronized void removeMouseListener(MouseListener l) {
    videoCanvas.removeMouseListener(l);
  }

  @Override
  public synchronized void addMouseMotionListener(MouseMotionListener l) {
    videoCanvas.addMouseMotionListener(l);
  }

  @Override
  public synchronized void removeMouseMotionListener(MouseMotionListener l) {
    videoCanvas.removeMouseMotionListener(l);
  }

  @Override
  public synchronized void addKeyListener(KeyListener l) {
    videoCanvas.addKeyListener(l);
  }

  @Override
  public synchronized void removeKeyListener(KeyListener l) {
    videoCanvas.removeKeyListener(l);
  }

}
