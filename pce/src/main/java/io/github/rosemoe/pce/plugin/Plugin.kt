/*******************************************************************************
 *   Copyright 2020 Rosemoe
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 ******************************************************************************/

/*******************************************************************************
 *   Copyright 2020 Rosemoe
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 ******************************************************************************/

package io.github.rosemoe.pce.plugin

import io.github.rosemoe.pce.widget.PceEditor
import kotlin.coroutines.CoroutineContext

interface Plugin {

    fun getPluginDescription(): PluginDescription

    /**
     * Create a [PluginSubInstance] to provide services for the given editor
     * You are unexpected to save the editor instance and coroutine context inside this plugin instance
     * All actions to the editor should be done in sub instance instead of this one
     */
    fun createSubInstance(editor: PceEditor, parentCoroutineContext: CoroutineContext)

}