# edgarDownload
Android下载库,已经用于公司产品中；还未提供下载示例.

#1.0
1.DownloadManager:
  下载队列的管理,当一个任务完成或结束,会自动开始下一个任务;
  addDownloadTask(DownloadRequest downloadRequest): 增加一个新的下载任务，使用者通过downloadRequest创建一个下载任务,并且返回一个download task对象
  resumeDownloadTask(AbsDownloadTask downloadTask): 恢复下载，使用者拿addDownloadTask返回的download task对象,可以进行恢复下载;
  pauseDownload(AbsDownloadTask downloadTask): 暂停下载,传入创建好的downloadTask,进行暂停操作
  removeDownload(AbsDownloadTask downloadTask):删除下载,下载会停止.

2.AbsDownloadTask:
  可以单独使用,可以不必需要DownloadManager运行,
  通过 startDownload(ExecutorService executorService): 传入你的线程池,内部就会开始下载;

3.HttpDownloadTask:
  默认的下载任务, 看名字应该懂,Http下载，默认使用http下载,
  可能有些需要socket下载,可以继承AbsDownloadTask.