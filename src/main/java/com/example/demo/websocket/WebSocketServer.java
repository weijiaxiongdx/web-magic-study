package com.example.demo.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.util.concurrent.ConcurrentHashMap;

@Component
@ServerEndpoint("/websocket/{name}")
public class WebSocketServer {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketServer.class);

    private Session session;

    private String name;

    private static ConcurrentHashMap<String, Session> webSocketMap = new ConcurrentHashMap<>();


    @OnOpen
    public void onOpen(Session session, @PathParam(value = "name") String name) { //连接建立时触发
        this.session = session;
        this.name = name;

        webSocketMap.put(name, session);
        logger.info("[WebSocketClient] 连接server成功，当前连接人数为：{}, 参数: {}", webSocketMap.size(),name);
    }


    @OnClose
    public void onClose() {
        webSocketMap.remove(this.name);
        logger.info("[WebSocketClient] 退出成功，当前连接人数为：{}", webSocketMap.size());
    }

    @OnMessage
    public void OnMessage(String message) {
        logger.info("[WebSocketServer] 收到了客户端发送来的消息：{}", message);
        GroupSending("这是服务端发出的消息...");
    }


    public void GroupSending(String message) {
        for (String name : webSocketMap.keySet()) {
            try {
                String msg = "欢迎 " + name + " 加入直播间";
                webSocketMap.get(name).getBasicRemote().sendText(msg);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
