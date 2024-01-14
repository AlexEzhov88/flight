package com.platform.test.block3;

import com.google.gson.Gson;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * Главный класс приложения для анализа данных о полетах.
 */
public class Main {
    /**
     * Точка входа в приложение. Анализирует данные о полетах и записывает результат в файл.
     *
     * @param args аргументы командной строки (не используются)
     */
    public static void main(String[] args) {
        // Получаем InputStream для файла tickets.json из ресурсов
        InputStream inputStream = Main.class.getResourceAsStream("/tickets.json");

        if (inputStream != null) {
            try (Scanner scanner = new Scanner(inputStream, StandardCharsets.UTF_8)) {
                scanner.useDelimiter("\\A"); // Читаем весь файл целиком
                String jsonContent = scanner.hasNext() ? scanner.next() : "";

                // Преобразуем JSON-строку в объект TicketList с использованием Gson
                Gson gson = new Gson();
                TicketList ticketList = gson.fromJson(jsonContent, TicketList.class);

                FlightAnalyzerService flightAnalyzerService = new FlightAnalyzerServiceImpl();
                String analysisResult = flightAnalyzerService.analyzeFlights(ticketList, "VVO", "TLV");

                // Записываем результаты в файл
                writeToFile(analysisResult);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Ресурс tickets.json не найден.");
        }
    }

    /**
     * Записывает строку в файл.
     *
     * @param content строка для записи
     */
    private static void writeToFile(String content) {
        try (FileWriter writer = new FileWriter("output.txt", StandardCharsets.UTF_8)) {
            writer.write(content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
