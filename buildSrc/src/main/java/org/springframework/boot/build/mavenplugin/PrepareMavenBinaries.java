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

package org.springframework.boot.build.mavenplugin;

import org.gradle.api.DefaultTask;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.SetProperty;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

/**
 * {@link Task} to make Maven binaries available for integration testing.
 *
 * @author Andy Wilkinson
 */
public abstract class PrepareMavenBinaries extends DefaultTask {

	@OutputDirectory
	public abstract DirectoryProperty getOutputDir();

	@Input
	public abstract SetProperty<String> getVersions();

	@TaskAction
	public void prepareBinaries() {
		getProject().sync((sync) -> {
			sync.into(getOutputDir());
			for (String version : getVersions().get()) {
				Configuration configuration = getProject().getConfigurations()
					.detachedConfiguration(getProject().getDependencies()
						.create("org.apache.maven:apache-maven:" + version + ":bin@zip"));
				sync.from(getProject().zipTree(configuration.getSingleFile()));
			}
		});

	}

}
