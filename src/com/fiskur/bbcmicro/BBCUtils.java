package com.fiskur.bbcmicro;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import android.view.KeyEvent;

public class BBCUtils {
	private static Map<Character, Integer> mBBCKeyboardMap;
	private static Map<Character, Integer> mBBCKeyboardShiftMap;
	private static BBCUtils instance = null;
	public static final int BBCKEY_ENTER = 0x49;
	public static final int BBCKEY_SPACE = 0x62;
	
	public static BBCUtils getInstance(){
		if(instance == null){
			instance = new BBCUtils();
		}
		return instance;
	}
	
	private BBCUtils(){
		mBBCKeyboardMap = new HashMap<Character, Integer>();
		mBBCKeyboardShiftMap = new HashMap<Character, Integer>();
		//Numbers
		mBBCKeyboardMap.put('0', 0x27);
		mBBCKeyboardMap.put('1', 0x30);
		mBBCKeyboardMap.put('2', 0x31);
		mBBCKeyboardMap.put('3', 0x11);
		mBBCKeyboardMap.put('4', 0x12);
		mBBCKeyboardMap.put('5', 0x13);
		mBBCKeyboardMap.put('6', 0x34);
		mBBCKeyboardMap.put('7', 0x24);
		mBBCKeyboardMap.put('8', 0x15);
		mBBCKeyboardMap.put('9', 0x26);
		
		//Shift keys
		mBBCKeyboardShiftMap.put('!', 0x30);//1 + shift
		mBBCKeyboardShiftMap.put('"', 0x31);//2 + shift
		mBBCKeyboardShiftMap.put('#', 0x11);//3 + shift
		mBBCKeyboardShiftMap.put('$', 0x12);//4 + shift
		mBBCKeyboardShiftMap.put('%', 0x13);//5 + shift
		mBBCKeyboardShiftMap.put('&', 0x34);//6 + shift
		mBBCKeyboardShiftMap.put('\'', 0x24);//7 + shift
		mBBCKeyboardShiftMap.put('(', 0x15);//8 + shift
		mBBCKeyboardShiftMap.put(')', 0x26);//9 + shift
		
		//Letters
		mBBCKeyboardMap.put('a', 0x41);
		mBBCKeyboardMap.put('b', 0x64);
		mBBCKeyboardMap.put('c', 0x52);
		mBBCKeyboardMap.put('d', 0x32);
		mBBCKeyboardMap.put('e', 0x22);
		mBBCKeyboardMap.put('f', 0x43);
		mBBCKeyboardMap.put('g', 0x53);
		mBBCKeyboardMap.put('h', 0x54);
		mBBCKeyboardMap.put('i', 0x25);
		mBBCKeyboardMap.put('j', 0x45);
		mBBCKeyboardMap.put('k', 0x46);
		mBBCKeyboardMap.put('l', 0x56);
		mBBCKeyboardMap.put('m', 0x65);
		mBBCKeyboardMap.put('n', 0x55);
		mBBCKeyboardMap.put('o', 0x36);
		mBBCKeyboardMap.put('p', 0x37);
		mBBCKeyboardMap.put('q', 0x10);
		mBBCKeyboardMap.put('r', 0x33);
		mBBCKeyboardMap.put('s', 0x51);
		mBBCKeyboardMap.put('t', 0x23);
		mBBCKeyboardMap.put('u', 0x35);
		mBBCKeyboardMap.put('v', 0x63);
		mBBCKeyboardMap.put('w', 0x21);
		mBBCKeyboardMap.put('x', 0x42);
		mBBCKeyboardMap.put('y', 0x44);
		mBBCKeyboardMap.put('z', 0x61);
		
		//Other chars
		mBBCKeyboardMap.put('\n', BBCKEY_ENTER);
		mBBCKeyboardMap.put(' ', BBCKEY_SPACE);
		
		//Symbols
		mBBCKeyboardMap.put(',', 0x66);
		mBBCKeyboardMap.put('.', 0x67);
		mBBCKeyboardMap.put('/', 0x68);
		mBBCKeyboardMap.put(';', 0x57);
		mBBCKeyboardMap.put(':', 0x48);
		mBBCKeyboardMap.put(']', 0x58);
		mBBCKeyboardMap.put('@', 0x47);
		mBBCKeyboardMap.put('[', 0x38);
		mBBCKeyboardMap.put('-', 0x17);
		
		//Shift symbols
		mBBCKeyboardShiftMap.put('<', 0x66);//, + shift
		mBBCKeyboardShiftMap.put('>', 0x67);//. + shift
		mBBCKeyboardShiftMap.put('?', 0x68);/// + shift
		mBBCKeyboardShiftMap.put('+', 0x57);//; + shift
		mBBCKeyboardShiftMap.put('*', 0x48);//: + shift
		mBBCKeyboardShiftMap.put('}', 0x58);//] + shift
		mBBCKeyboardShiftMap.put('{', 0x38);//[ + shift
		mBBCKeyboardShiftMap.put('=', 0x17);//- + shift
	
	}
	
