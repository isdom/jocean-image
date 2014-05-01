/**
 * 
 */
package org.jocean.image;

import java.io.InputStream;

import org.jocean.idiom.Triple;
import org.jocean.idiom.block.IntsBlob;
import org.jocean.idiom.pool.BytesPool;
import org.jocean.idiom.pool.IntsPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    
    public static RawImage decodeStreamAsRawImage(final Context ctx, final InputStream is) 
            throws Exception {
        final JPEGDecoder decoder = new JPEGDecoder(ctx.bytesPool, new ImageBitsInputStream(is));
        final Triple<Integer, Integer, IntsBlob> raw = decoder.decode(ctx.intsPool);
        if ( null != raw ) {
            try {
                final RawImage img = new RawImage(raw.getFirst(), raw.getSecond(), raw.getThird());
                return img;
            }
            finally {
                raw.getThird().release();
            }
        }
        
        return null;
    }
}
