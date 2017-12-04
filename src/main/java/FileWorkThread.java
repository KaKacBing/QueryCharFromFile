import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
 * All rights Reserved, Designed By KaKac
 * Copyright:   Copyright(C) 2016
 * author:      liub
 * Createdate:  2017/12/4 14:02
 */
public class FileWorkThread implements Runnable {

    /**
     * mbf 缓存映射
     */
    private MappedByteBuffer mappedByteBuffer;

    /**
     * 文件通道
     */
    private FileChannel fileChannel;
    /**
     * 字符统计
     */
    private Map<Character, Integer> charMap = new HashMap<>();

    /**
     * 文件锁
     */
    private FileLock fileLock;

    /**
     * 计数器
     */
    private CountDownLatch countDownLatch;

    /**
     * 设置 计数器
     *
     * @param countDownLatch 计数器
     */
    public void setCountDownLatch(CountDownLatch countDownLatch) {
        this.countDownLatch = countDownLatch;
    }

    public FileWorkThread(File file, long position, long size) {
        try {
            // 得到当前文件的通道
            fileChannel = new RandomAccessFile(file, "rw").getChannel();
            // 锁定记录
            fileLock = fileChannel.lock(position, size, false);
            // 数据map获取
            mappedByteBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, position, size);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            String str = Charset.forName("utf-8").decode(mappedByteBuffer).toString();
            // 字符串分割
            String[] split = str.split("[\\P{L}]+");
            for (String s : split) {
                if (null == s || "".equals(s.trim())) {
                    continue;
                }
                char first = s.charAt(0);
                if (charMap.containsKey(first)) {
                    charMap.put(first, charMap.get(first) + 1);
                } else {
                    charMap.put(first, 1);
                }
            }
            // 锁释放，通道关闭
            fileLock.release();
            fileChannel.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            countDownLatch.countDown();
        }
    }

    /**
     * 返回 字符map
     *
     * @return map
     */
    public Map<Character, Integer> getCharMap() {
        return charMap;
    }
}
