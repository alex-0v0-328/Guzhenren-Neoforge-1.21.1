package com.unknown.guzhenren;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.unknown.guzhenren.command.ModEnumArgument;
import com.unknown.guzhenren.custom.enums.path.GuPath;
import com.unknown.guzhenren.custom.enums.path.MarkTag;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * A suggestion computed from an argument parsed earlier, asked for through the {@code /gzr} alias as
 * well as through the full root.
 *
 * <p>⚠ The tree is a stand-in: no command ships such a suggestion today, and a real node is typed to a
 * {@code CommandSourceStack} no test can build. It guards the seam in {@code get}, not a feature.
 *
 * @author Alex
 * @since 1.0.0
 */
class ModEnumArgumentTest {

    private static final String ARG_PATH = "path";
    private static final String ARG_TAG = "tag";

    @Test
    @DisplayName("the /gzr alias offers what the full root does -- a redirect hides the earlier word")
    void aliasOffersWhatTheFullRootDoes() {
        List<String> expected = List.of("natural", "race", "strength_bear", "strength_beasts",
                "strength_boar", "strength_human");
        assertEquals(expected, suggest("guzhenren strength "));
        assertEquals(expected, suggest("gzr strength "));
    }

    @Test
    @DisplayName("a path owning no tags is offered the two unowned ones alone, through either root")
    void unownedPathOffersTheTwoUnownedTags() {
        assertEquals(List.of("natural", "race"), suggest("guzhenren qi "));
        assertEquals(List.of("natural", "race"), suggest("gzr qi "));
    }

    @Test
    @DisplayName("a nonsense path offers nothing and throws nothing -- the argument accepts any bare word")
    void nonsensePathOffersNothing() {
        assertTrue(suggest("guzhenren not_a_path ").isEmpty());
        assertTrue(suggest("gzr not_a_path ").isEmpty());
    }

    //region the tree under test

    private static List<String> suggest(String input) {
        CommandDispatcher<Object> dispatcher = dispatcher();
        Suggestions offered = dispatcher.getCompletionSuggestions(dispatcher.parse(input, new Object())).join();
        return offered.getList().stream().map(Suggestion::getText).toList();
    }

    private static CommandDispatcher<Object> dispatcher() {
        CommandDispatcher<Object> dispatcher = new CommandDispatcher<>();
        LiteralCommandNode<Object> root = dispatcher.register(
                LiteralArgumentBuilder.<Object>literal("guzhenren")
                        .then(RequiredArgumentBuilder.<Object, String>argument(ARG_PATH, StringArgumentType.word())
                                .then(RequiredArgumentBuilder
                                        .<Object, String>argument(ARG_TAG, StringArgumentType.word())
                                        .suggests(ModEnumArgumentTest::offerFittingTags))));
        dispatcher.register(LiteralArgumentBuilder.<Object>literal("gzr").redirect(root));
        return dispatcher;
    }

    private static CompletableFuture<Suggestions> offerFittingTags(
            CommandContext<Object> context, SuggestionsBuilder builder) throws CommandSyntaxException {
        GuPath path = ModEnumArgument.get(context, ARG_PATH, GuPath.values());
        for (MarkTag tag : MarkTag.values()) {
            if (tag.fitsOn(path)) builder.suggest(tag.getSerializedName());
        }
        return builder.buildFuture();
    }

    //endregion
}