	public String[] getBBCKeyLabels(){
		
		String[] bbcKeylabels = new String[mBBCKeyboardMap.size() + mBBCKeyboardShiftMap.size()];
		int index = 0;
		for(Character key : mBBCKeyboardMap.keySet()){
			bbcKeylabels[index] = "" + key;
			index++;
		}
		for(Character key : mBBCKeyboardShiftMap.keySet()){
			bbcKeylabels[index] = "" + key;
			index++;
		}
		return bbcKeylabels;
	}
	
	public KeyMap[] getKeyMaps(){
		KeyMap[] keyMaps = new KeyMap[mBBCKeyboardMap.size() + mBBCKeyboardShiftMap.size()];
		int index = 0;
		for(Character key : mBBCKeyboardMap.keySet()){
			keyMaps[index] = new KeyMap(key, mBBCKeyboardMap.get(key), -1);
			index++;
		}
		for(Character key : mBBCKeyboardShiftMap.keySet()){
			keyMaps[index] = new KeyMap(key, mBBCKeyboardShiftMap.get(key), -1);
			index++;
		}
		return keyMaps;
	}
	
	public class KeyMap{
		char mKey;
		int mScanCode;
		int mRemapCode;
		
		public KeyMap(){
			
		}
		
		public KeyMap(char key, int scanCode, int remapCode){
			mKey = key;
			mScanCode = scanCode;
			mRemapCode = remapCode;
		}
		
		public void setChar(char key){
			mKey = key;
		}
		
		public char getKey(){
			return mKey;
		}
		
		public void setScanCode(int scanCode){
			mScanCode = scanCode;
		}
		
		public int getScanCode(){
			return mScanCode;
		}
		
		public void setRemapCode(int remapCode){
			mRemapCode = remapCode;
		}
		
		public int getRemapCode(){
			return mRemapCode;
		}
	}
	
	public int getShiftScanCode(char c){
		if(mBBCKeyboardShiftMap.containsKey(c)){
			return mBBCKeyboardShiftMap.get(c);
		}else{
			return -1; 
		}
	}
	
