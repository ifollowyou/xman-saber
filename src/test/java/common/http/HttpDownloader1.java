/**
 * 专注互联网,分享创造价值
 *  maoxiang@gmail.com
 *  2010-3-30下午04:40:06
 */
package common.http;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.concurrent.atomic.AtomicLong;

import javax.net.ssl.SSLHandshakeException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Header;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.NoHttpResponseException;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.params.ConnPerRoute;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;

/**
 * 一个多线程支持断点续传的工具类<br/>
 * 2010-03 用Htpp Component重写
 */
public class HttpDownloader1 {

	private final Log log = LogFactory.getLog(getClass().getName());
	private int threads = 5; // 总共的线程数
	private int maxThreads = 10; // 最大的线程数
	private String destUrl; // 目标的URL
	private String savePath; // 保存的路径
	private File lockFile;// 用来保存进度的文件
	private String userAgent = "jHttpDownload";
	private boolean useProxy = false;
	private String proxyServer;
	private int proxyPort;
	private String proxyUser;
	private String proxyPassword;
	private int blockSize = 1024 * 4; // 4K 一个块
	// 1个位代表一个块,，用来标记是否下载完成
	private byte[] blockSet;
	private int blockPage; // 每个线程负责的大小
	private int blocks;
	private boolean running; // 是否运行中,避免线程不能释放
	private DefaultHttpClient httpClient;//
	// =======下载进度信息
	private long beginTime;
	private AtomicLong downloaded = new AtomicLong(0); // 已下载的字节数\
	private long fileLength; // 总的字节数
	// 监控线程,用来保存进度和汇报进度
	private MonitorThread monitorThread = new MonitorThread();

	public HttpDownloader1(String destUrl, String savePath, int threads) {
		this.threads = threads;
		this.destUrl = destUrl;
		this.savePath = savePath;
	}

	public HttpDownloader1(String destUrl, String savePath) {
		this(destUrl, savePath, 5);
	}

	/**
	 * 开始下载
	 */
	public boolean download() {
		log.info("下载文件" + destUrl + ",保存路径=" + savePath);
		beginTime = System.currentTimeMillis();
		boolean ok = false;
		try {
			File saveFile = new File(savePath);
			lockFile = new File(savePath + ".lck");
			if (lockFile.exists() && !lockFile.canWrite()) {
				throw new Exception("文件被锁住，或许已经在下载中了");
			}
			File parent = saveFile.getParentFile();
			if (!parent.exists()) {
				log.info("创建目录=" + parent.getAbsolutePath());
			}
			if (!parent.canWrite()) {
				throw new Exception("保存目录不可写");
			}
			if (saveFile.exists()) {
				if (!saveFile.canWrite()) {
					throw new Exception("保存文件不可写,无法继续下载");
				}
				log.info("检查之前下载的文件");
				if (lockFile.exists()) {
					log.info("加载之前下载进度");
					loadPrevious();
				}
			} else {
				lockFile.createNewFile();
			}
			// 1初始化httpClient
			setupHttpClient();
			HttpResponse response = getResponse(0);
			Header length = response.getFirstHeader("Content-Length");
			if (length != null) {
				try {
					fileLength = Long.parseLong(length.getValue());
				} catch (Exception e) {
				}
			}
			log.info("下载文件的大小:" + fileLength);
			if (fileLength <= 0) {
				// 不支持多线程下载,采用单线程下载
				log.info("服务器不能返回文件大小，采用单线程下载");
				threads = 1;
			}
			if (response.getFirstHeader("Content-Range") == null) {
				log.info("服务器不支持断线续传");
				threads = 1;
			} else {
				log.info("服务器支持断点续传");
			}
			if (blockSet != null) {
				log.info("检查文件，是否能够续传");
				if (blockSet.length * 8l * blockSize < fileLength) {
					log.info("文件大小已改变，需要重新下载");
					blockSet = null;
				}
			}
			//
			if (fileLength > 0 && parent.getFreeSpace() < fileLength) {
				throw new Exception("磁盘空间不够");
			}
			if (fileLength > 0) {
				int i = (int) (fileLength / blockSize);
				if (fileLength % blockSize > 0) {
					i++;
				}
				blocks = i;
				log.info("文件的块数:" + blocks);
				blockSet = BitUtil.createBit(blocks);
			} else {
				// 一个块
				blocks = 1;
			}
			blockPage = blocks / threads; // 每个线程负责的块数
			log.info("分配线程。线程数量=" + threads + ",块总数=" + blocks + ",总字节数="
					+ fileLength + ",每块大小=" + blockSize + ",块/线程=" + blockPage);
			// 检查
			running = true;
			ThreadGroup downloadGroup = new ThreadGroup("download");
			for (int i = 0; i < threads; i++) {
				int begin = i * blockPage;
				int end = (i + 1) * blockPage;
				if (i == threads - 1 && blocks % threads > 0) {
					// 如果最后一个线程，有余数，需要修正
					end = blocks;
				}
				// 扫描每个线程的块是否有需要下载的
				boolean needDownload = false;
				for (int j = begin; j < end; j++) {
					if (!BitUtil.getBit(blockSet, j)) {
						needDownload = true;
						break;
					}
				}
				if (!needDownload) {
					log.info("所有块已经下载完毕.Begin=" + begin + ",End=" + end);

				}
				// 启动下载其他线程
				DownloadThread downloadThread = new DownloadThread(
						downloadGroup, i, begin, end);
				downloadThread.start();
			}
			monitorThread.setStop(false);
			monitorThread.start();
			while (downloadGroup.activeCount() > 0) {
				Thread.sleep(2000);
			}
			ok = true;
		} catch (Exception e) {
			e.printStackTrace();
			log.error(e);
		} finally {
			// closeHttpClient();
			if (ok) {
				log.info("删除进度文件:" + lockFile.getAbsolutePath());
				lockFile.delete();
			}
			httpClient = null;
		}
		monitorThread.setStop(true);
		log.info("下载完成，耗时:"
				+ getTime((System.currentTimeMillis() - beginTime) / 1000));
		return ok;
	}

