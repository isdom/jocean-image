package org.jocean.image;

import java.io.FileInputStream;

import org.jocean.idiom.block.BlockUtils;
import org.jocean.idiom.pool.PoolUtils;

import com.alibaba.fastjson.JSON;

public class RawImageTest {

    public static void main(String[] args) throws Exception {
        RawImage.initDefaultPool(PoolUtils.createCachedIntsPool(1024));
        
        final RawImage img = new RawImage(10, 10, 
                BlockUtils.createIntsBlob(100, PoolUtils.createCachedIntsPool(100)), false)
                .setProperty("ID", "1001").setProperty("link", "http://www.cnblogs.com/cczhoufeng/archive/2013/04/03/2997836.html");
        
        final String json = JSON.toJSONString(img);
        System.out.println(json);
        
        RawImage img2 = JSON.parseObject(json, RawImage.class);
        System.out.println(JSON.toJSONString(img2));
        
        
        final RawImage img3 = RawImage.decodeFrom(new FileInputStream("/Users/isdom/Desktop/50dc26cf341165217a3af24ad9dc2b1a.0"));
        System.out.println(img3);
    }
}
