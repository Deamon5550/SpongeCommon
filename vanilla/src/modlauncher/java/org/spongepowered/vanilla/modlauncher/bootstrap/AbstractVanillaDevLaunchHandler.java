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
package org.spongepowered.vanilla.modlauncher.bootstrap;

import cpw.mods.gross.Java9ClassLoaderUtil;
import cpw.mods.modlauncher.api.ILaunchHandlerService;
import cpw.mods.modlauncher.api.ITransformingClassLoaderBuilder;
import org.spongepowered.plugin.PluginCandidate;
import org.spongepowered.plugin.PluginLanguageService;
import org.spongepowered.plugin.jvm.JarPluginCandidate;
import org.spongepowered.vanilla.modlauncher.Main;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * The Sponge {@link ILaunchHandlerService launch handler} for development environments.
 */
public abstract class AbstractVanillaDevLaunchHandler extends AbstractVanillaLaunchHandler {

    @Override
    public void configureTransformationClassLoader(final ITransformingClassLoaderBuilder builder) {
        for (final URL url : Java9ClassLoaderUtil.getSystemClassPathURLs()) {
            if (url.toString().contains("mixin") && url.toString().endsWith(".jar")) {
                continue;
            }

            try {
                builder.addTransformationPath(Paths.get(url.toURI()));
            } catch (final URISyntaxException ex) {
                log.error("Failed to add Mixin transformation path", ex);
            }
        }

        builder.setClassBytesLocator(s -> {
                for (final Map.Entry<PluginLanguageService, List<PluginCandidate>> serviceCandidates :
                    Main.getPluginLocator().getCandidates().entrySet()) {
                    for (final PluginCandidate candidate : serviceCandidates.getValue()) {
                        if (!(candidate instanceof JarPluginCandidate)) {
                            continue;
                        }

                        final Path path = ((JarPluginCandidate) candidate).getFileSystem().getPath(s);
                        if (Files.exists(path)) {
                            try {
                                return Optional.of(path.toUri().toURL());
                            } catch (final MalformedURLException e) {
                                e.printStackTrace();
                                return Optional.empty();
                            }
                        }
                    }
                }
                return Optional.empty();
            }
        );
    }
}
