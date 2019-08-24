package com.loop.hyh.core.server;

import com.loop.hyh.core.connector.Acceptor;
import com.loop.hyh.core.connector.IdleSocketCleaner;
import com.loop.hyh.core.connector.Poller;
import com.loop.hyh.core.wrapper.NioSocketWrapper;
import com.loop.hyh.core.servlet.base.DispatcherServlet;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class Server {

    private static final int DEFAULT_PORT = 8080;
    private int pollerCount = Math.min(2, Runtime.getRuntime().availableProcessors());
    private ServerSocketChannel server;
    private DispatcherServlet dispatcherServlet;
    private volatile boolean isRunning = true;
    private Acceptor acceptor;
    private List<Poller> pollers;
    private AtomicInteger pollerRotater = new AtomicInteger(0);
    private int maxKeepAliveRequests = 100;
    private int keepAliveTimeout = 5000;
    private IdleSocketCleaner cleaner;

    public Server() {
        this(DEFAULT_PORT);
    }

    public Server(int port) {
        try {
            initServerSocket(port);
            initPoller();
            initAcceptor();
            initIdleSocketCleaner();
            dispatcherServlet = new DispatcherServlet();
            log.info("服务器启动");
        } catch (Exception e) {
            e.printStackTrace();
            log.info("初始化服务器失败");
            close();

        }
    }
    
    
    public void execute(NioSocketWrapper socketWrapper) {
        dispatcherServlet.doDispatch(socketWrapper);
    }

    private void initServerSocket(int port) throws IOException {
        server = ServerSocketChannel.open();
        server.bind(new InetSocketAddress(port));
        server.configureBlocking(true);
    }

    private void initPoller() throws IOException {
        pollers = new ArrayList<>(pollerCount);
        for (int i = 0; i < pollerCount; i++) {
            String pollName = "Poller-" + i;
            Poller poller = new Poller(this, pollName);
            Thread pollerThread = new Thread(poller, pollName);
            pollerThread.setDaemon(true);
            pollerThread.start();
            pollers.add(poller);
        }
    }

    /**
     * 轮询Poller，实现负载均衡
     *
     * @return
     */
    private Poller getPoller() {
        int idx = Math.abs(pollerRotater.incrementAndGet()) % pollers.size();
        return pollers.get(idx);
    }


    /**
     * 初始化Acceptor
     */
    private void initAcceptor() {
        this.acceptor = new Acceptor(this);
        Thread t = new Thread(acceptor, "Acceptor");
        t.setDaemon(true);
        t.start();
    }

    /**
     * 初始化IdleSocketCleaner
     */
    private void initIdleSocketCleaner() {
        cleaner = new IdleSocketCleaner(pollers,keepAliveTimeout);
        cleaner.start();
    }

    public void close() {
        cleaner.shutdown();
        for (Poller poller : pollers) {
            try {
                poller.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        isRunning = false;
        dispatcherServlet.shutdown();
    }


    public boolean isRunning() {
        return isRunning;
    }

    public SocketChannel serverSocketAccept() throws IOException {
        return server.accept();
    }

    /**
     * 将Acceptor接收到的socket放到随机一个Poller的Queue中
     *
     * @param socket
     * @return
     */
    public void setSocketOptions(SocketChannel socket) throws IOException {
        server.configureBlocking(false);
        getPoller().register(socket, true);
        server.configureBlocking(true);
    }

    public int getKeepAliveTimeout() {
        return this.keepAliveTimeout;
    }

}
