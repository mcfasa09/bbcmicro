package com.littlefluffytoys.beebdroid;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class BeebView extends SurfaceView implements SurfaceHolder.Callback {
	private static final String TAG = "BeebView";
	// Emulated display
	static final int W = 672;
	static final int H = 272 * 2;
	static final float ASPECT = ((float) H / (float) W);

	public EGL10 egl;
	int textureId; // Bitmap screen;

	int screenwidth;
	int screenheight;

	private Rect rcSrc;
	private Rect rcDst;
	private Paint paint;
	private Bitmap screen;
	public GL10 gl;
	private EGLConfig config;
	private EGLContext ctxt;
	public EGLDisplay display;
	public EGLSurface surface;
	public int width;
	public int height;

	public BeebView(Context context, AttributeSet attrs) {
		super(context, attrs);
		getHolder().addCallback(this);
		screen = Bitmap.createBitmap(W, H, Bitmap.Config.RGB_565);
		rcSrc = new Rect(0, 0, W, H / 2);
	}
	
	public Bitmap getScreenshot(){
		//Canvas canvas = getHolder().lockCanvas(); 
		//paint = new Paint();
		//canvas.drawBitmap(screen, rcSrc, rcDst, paint); 
		//getHolder().unlockCanvasAndPost(canvas);
		
		return null;
	}

	private void cleanupgl() {
		// Unbind and destroy the old EGL surface, if there is one.
		if (surface != null) {
			egl.eglMakeCurrent(display, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_CONTEXT);
			egl.eglDestroySurface(display, surface);
			surface = null;
		}
	}

	public void initgl() {
		cleanupgl();

		// Create new surface. Must succeed.
		surface = egl.eglCreateWindowSurface(display, config, getHolder(), null);
		if (null == surface) {
			throw new RuntimeException("eglCreateWindowSurface");
		}

		// Bind the rendering context to the surface.
		if (!egl.eglMakeCurrent(display, surface, surface, ctxt)) {
			throw new RuntimeException("eglMakeCurrent");
		}
		gl = (GL10) ctxt.getGL();

	}

	@Override
	public void onMeasure(int ws, int hs) {
		super.onMeasure(ws, hs);
		int w = getMeasuredWidth();
		int h = getMeasuredHeight();
		rcDst = new Rect(0, 0, w, h);// (int)(w * ASPECT));
		Log.d(TAG, "beebView is " + rcDst.width() + "x" + rcDst.height());
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// Lots of stunningly tedious EGL setup. All we want is a 565 surface.
		egl = (EGL10) EGLContext.getEGL();
		display = egl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);
		int[] version = new int[2];
		if (!egl.eglInitialize(display, version)) {
			throw new RuntimeException("eglInitialize failed");
		}
		/*
		 * int[] attrib_list = new int[] { EGL10.EGL_RED_SIZE, 5,
		 * EGL10.EGL_GREEN_SIZE, 6, EGL10.EGL_BLUE_SIZE, 5,
		 * EGL10.EGL_ALPHA_SIZE, 0, EGL10.EGL_DEPTH_SIZE, 0, EGL10.EGL_NONE };
		 * egl.eglGetConfigs(display, configs, config_size, num_config)
		 * EGLConfig[] configs = new EGLConfig[1]; int[] numConfigs = new int[]
		 * {1}; egl.eglChooseConfig(display, attrib_list, configs,
		 * configs.length, numConfigs); if (0 == numConfigs[0]) { throw new
		 * RuntimeException("No matching EGL config"); } config = configs[0];
		 */

		config = getEglConfig565(egl, display);

		ctxt = egl.eglCreateContext(display, config, EGL10.EGL_NO_CONTEXT, null);

	}
	
	private EGLConfig getEglConfig565(EGL10 egl, EGLDisplay display) {
		int[] version = new int[2];
		egl.eglInitialize(display, version);

		EGLConfig[] conf = new EGLConfig[100];
		int[] num_conf = new int[100];
		egl.eglGetConfigs(display, conf, 100, num_conf);

		int[] red = new int[1];
		int[] blue = new int[1];
		int[] green = new int[1];

		for (int i = 0; i < 100; i++) {
			if (conf[i] == null)
				break;
			egl.eglGetConfigAttrib(display, conf[i], EGL10.EGL_RED_SIZE, red);
			egl.eglGetConfigAttrib(display, conf[i], EGL10.EGL_BLUE_SIZE, blue);
			egl.eglGetConfigAttrib(display, conf[i], EGL10.EGL_GREEN_SIZE, green);
			if (red[0] == 5 && green[0] == 6 && blue[0] == 5) {
				return conf[i];
			}
		}
		return null;
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		Log.d(TAG, "surfaceChanged");
		this.width = width;
		this.height = height;
		gl = null;
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		Log.d(TAG, "surfaceDestroyed");
		cleanupgl();
		egl = null;
	}

	/*
	 * public void blit(int wh) { screenwidth = wh & 0xffff; if
	 * (screenwidth<=320) screenwidth*=2; screenheight = wh>>16;
	 * 
	 * // Try to fill the display properly //rcSrc.set(0,0,W, H); //int adj = (W
	 * - screenwidth)/2; //rcSrc.inset(adj, 0); //int w = getMeasuredWidth();
	 * //float fh = w * ASPECT; //fh *= 1.2f + (adj/(float)W); //rcDst.set(0,0,
	 * w, (int)fh);
	 * 
	 * Canvas canvas = getHolder().lockCanvas(); canvas.drawBitmap(screen,
	 * rcSrc, rcDst, paint); getHolder().unlockCanvasAndPost(canvas); }
	 */

}
