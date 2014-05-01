/**
 * 
 */
package org.jocean.image;

import java.io.InputStream;

import org.jocean.idiom.block.BlockUtils;
import org.jocean.idiom.block.IntsBlob;
import org.jocean.idiom.block.WriteableInts;
import org.jocean.idiom.pool.BytesPool;
import org.jocean.idiom.pool.IntsPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ar.com.hjg.pngj.IImageLine;
import ar.com.hjg.pngj.ImageLineInt;
import ar.com.hjg.pngj.PngReader;

import com.alibaba.simpleimage.codec.jpeg.JPEGDecoder;
import com.alibaba.simpleimage.io.ImageBitsInputStream;

/**
 * @author isdom
 *
 */
public class ImageUtils {
    
    private static final Logger LOG = 
            LoggerFactory.getLogger(ImageUtils.class);

    public static final class Context {
        
        public Context(final BytesPool bytesPool, final IntsPool intsPool ) {
            this.bytesPool = bytesPool;
            this.intsPool = intsPool;
        }
        
        final BytesPool bytesPool;
        final IntsPool intsPool;
    }
    
    public static RawImage decodeJPEGStreamAsRawImage(final Context ctx, final InputStream is) 
            throws Exception {
        final JPEGDecoder decoder = new JPEGDecoder(ctx.bytesPool, new ImageBitsInputStream(is));
        return decoder.decode(ctx.intsPool);
    }
    
    public static RawImage decodePNGStreamAsRawImage(final Context ctx, final InputStream is) 
            throws Exception {
        final PngReader pngr = new PngReader(is);
        final int channels = pngr.imgInfo.channels;
        if (channels < 3 || pngr.imgInfo.bitDepth != 8) {
            LOG.warn("This method is for RGB8/RGBA8 images");
            return null;
        }
        final WriteableInts output = BlockUtils.createWriteableInts(ctx.intsPool);
        
        while(pngr.hasMoreRows()) {
            final IImageLine l1 = pngr.readRow();
            final int[] scanline = ((ImageLineInt) l1).getScanline(); // to save typing
            for (int j = 0; j < pngr.imgInfo.cols; j++) {
                final int R = scanline[j * channels] & 0xff;
                final int G = scanline[j * channels+1] & 0xff;
                final int B = scanline[j * channels+2] & 0xff;
                output.write( 0xff000000 | (R << 16) | (G << 8) | B );
            }
        }
        final IntsBlob ints = output.drainToIntsBlob();
        try {
            return new RawImage(pngr.imgInfo.cols, pngr.imgInfo.rows, ints);
        }
        finally {
            ints.release();
            pngr.close();
        }
    }
}
