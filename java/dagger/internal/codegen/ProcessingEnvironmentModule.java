/*
 * Copyright (C) 2018 The Dagger Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dagger.internal.codegen;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.googlejavaformat.java.filer.FormattingFiler;
import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import dagger.Reusable;
import dagger.internal.codegen.ProcessingEnvironmentModule.ElementsModule;
import java.util.Map;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

/** Bindings that depend on the {@link ProcessingEnvironment}. */
@Module(includes = ElementsModule.class)
final class ProcessingEnvironmentModule {

  private final ProcessingEnvironment processingEnvironment;

  ProcessingEnvironmentModule(ProcessingEnvironment processingEnvironment) {
    this.processingEnvironment = checkNotNull(processingEnvironment);
  }

  @Provides
  @ProcessingOptions
  Map<String, String> processingOptions() {
    return processingEnvironment.getOptions();
  }

  @Provides
  Messager messager() {
    return processingEnvironment.getMessager();
  }

  @Provides
  @Reusable // to avoid noting about disabling the formatter more than once
  Filer filer(CompilerOptions compilerOptions, Messager messager) {
    if (compilerOptions.headerCompilation()) {
      return processingEnvironment.getFiler();
    } else {
      return new FormattingFiler(processingEnvironment.getFiler());
    }
  }

  @Provides
  Types types() {
    return processingEnvironment.getTypeUtils();
  }

  @Provides
  SourceVersion sourceVersion() {
    return processingEnvironment.getSourceVersion();
  }

  @Provides
  DaggerElements daggerElements() {
    return new DaggerElements(processingEnvironment);
  }

  @Provides
  @Reusable // to avoid parsing options more than once
  CompilerOptions compilerOptions(DaggerElements elements) {
    return CompilerOptions.create(processingEnvironment, elements);
  }

  @Module
  interface ElementsModule {
    @Binds
    Elements elements(DaggerElements daggerElements);
  }
}
