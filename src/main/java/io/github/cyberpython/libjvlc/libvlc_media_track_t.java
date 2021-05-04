package io.github.cyberpython.libjvlc;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.Union;
import java.util.Arrays;
import java.util.List;
/**
 * This file was autogenerated by <a href="http://jnaerator.googlecode.com/">JNAerator</a>,<br>
 * a tool written by <a href="http://ochafik.com/">Olivier Chafik</a> that <a href="http://code.google.com/p/jnaerator/wiki/CreditsAndLicense">uses a few opensource projects.</a>.<br>
 * For help, please visit <a href="http://nativelibs4java.googlecode.com/">NativeLibs4Java</a> , <a href="http://rococoa.dev.java.net/">Rococoa</a>, or <a href="http://jna.dev.java.net/">JNA</a>.
 */
public class libvlc_media_track_t extends Structure {
	public int i_codec;
	public int i_original_fourcc;
	public int i_id;
	/** @see libvlc_track_type_t */
	public int i_type;
	public int i_profile;
	public int i_level;
	public field1_union field1;
	public int i_bitrate;
	public Pointer psz_language;
	public Pointer psz_description;
	public static class field1_union extends Union {
		public io.github.cyberpython.libjvlc.libvlc_audio_track_t.ByReference audio;
		public io.github.cyberpython.libjvlc.libvlc_video_track_t.ByReference video;
		public io.github.cyberpython.libjvlc.libvlc_subtitle_track_t.ByReference subtitle;
		public field1_union() {
			super();
		}
		public field1_union(io.github.cyberpython.libjvlc.libvlc_audio_track_t.ByReference audio) {
			super();
			this.audio = audio;
			setType(io.github.cyberpython.libjvlc.libvlc_audio_track_t.ByReference.class);
		}
		public field1_union(io.github.cyberpython.libjvlc.libvlc_video_track_t.ByReference video) {
			super();
			this.video = video;
			setType(io.github.cyberpython.libjvlc.libvlc_video_track_t.ByReference.class);
		}
		public field1_union(io.github.cyberpython.libjvlc.libvlc_subtitle_track_t.ByReference subtitle) {
			super();
			this.subtitle = subtitle;
			setType(io.github.cyberpython.libjvlc.libvlc_subtitle_track_t.ByReference.class);
		}
		public field1_union(Pointer peer) {
			super(peer);
		}
		public static class ByReference extends field1_union implements Structure.ByReference {
			
		};
		public static class ByValue extends field1_union implements Structure.ByValue {
			
		};
	};
	public libvlc_media_track_t() {
		super();
	}
	protected List<String> getFieldOrder() {
		return Arrays.asList("i_codec", "i_original_fourcc", "i_id", "i_type", "i_profile", "i_level", "field1", "i_bitrate", "psz_language", "psz_description");
	}
	public libvlc_media_track_t(Pointer peer) {
		super(peer);
	}
	public static class ByReference extends libvlc_media_track_t implements Structure.ByReference {
		
	};
	public static class ByValue extends libvlc_media_track_t implements Structure.ByValue {
		
	};
}