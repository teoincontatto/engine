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

package com.torodb.core.transaction.metainf.impl;

import com.google.inject.PrivateModule;
import com.google.inject.Singleton;
import com.torodb.core.transaction.metainf.MetainfoRepository;
import com.torodb.core.transaction.metainf.impl.metainfo.mvcc.MvccMetainfoRepository;

/**
 * A module that binds {@link MetainfoRepository} and other metainf related stuff.
 */
public class MetainfModule extends PrivateModule {

  @Override
  protected void configure() {
    bind(MetainfoRepository.class)
        .to(MvccMetainfoRepository.class)
        .in(Singleton.class);
    expose(MetainfoRepository.class);
  }

}
