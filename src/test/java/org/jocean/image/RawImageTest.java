package org.jocean.image;

import org.jocean.idiom.block.BlockUtils;
import org.jocean.idiom.pool.PoolUtils;

import com.alibaba.fastjson.JSON;

public class RawImageTest {

    public static void main(String[] args) throws Exception {
        final RawImage img = new RawImage(10, 10, 
                BlockUtils.createIntsBlob(100, PoolUtils.createCachedIntsPool(100)), false)
                .setProperty("ID", "1001").setProperty("link", "http://www.cnblogs.com/cczhoufeng/archive/2013/04/03/2997836.html");
        
        final String json = JSON.toJSONString(img);
        System.out.println(json);
        
        RawImage img2 = JSON.parseObject(json, RawImage.class);
        System.out.println(JSON.toJSONString(img2));
    }
}
