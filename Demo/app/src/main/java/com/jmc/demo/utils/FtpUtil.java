package com.jmc.demo.utils;

import android.util.Log;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.ftplet.Authority;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.usermanager.impl.BaseUser;
import org.apache.ftpserver.usermanager.impl.WritePermission;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadPoolExecutor;

public class FtpUtil {
    private final String TAG = FtpUtil.class.getSimpleName();

    private final int RETRY_MAX_TIMES = 3, RETRY_DELAY = 1000 * 5;

    private final int FTP_TIMEOUT = 1000 * 10;

    private ThreadPoolExecutor threadPool;

    private FtpServer ftpServer;

    private FTPClient ftpClient;

    private FtpDownloadListener listener;

    private static class InnerHolder {
        private static final FtpUtil INSTANCE = new FtpUtil();
    }

    public static FtpUtil getInstance() {
        return InnerHolder.INSTANCE;
    }

    public boolean startService(FtpConfig config) {
        try {
            if (isServerConnected()) {
                try {
                    ftpServer.stop();
                } catch (Exception ignore) {
                }
            }

            ftpServer = null;

            if (config != null) {
                FtpServerFactory serverFactory = new FtpServerFactory();

                BaseUser baseUser = new BaseUser();
                baseUser.setName(config.userName);
                baseUser.setPassword(config.password);
                baseUser.setHomeDirectory(config.rootPath);

                List<Authority> authorities = new ArrayList<>();
                authorities.add(new WritePermission());
                baseUser.setAuthorities(authorities);
                serverFactory.getUserManager().save(baseUser);

                ListenerFactory factory = new ListenerFactory();
                factory.setPort(config.port);

                serverFactory.addListener("default", factory.createListener());

                ftpServer = serverFactory.createServer();

                ftpServer.start();

                return true;
            }
        } catch (FtpException e) {
            e.printStackTrace();
        }
        return false;

    }

    public void stopService() {
        if (ftpServer != null) ftpServer.stop();
        ftpServer = null;
    }

    public boolean isServerConnected() {
        if (ftpServer != null) return !ftpServer.isSuspended() && !ftpServer.isStopped();
        return false;
    }

    public void startDownload(FtpDownloadListener listener, Set<String> fileNames) {
        this.listener = listener;
//        threadPool.execute(new DownloadRunnable(apkListCache, resourcesListCache));
    }

    public boolean connectClientWithRetry(FtpConfig config) {
        boolean success;
        int retry = 0;
        do {
            if (retry != 0) {
                try {
                    Thread.sleep(RETRY_DELAY);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            success = connectClient(config);
            ++retry;
        } while (!success && retry < RETRY_MAX_TIMES);
        return success;
    }

    public boolean connectClient(FtpConfig config) {
        Log.w(TAG, "FTP DownloadRunnable connect start...");

        try {
            Log.w(TAG, "Init FTP start...");
            ftpClient = new FTPClient();

            ftpClient.setDefaultTimeout(FTP_TIMEOUT);
            ftpClient.setConnectTimeout(FTP_TIMEOUT);
            ftpClient.setDataTimeout(FTP_TIMEOUT);

            ftpClient.setControlEncoding("utf-8");

            Log.w(TAG, "Connect FTP start...");
            ftpClient.connect(config.hostName, config.port);

            int reply = ftpClient.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                Log.w(TAG, "Connect FTP fail..." + reply);
                ftpClient.disconnect();
                return false;
            }

            Log.w(TAG, "Login FTP start...");
            ftpClient.login(config.userName, config.password);
            reply = ftpClient.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                Log.w(TAG, "Login FTP fail..." + reply);
                ftpClient.disconnect();
                return false;
            }
            Log.w(TAG, "Login FTP success...");

            ftpClient.setRemoteVerificationEnabled(false);
            ftpClient.enterLocalPassiveMode();
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            Log.w(TAG, "Connect FTP finish!");

//  TODO          threadPool = ThreadPoolUtil.getInstance().newThreadPoolExecutor();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "Connect FTP err:" + e.getMessage());
        }
        Log.w(TAG, "FTP DownloadRunnable connect end...");
        return false;
    }

