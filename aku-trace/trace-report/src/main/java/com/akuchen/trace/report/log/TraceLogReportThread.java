package com.akuchen.trace.report.log;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.akuchen.trace.report.http.TraceReportHttp;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class TraceLogReportThread {
    private static Logger logger = LoggerFactory.getLogger(TraceLogReportThread.class);



    private Thread localThread;
    private volatile boolean toStop = false;
    public TraceLogReportThread(){



        localThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!toStop) {
                    try {
                        // clean log dir, over logRetentionDays
                        File[] childDirs = new File(TraceFileAppender.getLogPath()).listFiles();
                        if (childDirs!=null && childDirs.length>0) {

                            // today
                            Calendar todayCal = Calendar.getInstance();
                            todayCal.set(Calendar.HOUR_OF_DAY,0);
                            todayCal.set(Calendar.MINUTE,0);
                            todayCal.set(Calendar.SECOND,0);
                            todayCal.set(Calendar.MILLISECOND,0);

                            Date todayDate = todayCal.getTime();

                            for (File childFile: childDirs) {

                                // valid
                                if (!childFile.isDirectory()) {
                                    continue;
                                }
                                if (childFile.getName().indexOf("-") == -1) {
                                    continue;
                                }

                                // file create date
                                Date logFileCreateDate = null;
                                try {
                                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                                    logFileCreateDate = simpleDateFormat.parse(childFile.getName());
                                } catch (ParseException e) {
                                    logger.error(e.getMessage(), e);
                                }
                                if (logFileCreateDate == null) {
                                    continue;
                                }

                                File[] files = childFile.listFiles();

                                if (files != null) {
                                    Arrays.sort(files, new Comparator<File>() {
                                        public int compare(File f1, File f2) {
                                            return Long.compare(f2.lastModified(), f1.lastModified());
                                        }
                                    });


                                    for (File file : files) {
                                        String logs = FileUtils.readFileToString(file, "UTF-8");
                                        String[] splitFileName = file.getName().split("#");
                                        String tid = splitFileName[0];
                                        String application= splitFileName[1].split("\\.")[0];
                                        Boolean flag = TraceReportHttp.add(tid, application, logs);
                                        if(flag){
                                            file.delete();
                                        }
                                    }
                                }
                            }
                        }

                    } catch (Exception e) {
                        if (!toStop) {
                            logger.error(e.getMessage(), e);
                        }

                    }

                    try {
                        TimeUnit.SECONDS.sleep(15);
                    } catch (InterruptedException e) {
                        if (!toStop) {
                            logger.error(e.getMessage(), e);
                        }
                    }
                }
                logger.info(">>>>>>>>>>> xxl-job, executor JobLogFileCleanThread thread destory.");

            }
        });
        localThread.setDaemon(true);
        localThread.setName("xxl-job, executor JobLogFileCleanThread");
        localThread.start();
    }

    public void toStop() {
        toStop = true;

        if (localThread == null) {
            return;
        }

        // interrupt and wait
        localThread.interrupt();
        try {
            localThread.join();
        } catch (InterruptedException e) {
            logger.error(e.getMessage(), e);
        }
    }

}
