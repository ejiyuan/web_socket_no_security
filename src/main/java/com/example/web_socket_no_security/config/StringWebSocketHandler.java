package com.example.web_socket_no_security.config;


import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class StringWebSocketHandler extends TextWebSocketHandler {

    /**
     * 线程安全的Map
     */
    private static ConcurrentHashMap<String, WebSocketSession> sessionMap = new ConcurrentHashMap<>();

    /**
     * 建立连接，
     * 客户端 websocket = new WebSocket("ws://localhost:8080/websocket")到这里
     *
     * @param session
     * @throws Exception
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        System.out.println("成功建立连接");
        String userId = session.getUri().toString().split("userId=")[1];
        System.out.println(userId);
        if (userId != null) {
            sessionMap.put(userId, session);
            session.sendMessage(new TextMessage(userId + "成功建立socket连接，" + "当前在线人数：" + sessionMap.size()));
            //System.out.println(userId);
            System.out.println(session);
        }
        System.out.println(userId + "成功建立socket连接，" + "当前在线人数：" + sessionMap.size());
    }

    /**
     * 发送消息
     * 客户端调用websocket.send()到这里
     *
     * @param session
     * @param message
     * @throws Exception
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        JSONObject jsonObject = JSONObject.parseObject(message.getPayload());

        String userId = jsonObject.get("receiver").toString(); //获取消息发给谁
        TextMessage sendMessage = new TextMessage(jsonObject.toJSONString());
        if (sessionMap.containsKey(userId)) {
            WebSocketSession ws = sessionMap.get(userId);
            if (ws.isOpen()) {
                sessionMap.get(userId).sendMessage(sendMessage);
            }
        }
    }


    /**
     * 关闭连接
     * 客户端通过 websocket.close()方法到这里
     *
     * @param session
     * @param status
     * @throws Exception
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessionMap.remove(session.getAttributes().get("WEBSOCKET_USERID"));
        super.afterConnectionClosed(session, status);
    }

    /**
     * 连接出错后
     *
     * @param session
     * @param exception
     * @throws Exception
     */
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        if (session.isOpen()) {
            session.close();
        }
        System.out.println("连接出错");
        sessionMap.remove(session.getAttributes().get("WEBSOCKET_USERID"));
    }
}