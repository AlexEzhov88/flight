package com.platform.test.block3;

import java.util.List;

/**
 * Интерфейс для анализа данных о полетах.
 */
public interface FlightAnalyzerService {
    /**
     * Анализирует данные о полетах и возвращает результат в виде строки.
     *
     * @param ticketList  список билетов
     * @param origin      место отправления
     * @param destination место назначения
     * @return результат анализа в виде строки
     */
    String analyzeFlights(TicketList ticketList, String origin, String destination);
}
