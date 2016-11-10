package cn.edu.scu.carrecorder.util;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.SocketException;

import cn.edu.scu.carrecorder.classes.ProgressBufferedOutputStream;

/**
 * 2013年09月20日21:38:04
 *
 * 用于Android和FTP服务器进行交互的工具类
 *
 * @author xiaoyaomeng
 *
 */
public class FTPUtils {
    private FTPClient ftpClient = null;
    private static FTPUtils ftpUtilsInstance = null;
    private String FTPUrl;
    private int FTPPort;
    private String UserName;
    private String UserPassword;

    public interface IProgressListener {
        void onProgress(long bytescount, long bytestotal);
    }

    public FTPUtils()
    {
        ftpClient = new FTPClient();
    }
    /*
     * 得到类对象实例（因为只能有一个这样的类对象，所以用单例模式）
     */
    public  static FTPUtils getInstance() {
        if (ftpUtilsInstance == null)
        {
            ftpUtilsInstance = new FTPUtils();
        }
        return ftpUtilsInstance;
    }

    /**
     * 设置FTP服务器
     * @param FTPUrl   FTP服务器ip地址
     * @param FTPPort   FTP服务器端口号
     * @param UserName    登陆FTP服务器的账号
     * @param UserPassword    登陆FTP服务器的密码
     * @return
     */
    public boolean initFTPSetting(String FTPUrl, int FTPPort, String UserName, String UserPassword)
    {
        this.FTPUrl = FTPUrl;
        this.FTPPort = FTPPort;
        this.UserName = UserName;
        this.UserPassword = UserPassword;

        int reply;

        try {
            //1.要连接的FTP服务器Url,Port
            ftpClient.connect(FTPUrl, FTPPort);

            //2.登陆FTP服务器
            ftpClient.login(UserName, UserPassword);

            //3.看返回的值是不是230，如果是，表示登陆成功
            reply = ftpClient.getReplyCode();

            if (!FTPReply.isPositiveCompletion(reply))
            {
                //断开
                ftpClient.disconnect();
                return false;
            }

            return true;

        } catch (SocketException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 上传文件
     * @param FilePath    要上传文件所在SDCard的路径
     * @param FileName    要上传的文件的文件名(如：Sim唯一标识码)
     * @return    true为成功，false为失败
     */
    public boolean uploadFile(String FilePath, String FileName) {

        if (!ftpClient.isConnected())
        {
            if (!initFTPSetting(FTPUrl,  FTPPort,  UserName,  UserPassword))
            {
                return false;
            }
        }

        try {

            //设置存储路径
            ftpClient.makeDirectory("/data");
            ftpClient.changeWorkingDirectory("/data");

            //设置上传文件需要的一些基本信息
            ftpClient.setBufferSize(1024);
            ftpClient.setControlEncoding("UTF-8");
            ftpClient.enterLocalPassiveMode();
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

            //文件上传吧～
            FileInputStream fileInputStream = new FileInputStream(FilePath);
            ftpClient.storeFile(FileName, fileInputStream);

            //关闭文件流
            fileInputStream.close();

            //退出登陆FTP，关闭ftpCLient的连接
            ftpClient.logout();
            ftpClient.disconnect();

            File fileToDel = new File(FilePath);
            if (fileToDel.exists()) {
                fileToDel.delete();
            }

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 下载文件
     * @param phoneNumber 根据号码来查找上传到服务器的文件
     * @param filepath   本地文件保存路径
     * @return   true为成功，false为失败
     */
    public boolean downLoadFile(String phoneNumber, String filepath, final FTPUtils.IProgressListener listener) {

        if (!ftpClient.isConnected())
        {
            if (!initFTPSetting(FTPUrl,  FTPPort,  UserName,  UserPassword))
            {
                return false;
            }
        }

        try {
            // 转到指定下载目录
            ftpClient.changeWorkingDirectory("/data");
            ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);

            // 列出该目录下所有文件
            FTPFile[] files = ftpClient.listFiles();
            FTPFile file_to_del = null;
            // 遍历所有文件，找到指定的文件
            for (FTPFile file : files) {
                if (file.getName().contains(phoneNumber)) {
                    file_to_del = file;
                    final long size = file.getSize();
                    //根据绝对路径初始化文件
                    File localFile = new File(filepath);

                    // 输出流
                    OutputStream outputStream = new FileOutputStream(localFile);
                    ProgressBufferedOutputStream pfos = new ProgressBufferedOutputStream(outputStream, new ProgressBufferedOutputStream.IProgressListener() {
                        @Override
                        public void onProgress(long len) {
                            listener.onProgress(len, size);
                        }
                    });

                    // 下载文件
                    ftpClient.retrieveFile(file.getName(), pfos);

                    //关闭流
                    outputStream.close();
                    break;
                }
            }
            /*if (file_to_del != null) {
                ftpClient.deleteFile(file_to_del.getName());
            }*/

            //退出登陆FTP，关闭ftpCLient的连接
            ftpClient.logout();
            ftpClient.disconnect();


        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return true;
    }

}