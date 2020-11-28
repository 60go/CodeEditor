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

import io.github.rosemoe.pce.event.Event
import io.github.rosemoe.pce.widget.PceEditor
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.newCoroutineContext
import kotlin.coroutines.CoroutineContext

/**
 * Plugin instance for editor instances.
 * [PluginSubInstance] is created by [Plugin] to provide service for a specified editor instance.
 * The service target can not be changed.
 * @see [Plugin]
 */
abstract class PluginSubInstance constructor(protected val editor: PceEditor, parentCoroutineContext: CoroutineContext) : CoroutineScope {

    val _coroutineContext: CoroutineContext

    override val coroutineContext: CoroutineContext
        get() = _coroutineContext

    init {
        _coroutineContext = CoroutineExceptionHandler { _, throwable ->
            //TODO
        }.plus(parentCoroutineContext)
    }

    companion object {
        /**
         * Return value for [EventConsumer.onEvent]
         * Indicate that this plugin is unexpected to receive more events from this listener
         */
        const val EVENT_UNSUBSCRIBE = 2

        /**
         * Return value for [EventConsumer.onEvent]
         * Indicate that this plugin has consumed the event and do not dispatch it to other plugins
         */
        const val EVENT_INTERCEPT = 1

        /**
         * Return value for [EventConsumer.onEvent]
         * Continue to dispatch the event to other plugins
         */
        const val EVENT_CONTINUE = 0
    }

    /**
     * Map to save consumers
     * @see PluginSubInstance.subscribe
     */
    private val consumerMap: MutableMap<Class<*>, MutableList<EventConsumer<*>>> = HashMap()

    /**
     * Invoke all [EventConsumer] with correct kind in this plugin.
     * Return result after handling the event
     *
     * @return Either [EVENT_INTERCEPT] or [EVENT_CONTINUE]
     */
    internal fun invokeConsumers(event: Event): Int {
        val consumers: MutableList<EventConsumer<*>>
        synchronized(consumerMap) {
            consumers = consumerMap[event.javaClass] ?: return EVENT_CONTINUE
        }
        synchronized(consumers) {
            val itr = consumers.iterator()
            while (itr.hasNext()) {
                val consumer = itr.next()
                val returnCode = (consumer as EventConsumer<Event>).onEvent(event)
                if (returnCode == EVENT_INTERCEPT) {
                    return EVENT_INTERCEPT
                } else if (returnCode == EVENT_UNSUBSCRIBE) {
                    itr.remove()
                }
            }
            return EVENT_CONTINUE
        }
    }

    /**
     * Subscribe a kind of [Event]. For convenience, one kind of [Event] can be subscribed by
     * multiple consumers.
     */
    inline fun <reified E : Event> subscribe(listener: EventConsumer<E>) = subscribe(E::class.java, listener)

    /**
     * Subscribe a kind of [Event]. For convenience, one kind of [Event] can be subscribed by
     * multiple consumers.
     */
    fun <E : Event> subscribe(type: Class<E>, listener: EventConsumer<E>) {
        var consumers: MutableList<EventConsumer<*>>?
        synchronized(consumerMap) {
            consumers = consumerMap[type]
            if (consumers == null) {
                consumerMap[type] = ArrayList()
                consumers = consumerMap[type]
            }
        }
        synchronized(consumers!!) {
            consumers!!.add(listener)
        }
    }

    internal fun enable() {

    }

    internal fun disable() {

    }

    abstract fun onEnable()

    abstract fun onDisable()

    /**
     * Consumer for event
     * @see EventConsumer.onEvent
     */
    interface EventConsumer<T : Event> {

        /**
         * Notify the listener that an event has happened recently
         *
         * The listener can handle the event here and decide whether to dispatch the event further
         * to other plugins.
         * **Other consumers for this kind of event in this plugin will not be called after you
         * return [EVENT_INTERCEPT]**
         *
         * As plugin priority is different, a plugin may be unable to intercept others or receive
         * some events
         *
         * @param event The event
         * @return A integer in [EVENT_CONTINUE], [EVENT_INTERCEPT] or [EVENT_UNSUBSCRIBE]
         * If it is not presented in those values, it is equal to [EVENT_CONTINUE]
         * @see EVENT_CONTINUE
         * @see EVENT_INTERCEPT
         * @see EVENT_UNSUBSCRIBE
         */
        fun onEvent(event: T): Int

    }

}
