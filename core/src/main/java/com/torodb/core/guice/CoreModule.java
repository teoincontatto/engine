/*
 * ToroDB
 * Copyright © 2014 8Kdata Technology (www.8kdata.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.torodb.core.guice;

import com.google.inject.Exposed;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.torodb.core.Shutdowner;
import com.torodb.core.TableRefFactory;
import com.torodb.core.impl.TableRefFactoryImpl;
import com.torodb.core.logging.DefaultLoggerFactory;
import com.torodb.core.logging.LoggerFactory;
import com.torodb.core.retrier.Retrier;
import com.torodb.core.retrier.SmartRetrier;
import com.torodb.core.transaction.InternalTransactionManager;

import java.util.concurrent.ThreadFactory;

/**
 * A module that binds core classes (like {@link Retrier} or {@link InternalTransactionManager} to
 * their default values.
 */
public class CoreModule extends EssentialToroModule {

  private final LoggerFactory lifecycleLoggerFactory;

  public CoreModule(LoggerFactory lifecycleLoggerFactory) {
    this.lifecycleLoggerFactory = lifecycleLoggerFactory;
  }
 
  @Override
  protected void configure() {
    expose(TableRefFactory.class);
    expose(Retrier.class);
    expose(InternalTransactionManager.class);
    exposeEssential(LoggerFactory.class);

    bind(TableRefFactory.class)
        .to(TableRefFactoryImpl.class)
        .asEagerSingleton();

    int maxCriticalAttempts = 100;
    int maxInfrequentAttempts = 5;
    int maxFrequentAttempts = 100;
    int maxDefaultAttempts = 10;

    bind(Retrier.class)
        .toInstance(new SmartRetrier(
            attempts -> attempts >= maxCriticalAttempts,
            attempts -> attempts >= maxInfrequentAttempts,
            attempts -> attempts >= maxFrequentAttempts,
            attempts -> attempts >= maxDefaultAttempts,
            CoreModule::millisToWait
        ));

    bind(InternalTransactionManager.class)
        .in(Singleton.class);

    bindEssential(LoggerFactory.class)
        .toInstance(DefaultLoggerFactory.getInstance());
  }

  private static int millisToWait(int attempts, int millis) {
    if (millis >= 2000) {
      return 2000;
    }
    int factor = (int) Math.round(millis * (1.5 + Math.random()));
    if (factor < 2) {
      assert millis <= 1;
      factor = 2;
    }
    return Math.min(2000, millis * factor);
  }

  @Provides
  @Exposed
  @Singleton
  protected Shutdowner createShutdowner(ThreadFactory threadFactory) {
    Shutdowner s = new Shutdowner(threadFactory, lifecycleLoggerFactory);

    return s;
  }

}
