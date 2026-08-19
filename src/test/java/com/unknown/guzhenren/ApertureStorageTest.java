package com.unknown.guzhenren;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.unknown.guzhenren.attachment.data.aperture.ApertureStorage;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ApertureStorageTest {

    @Test
    @DisplayName("storage owns its stacks instead of aliasing caller input")
    void storageCopiesCallerInput() {
        ItemStack source = new ItemStack(Items.DIRT, 3);
        ApertureStorage storage = new ApertureStorage(List.of(List.of(source)), List.of(source));

        source.setCount(7);

        assertEquals(3, storage.get(0).getFirst().getCount());
        assertEquals(3, storage.getVital(0).getCount());
    }

    @Test
    @DisplayName("storage readers return stack copies")
    void storageReadersDoNotExposeState() {
        ApertureStorage storage = new ApertureStorage(
                List.of(List.of(new ItemStack(Items.DIRT, 3))),
                List.of(new ItemStack(Items.DIRT, 4)));

        storage.get(0).getFirst().setCount(7);
        storage.getVital(0).setCount(8);

        assertEquals(3, storage.get(0).getFirst().getCount());
        assertEquals(4, storage.getVital(0).getCount());
    }

    @Test
    @DisplayName("with also copies a replacement stack")
    void withDoesNotAliasReplacement() {
        ItemStack replacement = new ItemStack(Items.DIRT, 3);
        ApertureStorage storage = ApertureStorage.DEFAULT.with(0, List.of(replacement));

        replacement.setCount(7);

        assertEquals(3, storage.get(0).getFirst().getCount());
    }

    @Test
    @DisplayName("page reads and comparisons stay bounded to the requested window")
    void pageOperationsUseOnlyTheirWindow() {
        ApertureStorage storage = ApertureStorage.DEFAULT.withPage(0, 2,
                List.of(new ItemStack(Items.DIRT, 3), ItemStack.EMPTY));

        List<ItemStack> page = storage.page(0, 2, 2);
        assertEquals(1, page.size());
        assertEquals(3, page.getFirst().getCount());
        assertTrue(storage.matchesPage(0, 2, page));

        page.getFirst().setCount(7);

        assertEquals(3, storage.page(0, 2, 1).getFirst().getCount());
    }
}
