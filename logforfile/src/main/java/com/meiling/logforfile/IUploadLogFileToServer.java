package com.meiling.logforfile;

import java.io.File;

/**
 * Created by Administrator on 9:34.
 *
 * @package com.meiling.logforfile
 * @auther By MeilingHong
 * @emall marisareimu123@gmail.com
 * @date 2018-02-02   09:34
 */

public interface IUploadLogFileToServer {

    /**
     * 考虑到灵活性,上传日志文件的方法开放出去，在方法执行完成后，根据用户的返回值，判断是否需要删除现存的日志文件
     *
     * @param logFiles
     *
     * @return true: 方法调用返回有，删除所有已有的日志文件； false ：保留现有的所有日志文件
     * [如果实现了日志上传的功能建议使用true进行返回，否则后续将出现重复上传日志的情况；由于返回后将根据返回值对日志文件进行操作]
     */
    boolean uploadLogFiles(File[] logFiles);
}
