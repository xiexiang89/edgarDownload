package com.edgar.download;

import com.edgar.download.task.AbsDownloadTask;

/**
 * Created by edgar on 2016/8/14.
 * 任务创建工厂
 * 实现该接口可自定义download task
 */
public interface TaskCreateFactory {

    /**
     * Create download task
     * @param downloadRequest 下载请求
     * @return 返回download task
     */
    AbsDownloadTask create(DownloadRequest downloadRequest);
}