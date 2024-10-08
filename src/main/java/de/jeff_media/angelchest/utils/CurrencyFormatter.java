package de.jeff_media.angelchest.utils;

import de.jeff_media.angelchest.AngelChestMain;

public class CurrencyFormatter {

    private final AngelChestMain main;

    public CurrencyFormatter(AngelChestMain main) {
        this.main = main;
    }

    public String format(double price) {
        try {
            return String.format(main.getConfig().getString("price-format", "%,.2f"), price);
        } catch (Throwable t) {
            return String.format("%,.2f", price);
        }
    }
}
