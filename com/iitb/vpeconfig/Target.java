package com.iitb.vpeconfig;


/*
 * Class which holds config details for each board type
 */
public class Target {
	protected String name;
	protected String protocol;
	protected int maximum_size=0;
	protected int speed;
	protected int low_fuses;
	protected int high_fuses;
	protected int extended_fuses=0x05;
	protected String path;
	protected String file;
	protected int unlock_bits;
	protected int lock_bits;
	protected String mcu;
	protected long f_cpu;
	protected String core;
	protected String variant;
	protected String VID;
	protected String PID;


	public String getName() {
		return name;
	}

	public String getProtocol() {
		return protocol;
	}

	public int getMaxSize() {
		return maximum_size;
	}

	public int getSpeed() {
		return speed;
	}

	public int getLowFuses() {
		return low_fuses;
	}

	public int getHighFuses() {
		return high_fuses;
	}

	public int getExFuses() {
		return extended_fuses;
	}

	public String getPath() {
		return path;
	}

	public String getFile() {
		return file;
	}

	public int getUnlockBits() {
		return unlock_bits;
	}

	public int getLockBits() {
		return lock_bits;
	}

	public String getMCU() {
		return mcu;
	}

	public long getFCPU() {
		return f_cpu;
	}

	public String getCore() {
		return core;
	}

	public String getVariant() {
		return variant;
	}
	public String getVID() {
		return VID;
	}
	public String getPID() {
		return PID;
	}
}

class Uno extends Target {
	Uno (){
		name="Arduino Uno";
		protocol="arduino";
		maximum_size=32256;
		speed=115200;
		low_fuses=0xff;
		high_fuses=0xde;
		extended_fuses=0x05;
		path="optiboot";
		file="optiboot_hex";
		unlock_bits=0x3F;
		lock_bits=0x0F;
		mcu="atmega328p";
		f_cpu=16000000L;
		core="arduino";
		variant="standard";

	}
}
class Atmega328 extends Target {

	Atmega328() {
		name="Arduino Duemilanove w/ ATmega328";

		protocol="arduino";
		maximum_size=30720;
		speed=57600;

		low_fuses=0xFF;
		high_fuses=0xDA;
		extended_fuses=0x05;
		path="atmega";
		file="ATmegaBOOT_168_hex";
		unlock_bits=0x3F;
		lock_bits=0x0F;

		mcu="atmega328p";
		f_cpu=16000000L;
		core="arduino";
		variant="standard";
	}

}