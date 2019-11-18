package com.idlefish.flutterboost;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.util.AttributeSet;
import android.view.Surface;
import android.view.TextureView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import io.flutter.Log;
import io.flutter.embedding.engine.renderer.FlutterRenderer;
import io.flutter.embedding.engine.renderer.FlutterUiDisplayListener;
import io.flutter.embedding.engine.renderer.RenderSurface;

public class XFlutterTextureView extends TextureView implements RenderSurface {
    private static final String TAG = "XFlutterTextureView";
    private boolean isSurfaceAvailableForRendering;
    private boolean isAttachedToFlutterRenderer;
    @Nullable
    private FlutterRenderer flutterRenderer;
    @NonNull
    private Set<FlutterUiDisplayListener> onFlutterUiDisplayListeners;
    private final SurfaceTextureListener surfaceTextureListener;

    public XFlutterTextureView(@NonNull Context context) {
        this(context, (AttributeSet)null);
    }

    public XFlutterTextureView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.isSurfaceAvailableForRendering = false;
        this.isAttachedToFlutterRenderer = false;
        this.onFlutterUiDisplayListeners = new HashSet<>();
        this.surfaceTextureListener = new SurfaceTextureListener() {
            public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
                Log.v("FlutterTextureView", "SurfaceTextureListener.onSurfaceTextureAvailable()");
                XFlutterTextureView.this.isSurfaceAvailableForRendering = true;
                if (XFlutterTextureView.this.isAttachedToFlutterRenderer) {
                    XFlutterTextureView.this.connectSurfaceToRenderer();
                }

            }

            public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surface, int width, int height) {
                Log.v("FlutterTextureView", "SurfaceTextureListener.onSurfaceTextureSizeChanged()");
                if (XFlutterTextureView.this.isAttachedToFlutterRenderer) {
                    XFlutterTextureView.this.changeSurfaceSize(width, height);
                }

            }

            public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surface) {
            }

            public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surface) {
                Log.v("FlutterTextureView", "SurfaceTextureListener.onSurfaceTextureDestroyed()");
                XFlutterTextureView.this.isSurfaceAvailableForRendering = false;
                if (XFlutterTextureView.this.isAttachedToFlutterRenderer) {
                    XFlutterTextureView.this.disconnectSurfaceFromRenderer();
                }

                return true;
            }
        };
        this.init();
    }

    private void init() {
        this.setSurfaceTextureListener(this.surfaceTextureListener);
    }

    public void attachToRenderer(@NonNull FlutterRenderer flutterRenderer) {
        Log.v("FlutterTextureView", "Attaching to FlutterRenderer.");
        if (this.flutterRenderer != null) {
            Log.v("FlutterTextureView", "Already connected to a FlutterRenderer. Detaching from old one and attaching to new one.");
            this.flutterRenderer.stopRenderingToSurface();
        }

        this.flutterRenderer = flutterRenderer;
        this.isAttachedToFlutterRenderer = true;
        if (this.isSurfaceAvailableForRendering) {
            Log.v("FlutterTextureView", "Surface is available for rendering. Connecting FlutterRenderer to Android surface.");
            this.connectSurfaceToRenderer();
        }

    }

    public void detachFromRenderer() {
        if (this.flutterRenderer != null) {
            if (this.getWindowToken() != null) {
                Log.v("FlutterTextureView", "Disconnecting FlutterRenderer from Android surface.");
                this.disconnectSurfaceFromRenderer();
            }

            this.flutterRenderer = null;
            this.isAttachedToFlutterRenderer = false;
        } else {
            Log.w("FlutterTextureView", "detachFromRenderer() invoked when no FlutterRenderer was attached.");
        }

    }

    private void connectSurfaceToRenderer() {
        if (this.flutterRenderer != null && this.getSurfaceTexture() != null) {
            Surface surface= new Surface(this.getSurfaceTexture());
//            this.flutterRenderer.surfaceCreated(surface);
            flutterRenderer.startRenderingToSurface(new Surface(getSurfaceTexture()));
            surface.release();
        } else {
            throw new IllegalStateException("connectSurfaceToRenderer() should only be called when flutterRenderer and getSurfaceTexture() are non-null.");
        }
    }

    private void changeSurfaceSize(int width, int height) {
        if (this.flutterRenderer == null) {
            throw new IllegalStateException("changeSurfaceSize() should only be called when flutterRenderer is non-null.");
        } else {
            Log.v("FlutterTextureView", "Notifying FlutterRenderer that Android surface size has changed to " + width + " x " + height);
            this.flutterRenderer.surfaceChanged(width, height);
        }
    }

    private void disconnectSurfaceFromRenderer() {
        if (this.flutterRenderer == null) {
            throw new IllegalStateException("disconnectSurfaceFromRenderer() should only be called when flutterRenderer is non-null.");
        } else {
//            this.flutterRenderer.surfaceDestroyed();
            flutterRenderer.stopRenderingToSurface();
        }
    }

    public void addOnFirstFrameRenderedListener(@NonNull FlutterUiDisplayListener listener) {
        this.onFlutterUiDisplayListeners.add(listener);
    }

    public void removeOnFirstFrameRenderedListener(@NonNull FlutterUiDisplayListener listener) {
        this.onFlutterUiDisplayListeners.remove(listener);
    }

    public void onFirstFrameRendered() {
        Log.v("FlutterTextureView", "onFirstFrameRendered()");
        Iterator var1 = this.onFlutterUiDisplayListeners.iterator();

        while(var1.hasNext()) {
            FlutterUiDisplayListener listener = (FlutterUiDisplayListener)var1.next();
            listener.onFlutterUiDisplayed();
        }

    }

    @Nullable
    @Override
    public FlutterRenderer getAttachedRenderer() {
        return flutterRenderer;
    }
}