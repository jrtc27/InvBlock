/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jrtc27.invblock.listeners;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Iterator;
import java.util.Set;

import junit.framework.Assert;

import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.junit.Test;
import org.reflections.Reflections;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

/**
 * Ensure we aren't missing any {@link org.bukkit.event.EventHandler} annotations, and that all listeners implement {@link org.bukkit.event.Listener}
 *
 * @author Feildmaster, jrtc27
 */
public class TestListeners {
	private Class[] knownListeners = new Class[] { PlayerListener.class };

	@Test
	public void testForUnknown() {
		final Reflections reflections = new Reflections(new ConfigurationBuilder().setUrls(ClasspathHelper.forPackage("com.jrtc27.invblock.listeners")));

		final Set<Class<? extends Listener>> foundListeners = reflections.getSubTypesOf(Listener.class);
		for (Class clazz : this.knownListeners) {
			foundListeners.remove(clazz);
		}

		final Iterator<Class<? extends Listener>> unknownListeners = foundListeners.iterator();

		if (unknownListeners.hasNext()) {
			boolean plural = false;
			final StringBuilder builder = new StringBuilder(unknownListeners.next().getCanonicalName());

			while (unknownListeners.hasNext()) {
				plural = true;
				final Class unknownListener = unknownListeners.next();
				if (unknownListeners.hasNext()) {
					builder.append(", ");
				} else {
					builder.append(" and ");
				}
				builder.append(unknownListener.getCanonicalName());
			}

			Assert.fail("The listener" + (plural ? "s " : " ") + builder.toString() + " ha" + (plural ? "ve" : "s") + " not been added to the knownListeners array!");
		}
	}

	@Test
	public void testAllKnown() {
		for (final Class clazz : this.knownListeners) {
			testListener(clazz);
		}
	}

	/**
	 * Test a class which should implement {@link org.bukkit.event.Listener} and contain {@link org.bukkit.event.EventHandler} annotations
	 * <p/>
	 * <em>Author: Feildmaster</em>
	 */
	private void testListener(final Class clazz) {
		// Assert the class is even a listener
		Assert.assertTrue("Class: " + clazz.getSimpleName() + " does not implement Listener!", Listener.class.isAssignableFrom(clazz));

		for (Method method : clazz.getDeclaredMethods()) {
			// We only care about public functions.
			if (!Modifier.isPublic(method.getModifiers())) continue;
			// Don't mess with non-void
			if (!Void.TYPE.equals(method.getReturnType())) continue;
			// Only look for functions with 1 parameter
			if (method.getParameterTypes().length != 1) continue;

			// This is an event function...
			if (Event.class.isAssignableFrom(method.getParameterTypes()[0])) {
				// Make sure @EventHandler is present!
				Assert.assertTrue(method.getName() + " is missing @EventHandler!", method.isAnnotationPresent(EventHandler.class));
			}
		}
	}
}
