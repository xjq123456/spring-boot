/*
 * Copyright 2012-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.boot.admin;

/**
 * An MBean contract to control and monitor a running {@code SpringApplication} over JMX.
 * Intended for internal use only.
 *
 * @author Stephane Nicoll
 * @since 1.3.0
 */
public interface SpringApplicationAdminMXBean {

	/**
	 * Specify if the application has fully started and is now ready.
	 * @return {@code true} if the application is ready
	 * @see org.springframework.boot.context.event.ApplicationReadyEvent
	 */
	/**
	 * 指定应用程序是否已经完全启动并且现在已准备好。
	 * @return {@code true} 如果应用程序已准备好
	 * @see org.springframework.boot.context.event.ApplicationReadyEvent
	 */
	boolean isReady();

	/**
	 * Specify if the application runs in an embedded web container. Return {@code false}
	 * on a web application that hasn't fully started yet, so it is preferable to wait for
	 * the application to be {@link #isReady() ready}.
	 * @return {@code true} if the application runs in an embedded web container
	 * @see #isReady()
	 */
	/**
	 * 指定应用程序是否在嵌入式 Web 容器中运行。对于尚未完全启动的 Web 应用程序，返回 {@code false}，因此最好等到应用程序 {@link #isReady() 准备好} 后再进行操作。
	 * @return {@code true} 如果应用程序在嵌入式 Web 容器中运行
	 * @see #isReady()
	 */
	boolean isEmbeddedWebApplication();

	/**
	 * Return the value of the specified key from the application
	 * {@link org.springframework.core.env.Environment Environment}.
	 * @param key the property key
	 * @return the property value or {@code null} if it does not exist
	 */
	/**
	 * 从应用程序的 {@link org.springframework.core.env.Environment Environment} 中返回指定键的值。
	 * @param key 属性键
	 * @return 属性值，如果不存在则返回 {@code null}
	 */
	String getProperty(String key);

	/**
	 * Shutdown the application.
	 * @see org.springframework.context.ConfigurableApplicationContext#close()
	 */
	void shutdown();

}
