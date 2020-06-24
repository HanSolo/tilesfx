/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2016-2020 Gerrit Grunwald.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.hansolo.tilesfx;

/**
 * User: divine threepwood
 * Date: 09.06.19
 * Time: 21:00
 */
public class DemoLauncher {

    /**
     *
     * This launcher starts the tilesfx demo pane.
     *
     * Note: This launcher is required to make sure all openjfx java modules are linked via the classpath.
     * Please checkout issue https://github.com/HanSolo/tilesfx/issues/73 for more details.
     *
     * @param args application arguments
     */
    public static void main(String[] args) {
        Demo.main(args);
    }
}
