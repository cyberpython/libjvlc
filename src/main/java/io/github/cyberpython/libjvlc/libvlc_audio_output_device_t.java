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
public class libvlc_audio_output_device_t extends Structure {
	/** < Next entry in list */
	public libvlc_audio_output_device_t.ByReference p_next;
	/** < Device identifier string */
	public Pointer psz_device;
	/** < User-friendly device description */
	public Pointer psz_description;
	public libvlc_audio_output_device_t() {
		super();
	}
	protected List<String> getFieldOrder() {
		return Arrays.asList("p_next", "psz_device", "psz_description");
	}
	public libvlc_audio_output_device_t(libvlc_audio_output_device_t.ByReference p_next, Pointer psz_device, Pointer psz_description) {
		super();
		this.p_next = p_next;
		this.psz_device = psz_device;
		this.psz_description = psz_description;
	}
	public libvlc_audio_output_device_t(Pointer peer) {
		super(peer);
	}
	public static class ByReference extends libvlc_audio_output_device_t implements Structure.ByReference {
		
	};
	public static class ByValue extends libvlc_audio_output_device_t implements Structure.ByValue {
		
	};
}
