package com.xiaoleilu.loServer;

import com.xiaoleilu.loServer.handler.ActionHandler;

import cn.hutool.core.date.DateUtil;
import cn.hutool.log.Log;
import cn.hutool.log.StaticLog;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.stream.ChunkedWriteHandler;

/**
 * LoServer starter<br>
 * 用于启动服务器的主对象<br>
 * 使用LoServer.start()启动服务器<br>
 * 服务的Action类和端口等设置在ServerSetting中设置
 * @author Looly
 *
 */
public class LoServer {
	private static final Log log = StaticLog.get();
	
	/**
	 * 启动服务
	 * @param port 端口
	 * @throws InterruptedException 
	 */
	public void start(int port) throws InterruptedException {
		long start = System.currentTimeMillis();
		
		// Configure the server.
		final EventLoopGroup bossGroup = new NioEventLoopGroup(1);
		final EventLoopGroup workerGroup = new NioEventLoopGroup();
		
		try {
			final ServerBootstrap b = new ServerBootstrap(); // 1、创建ServerBootStrap实例
			// 2、设置并绑定Reactor线程池：EventLoopGroup，EventLoop就是处理所有注册到本线程的Selector上面的Channel
			b.group(bossGroup, workerGroup)
				.option(ChannelOption.SO_BACKLOG, 1024)
				.channel(NioServerSocketChannel.class) // 3、设置并绑定服务端的channel
//				.handler(new LoggingHandler(LogLevel.INFO))
				// 4、创建处理网络事件的ChannelPipeline和handler，网络事件以流的形式在其中流转，
					// handler完成多数的功能定制：比如编解码 SSl安全认证
				.childHandler(new ChannelInitializer<SocketChannel>(){
					@Override
					protected void initChannel(SocketChannel ch) throws Exception {
						ch.pipeline()
						.addLast(new HttpServerCodec())
						//把多个消息转换为一个单一的FullHttpRequest或是FullHttpResponse
						.addLast(new HttpObjectAggregator(65536))
						//压缩Http消息
//						.addLast(new HttpChunkContentCompressor())
						//大文件支持
						.addLast(new ChunkedWriteHandler())
						
						.addLast(new ActionHandler());
					}
				});
			// 6、当轮训到准备就绪的channel后，由Reactor线程：NioEventLoop执行pipline中的方法，最终调度并执行channelHandler
			final Channel ch = b.bind(port).sync().channel();
			log.info("***** Welcome To LoServer on port [{}], startting spend {}ms *****", port, DateUtil.spendMs(start));
			ch.closeFuture().sync();
		} finally {
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
		}
	}
	
	/**
	 * 启动服务器
	 */
	public static void start() {
		try {
			new LoServer().start(ServerSetting.getPort());
		} catch (InterruptedException e) {
			log.error("LoServer start error!", e);
		}
	}
}
