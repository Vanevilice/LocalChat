package com.javarush.task.task30.task3008.client;

import com.javarush.task.task30.task3008.Connection;
import com.javarush.task.task30.task3008.ConsoleHelper;
import com.javarush.task.task30.task3008.Message;
import com.javarush.task.task30.task3008.MessageType;

import java.io.Console;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;

public class Client {

    public static void main(String[] args) {
        Client client = new Client();
        client.run();
    }

   protected Connection connection;

    private volatile boolean clientConnected = false;

    public void run() {
        SocketThread helpSocket = getSocketThread();
        helpSocket.setDaemon(true);
        helpSocket.start();

        try {
            synchronized (this) {
            wait();
            }
        } catch (InterruptedException e) {
            ConsoleHelper.writeMessage("Произошла ошибка во время работы клиента");
            return;
        }
        if (clientConnected)
            ConsoleHelper.writeMessage("Соедение с сервером установлено. Для входа наберите команду 'exit'.");
            else
                ConsoleHelper.writeMessage("Произошла ошибка во время работы клиента");

            while(clientConnected) {
            String text = ConsoleHelper.readString();
            if (text.equalsIgnoreCase("exit")) {
            break;
            }

            if (shouldSendTextFromConsole()) sendTextMessage(text);

        }
    }

   public class SocketThread extends Thread {

        public void run() {
            try {
                connection = new Connection(new Socket(getServerAddress(),getServerPort()));
                clientHandshake();
                clientMainLoop();
            } catch (IOException | ClassNotFoundException e) {
                notifyConnectionStatusChanged(false);
            }
        }

        protected void processIncomingMessage(String message) {
            ConsoleHelper.writeMessage(message);
        }

        protected void informAboutAddingNewUser(String userName) {
            ConsoleHelper.writeMessage(userName + " присоединился к чату");
        }

        protected void informAboutDeletingNewUser(String userName) {
            ConsoleHelper.writeMessage(userName + " покинул чат");
        }

        protected void notifyConnectionStatusChanged(boolean clientConnected) {
            Client.this.clientConnected = clientConnected;
            synchronized (Client.this) {
                Client.this.notify();
            }
        }

        protected void clientHandshake() throws IOException,ClassNotFoundException {
            while(true) {
               Message message = connection.receive();
                if(message.getType() == MessageType.NAME_REQUEST) {
                    String userName = getUserName();
                    connection.send(new Message(MessageType.USER_NAME, userName));
                }
                    else if(message.getType() == MessageType.NAME_ACCEPTED) {
                        notifyConnectionStatusChanged(true);
                        return;
                    }
                    else {
                    throw new IOException("Unexpected MessageType");
                }
                }
            }



            protected void clientMainLoop() throws IOException, ClassNotFoundException {
            while (true) {
                Message message = connection.receive();
                String stringMes = message.toString();
                if (message.getType() == MessageType.TEXT) {
                    processIncomingMessage(message.getData());
                }
                else if (message.getType() == MessageType.USER_ADDED){
                    informAboutAddingNewUser(message.getData());
                }
                else if (message.getType() == MessageType.USER_REMOVED) {
                    informAboutDeletingNewUser(message.getData());
                }
                else
                    throw new IOException("Unexpected MessageType");
            }
            }


    }

    protected String getServerAddress() {
        ConsoleHelper.writeMessage("Введите адрес сервера");
        return ConsoleHelper.readString();
    }

    protected int getServerPort() {
       ConsoleHelper.writeMessage("Введите номер порта сервера");
       return ConsoleHelper.readInt();
    }

    protected String getUserName() {
       ConsoleHelper.writeMessage("Введите имя пользователя");
       return ConsoleHelper.readString();
    }

    protected boolean shouldSendTextFromConsole() {
       return true;
    }

    protected SocketThread getSocketThread() {
       SocketThread socketThread = new SocketThread();
       return socketThread;
    }

    protected void sendTextMessage(String text)  {
       try {
           connection.send(new Message(MessageType.TEXT, text));
       } catch (IOException e) {
           ConsoleHelper.writeMessage("При отправке сообщения возникла ошибка");
           clientConnected = false;
       }
    }
}
