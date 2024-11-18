package com.akuchen.trace.parse.jdi.sharememory;

import com.akuchen.trace.parse.enums.StatusEnum;
import lombok.extern.slf4j.Slf4j;

import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Optional;
import java.util.function.Function;

/**
 * 共享内存管理
 * 算了 暂时弃用了,用rmi先试试
 */
@Slf4j
public class SharedMemoryManager {

    private static final int SHARED_MEMORY_SIZE = 1024;


    public static Integer read() {
        Object o = actionSharedMemory((sharedMemory) -> {
            // 从共享内存中读取数据
            int data = sharedMemory.getInt(0);
            log.info("Data from shared memory: {}", data);
            return data;
        });
        return (Integer) o;
    }

    /**
     * 写入启动状态
     */
    public static void writeCompleted() {
         actionSharedMemory((sharedMemory)->{
            sharedMemory.putInt(0, StatusEnum.COMPELETED.getType());
            return null;
        });
    }

    private static Object actionSharedMemory(Function<MappedByteBuffer, Object> function) {
        FileChannel fileChannel = null;
        RandomAccessFile sharedMemoryFile = null;
        try {
            // 打开共享内存文件
            sharedMemoryFile = new RandomAccessFile("sm_aku_trace.bin", "rw");
            // 在文件通道中打开共享内存区域
            fileChannel = sharedMemoryFile.getChannel();
            MappedByteBuffer sharedMemory = fileChannel.map(FileChannel.MapMode.READ_WRITE, 0, SHARED_MEMORY_SIZE);
            Object apply = function.apply(sharedMemory);
            return apply;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            End(fileChannel,sharedMemoryFile);
        }
        return null;
    }

    /**
     *关闭资源
     */
    private static void End(FileChannel fileChannel, RandomAccessFile sharedMemoryFile) {
        Optional.ofNullable(fileChannel).ifPresent((channel) -> {
            try {
                channel.close();
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        });
        Optional.ofNullable(sharedMemoryFile).ifPresent((memory) -> {
            try {
                memory.close();
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        });
    }
}
