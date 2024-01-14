package com.platform.test.block3;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Реализация интерфейса {@link FlightAnalyzerService} для анализа данных о полетах.
 */
public class FlightAnalyzerServiceImpl implements FlightAnalyzerService {

    /**
     * Анализирует данные о полетах и возвращает результат в виде строки.
     *
     * @param ticketList  список билетов
     * @param origin      место отправления
     * @param destination место назначения
     * @return результат анализа в виде строки
     */
    @Override
    public String analyzeFlights(TicketList ticketList, String origin, String destination) {
        StringBuilder result = new StringBuilder();

        List<Ticket> relevantTickets = filterTickets(ticketList.getTickets(), origin, destination);

        if (!relevantTickets.isEmpty()) {
            result.append("Минимальное время полета между ").append(origin).append(" и ").append(destination)
                    .append(" для каждого авиаперевозчика:\n");
            result.append(calculateMinFlightTime(relevantTickets));

            result.append("\n\nРазница между средней ценой и медианой для полета между ").append(origin)
                    .append(" и ").append(destination).append(":\n");
            result.append(calculatePriceDifference(relevantTickets));
        } else {
            result.append("Не найдены подходящие билеты.");
        }

        return result.toString();
    }

    /**
     * Фильтрует билеты по местам отправления и назначения.
     *
     * @param tickets     список билетов
     * @param origin      место отправления
     * @param destination место назначения
     * @return отфильтрованный список билетов
     */
    private List<Ticket> filterTickets(List<Ticket> tickets, String origin, String destination) {
        return tickets.stream()
                .filter(ticket ->
                        ticket.getOrigin().equals(origin)
                                && ticket.getDestination().equals(destination)
                                && ticket.getDeparture_date() != null
                                && ticket.getDeparture_time() != null
                                && ticket.getArrival_date() != null
                                && ticket.getArrival_time() != null)
                .collect(Collectors.toList());
    }

    /**
     * Рассчитывает минимальное время полета для каждого авиаперевозчика.
     *
     * @param tickets список билетов
     * @return строка с результатами расчета
     */
    private String calculateMinFlightTime(List<Ticket> tickets) {
        StringBuilder result = new StringBuilder();

        tickets.stream()
                .collect(Collectors.groupingBy(Ticket::getCarrier,
                        Collectors.minBy(Comparator.comparingInt(this::calculateFlightTime))))
                .forEach((carrier, ticket) -> {
                    long flightTime = ticket.map(this::calculateFlightTime).orElse(0);
                    result.append(carrier).append(": ").append(convertTimeToHoursAndMinutes(flightTime)).append("\n");
                });

        return result.toString();
    }

    /**
     * Рассчитывает время полета в минутах.
     *
     * @param ticket билет
     * @return время полета в минутах
     */
    private int calculateFlightTime(Ticket ticket) {
        long departureMillis = parseDateTime(ticket.getDeparture_date(), ticket.getDeparture_time());
        long arrivalMillis = parseDateTime(ticket.getArrival_date(), ticket.getArrival_time());
        return (int) ((arrivalMillis - departureMillis) / (1000 * 60)); // разница в минутах
    }

    /**
     * Конвертирует время из минут в формат "часы минуты".
     *
     * @param totalMinutes общее количество минут
     * @return время в формате "часы минуты"
     */
    private String convertTimeToHoursAndMinutes(long totalMinutes) {
        long hours = totalMinutes / 60;
        long minutes = totalMinutes % 60;
        return hours + " часов " + minutes + " минут";
    }

    /**
     * Рассчитывает разницу между средней ценой и медианой для полета.
     *
     * @param tickets список билетов
     * @return строка с результатами расчета
     */
    private String calculatePriceDifference(List<Ticket> tickets) {
        StringBuilder result = new StringBuilder();

        List<Integer> prices = tickets.stream().map(Ticket::getPrice).collect(Collectors.toList());

        if (!prices.isEmpty()) {
            double averagePrice = prices.stream().mapToInt(Integer::intValue).average().orElse(0.0);
            double medianPrice = calculateMedian(prices);
            result.append(averagePrice - medianPrice);
        } else {
            result.append("Не удалось рассчитать разницу между средней ценой и медианой.");
        }

        return result.toString();
    }

    /**
     * Рассчитывает медиану списка цен.
     *
     * @param prices список цен
     * @return медиана
     */
    private double calculateMedian(List<Integer> prices) {
        List<Integer> sortedPrices = prices.stream().sorted().toList();
        int size = sortedPrices.size();
        return size % 2 == 0 ?
                (sortedPrices.get(size / 2 - 1) + sortedPrices.get(size / 2)) / 2.0 :
                sortedPrices.get(size / 2);
    }

    /**
     * Парсит дату и время и возвращает время в миллисекундах.
     *
     * @param date дата
     * @param time время
     * @return время в миллисекундах
     * @throws DateTimeParseException если не удалось распарсить дату и время
     */
    private long parseDateTime(String date, String time) {
        String dateTimeString = date + " " + time;

        DateTimeFormatter[] formatters = {
                DateTimeFormatter.ofPattern("dd.MM.yy H:mm"),
                DateTimeFormatter.ofPattern("dd.MM.yy HH:mm")
                // добавьте дополнительные форматы, если необходимо
        };

        for (DateTimeFormatter formatter : formatters) {
            try {
                return LocalDateTime.parse(dateTimeString, formatter)
                        .atZone(ZoneId.systemDefault())
                        .toInstant()
                        .toEpochMilli();
            } catch (DateTimeParseException ignored) {
                // Пробуем следующий формат
            }
        }

        // Если не удалось распарсить, можно выбрасывать исключение или вернуть какое-то значение по умолчанию
        throw new DateTimeParseException("Unable to parse date and time: " + dateTimeString, dateTimeString, 0);
    }
}
