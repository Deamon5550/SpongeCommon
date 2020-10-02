/*
 * This file is part of Sponge, licensed under the MIT License (MIT).
 *
 * Copyright (c) SpongePowered <https://www.spongepowered.org>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.spongepowered.test.advancement;

import com.google.inject.Inject;
import net.kyori.adventure.text.Component;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.advancement.Advancement;
import org.spongepowered.api.advancement.AdvancementProgress;
import org.spongepowered.api.advancement.AdvancementTree;
import org.spongepowered.api.advancement.AdvancementTypes;
import org.spongepowered.api.advancement.DisplayInfo;
import org.spongepowered.api.advancement.TreeLayout;
import org.spongepowered.api.advancement.TreeLayoutElement;
import org.spongepowered.api.advancement.criteria.AdvancementCriterion;
import org.spongepowered.api.advancement.criteria.OrCriterion;
import org.spongepowered.api.advancement.criteria.ScoreAdvancementCriterion;
import org.spongepowered.api.advancement.criteria.trigger.FilteredTrigger;
import org.spongepowered.api.advancement.criteria.trigger.FilteredTriggerConfiguration;
import org.spongepowered.api.advancement.criteria.trigger.Trigger;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.data.persistence.AbstractDataBuilder;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataSerializable;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.advancement.AdvancementTreeEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.item.inventory.ChangeInventoryEvent;
import org.spongepowered.api.event.item.inventory.container.InteractContainerEvent;
import org.spongepowered.api.event.lifecycle.RegisterCatalogEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.BlockCarrier;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.query.QueryTypes;
import org.spongepowered.api.item.inventory.type.CarriedInventory;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.jvm.Plugin;
import org.spongepowered.test.LoadableModule;

import java.util.Optional;

@Plugin("advancementtest")
public final class AdvancementTest implements LoadableModule {

    private final PluginContainer plugin;
    private boolean enabled = false;
    private Advancement rootAdvancement;
    private Trigger<InventoryChangeTriggerConfig> inventoryChangeTrigger;
    private TriggerListeners listeners = new TriggerListeners();
    private ScoreAdvancementCriterion counter1;
    private AdvancementCriterion counter1Bypass;
    private ScoreAdvancementCriterion counter2;
    private Advancement counterAdvancement1;
    private Advancement counterAdvancement2;

    @Inject
    public AdvancementTest(final PluginContainer plugin) {
        this.plugin = plugin;
    }

    @Override
    public void enable(CommandContext ctx) {
        this.enabled = true;
        Sponge.getEventManager().registerListeners(this.plugin, this.listeners);
        try {
            Sponge.getCommandManager().process("reload");
        } catch (CommandException e) {
            e.printStackTrace();
        }
        ctx.getCause().first(ServerPlayer.class).map(player -> player.getProgress(this.rootAdvancement).grant());
    }

    @Override
    public void disable(CommandContext ctx) {
        this.enabled = false;
        Sponge.getEventManager().unregisterListeners(this.listeners);
        try {
            Sponge.getCommandManager().process("reload");
        } catch (CommandException e) {
            e.printStackTrace();
        }
    }

    @Listener
    public void onTreeAdjust(AdvancementTreeEvent.GenerateLayout event) {
        final AdvancementTree tree = event.getTree();
        final TreeLayout layout = event.getLayout();
        for (TreeLayoutElement element : layout.getElements()) {
            System.out.println(element);
        }
    }

    @Listener
    public void onTriggerRegistry(RegisterCatalogEvent<Trigger> event) {
        Sponge.getDataManager().registerBuilder(InventoryChangeTriggerConfig.class, new InventoryChangeTriggerConfig.Builder());
        this.inventoryChangeTrigger = Trigger.builder()
                .dataSerializableConfig(InventoryChangeTriggerConfig.class)
                .listener(triggerEvent -> {
                    final ItemStack stack = triggerEvent.getTrigger().getConfiguration().stack;
                    final int found = triggerEvent.getPlayer().getInventory().query(QueryTypes.ITEM_STACK_IGNORE_QUANTITY, stack).totalQuantity();
                    triggerEvent.setResult(stack.getQuantity() <= found);
                })
                .id("my_inventory_trigger")
                .build();
        event.register(this.inventoryChangeTrigger);
    }


    @Listener
    @SuppressWarnings("unchecked")
    public void onAdvancementRegistry(RegisterCatalogEvent<Advancement> event) {

        if (!enabled) {
            return;
        }

        this.rootAdvancement = Advancement.builder()
                .criterion(AdvancementCriterion.dummy())
                .displayInfo(DisplayInfo.builder()
                                .icon(ItemTypes.COMMAND_BLOCK)
                                .title(Component.text("Advancement Tests"))
                                .description(Component.text("Dummy trigger. Granted manually after testplugin is enabled"))
                                .build())
                .root().background("textures/gui/advancements/backgrounds/stone.png")
                .key(ResourceKey.of(this.plugin, "root"))
                .build();
        event.register(rootAdvancement);

        final AdvancementCriterion someDirtCriterion = AdvancementCriterion.builder().trigger(
                FilteredTrigger.builder()
                        .type(inventoryChangeTrigger)
                        .config(new InventoryChangeTriggerConfig(ItemStack.of(ItemTypes.DIRT)))
                        .build()
        ).name("some_dirt").build();

        final Advancement someDirt = Advancement.builder()
                .criterion(someDirtCriterion)
                .displayInfo(DisplayInfo.builder()
                        .icon(ItemTypes.DIRT)
                        .title(Component.text("Got dirt!"))
                        .type(AdvancementTypes.TASK)
                        .build())
                .parent(this.rootAdvancement)
                .key(ResourceKey.of(this.plugin, "some_dirt"))
                .build();
        event.register(someDirt);

        final AdvancementCriterion lotsOfDirtCriterion = AdvancementCriterion.builder().trigger(
                FilteredTrigger.builder()
                        .type(inventoryChangeTrigger)
                        .config(new InventoryChangeTriggerConfig(ItemStack.of(ItemTypes.DIRT, 64)))
                        .build()
        ).name("lots_of_dirt").build();

        final Advancement lotsOfDirt = Advancement.builder()
                .criterion(lotsOfDirtCriterion)
                .displayInfo(DisplayInfo.builder()
                        .icon(ItemTypes.DIRT)
                        .title(Component.text("Got more dirt!"))
                        .type(AdvancementTypes.GOAL)
                        .build())
                .parent(someDirt)
                .key(ResourceKey.of(this.plugin, "lots_of_dirt"))
                .build();
        event.register(lotsOfDirt);

        final AdvancementCriterion tonsOfDirtCriterion = AdvancementCriterion.builder().trigger(
                FilteredTrigger.builder()
                        .type(inventoryChangeTrigger)
                        .config(new InventoryChangeTriggerConfig(ItemStack.of(ItemTypes.DIRT, 64*9)))
                        .build()
        ).name("tons_of_dirt").build();

        final Advancement tonsOfDirt = Advancement.builder()
                .criterion(tonsOfDirtCriterion)
                .displayInfo(DisplayInfo.builder()
                        .icon(ItemTypes.DIRT)
                        .title(Component.text("Got tons of dirt!"))
                        .type(AdvancementTypes.CHALLENGE)
                        .hidden(true)
                        .build())
                .parent(lotsOfDirt)
                .key(ResourceKey.of(this.plugin, "tons_of_dirt"))
                .build();
        event.register(tonsOfDirt);

        this.counter1 = ScoreAdvancementCriterion.builder().goal(10).name("counter").build();
        this.counter1Bypass = AdvancementCriterion.dummy();
        this.counterAdvancement1 = Advancement.builder()
                .criterion(OrCriterion.of(counter1, counter1Bypass))
                .displayInfo(DisplayInfo.builder()
                        .icon(ItemTypes.CHEST)
                        .title(Component.text("Open some chests."))
                        .type(AdvancementTypes.GOAL)
                        .build())
                .parent(this.rootAdvancement)
                .key(ResourceKey.of(this.plugin, "counting"))
                .build();
        event.register(this.counterAdvancement1);

        this.counter2 = ScoreAdvancementCriterion.builder().goal(20).name("counter").build();
        this.counterAdvancement2 = Advancement.builder()
                .criterion(counter2)
                .displayInfo(DisplayInfo.builder()
                        .icon(ItemTypes.CHEST)
                        .title(Component.text("Open more chests"))
                        .type(AdvancementTypes.CHALLENGE)
                        .build())
                .parent(counterAdvancement1)
                .key(ResourceKey.of(this.plugin, "counting_more"))
                .build();
        event.register(this.counterAdvancement2);
    }

    public class TriggerListeners {

        @Listener
        public void onContainerEvent(ChangeInventoryEvent event, @First ServerPlayer player) {
            AdvancementTest.this.inventoryChangeTrigger.trigger(player);
        }

        @Listener
        public void onConainterEvent(InteractContainerEvent.Open event, @First ServerPlayer player) {

            final AdvancementProgress progress1 = player.getProgress(AdvancementTest.this.counterAdvancement1);
            if (progress1.achieved()) {
                final AdvancementProgress progress2 = player.getProgress(AdvancementTest.this.counterAdvancement2);
                progress2.require(AdvancementTest.this.counter2).add(1);

            } else {
                progress1.require(AdvancementTest.this.counter1).add(1);
                final Object carrier = ((CarriedInventory) event.getContainer()).getCarrier().orElse(null);
                if (carrier instanceof BlockCarrier) {
                    if (((BlockCarrier) carrier).getLocation().getBlockType().isAnyOf(BlockTypes.TRAPPED_CHEST)) {
                        progress1.require(AdvancementTest.this.counter1Bypass).grant();
                    }
                }
            }
        }

    }

    public static class InventoryChangeTriggerConfig implements FilteredTriggerConfiguration, DataSerializable {
        private ItemStack stack;

        public InventoryChangeTriggerConfig(ItemStack stack) {
            this.stack = stack;
        }

        public InventoryChangeTriggerConfig(DataView stack) {
            this.stack = ItemStack.builder().fromContainer(stack).build();
        }

        @Override
        public int getContentVersion() {
            return 1;
        }

        @Override
        public DataContainer toContainer() {
            return this.stack.toContainer();
        }

        private static class Builder extends AbstractDataBuilder<InventoryChangeTriggerConfig> {

            public Builder() {
                super(InventoryChangeTriggerConfig.class, 1);
            }

            @Override
            protected Optional<InventoryChangeTriggerConfig> buildContent(DataView container) throws InvalidDataException {
                return Optional.of(new InventoryChangeTriggerConfig(container));
            }
        }
    }

}
