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
package org.spongepowered.common.advancement.criterion;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.advancements.ICriterionInstance;
import net.minecraft.advancements.criterion.AbstractCriterionTrigger;
import net.minecraft.advancements.criterion.CriterionInstance;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.common.accessor.advancements.CriteriaTriggersAccessor;

public class SpongeScoreTrigger extends AbstractCriterionTrigger<SpongeScoreTrigger.Instance> {

    public static final SpongeScoreTrigger SCORE_TRIGGER = CriteriaTriggersAccessor.accessor$register(new SpongeScoreTrigger(new ResourceLocation("sponge:score")));

    private ResourceLocation resourceLocation;

    private SpongeScoreTrigger(ResourceLocation resourceLocation) {
        this.resourceLocation = resourceLocation;
    }

    @Override
    public ResourceLocation getId() {
        return this.resourceLocation;
    }

    @Override
    public SpongeScoreTrigger.Instance deserializeInstance(JsonObject json, JsonDeserializationContext context) {
        final int score = JSONUtils.getInt(json, "score");
        return new SpongeScoreTrigger.Instance(this.resourceLocation, score);
    }

    public static class Instance extends CriterionInstance {

        private int score;
        public Instance(ResourceLocation criterionIn, int score) {
            super(criterionIn);
        }

        public static Instance ofScore(int score) {
            return new Instance(SpongeScoreTrigger.SCORE_TRIGGER.getId(), score);
        }

        @Override
        public JsonElement serialize() {
            final JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("score", this.score);
            return jsonObject;
        }
    }
}
