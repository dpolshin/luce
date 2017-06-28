package foo.bar.luce.index;

import foo.bar.luce.model.FileDescriptor;
import foo.bar.luce.util.FileUtil;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.File;
import java.net.URL;

@RunWith(JUnit4.class)
public class TestChecksum {

    @Test
    public void testChecksum() throws Exception {
        URL url = this.getClass().getClassLoader().getResource("loremipsum.txt");
        //noinspection ConstantConditions
        File file = new File(url.toURI());
        long hash = FileUtil.hash(new FileDescriptor(file));
        Assert.assertEquals(3882244716L, hash);
    }
}
