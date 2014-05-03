package org.jocean.image;


import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.jocean.idiom.AbstractReferenceCounted;
import org.jocean.idiom.Propertyable;
import org.jocean.idiom.block.BlockUtils;
import org.jocean.idiom.block.IntsBlob;
import org.jocean.idiom.block.RandomAccessInts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RawImage extends AbstractReferenceCounted<RawImage> 
    implements Propertyable<RawImage> {
    
    private static final Logger LOG = 
            LoggerFactory.getLogger(RawImage.class);
    
    public interface PixelArrayDrawer<T> {
        /**
        Treat the specified array of colors as a bitmap, and draw it. This gives the same result as first creating a bitmap from the array, and then drawing it, but this method avoids explicitly creating a bitmap object which can be more efficient if the colors are changing often.

        Parameters
        colors  Array of colors representing the pixels of the bitmap
        offset  Offset into the array of colors for the first pixel
        stride  The number of colors in the array between rows (must be >= width or <= -width).
        x   The X coordinate for where to draw the bitmap
        y   The Y coordinate for where to draw the bitmap
        width   The width of the bitmap
        height  The height of the bitmap
        hasAlpha    True if the alpha channel of the colors contains valid values. If false, the alpha byte is ignored (assumed to be 0xFF for every pixel).
        */
        public void drawPixelArray(T ctx, int[] colors, int offset, int stride, float x, float y, int width, int height, boolean hasAlpha);
    }
    
    public interface PixelDrawer<T> {
        /**
        Treat the specified array of colors as a bitmap, and draw it. This gives the same result as first creating a bitmap from the array, and then drawing it, but this method avoids explicitly creating a bitmap object which can be more efficient if the colors are changing often.

        Parameters
        colors  Array of colors representing the pixels of the bitmap
        offset  Offset into the array of colors for the first pixel
        stride  The number of colors in the array between rows (must be >= width or <= -width).
        x   The X coordinate for where to draw the bitmap
        y   The Y coordinate for where to draw the bitmap
        width   The width of the bitmap
        height  The height of the bitmap
        hasAlpha    True if the alpha channel of the colors contains valid values. If false, the alpha byte is ignored (assumed to be 0xFF for every pixel).
        */
        public void drawPixel(T ctx, int x, int y, int color);
    }
    
    public RawImage(final int w, final int h, final IntsBlob ints, final boolean hasAlpha) {
        this._width = w;
        this._height = h;
        this._ints = ints.retain();
        this._hasAlpha = hasAlpha;
    }
    
    @Override
    public <T> T getProperty(final String key) {
        return (T)this._properties.get(key);
    }

    @Override
    public <T> RawImage setProperty(final String key, T obj) {
        this._properties.put(key, obj);
        return this;
    }

    @Override
    public Map<String, Object> getProperties() {
        return Collections.unmodifiableMap(this._properties);
    }
    
    public int getSizeInByte() {
        return this._ints.length() * 4;
    }
    
    public int getWidth() {
        return this._width;
    }

    public int getHeight() {
        return this._height;
    }
    
    public RawImage createScaleImage(final float scaleRatio) {
        return createScaleImage( (int)(this._width * scaleRatio), (int)(this._height * scaleRatio));
    }
    
    public RawImage createScaleImage(final int w, final int h) {
        final IntsBlob ints = BlockUtils.createIntsBlob(w * h, this._ints.pool());
        
        boolean interpolate = true; // 插值模式   
        final int dstWidth = w;
        final int dstHeight = h;
        final int roiWidth = this._width;   
        final int roiHeight = this._height;
        final int width = roiWidth;
        double srcCenterX = roiWidth / 2.0;   
        double srcCenterY = roiHeight / 2.0;   
        double dstCenterX = dstWidth / 2.0;   
        double dstCenterY = dstHeight / 2.0;   
        double xScale = (double) dstWidth / roiWidth;   
        double yScale = (double) dstHeight / roiHeight;   
   
        double xlimit = width - 1.0, xlimit2 = width - 1.001;   
   
        if (interpolate) {   
            dstCenterX += xScale / 2.0;   
            dstCenterY += yScale / 2.0;   
        }   
   
        double xs, ys;   
        for (int y = 0; y <= dstHeight - 1; y++) {   
            ys = (y - dstCenterY) / yScale + srcCenterY;   
   
            for (int x = 0; x <= dstWidth - 1; x++) {   
                xs = (x - dstCenterX) / xScale + srcCenterX;   
                if (interpolate) {   
                    if (xs < 0.0)   
                        xs = 0.0;   
                    if (xs >= xlimit)   
                        xs = xlimit2; 
                    ints.writeAt(x + y*w, getInterpolatedPixel(xs, ys, width, this._ints));
                }   
            }   
        }
        
        final RawImage scaled = new RawImage(w, h, ints, this._hasAlpha);
        ints.release();
        scaled._properties.putAll(this.getProperties());
        return scaled;
    }

    public <T> void drawScale(final PixelDrawer<T> drawer, final T ctx, final int left, int top, int right, int bottom) {
        
        boolean interpolate = true; // 插值模式   
        final int dstWidth = right - left;
        final int dstHeight = bottom - top;
        final int roiWidth = this._width;   
        final int roiHeight = this._height;
        final int width = roiWidth;   
        double srcCenterX = roiWidth / 2.0;   
        double srcCenterY = roiHeight / 2.0;   
        double dstCenterX = dstWidth / 2.0;   
        double dstCenterY = dstHeight / 2.0;   
        double xScale = (double) dstWidth / roiWidth;   
        double yScale = (double) dstHeight / roiHeight;   
   
        double xlimit = width - 1.0, xlimit2 = width - 1.001;   
   
        if (interpolate) {   
            // if (xScale<=0.25 && yScale<=0.25){   
            // makeThumbnail();   
            // return ;   
            // }   
            dstCenterX += xScale / 2.0;   
            dstCenterY += yScale / 2.0;   
        }   
   
        double xs, ys;   
        for (int y = 0; y <= dstHeight - 1; y++) {   
            ys = (y - dstCenterY) / yScale + srcCenterY;   
   
            for (int x = 0; x <= dstWidth - 1; x++) {   
                xs = (x - dstCenterX) / xScale + srcCenterX;   
                if (interpolate) {   
                    if (xs < 0.0)   
                        xs = 0.0;   
                    if (xs >= xlimit)   
                        xs = xlimit2; 
                    
                    drawer.drawPixel(ctx, x + left, y + top, 
                            getInterpolatedPixel(xs, ys, width, this._ints));
                }   
            }   
        }   
    }
    
    private static final int xy2index(int x, int y, int w) {
        return y * w + x;
    }
    
    private static final int getInterpolatedPixel(final double x, final double y, final int w, final RandomAccessInts ints) {   
        int xbase = (int) x;   
        int ybase = (int) y;   
        double xFraction = x - xbase;   
        double yFraction = y - ybase;   
   
        int lowerLeft = ints.getAt(xy2index((int) x, (int) y, w));   
        // lowerLeft = lowerLeft << 8 >>> 8;   
        int all = (lowerLeft & 0xff000000) >> 24;
        int rll = (lowerLeft & 0xff0000) >> 16;   
        int gll = (lowerLeft & 0xff00) >> 8;   
        int bll = lowerLeft & 0xff;
   
        int lowerRight = ints.getAt(xy2index((int) x + 1, (int) y, w));
        // lowerRight = lowerRight << 8 >>> 8;   
        int alr = (lowerRight & 0xff000000) >> 24;
        int rlr = (lowerRight & 0xff0000) >> 16;   
        int glr = (lowerRight & 0xff00) >> 8;   
        int blr = lowerRight & 0xff;   
   
        int upperRight = ints.getAt(xy2index((int) x + 1, (int) y + 1, w));   
        // upperRight = upperRight << 8 >>> 8;   
        int aur = (upperRight & 0xff000000) >> 24;
        int rur = (upperRight & 0xff0000) >> 16;   
        int gur = (upperRight & 0xff00) >> 8;   
        int bur = upperRight & 0xff;   
   
        int upperLeft = ints.getAt(xy2index((int) x, (int) y + 1, w));
        // upperLeft = upperLeft << 8 >>> 8;   
        int aul = (upperLeft & 0xff000000) >> 24;
        int rul = (upperLeft & 0xff0000) >> 16;   
        int gul = (upperLeft & 0xff00) >> 8;   
        int bul = upperLeft & 0xff;   
   
        int a, r, g, b;   
        a = (all + alr + aur + aul) / 4;
        
        double upperAverage, lowerAverage;   
        upperAverage = rul + xFraction * (rur - rul);   
        lowerAverage = rll + xFraction * (rlr - rll);   
        r = (int) (lowerAverage + yFraction * (upperAverage - lowerAverage) + 0.5);   
        upperAverage = gul + xFraction * (gur - gul);   
        lowerAverage = gll + xFraction * (glr - gll);   
        g = (int) (lowerAverage + yFraction * (upperAverage - lowerAverage) + 0.5);   
        upperAverage = bul + xFraction * (bur - bul);   
        lowerAverage = bll + xFraction * (blr - bll);   
        b = (int) (lowerAverage + yFraction * (upperAverage - lowerAverage) + 0.5);   
        return ((a & 0xff) << 24) | ((r & 0xff) << 16) | ((g & 0xff) << 8) | b & 0xff;   
    }   
    
    /**
     * @param canvas
     */
    public <T> void drawDirect(final PixelArrayDrawer<T> drawer, final T ctx, final int left, int top) {
        int currentx = 0;
        int currenty = 0;
        
        for ( int idx = 0; idx < this._ints.totalBlockCount(); idx++) {
            
            //|         |#############|   <----- top 
            //#########################   <-----+
            //....                              |
            //....                              +-- body
            //....                              |
            //#########################   <-----+
            //#######                     <------ bottom
            final int[] colors = this._ints.getBlockAt(idx);
            int currentoffset = 0;
            int w = 0, h = 0; 
            int restLength = colors.length;
            
            if ( currentx > 0 ) {
                // draw top
                w = Math.min(this._width - currentx, restLength);
                h = 1;
                drawer.drawPixelArray(ctx, colors, currentoffset, this._width, 
                        left + currentx, top + currenty, w, h, this._hasAlpha);
                currentoffset += w;
                restLength -= w;
                currentx += w;
                if ( currentx == this._width ) {
                    // 递进到下一row
                    currentx = 0;
                    currenty++;
                }
            }
            if ( restLength > 0 ) {
                // draw body
                w = Math.min(this._width, restLength);
                h = restLength / w;
                drawer.drawPixelArray(ctx, colors, currentoffset, this._width, 
                        left + currentx, top + currenty, w, h, this._hasAlpha);
                currentoffset += w * h;
                restLength -= w * h;
                if ( h > 1 ) {
                    currentx = 0;
                    currenty += h;
                }
                else {
                    currentx += w;
                    if ( currentx == this._width ) {
                        // 递进到下一row
                        currentx = 0;
                        currenty++;
                    }
                }
            }
            
            if ( restLength > 0 ) {
                // draw bottom
                w = restLength;
                h = 1;
                drawer.drawPixelArray(ctx, colors, currentoffset, this._width, 
                        left + currentx, top + currenty, w, h, this._hasAlpha);
                currentx += w;
                if ( currentx == this._width ) {
                    // 递进到下一row
                    currentx = 0;
                    currenty++;
                }
            }
        }
    }
    
    @Override
    protected void deallocate() {
        if (  this._ints.release() ) {
            if ( LOG.isTraceEnabled() ) {
                LOG.trace("RawImage:prop({}) and Kbytes({}) release rawinfo succeed .", this._properties, 
                        this._width * this._height * 4.0f / 1024);
            }
        }
        else {
            LOG.warn("RawImage:prop({}) and Kbytes({}) !NOT! release it's rawinfo.", this._properties,
                    this._width * this._height * 4.0f / 1024);
        }
    }
    
    private final int _width;
    private final int _height;
    private final IntsBlob _ints;
    private final boolean _hasAlpha;
    private final Map<String, Object> _properties = new HashMap<String, Object>();
}
