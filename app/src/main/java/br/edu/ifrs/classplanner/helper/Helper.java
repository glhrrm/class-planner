package br.edu.ifrs.classplanner.helper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Locale;

public class Helper {

    public static final int DATE = 1;
    public static final int TIME = 2;

    public static boolean isValidDateOrTime(String value, int format) {
        DateTimeFormatter formatter;
        String result;
        switch (format) {
            case DATE:
                formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                LocalDate date;
                try {
                    date = LocalDate.parse(value, formatter);
                    result = date.format(formatter);
                    return result.equals(value);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case TIME:
                formatter = DateTimeFormatter.ofPattern("HH:mm");
                LocalTime time;
                try {
                    time = LocalTime.parse(value, formatter);
                    result = time.format(formatter);
                    return result.equals(value);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + format);
        }
        return false;
    }

    public static boolean isHoliday(String date) {
        String[] holidays = {
                "01/01/2021",
                "02/02/2021",
                "15/02/2021",
                "02/04/2021",
                "21/04/2021",
                "01/05/2021",
                "03/06/2021",
                "07/09/2021",
                "20/09/2021",
                "12/10/2021",
                "02/11/2021",
                "15/11/2021",
                "25/12/2021"
        };
        return Arrays.asList(holidays).contains(date);
    }

//    public static int generateJobId(String date, String time) {
//        LocalDateTime localDateTime = parseDateTime(date + time);
//        long millis = ZonedDateTime.of(localDateTime, ZoneId.systemDefault()).toInstant().toEpochMilli();
//        return (int) (millis % Integer.MAX_VALUE);
//    }

    public static LocalDate parseDate(String date) {
        return LocalDate.parse(date,
                DateTimeFormatter.ofPattern("dd/MM/yyyy")
                        .withLocale(new Locale("pt", "BR")));
    }

    public static LocalDateTime parseDateTime(String dateTime) {
        return LocalDateTime.parse(dateTime,
                DateTimeFormatter.ofPattern("dd/MM/yyyyHH:mm")
                        .withLocale(new Locale("pt", "BR")));
    }
}
