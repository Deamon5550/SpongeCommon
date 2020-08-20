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
package org.spongepowered.vanilla.launch.plugin.loader;

import org.spongepowered.common.launch.plugin.loader.PluginLocator;
import org.spongepowered.plugin.PluginCandidate;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.PluginEnvironment;
import org.spongepowered.plugin.PluginLanguageService;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

public final class VanillaPluginLocator implements PluginLocator {

    private final PluginEnvironment pluginEnvironment;
    private final Map<String, PluginLanguageService> languageServices;
    private final Map<String, List<Path>> pluginFiles;

    private final Map<PluginLanguageService, List<PluginCandidate>> pluginCandidates;

    public VanillaPluginLocator(final PluginEnvironment pluginEnvironment) {
        this.pluginEnvironment = pluginEnvironment;
        this.languageServices = new HashMap<>();
        this.pluginFiles = new HashMap<>();
        this.pluginCandidates = new HashMap<>();
    }

    @Override
    public PluginEnvironment getPluginEnvironment() {
        return this.pluginEnvironment;
    }

    public Map<String, PluginLanguageService> getServices() {
        return Collections.unmodifiableMap(this.languageServices);
    }

    public Map<String, Collection<Path>> getResources() {
        return Collections.unmodifiableMap(this.pluginFiles);
    }

    public Map<PluginLanguageService, List<PluginCandidate>> getCandidates() {
        return this.pluginCandidates;
    }

    public void initialize() {
        for (final Map.Entry<String, PluginLanguageService> entry : this.languageServices.entrySet()) {
            entry.getValue().initialize(this.pluginEnvironment);
        }
    }

    public void discoverLanguageServices() {
        final ServiceLoader<PluginLanguageService> serviceLoader = ServiceLoader.load(PluginLanguageService.class, VanillaPluginLocator.class
                .getClassLoader());

        for (final Iterator<PluginLanguageService>iter = (Iterator<PluginLanguageService>) (Object) serviceLoader.iterator(); iter.hasNext(); ) {
            final PluginLanguageService next;

            try {
                next = iter.next();
            } catch (final ServiceConfigurationError e) {
                this.pluginEnvironment.getLogger().error("Error encountered initializing plugin loader!", e);
                continue;
            }

            this.languageServices.put(next.getName(), next);
        }
    }

    public void discoverPluginResources() {
        for (final Map.Entry<String, PluginLanguageService> languageEntry : this.languageServices.entrySet()) {
            final PluginLanguageService languageService = languageEntry.getValue();
            final List<Path> pluginFiles = languageService.discoverPluginResources(this.pluginEnvironment);
            if (pluginFiles.size() > 0) {
                this.pluginFiles.put(languageEntry.getKey(), pluginFiles);
            }
        }
    }

    public void createPluginCandidates() {
        for (final Map.Entry<String, PluginLanguageService> languageEntry : this.languageServices.entrySet()) {
            final PluginLanguageService languageService = languageEntry.getValue();
            final List<PluginCandidate> pluginCandidates = languageService.createPluginCandidates(this.pluginEnvironment);
            if (pluginCandidates.size() > 0) {
                this.pluginCandidates.put(languageService, pluginCandidates);
            }
        }
    }
}
