/*
 * This file is part of gerdagradle-convention, licensed under the MIT License (MIT).
 *
 * Copyright (c) GerdaPowered <https://www.fotontv.fun>
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
package fun.fotontv.gradle.convention;

import org.gradle.api.artifacts.dsl.RepositoryHandler;

public final class ConventionConstant {

    private void ConventionConstants() {
    }

    public static final class ProjectProperties {
        public static final String GERDA_SIGNING_KEY = "gerdaSigningKey";
        public static final String GERDA_SIGNING_PASSWORD = "gerdaSigningPassword";
        public static final String GERDA_SNAPSHOT_REPO = "gerdaSnapshotRepo";
        public static final String GERDA_RELEASE_REPO = "gerdaReleaseRepo";
        public static final String GERDA_KEY_STORE = "gerdaKeyStore";
        public static final String GERDA_KEY_STORE_ALIAS = "gerdaKeyStoreAlias";
        public static final String GERDA_KEY_STORE_PASSWORD = "gerdaKeyStorePassword";

        private ProjectProperties() {
        }
    }

    public static final class Locations {
        public static final String LICENSE_HEADER = "HEADER.txt";

        private Locations() {
        }
    }

    public static final class Configurations {
        public static final String PARENT_PROJECT = "parentProject";
        public static final String IMPLEMENTATION_LIBS = "implementationLibs";

        private Configurations() {
        }
    }

    public static void gerdaRepo(final RepositoryHandler repos) {
        repos.maven(repo -> {
            repo.setUrl("https://repo.fotontv.fun/repository/maven-public/");
            repo.setName("gerda");
        });
    }
}
