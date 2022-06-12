package com.javarush.task.task30.task3008.client;

import com.javarush.task.task30.task3008.ConsoleHelper;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Random;

public class BotClient extends Client{

    public static void main(String[] args) {
        BotClient client = new BotClient();
        client.run();
    }



    public class BotSocketThread extends SocketThread {
        @Override
        protected void clientMainLoop() throws IOException, ClassNotFoundException {
            sendTextMessage("Привет чатику. Я бот. Понимаю команды: дата, день, месяц, год, время, час, минуты, секунды.");
            super.clientMainLoop();
        }

        @Override
        protected void processIncomingMessage(String message) {
                // Выводим текст сообщения в консоль
                ConsoleHelper.writeMessage(message);

                // Отделяем отправителя от текста сообщения
                String userNameDelimiter = ": ";
                String[] split = message.split(userNameDelimiter);
                if (split.length != 2) return;

                String messageWithoutUserName = split[1];

                // Подготавливаем формат для отправки даты согласно запросу
                String format = null;
                switch (messageWithoutUserName) {
                    case "дата":
                        format = "d.MM.YYYY";
                        break;
                    case "день":
                        format = "d";
                        break;
                    case "месяц":
                        format = "MMMM";
                        break;
                    case "год":
                        format = "YYYY";
                        break;
                    case "время":
                        format = "H:mm:ss";
                        break;
                    case "час":
                        format = "H";
                        break;
                    case "минуты":
                        format = "m";
                        break;
                    case "секунды":
                        format = "s";
                        break;
                }

                if (format != null) {
                    String answer = new SimpleDateFormat(format).format(Calendar.getInstance().getTime());
                    BotClient.this.sendTextMessage("Информация для " + split[0] + ": " + answer);
                }
            }
        }



    @Override
    protected SocketThread getSocketThread() {
        return new BotSocketThread();
    }

    @Override
    protected boolean shouldSendTextFromConsole() {
        return false;
    }

    @Override
    protected String getUserName() {
        int rand = (int) (Math.random()*100);
        String botName = "date_bot_" + rand;
        return botName;
    }
}
