package com.unknown.guzhenren.item.gu;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class MealPortionTest {

    @Test
    void capsItemsAtCount() {
        TendedGuItem.Meal meal = TendedGuItem.portion(3, 100, 10, 5);
        assertEquals(3, meal.items());
        assertEquals(1, meal.gained());
    }
    @Test
    void capsItemsAtNeed() {
        TendedGuItem.Meal meal = TendedGuItem.portion(99, 4, 10, 5);
        assertEquals(8, meal.items());
        assertEquals(4, meal.gained());
    }
    @Test
    void roundsEatenUpToCoverGained() {
        TendedGuItem.Meal meal = TendedGuItem.portion(99, 100, 3, 5);
        assertEquals(60, meal.items());
        assertEquals(100, meal.gained());
        assertEquals(60, meal.eaten());
    }
    @Test
    void zeroGainYieldsZeroEaten() {
        TendedGuItem.Meal meal = TendedGuItem.portion(99, 0, 10, 5);
        assertEquals(0, meal.items());
        assertEquals(0, meal.gained());
        assertEquals(0, meal.eaten());
    }
}