	public int getScanCode(char c){
		if(mBBCKeyboardMap.containsKey(c)){
			return mBBCKeyboardMap.get(c);
		}else{
			return -1; 
		}
	}	
	
	
	public static int lookupKeycode(boolean shiftDown, int keycode) {
		switch (keycode) {
		// case KeyEvent.KEYCODE_SHIFT_LEFT: return 0x00;
		// case KeyEvent.KEYCODE_SHIFT_RIGHT: return 0x00;
		case KeyEvent.KEYCODE_0:
			return shiftDown ? 0x126 : 0x27;
		case KeyEvent.KEYCODE_1:
			return 0x30;
		case KeyEvent.KEYCODE_2:
			return 0x31;
		case KeyEvent.KEYCODE_3:
			return 0x11;
		case KeyEvent.KEYCODE_4:
			return 0x12;
		case KeyEvent.KEYCODE_5:
			return 0x13;
		case KeyEvent.KEYCODE_6:
			return 0x34;
		case KeyEvent.KEYCODE_7:
			return shiftDown ? 0x134 : 0x24;
		case KeyEvent.KEYCODE_8:
			return 0x15;
		case KeyEvent.KEYCODE_9:
			return shiftDown ? 0x115 : 0x26;
		case KeyEvent.KEYCODE_A:
			return 0x41;
		case KeyEvent.KEYCODE_B:
			return 0x64;
		case KeyEvent.KEYCODE_C:
			return 0x52;
		case KeyEvent.KEYCODE_D:
			return 0x32;
		case KeyEvent.KEYCODE_E:
			return 0x22;
		case KeyEvent.KEYCODE_F:
			return 0x43;
		case KeyEvent.KEYCODE_G:
			return 0x53;
		case KeyEvent.KEYCODE_H:
			return 0x54;
		case KeyEvent.KEYCODE_I:
			return 0x25;
		case KeyEvent.KEYCODE_J:
			return 0x45;
		case KeyEvent.KEYCODE_K:
			return 0x46;
		case KeyEvent.KEYCODE_L:
			return 0x56;
		case KeyEvent.KEYCODE_M:
			return 0x65;
		case KeyEvent.KEYCODE_N:
			return 0x55;
		case KeyEvent.KEYCODE_O:
			return 0x36;
		case KeyEvent.KEYCODE_P:
			return 0x37;
		case KeyEvent.KEYCODE_Q:
			return 0x10;
		case KeyEvent.KEYCODE_R:
			return 0x33;
		case KeyEvent.KEYCODE_S:
			return 0x51;
		case KeyEvent.KEYCODE_T:
			return 0x23;
		case KeyEvent.KEYCODE_U:
			return 0x35;
		case KeyEvent.KEYCODE_V:
			return 0x63;
		case KeyEvent.KEYCODE_W:
			return 0x21;
		case KeyEvent.KEYCODE_X:
			return 0x42;
		case KeyEvent.KEYCODE_Y:
			return 0x44;
		case KeyEvent.KEYCODE_Z:
			return 0x61;
		case KeyEvent.KEYCODE_SPACE:
			return 0x62;
		case KeyEvent.KEYCODE_ENTER:
			return 0x49;
		case KeyEvent.KEYCODE_DEL:
			return 0x59;
		case KeyEvent.KEYCODE_APOSTROPHE:
			return shiftDown ? 0x31 : 0x124;
		case KeyEvent.KEYCODE_POUND:
			return 0x111; // '#' is Shift+3
		case KeyEvent.KEYCODE_MINUS:
			return shiftDown ? 0x238 : 0x17;
		case KeyEvent.KEYCODE_EQUALS:
			return 0x117;
		case KeyEvent.KEYCODE_AT:
			return 0x47;
		case KeyEvent.KEYCODE_STAR:
			return 0x148;
		case KeyEvent.KEYCODE_PERIOD:
			return shiftDown ? 0x248 : 0x67;
		case KeyEvent.KEYCODE_SEMICOLON:
			return 0x57;
		case KeyEvent.KEYCODE_SLASH:
			return 0x68;
		case KeyEvent.KEYCODE_PLUS:
			return 0x157;
		case KeyEvent.KEYCODE_COMMA:
			return 0x66;
			// case KeyEvent.KEYCODE_GRAVE: return 0x??;
			// case KeyEvent.KEYCODE_LEFT_BRACKET: return 0x??;
			// case KeyEvent.KEYCODE_RIGHT_BRACKET: return 0x??;
		}
		return 0xaa;
	}
	
