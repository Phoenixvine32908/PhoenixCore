package net.phoenix.core.utils;

public final class CompactCount {

    private CompactCount() {}

    public static String compactIfNumeric(String s) {
        if (s == null || s.isEmpty()) return s;

        String cleaned = s.replace(",", "").replace("_", "").trim();
        if (cleaned.isEmpty()) return s;

        for (int i = 0; i < cleaned.length(); i++) {
            if (!Character.isDigit(cleaned.charAt(i))) return s;
        }

        long v;
        try {
            v = Long.parseLong(cleaned);
        } catch (Throwable t) {
            return s;
        }

        if (v < 10_000) return s;

        if (v >= 1_000_000_000L) return fmt(v, 1_000_000_000L, "B");
        if (v >= 1_000_000L) return fmt(v, 1_000_000L, "M");
        return fmt(v, 1_000L, "k");
    }

    private static String fmt(long v, long unit, String suf) {
        double d = (double) v / (double) unit;

        if (d >= 100.0) return ((long) d) + suf;

        double t = Math.floor(d * 10.0) / 10.0;
        if (t == Math.floor(t)) return ((long) t) + suf;
        return t + suf;
    }
}
