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
package org.spongepowered.common.world;

import org.spongepowered.api.CatalogKey;
import org.spongepowered.api.service.context.Context;
import org.spongepowered.api.world.dimension.DimensionType;
import org.spongepowered.common.bridge.world.dimension.DimensionTypeBridge;

public final class SpongeDimensionType implements DimensionType {

    private final net.minecraft.world.dimension.DimensionType dimensionType;

    public SpongeDimensionType(net.minecraft.world.dimension.DimensionType dimensionType) {
        this.dimensionType = dimensionType;
    }

    @Override
    public CatalogKey getKey() {
        return ((DimensionTypeBridge) this.dimensionType).bridge$getKey();
    }

    @Override
    public boolean hasSkylight() {
        return this.dimensionType.hasSkyLight();
    }

    @Override
    public Context getContext() {
        return ((DimensionTypeBridge) this.dimensionType).bridge$getContext();
    }

    public net.minecraft.world.dimension.DimensionType getMinecraftDimensionType() {
        return this.dimensionType;
    }
}