	public ByteBuffer loadFile(File file) {
		InputStream strm;
		ByteBuffer buff = null;
		try {
			strm = new FileInputStream(file);
			int size = strm.available();
			buff = ByteBuffer.allocateDirect(size);
			byte[] localbuff = new byte[size];
			strm.read(localbuff, 0, size);
			strm.close();
			buff.put(localbuff);
			buff.position(0);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		return buff;
	}
	
	public static byte[] readInputStream(InputStream inputStream) throws IOException {
		byte[] buffer = new byte[4096];
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream(4096);

		// Do the first byte via a blocking read
		outputStream.write(inputStream.read());

		// Slurp the rest
		int available = 0;// inputStream.available();
		boolean run = true;
		while (run && (available = inputStream.available()) > 0) {
			// Log.d(TAG, "slurp " + available);
			while (available > 0) {
				int cbToRead = Math.min(buffer.length, available);
				int cbRead = inputStream.read(buffer, 0, cbToRead);
				if (cbRead <= 0) {
					run = false;
					break;
				}
				outputStream.write(buffer, 0, cbRead);
				available -= cbRead;
			}
		}
		return outputStream.toByteArray();
	}
	

	
	public static class BeebKeys {
		public static final int BBCKEY_BREAK = 0xaa;
		//  0x00    
		public static final int BBCKEY_SHIFT = 0x100;
		public static final int BBCKEY_CTRL = 0x01;
		// 	0x10     Q    3    4    5    F4   8    F7   -=   ^~
		public static final int BBCKEY_Q = 0x10;
		public static final int BBCKEY_3 = 0x11;
		public static final int BBCKEY_4 = 0x12;
		public static final int BBCKEY_5 = 0x13;
		public static final int BBCKEY_F4 = 0x14;
		public static final int BBCKEY_8 = 0x15;
		public static final int BBCKEY_F7 = 0x16;
		public static final int BBCKEY_MINUS = 0x17;
		public static final int BBCKEY_EQUALS = 0x117;
		public static final int BBCKEY_TILDE = 0x118;
		public static final int BBCKEY_CARET = 0x18;
		public static final int BBCKEY_ARROW_LEFT = 0x19;
		// 0x20     F0   W    E    T    7    I    9    0  ���_
		public static final int BBCKEY_F0 = 0x20;
		public static final int BBCKEY_W = 0x21;
		public static final int BBCKEY_E = 0x22;
		public static final int BBCKEY_T = 0x23;
		public static final int BBCKEY_7 = 0x24;
		public static final int BBCKEY_I = 0x25;
		public static final int BBCKEY_9 = 0x26;
		public static final int BBCKEY_0 = 0x27;
		public static final int BBCKEY_UNDERSCORE = 0x28;
		public static final int BBCKEY_POUND = 0x128;
		public static final int BBCKEY_ARROW_DOWN = 0x29;
		// 0x30     1    2    O    R    6   U    O    P    [(
		public static final int BBCKEY_1 = 0x30;
		public static final int BBCKEY_2 = 0x31;
		public static final int BBCKEY_D = 0x32;
		public static final int BBCKEY_R = 0x33;
		public static final int BBCKEY_6 = 0x34;
		public static final int BBCKEY_U = 0x35;
		public static final int BBCKEY_O = 0x36;
		public static final int BBCKEY_P = 0x37;
		public static final int BBCKEY_BRACKET_LEFT = 0x138;
		public static final int BBCKEY_BRACKET_LEFT_SQ = 0x38;
		public static final int BBCKEY_ARROW_UP = 0x39;
		// 0x40     CAP  A    X    F    Y    J    K    @    :*
		public static final int BBCKEY_CAPS = 0x40;
		public static final int BBCKEY_A = 0x41;
		public static final int BBCKEY_X = 0x42;
		public static final int BBCKEY_F = 0x43;
		public static final int BBCKEY_Y = 0x44;
		public static final int BBCKEY_J = 0x45;
		public static final int BBCKEY_K = 0x46;
		public static final int BBCKEY_AT = 0x47;
		public static final int BBCKEY_COLON = 0x48;
		public static final int BBCKEY_STAR = 0x148;
		public static final int BBCKEY_ENTER = 0x49;
		// 	0x50     SLC  S    C    G    H    N    L    ;+   ])
		public static final int BBCKEY_SHIFTLOCK = 0x50;
		public static final int BBCKEY_S = 0x51;
		public static final int BBCKEY_C = 0x52;
		public static final int BBCKEY_G = 0x53;
		public static final int BBCKEY_H = 0x54;
		public static final int BBCKEY_N = 0x55;
		public static final int BBCKEY_L = 0x56;
		public static final int BBCKEY_SEMICOLON = 0x57;
		public static final int BBCKEY_PLUS = 0x157;
		public static final int BBCKEY_BRACKET_RIGHT = 0x158;
		public static final int BBCKEY_BRACKET_RIGHT_SQ = 0x58;
		public static final int BBCKEY_DELETE = 0x59;
		// 0x60     TAB  Z    SPC  V    B    M    ,<   .>   /?
		public static final int BBCKEY_TAB = 0x60;
		public static final int BBCKEY_Z = 0x61;
		public static final int BBCKEY_SPACE = 0x62;
		public static final int BBCKEY_V = 0x63;
		public static final int BBCKEY_B = 0x64;
		public static final int BBCKEY_M = 0x65;
		public static final int BBCKEY_COMMA = 0x66;
		public static final int BBCKEY_LESS_THAN = 0x166;
		public static final int BBCKEY_PERIOD = 0x67;
		public static final int BBCKEY_MORE_THAN = 0x167;
		public static final int BBCKEY_SLASH = 0x68;
		public static final int BBCKEY_QUESTIONMARK = 0x168;
		public static final int BBCKEY_COPY = 0x69;
		// 0x70     ESC  F1   F2   F3   F5   F6   F8   F9   \| 
		public static final int BBCKEY_ESCAPE = 0x70;
		public static final int BBCKEY_F1 = 0x71;
		public static final int BBCKEY_F2 = 0x72;
		public static final int BBCKEY_F3 = 0x73;
		public static final int BBCKEY_F5 = 0x74;
		public static final int BBCKEY_F6 = 0x75;
		public static final int BBCKEY_F8 = 0x76;
		public static final int BBCKEY_F9 = 0x77;
		public static final int BBCKEY_BACKSLASH = 0x78;
		public static final int BBCKEY_ARROW_RIGHT = 0x79;
		
	}
	


}
