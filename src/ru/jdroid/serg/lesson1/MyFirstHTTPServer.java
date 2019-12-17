package ru.jdroid.serg.lesson1;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;

public class MyFirstHTTPServer {

    // Объявление порта (константа)
    public static final int PORT = 9999;

    public static void main(String[] args) {
        // Работаем с вводом выводом -> нужен try{}catch
        try {
            // InetAddress позволяет узнать ip этого компютера и его имя :)
            InetAddress inetAddress = InetAddress.getLocalHost();
            // ServerSocket - Специальный класс, который умеет слушать порт и создавать соединения
            ServerSocket serverSocket = new ServerSocket(PORT);

            System.out.println("Я сервер, мой ip :" + inetAddress.getHostAddress());
            System.out.println("Моё доменное имя :" + inetAddress.getHostName());
            System.out.println("Слушаю порт " + PORT + "...");
            System.out.println("Для подключения введите в браузер возможные URL...");
            System.out.println("http://" + inetAddress.getHostAddress() + ":" + PORT + "/какие-то_данные/");
            System.out.println("http://" + inetAddress.getHostName() + ":" + PORT + "/какие-то_данные/");
            System.out.println("http://localhost:" + PORT + "/какие-то_данные/");

            // Тут программа зависает до тех пор, пока serverSocket не поймает нового клиента
            Socket socket = serverSocket.accept(); // < -- Сюда сохраняется socket (inet соединение)
            // Как только клиент подключился по нужному порту, accept() сразу создаёт Socket и возвращает его
            // Socket - класс, который умеет хранить информацию о соединении, хранить потоки ввода вывода, так-же
            // ip подключившегося клиента.

            System.out.println("Клиент подключился {" + socket.getInetAddress() + "}, есть сокет !\n");
            System.out.println("Создаю объекты инструментов для чтения данных из потока, и для записи данных в поток -->\n");

            // Тут мы создаём объекты для чтения из входного потока сокета, и записи в выходной поток сокета.
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream())); // <-- IN
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream())); // OUT -->
            System.out.println("Жду, когда в потоке появятся данные от клиента...\n");

            String line; // Будем читать сообщение от клиента по строчкам
            StringBuilder result = new StringBuilder(); // А сюда будем закидывать эти строчки (собират вместе)
            // StringBuilder гораздо лучще чем "str"+"str" или str += "new part" т.к. при конкотенации объект String
            // пересоздаётся заново (неявно) str = new String(char[]*); внутри String обычный char[] его нельзя менять,
            // т.к. он фиксированной длины (new char[5];) -> при увеличении строки через + всё заново создаётся
            // (это нехорошо, когда строки могут быть большие и + идёт сериями) :)

            System.out.println("Читаю поток от клиента -->\n");
            // читаем пока строки не закончатся
            while ((line = reader.readLine()) != null) {
                System.out.print(".");
                // !это условие защитное! Некоторые браузеры, в качестве заключающего символа eof
                // используют пустую строку \r\n\r\n  (\r\n[тут пустая строка 0 длины]\r\n)
                // если не выйти, при данных обстоятельствах, то можно зависнуть в этом цикле...
                if (line.isEmpty()) {
                    break;
                }
                // просто приклеиваем в StringBuilder новые строки
                result.append(line);
                // SB склеивает всё в одну строчку (как print), поэтому надо добавить \n в конце строки
                result.append('\n');
            }

            System.out.println("\nВсё прочитал! Запрос от клиента в кодировке ASCII(можем прочитать)=>\n");
            System.out.println(result);
            System.out.println("Запрос от клиента в сыром виде(не можем прочитать)=>\n");
            // Выведем код каждой буковки для оценки того, как это видит компьютер (он так видит любые файлы)
            for (int charCode : result.toString().toCharArray()) {
                // формат нужен для приведения десятичных чисел в 0x0 (шеснадцатеричные)
                // 10 -> 0A   11 -> 0B и т.д.
                System.out.print("[" + String.format("%02X", charCode) + "].");
                // для красоты (чтобы print не лепил всё в одну строчку)
                if (charCode == '\n') System.out.print('\n');
            }

            System.out.println("Отправляю ответ ...");

            // Это типа наша страничка )
            // она могла быть взята например из файла
            String htmlContent =
                    "<!DOCTYPE html>\n" +
                            "<html>\n" +
                            "<head>\n" +
                            "<meta charset=\"UTF-8\">\n" +
                            "</head>\n" +
                            "<body>\n" +
                            "<h1>\n" +
                            "Hello World!\n" +
                            "</h1>\n" +
                            "</body>\n" +
                            "</html>";

            // далее мы последовательно отправляем строчки через выходной поток OUT -->
            // это стандартный ответ сервера на GET HTTP/1.1 запрос
            // здесь код 200 OK - это успешный запрос
            // есть ещё ряд других кодов ошибок )
            // 404 Not Found  500 Internal Server Error  и т.д.
            writer.println("HTTP/1.1 200 OK"); // говорим клиенту, что его запрос принят и обработан успешно
            writer.println(new Date().toString()); // отправим текущую дату
            writer.println("Server: MyServer1.0"); // версию и название программы, которая обработала запрос )
            writer.println("Connection: Closed"); // говорим, что закроем соединение после отправки
            writer.println("Content-Type: text/html; charset=utf-8"); // тип дополнительных данных (может быть image или что-то ещё)
            writer.println("Content-Length: " + htmlContent.length()); // длинна доп контента
            writer.println(); // пустая разделительная строка которую ловил {line.isEmpty()} отделяет заголовок от доп контента
            writer.println(htmlContent); // отправляем HTML доп контентом (тело ответа)

            System.out.println("Ответ отправлен!");

            System.out.println("Закрываю потоки!");
            writer.flush(); // удаляем неотправленные данные из буфера
            writer.close();
            reader.close();

            System.out.println("Разрываю соединение!");
            socket.close();

            System.out.println("Закрываю сервер!");
            serverSocket.close();

            System.out.println("Завершение.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