    public void disconnectClient() {
        if (ftpClient != null) {
            if (ftpClient.isConnected()) {
                try {
                    ftpClient.logout();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    ftpClient.disconnect();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        if (threadPool != null)
            threadPool.shutdown();
        threadPool = null;
        ftpClient = null;
        listener = null;
    }

    private void download(String filePath, List<String> fileNameList, FtpDownloadListener listener) {
        Log.w(TAG, "FTP Download start..." + filePath + "--" + fileNameList + "--" + listener);

        if (ftpClient != null) {
            try {
                Log.w(TAG, "FTP Download changeWorkingDirectory start...");
                ftpClient.changeWorkingDirectory("/" + filePath);
                Log.w(TAG, "FTP Download changeWorkingDirectory end..." + ftpClient.printWorkingDirectory());
            } catch (Exception e) {
                e.printStackTrace();
                Log.w(TAG, "FTP Download changeWorkingDirectory err:" + e.getMessage());
                if (listener != null)
                    listener.onDownloadResultBack(false, "", "");
                return;
            }
            Log.w(TAG, "FTP Download File list start...");
            FTPFile[] ftpFiles;
            try {
                ftpFiles = ftpClient.listFiles();
            } catch (Exception e) {
                e.printStackTrace();
                Log.w(TAG, "FTP Download File list err:" + e.getMessage());
                if (listener != null)
                    listener.onDownloadResultBack(false, "", "");
                return;
            }
            if (ftpFiles != null) {
                Log.w(TAG, "FTP Download File list end:" + ftpFiles.length);
                for (FTPFile ftpFile : ftpFiles) {
                    Log.w(TAG, "FTP Download #### File name:" + ftpFile.getName() + "=" + ftpFile.getSize());

                    if (fileNameList.contains(ftpFile.getName())) {
                        if (ftpFile.isFile()) {
                            Log.w(TAG, "FTP Download is File...");
//   TODO                         File file = PackageManager.getInstance().getUpdateFile(filePath, ftpFile.getName());
                            try {
                                boolean success = downloadFile(ftpClient, null, ftpFile);
                                if (listener != null)
                                    listener.onDownloadResultBack(success, filePath, ftpFile.getName());
                            } catch (Exception e) {
                                Log.w(TAG, "FTP retrieveFile err:" + e.getMessage());
                                e.printStackTrace();
                                if (listener != null)
                                    listener.onDownloadResultBack(false, "", "");
                            }
                        } else {
                            Log.w(TAG, "FTP Download is Dir...");
                        }
                    }
                }
            } else {
                if (listener != null)
                    listener.onDownloadResultBack(false, "", "");
                Log.w(TAG, "FTP Download err FtpFiles==null");
            }
        }
    }

    private boolean downloadFile(FTPClient ftpClient, File localFile, FTPFile ftpFile) throws IOException {
        OutputStream outputStream = new FileOutputStream(localFile);
        Log.w(TAG, "FTP retrieveFile start..." + ftpFile.getSize());
        boolean flag = ftpClient.retrieveFile(ftpFile.getName(), outputStream);
        Log.w(TAG, "FTP retrieveFile end...");
        outputStream.close();
        return flag;
    }

    class DownloadRunnable implements Runnable {


        List<String> apkListNames, resourcesListNames;


        DownloadRunnable() {
            apkListNames = new ArrayList<>();
            resourcesListNames = new ArrayList<>();
        }


        @Override
        public void run() {
            Log.w(TAG, "FTP DownloadRunnable start...");

            if (ftpClient != null) {
                Log.w(TAG, "FTP DownloadRunnable download start...");

//                if (apkListCache != null) {
//                    for (ApkInfo info : apkListCache) {
//                        if (PackageManager.getInstance().validationApkFile(PackageManager.getInstance().getUpdateFile(Constant.FilePath.APK_FOLDER, info.getApkName()), info.getApkSign())) {
//                            if (listener != null)
//                                listener.onDownloadResultBack(true, Constant.FilePath.APK_FOLDER, info.getApkName());
//                        } else {
//                            apkListNames.add(info.getApkName());
//                        }
//
//                    }
//
//                }

//                    download(Constant.FilePath.APK_FOLDER, apkListNames, listener);

//                if (resourcesListCache != null) {
//                    for (ResourceInfo info : resourcesListCache) {
//                        if (PackageManager.getInstance().validationApkFile(PackageManager.getInstance().getUpdateFile(Constant.FilePath.RESOURCE_FOLDER, info.getResourcesName()), info.getResourcesSign())) {
//                            if (listener != null)
//                                listener.onDownloadResultBack(true, Constant.FilePath.RESOURCE_FOLDER, info.getResourcesName());
//                        } else {
//                            resourcesListNames.add(info.getResourcesName());
//                        }
//                    }
//                }

//                    download(Constant.FilePath.RESOURCE_FOLDER, resourcesListNames, listener);

                Log.w(TAG, "FTP DownloadRunnable download end...");
            }
        }
    }

    public static final class FtpConfig {
        int port = -1;
        String hostName, userName, password;
        String rootPath;

        public FtpConfig() {

        }

        public FtpConfig(String hostName, String rootPath, String userName, String password, int port) {
            this.hostName = hostName;
            this.rootPath = rootPath;
            this.userName = userName;
            this.password = password;
            this.port = port;
        }

        public FtpConfig setRootPath(String rootPath) {
            this.rootPath = rootPath;
            return this;
        }

        public FtpConfig setPort(int port) {
            this.port = port;
            return this;
        }

        public FtpConfig setHostName(String hostName) {
            this.hostName = hostName;
            return this;
        }

        public FtpConfig setUserName(String userName) {
            this.userName = userName;
            return this;
        }

        public FtpConfig setPassword(String password) {
            this.password = password;
            return this;
        }


        @Override
        public String toString() {
            return "FtpConfig{" +
                    "port=" + port +
                    ", hostName='" + hostName + '\'' +
                    ", userName='" + userName + '\'' +
                    ", password='" + password + '\'' +
                    ", rootPath='" + rootPath +
                    '}';
        }
    }

    public interface FtpDownloadListener {
        void onDownloadResultBack(boolean success, String filePath, String fileName);
    }
}
