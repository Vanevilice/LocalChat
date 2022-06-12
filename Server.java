package com.javarush.task.task30.task3008;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Server {

    private static Map<String,Connection> connectionMap = new ConcurrentHashMap<>();


    public static void sendBroadcastMessage(Message message) {
        try {
            for (Connection connection: connectionMap.values()) {
            connection.send(message);
            }

        } catch (IOException e) {
            ConsoleHelper.writeMessage("Не получилось отправить сообщение!!!");
        }
    }

   private static class Handler extends Thread {
        Socket socket;

        public Handler(Socket socket) {
            this.socket = socket;
        }

        public void run()  {
            ConsoleHelper.writeMessage("Установлено новое соедение с удаленным адресом: " + socket.getRemoteSocketAddress());

            String userName = null;

            try (Connection connection = new Connection(socket)) {
               userName = serverHandshake(connection);
               sendBroadcastMessage(new Message(MessageType.USER_ADDED,userName));
               notifyUsers(connection,userName);
               serverMainLoop(connection,userName);


            } catch (IOException | ClassNotFoundException e) {
                ConsoleHelper.writeMessage("Ошибка при обмене данными с " + socket.getRemoteSocketAddress());
            }

            if(userName != null) {
                connectionMap.remove(userName);
                sendBroadcastMessage(new Message(MessageType.USER_REMOVED,userName));
            }
            ConsoleHelper.writeMessage("Соединение с " + socket.getRemoteSocketAddress() + " закрыто.");
        }

        private  String serverHandshake(Connection connection) throws IOException, ClassNotFoundException {
            while(true) {
                connection.send(new Message(MessageType.NAME_REQUEST));
                Message message = connection.receive();
                if (message.getType() != MessageType.USER_NAME) {
                    ConsoleHelper.writeMessage("Получено сообщение от " + socket.getRemoteSocketAddress() + ". Тип сообщения не соответствует протоколу.");
                    continue;
                }

                String userName = message.getData();

                if(userName.isEmpty()) {
                    ConsoleHelper.writeMessage("Попытка подключения с пустым именем от: " + socket.getRemoteSocketAddress());
                    continue;
                }
                if(connectionMap.containsKey(userName)) {
                    ConsoleHelper.writeMessage("Такое имя уже существует, введите новое имя пользователя");
                    continue;
                }

                connectionMap.put(userName,connection);
                connection.send(new Message(MessageType.NAME_ACCEPTED));
                return userName;
            }
        }

        private void notifyUsers(Connection connection,String userName) throws IOException {
            for(String name: connectionMap.keySet()) {
                if(name.equals(userName)) continue;
            connection.send(new Message(MessageType.USER_ADDED,name));
            }
        }

        private void serverMainLoop(Connection connection,String userName) throws IOException,ClassNotFoundException {
            while (true) {
                Message message = connection.receive();
                if (message.getMessageType() == MessageType.TEXT) {
                    String data = message.getData();
                    sendBroadcastMessage(new Message(MessageType.TEXT, userName + ": " + data));
                } else {
                    ConsoleHelper.writeMessage("Получено сообщение от " + socket.getRemoteSocketAddress() + "Тип сообщения не соответствует протоколу");
                }
            }
        }


    }

    public static void main(String[] args) {
        ConsoleHelper.writeMessage("Введите порт сервера");
        int port = ConsoleHelper.readInt();
        try (ServerSocket serverSocket = new ServerSocket(port)) {

            ConsoleHelper.writeMessage("Server is ON");

            while(true) {
                Socket socket = serverSocket.accept();
                new Handler(socket).start();
            }
        } catch (Exception e) {
            ConsoleHelper.writeMessage("Произошла ошибка при запуске или работе сервера.");
        }





    }
}
