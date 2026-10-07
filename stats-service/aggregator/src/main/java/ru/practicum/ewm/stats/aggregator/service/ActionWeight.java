package ru.practicum.ewm.stats.aggregator.service;

import ru.practicum.ewm.stats.avro.ActionTypeAvro;

public final class ActionWeight {

    private static final double VIEW_WEIGHT = 0.4;
    private static final double REGISTER_WEIGHT = 0.8;
    private static final double LIKE_WEIGHT = 1.0;

    private ActionWeight() {
    }

    public static double of(ActionTypeAvro actionType) {
        return switch (actionType) {
            case VIEW -> VIEW_WEIGHT;
            case REGISTER -> REGISTER_WEIGHT;
            case LIKE -> LIKE_WEIGHT;
        };
    }
}
