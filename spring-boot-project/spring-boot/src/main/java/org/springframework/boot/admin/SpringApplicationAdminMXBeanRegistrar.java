/*
 * Copyright 2012-2024 the original author or authors.
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

import java.lang.management.ManagementFactory;

import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.event.GenericApplicationListener;
import org.springframework.core.Ordered;
import org.springframework.core.ResolvableType;
import org.springframework.core.env.Environment;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.util.Assert;

/**
 * Register a {@link SpringApplicationAdminMXBean} implementation to the platform
 * {@link MBeanServer}.
 *
 * @author Stephane Nicoll
 * @author Andy Wilkinson
 * @since 1.3.0
 */
public class SpringApplicationAdminMXBeanRegistrar implements ApplicationContextAware, GenericApplicationListener,
		EnvironmentAware, InitializingBean, DisposableBean {

	private static final Log logger = LogFactory.getLog(SpringApplicationAdmin.class);

	private ConfigurableApplicationContext applicationContext;

	private Environment environment = new StandardEnvironment();

	private final ObjectName objectName;

	private boolean ready = false;

	private boolean embeddedWebApplication = false;

	public SpringApplicationAdminMXBeanRegistrar(String name) throws MalformedObjectNameException {
		this.objectName = new ObjectName(name);
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		Assert.state(applicationContext instanceof ConfigurableApplicationContext,
				"ApplicationContext does not implement ConfigurableApplicationContext");
		this.applicationContext = (ConfigurableApplicationContext) applicationContext;
	}

	@Override
	public void setEnvironment(Environment environment) {
		this.environment = environment;
	}

	@Override
	public boolean supportsEventType(ResolvableType eventType) {
		//从 eventType 中获取它对应的原始类（raw class），并将其赋值给 type 变量。
		Class<?> type = eventType.getRawClass();
		if (type == null) {
			return false;
		}
		// 判断是否支持 ApplicationReadyEvent 类型的事件或 WebServerInitializedEvent 类型的事件。
		return ApplicationReadyEvent.class.isAssignableFrom(type)
				|| WebServerInitializedEvent.class.isAssignableFrom(type);
		//ApplicationReadyEvent
		//ApplicationReadyEvent 是 Spring Boot 在应用完全启动并且所有 CommandLineRunners 和 ApplicationRunners 已经被调用之后发布的事件。这意味着应用程序已经完全准备好，可以接收请求了。在这个事件触发之后，你可以确保所有的初始化工作已经完成。
		//
		//WebServerInitializedEvent
		//WebServerInitializedEvent 是在 Spring Boot 应用启动 Web 服务器并且服务器已经准备好处理请求时发布的事件。这个事件通常用于在服务器启动后获取服务器的配置信息（比如端口号）。
	}

	@Override
	public boolean supportsSourceType(Class<?> sourceType) {
		return true;
	}

	/**
	 * 使用 on 开头命名方法在事件驱动编程中是一种常见的命名约定，表示该方法是对某个事件的响应或处理方法
	 * @param event
	 */
	@Override
	public void onApplicationEvent(ApplicationEvent event) {
		// 检查传入的 event 是否是 ApplicationReadyEvent 的实例。
		if (event instanceof ApplicationReadyEvent readyEvent) {
			// 如果是 ApplicationReadyEvent，则调用 onApplicationReadyEvent 方法处理该事件。
			onApplicationReadyEvent(readyEvent);
		}
		// 检查传入的 event 是否是 WebServerInitializedEvent 的实例。
		if (event instanceof WebServerInitializedEvent initializedEvent) {
			// 如果是 WebServerInitializedEvent，则调用 onWebServerInitializedEvent 方法处理该事件。
			onWebServerInitializedEvent(initializedEvent);
		}
	}

	@Override
	public int getOrder() {
		// 返回一个整型值，表示该组件的优先级顺序。
		// Ordered.HIGHEST_PRECEDENCE 是一个常量，表示最高优先级，数值通常为 Integer.MIN_VALUE。
		return Ordered.HIGHEST_PRECEDENCE;
	}

	void onApplicationReadyEvent(ApplicationReadyEvent event) {
		// 检查当前的 applicationContext 是否与事件中的 applicationContext 相等
		if (this.applicationContext.equals(event.getApplicationContext())) {
			// 如果相等，将 ready 标志设置为 true，表示应用程序已经准备就绪
			this.ready = true;
		}
	}

	void onWebServerInitializedEvent(WebServerInitializedEvent event) {
		// 检查当前的 applicationContext 是否与事件中的 applicationContext 相等
		if (this.applicationContext.equals(event.getApplicationContext())) {
// 如果相等，将 embeddedWebApplication 标志设置为 true，表示应用程序使用了嵌入式 Web 服务器
			this.embeddedWebApplication = true;
		}
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		// 获取 Java 虚拟机的 MBeanServer 实例，这是一个用于管理 MBeans 的服务器
		MBeanServer server = ManagementFactory.getPlatformMBeanServer();
		// 注册一个新的 MBean（SpringApplicationAdmin）到 MBeanServer，并使用指定的 objectName
		server.registerMBean(new SpringApplicationAdmin(), this.objectName);
		// 如果日志级别为 Debug，记录 MBean 注册的调试信息
		if (logger.isDebugEnabled()) {
			logger.debug("Application Admin MBean registered with name '" + this.objectName + "'");
		}
	}

	@Override
	public void destroy() throws Exception {
		// 从 MBeanServer 中注销指定的 MBean
		ManagementFactory.getPlatformMBeanServer().unregisterMBean(this.objectName);
	}

	private final class SpringApplicationAdmin implements SpringApplicationAdminMXBean {

		@Override
		public boolean isReady() {
			return SpringApplicationAdminMXBeanRegistrar.this.ready;
		}

		@Override
		public boolean isEmbeddedWebApplication() {
			return SpringApplicationAdminMXBeanRegistrar.this.embeddedWebApplication;
		}

		@Override
		public String getProperty(String key) {
			return SpringApplicationAdminMXBeanRegistrar.this.environment.getProperty(key);
		}

		@Override
		public void shutdown() {
			logger.info("Application shutdown requested.");
			SpringApplicationAdminMXBeanRegistrar.this.applicationContext.close();
		}

	}

}
