package net.vpc.app.nuts.toolbox.nsh.test;

import net.vpc.app.nuts.toolbox.nsh.util.FilePath;
import org.junit.Assert;
import org.junit.Test;

public class FilePathTest {
//    @Test
//    public void testOk(){
//        Assert.assertEquals(
//                FilePath.of("ssh","192.168.2.3",21,"/my/path","vpc","me","/home/vpc/my-key.key"),
//                FilePath.of("ssh://vpc@192.168.2.3:21/my/path?password=me&key-file=/home/vpc/my-key.key")
//
//        );
//        Assert.assertEquals(
//                FilePath.of("ssh","192.168.2.3",21,"/my/path",null,"me","/home/vpc/my-key.key"),
//                FilePath.of("ssh://192.168.2.3:21/my/path?password=me&key-file=/home/vpc/my-key.key")
//
//        );
//        Assert.assertEquals(
//                FilePath.of("ssh","192.168.2.3",-1,"/my/path","vpc","me","/home/vpc/my-key.key"),
//                FilePath.of("ssh://vpc@192.168.2.3/my/path?password=me&key-file=/home/vpc/my-key.key")
//
//        );
//        Assert.assertEquals(
//                FilePath.of("ssh","192.168.2.3",21,"/","vpc","me","/home/vpc/my-key.key"),
//                FilePath.of("ssh://vpc@192.168.2.3:21/?password=me&key-file=/home/vpc/my-key.key")
//
//        );
//        Assert.assertEquals(
//                FilePath.of("ssh","192.168.2.3",-1,"/",null,"me","/home/vpc/my-key.key"),
//                FilePath.of("ssh://192.168.2.3/?password=me&key-file=/home/vpc/my-key.key")
//
//        );
//    }

    @Test
    public void testKo(){
        try{
            FilePath.of("ssh://192.168.2.3?password=me&key-file=/home/vpc/my-key.key");
            Assert.assertTrue(false);
        }catch (IllegalArgumentException ex){
            Assert.assertTrue(true);
        }
    }
}
