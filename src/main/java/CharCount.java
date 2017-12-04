import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * All rights Reserved, Designed By KaKac
 * Copyright:   Copyright(C) 2016
 * author:      liub
 * Createdate:  2017/12/4 11:03
 */
public class CharCount {

    public static void main(String[] args) throws IOException {
        // 获取文件分割的线程集合
        List<FileWorkThread> threadList = getFileWorkThreadList();
        // 设置线程的计数器
        CountDownLatch countDownLatch = new CountDownLatch(threadList.size());
        threadList.forEach(th -> th.setCountDownLatch(countDownLatch));

        // 线程池执行
        long start = System.currentTimeMillis();
        ExecutorService executorService = Executors.newFixedThreadPool(4);
        threadList.forEach(th -> executorService.execute(th));

        // 主线程 await
        long end = 0L;
        try {
            countDownLatch.await();
            end = System.currentTimeMillis();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // 线程池关闭
        executorService.shutdown();

        // 执行时间计算和模拟查询一个数值
        System.out.println("Time = " + (end - start));
        int aLeng = 0;
        char input = 'a';
        for (FileWorkThread aThreadList : threadList) {
            Map<Character, Integer> map = aThreadList.getCharMap();
            if (map.containsKey(input)) {
                aLeng = aLeng + map.get(input);
            }
        }
        System.out.println("char[" + input + "] , qty = " + aLeng);
    }

    /**
     * 文件分割并封装到线程里面
     *
     * @return 线程集合
     * @throws IOException io异常
     */
    private static List<FileWorkThread> getFileWorkThreadList() throws IOException {
        // 获取文件
        File file = new File("E://file.txt");
        RandomAccessFile accessFile = new RandomAccessFile(file, "r");

        // 分割文件
        // 默认1M 大小
        int defaultLength = 1024 * 1024;
        long length = file.length();
        long position = 0;

        List<FileWorkThread> threadList = new ArrayList<>();
        while ((position + defaultLength) < length) {
            accessFile.seek(position + defaultLength);
            int offset = 0;
            while (true) {
                int read = accessFile.read();
                // 查询到下一个空格 或者 换行则截止
                if (read == 32 || read == 10) {
                    break;
                }
                offset++;
            }
            // 设置一个线程分割文件
            FileWorkThread fileWorkThread = new FileWorkThread(file, position, defaultLength + offset);
            // 移动未知
            position = position + defaultLength + offset;
            // 添加到 list
            threadList.add(fileWorkThread);
        }
        // 最后的文件分割片
        if (position + defaultLength > length) {
            FileWorkThread fileWorkThread = new FileWorkThread(file, position, length - position);
            threadList.add(fileWorkThread);
        }
        accessFile.close();
        return threadList;
    }
}
