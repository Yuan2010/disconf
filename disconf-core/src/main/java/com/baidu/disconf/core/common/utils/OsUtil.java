package com.baidu.disconf.core.common.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * OsUtil
 *
 * @author knightliao
 *
 * 2017-12-08
 */
public final class OsUtil {
    private static final Logger logger = LoggerFactory.getLogger(OsUtil.class);

    private OsUtil() {
    }

    /**
     * 建多层目录
     *
     * @param filePath
     * @return boolean
     */
    public static boolean makeDirs(final String filePath) {
        File f = new File(filePath);
        if (!f.exists()) {
            return f.mkdirs(); // 递归创建目录，失败时有可能已经创建了一些父目录
        }
        return true;
    }

    /**
     * @Description: 文件或目录是否存在
     * @param filePathString
     * @return boolean
     */
    public static boolean isFileExist(final String filePathString) throws Exception {
        return new File(filePathString).exists();
    }

    /**
     * @Description: JOIN PATH
     * @param pathElements
     * @return boolean
     */
    public static String pathJoin(final String... pathElements) {
        final String path;
        if (pathElements == null || pathElements.length == 0) {
            path = File.separator;
        } else {
            final StringBuffer sb = new StringBuffer();
            for (final String pathElement : pathElements) {
                if (pathElement.length() > 0) {
                    sb.append(pathElement);
                    sb.append(File.separator);
                }
            }
            if (sb.length() > 0) {
                sb.deleteCharAt(sb.length() - 1);
            }
            path = sb.toString();
        }
        return (path);
    }

    /**
     * 获取File相对于Folder的相对路径
     * <p/>
     * returns null if file isn't relative to folder
     */
    public static String getRelativePath(File file, File folder) {
        String filePath = file.getAbsolutePath();
        String folderPath = folder.getAbsolutePath();
        if (filePath.startsWith(folderPath)) {
            return filePath.substring(folderPath.length() + 1);
        } else {
            return null;
        }
    }

    /**
     * @Description: 转移文件
     */
    public static void transferFile(File src, File dest) throws Exception {
        // 删除文件
        // LOGGER.info("start to remove download file: " + ""
        // + dest.getAbsolutePath());
        if (dest.exists()) {
            dest.delete();
        }
        // 转移临时下载文件至下载文件夹
        FileUtils.copyFile(src, dest);
    }

    /**
     * @Description: 具有重试机制的 ATOM 转移文件 ，并且会校验文件是否一致 才替换
     */
    public static void transferFileAtom(File src, File dest, boolean isDeleteSource) throws Exception {
        // 文件锁所在文件
        File lockFile = new File(dest + ".lock");
        FileOutputStream outStream = null;
        FileLock lock = null;
        try {
            int tryTime = 0;
            while (tryTime < 3) {
                try {
                    outStream = new FileOutputStream(lockFile);
                    FileChannel channel = outStream.getChannel();
                    lock = channel.tryLock();
                    if (lock != null) {
                        if (dest.exists()) {
                            // 判断内容是否一样
                            if (FileUtils.isFileEqual(src, dest)) {
                                // 内容如果一样，就只需要删除源文件就行了
                                if (isDeleteSource) {
                                    src.delete();
                                }
                                break;
                            }
                        }

                        logger.debug("start to replace " + src.getAbsolutePath() + " to " + dest.getAbsolutePath());

                        // 转移
                        transferFile(src, dest);
                        // 删除源文件
                        if (isDeleteSource) {
                            src.delete();
                        }
                        break;
                    }
                } catch (FileNotFoundException e) {
                    // 打不开文件，则后面进行重试
                    logger.warn(e.toString());
                } finally {
                    // 释放锁，通道；删除锁文件
                    if (null != lock) {
                        try {
                            lock.release();
                        } catch (IOException e) {
                            logger.warn(e.toString());
                        }
                        if (lockFile != null) {
                            lockFile.delete();
                        }
                    }
                    if (outStream != null) {
                        try {
                            outStream.close();
                        } catch (IOException e) {
                            logger.warn(e.toString());
                        }
                    }
                }

                // 进行重试
                logger.warn("try lock failed. sleep and try " + tryTime);
                tryTime++;
                try {
                    Thread.sleep(1000 * tryTime);
                } catch (Exception e) {
                    System.out.print("");
                }
            }
        } catch (IOException e) {
            logger.warn(e.toString());
        }
    }
}
