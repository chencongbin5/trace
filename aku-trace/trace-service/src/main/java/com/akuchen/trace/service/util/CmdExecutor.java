package com.akuchen.trace.service.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @description:
 * @author: copy from com.didichuxing.chefuqa.common.util;
 * @time: 2019/12/24 9:11 PM
 */
public class CmdExecutor {

    private static final Logger LOG = LoggerFactory.getLogger(CmdExecutor.class);

    private static AtomicInteger counter = new AtomicInteger(0);

    private static int maxThread = 64;

    private static ThreadPoolExecutor executor = new ThreadPoolExecutor(20, maxThread, 5 * 60, TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(1), r -> new Thread(r, "CmdThread-" + counter.getAndIncrement()));

    public static int executeCmd(String[] commands, Long timeout) throws Exception {
        StringBuffer ret = new StringBuffer();
        if (commands == null || commands.length == 0) {
            throw new IllegalArgumentException();
        }
        Process process = null;
        try {
            StringBuilder e = new StringBuilder();
            for (int builder = 0; builder < commands.length; ++builder) {
                e.append(commands[builder]);
                if (builder < commands.length - 1) {
                    e.append(" && ");
                }
            }
            LOG.info("CmdThreadPool:{}", executor);
            if (executor.getPoolSize() >= maxThread) {
                LOG.warn("CmdThreadPoolBusy");
            }

            LOG.info("executeCmd : bash -c " + e.toString());
            ProcessBuilder var12 = new ProcessBuilder("/bin/bash", "-c", e.toString());
            var12.redirectErrorStream(true);
            process = var12.start();
            ReadLine readLine = new ReadLine(process.getInputStream(), ret, true);
            Future readLineFuture = executor.submit(readLine);
            long begin = System.currentTimeMillis();
            if (process.waitFor(timeout, TimeUnit.MILLISECONDS)) {
                LOG.info("readLine.stop();");
                readLine.setFlag(false);
                LOG.info("progressBar.stop();");
                LOG.info("executeCmd done !!!!!!");
                LOG.info("worker done !!!!!! times = " + (System.currentTimeMillis() - begin) / 1000L + "s");
                return process.exitValue();
            } else {
                throw new TimeoutException();
            }
        } catch (IOException | InterruptedException var10) {
            LOG.error("executeCmd builder.start(); IOException : ", var10);
            throw var10;
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
    }

    private static class ReadLine implements Runnable {
        private static final Logger LOGGER = LoggerFactory.getLogger("commandOutputLogger");
        private final InputStream is;
        // private final StringBuffer sb;
        private volatile boolean flag;

        private ReadLine(InputStream is, StringBuffer sb, boolean flag) {
            this.is = is;
            // this.sb = sb;
            this.flag = flag;
        }

        public void setFlag(boolean flag) {
            this.flag = flag;
        }

        @Override
        public void run() {
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new InputStreamReader(this.is,"UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            try {
                String line;
                try {
                    while (flag && (line = reader.readLine()) != null) {
                        String e = line.trim();
                        if (e.length() != 0) {
                            LOGGER.info(e);
                            // 这个sb的太大了，另外有啥用
                            //this.sb.append(e + System.getProperty("line.separator"));
                        }
                    }
                } catch (IOException var12) {
                    LOGGER.error("@@@@@@@@@@@@@@ ReadLine Thread, read IOException : ", var12);
                }
            } finally {
                try {
                    reader.close();
                } catch (IOException var11) {
                    LOGGER.error("@@@@@@@@@@@@@@ ReadLine Thread, close IOException : ", var11);
                }

            }

        }
    }
}