package io.github.cyberpython.libjvlc;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import java.util.Arrays;
import java.util.List;
/**
 * This file was autogenerated by <a href="http://jnaerator.googlecode.com/">JNAerator</a>,<br>
 * a tool written by <a href="http://ochafik.com/">Olivier Chafik</a> that <a href="http://code.google.com/p/jnaerator/wiki/CreditsAndLicense">uses a few opensource projects.</a>.<br>
 * For help, please visit <a href="http://nativelibs4java.googlecode.com/">NativeLibs4Java</a> , <a href="http://rococoa.dev.java.net/">Rococoa</a>, or <a href="http://jna.dev.java.net/">JNA</a>.
 */
public class libvlc_title_description_t extends Structure {
	/** < duration in milliseconds */
	public long i_duration;
	/** < title name */
	public Pointer psz_name;
	/** < info if item was recognized as a menu, interactive or plain content by the demuxer */
	public int i_flags;
	public libvlc_title_description_t() {
		super();
	}
	protected List<String> getFieldOrder() {
		return Arrays.asList("i_duration", "psz_name", "i_flags");
	}
	public libvlc_title_description_t(long i_duration, Pointer psz_name, int i_flags) {
		super();
		this.i_duration = i_duration;
		this.psz_name = psz_name;
		this.i_flags = i_flags;
	}
	public libvlc_title_description_t(Pointer peer) {
		super(peer);
	}
	public static class ByReference extends libvlc_title_description_t implements Structure.ByReference {
		
	};
	public static class ByValue extends libvlc_title_description_t implements Structure.ByValue {
		
	};
}
