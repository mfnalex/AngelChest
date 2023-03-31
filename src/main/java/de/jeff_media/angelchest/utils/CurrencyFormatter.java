package de.jeff_media.angelchest.utils;

import de.jeff_media.angelchest.Main;

public class CurrencyFormatter {

    private final Main main;

    public CurrencyFormatter(Main main) {
        this.main = main;
    }

    public String format(double price) {
        try {
            return String.format(main.getConfig().getString("currency-format", "%,.2f"), price);
        } catch (Throwable t) {
            return String.format("%,.2f", price);
        }
    }
}