	private void loadPrevious() throws Exception {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		FileInputStream inStream = new FileInputStream(lockFile);
		byte[] buffer = new byte[4096];
		int n = 0;
		while (-1 != (n = inStream.read(buffer))) {
			outStream.write(buffer, 0, n);
		}
		outStream.close();
		inStream.close();
		blockSet = outStream.toByteArray();
		log.debug("之前的文件大小应该是:" + blockSet.length * 8l * blockSize);
	}

	private void setupHttpClient() throws Exception {
		HttpParams params = new BasicHttpParams();
		SchemeRegistry schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(new Scheme("http", PlainSocketFactory
				.getSocketFactory(), 80));
		ClientConnectionManager cm = new ThreadSafeClientConnManager(params,
				schemeRegistry);
		httpClient = new DefaultHttpClient(cm, params);
		httpClient.getParams().setParameter(CoreProtocolPNames.USER_AGENT,
				userAgent);
		ConnPerRoute defaultConnPerRoute = new ConnPerRoute() {

			public int getMaxForRoute(HttpRoute route) {
				return maxThreads;
			}

		};
		httpClient.getParams().setParameter(
				ConnManagerParams.MAX_CONNECTIONS_PER_ROUTE,
				defaultConnPerRoute);
		// 设置重试机制
		httpClient.setHttpRequestRetryHandler(myRetryHandler);
		if (useProxy) {
			log.info("采用代理服务器=" + proxyServer + ",端口=" + proxyPort);
			final HttpHost proxy = new HttpHost(proxyServer, proxyPort, "http");
			httpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY,
					proxy);
			if (proxyUser != null && proxyPassword != null) {
				httpClient.getCredentialsProvider().setCredentials(
						AuthScope.ANY,
						new UsernamePasswordCredentials(proxyUser,
								proxyPassword));
			}
		}
	}

	private HttpResponse response0; // 用来快速返回,减少一次连接

	private HttpResponse getResponse(long pos) throws Exception {
		if (pos == 0 && response0 != null) {
			return response0;
		}
		HttpGet httpget = new HttpGet(destUrl);
		HttpContext localContext = new BasicHttpContext();
		// 实现多线程下载的核心，也可以用来实现断点续传
		httpget.addHeader("RANGE", "bytes=" + pos + "-");
		HttpResponse response = httpClient.execute(httpget, localContext);
		if (pos == 0) {
			response0 = response;
		}
		return response;
	}

	// 重试机制
	private HttpRequestRetryHandler myRetryHandler = new HttpRequestRetryHandler() {

		public boolean retryRequest(IOException exception, int executionCount,
									HttpContext context) {
			if (executionCount >= 5) {
				return false;
			}
			if (exception instanceof NoHttpResponseException) {
				return true;
			}
			if (exception instanceof SSLHandshakeException) {
				return false;
			}
			HttpRequest request = (HttpRequest) context
					.getAttribute(ExecutionContext.HTTP_REQUEST);
			boolean idempotent = !(request instanceof HttpEntityEnclosingRequest);
			if (idempotent) {
				return true;
			}
			return false;
		}
	};

	private String getDesc() {
		long downloadBytes = downloaded.longValue();

		return String.format("已下载/总大小=%s/%s(%s),速度:%s,耗时:%s,剩余大小:%d",
				getFileSize(downloadBytes), getFileSize(fileLength),
				getProgress(fileLength, downloadBytes),
				getFileSize(downloadBytes
						/ ((System.currentTimeMillis() - beginTime) / 1000)),
				getTime((System.currentTimeMillis() - beginTime) / 1000),
				fileLength - downloadBytes);
	}

	private String getFileSize(long totals) {
		// 计算文件大小
		int i = 0;
		String j = "BKMGT";
		float s = totals;
		while (s > 1024) {
			s /= 1024;
			i++;
		}
		return String.format("%.2f", s) + j.charAt(i);
	}

	private String getProgress(long totals, long read) {
		if (totals == 0)
			return "0%";
		return String.format("%d", read * 100 / totals) + "%";
	}

	private String getTime(long seconds) {
		int i = 0;
		String j = "秒分时天";
		long s = seconds;
		String result = "";
		while (s > 0) {
			if (s % 60 > 0) {
				result = String.valueOf(s % 60) + (char) j.charAt(i) + result;
			}
			s /= 60;
			i++;
		}
		return result;
	}

	/**
	 * 一个下载线程.
	 */
	private class DownloadThread extends Thread {

		private RandomAccessFile destFile; // 用来实现保存的随机文件
		private int id = 0;
		private int blockBegin = 0; // 开始块
		private int blockEnd = 0; // 结束块
		private long pos;// 绝对指针

		private String getThreadName() {
			return "DownloadThread-" + id + "=>";
		}

		public DownloadThread(ThreadGroup group, int id, int blockBegin,
							  int blockEnd) throws Exception {
			super(group, "downloadThread-" + id);
			this.id = id;
			this.blockBegin = blockBegin;
			this.blockEnd = blockEnd;
			this.pos = 1l * blockBegin * blockSize; // 转换为长整型
			destFile = new RandomAccessFile(savePath, "rw");
		}

		public void run() {
			BufferedInputStream inputStream = null;
			try {
				log.info(getThreadName() + "下载线程." + this.toString());
				log.info(getThreadName() + ":定位文件位置.Pos=" + 1l * blockBegin
						* blockSize);
				destFile.seek(1l * blockBegin * blockSize);
				log.info(getThreadName() + ":开始下载.[ " + blockBegin + " - "
						+ blockEnd + "]");

				HttpResponse response = getResponse(pos);
				inputStream = new BufferedInputStream(response.getEntity()
						.getContent());
				byte[] b = new byte[blockSize];
				while (blockBegin < blockEnd) {
					if (!running) {
						log.info(getThreadName() + ":停止下载.当前块:" + blockBegin);
						return;
					}
					log.debug(getThreadName() + "下载块=" + blockBegin);
					int counts = 0; // 已下载字节数
					if (BitUtil.getBit(blockSet, blockBegin)) {
						log.debug(getThreadName() + ":块下载已经完成=" + blockBegin);
						destFile.skipBytes(blockSize);
						int skips = 0;
						while (skips < blockSize) {
							skips += inputStream.skip(blockSize - skips);
						}
						downloaded.addAndGet(blockSize);

					} else {
						while (counts < blockSize) {
							int read = inputStream.read(b, 0, blockSize
									- counts);
							if (read < 0)
								break;
							counts += read;
							destFile.write(b, 0, read);
							downloaded.addAndGet(read);
						}
						BitUtil.setBit(blockSet, blockBegin, true); // 标记已经下载完成
					}
					blockBegin++;
				}
				response.getEntity().consumeContent();
				log.info(getThreadName() + "下载完成.");
				return;
			} catch (Exception e) {
				log.error(getThreadName() + "下载错误:" + e.getMessage());
				e.printStackTrace();
			} finally {
				try {
					if (inputStream != null)
						inputStream.close();
				} catch (Exception te) {
					log.error(te);
				}
				try {
					if (destFile != null)
						destFile.close();
				} catch (Exception te) {
					log.error(te);
				}
			}
		}
	}

	// 监控线程,并保存进度，方便下次断点续传
	private class MonitorThread extends Thread {
		boolean stop = false;

		public void setStop(boolean stop) {
			this.stop = stop;
		}

		public void run() {
			FileOutputStream saveStream = null;
			try {
				saveStream = new FileOutputStream(lockFile);
				while (running && !stop) {
					log.info(getDesc());
					// 保存进度
					saveStream.write(blockSet);
					sleep(1000);
				}
			} catch (Exception e) {
				log.error(e);
			} finally {
				if (saveStream != null) {
					try {
						saveStream.close();
					} catch (Exception e) {
						log.error(e);
					}
				}
			}
		}
	}

	// 用来操作位的工具
	private static class BitUtil {
		public static byte[] createBit(int len) {
			int size = len / Byte.SIZE;
			if (len % Byte.SIZE > 0) {
				size++;
			}
			return new byte[size];
		}

		/** 取出某位，是0 还是1 */
		public static boolean getBit(byte[] bits, int pos) {
			int i = pos / Byte.SIZE;
			int b = bits[i];
			int j = pos % Byte.SIZE;
			byte c = (byte) (0x80 >>> (j - 1));
			return b == c;
		}

		/** 设置某位，是0 还是1 */
		public static void setBit(byte[] bits, int pos, boolean flag) {
			int i = pos / Byte.SIZE;
			byte b = bits[i];
			int j = pos % Byte.SIZE;
			byte c = (byte) (0x80 >>> (j - 1));
			if (flag) {
				bits[i] = (byte) (b | c);
			} else {
				c = (byte) (0xFF ^ c);
				bits[i] = (byte) (b & c);
			}
		}
	}
}
