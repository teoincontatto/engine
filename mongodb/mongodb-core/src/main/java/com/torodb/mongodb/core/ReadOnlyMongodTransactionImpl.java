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

package com.torodb.mongodb.core;

import com.torodb.mongowp.Status;
import com.torodb.mongowp.commands.Command;
import com.torodb.mongowp.commands.CommandExecutor;
import com.torodb.mongowp.commands.Request;
import com.torodb.torod.ReadOnlyTorodTransaction;

class ReadOnlyMongodTransactionImpl extends MongodTransactionImpl implements
    ReadOnlyMongodTransaction {

  private final ReadOnlyTorodTransaction torodTransaction;
  private final CommandExecutor<? super ReadOnlyMongodTransactionImpl> commandsExecutor;

  public ReadOnlyMongodTransactionImpl(MongodConnection connection) {
    super(connection);
    this.torodTransaction = connection.getTorodConnection().openReadOnlyTransaction();
    this.commandsExecutor = connection.getServer().getCommandsExecutorClassifier()
        .getReadOnlyCommandsExecutor();
  }

  @Override
  public ReadOnlyTorodTransaction getTorodTransaction() {
    return torodTransaction;
  }

  @Override
  protected <A, R> Status<R> executeProtected(Request req,
      Command<? super A, ? super R> command, A arg) {
    return commandsExecutor.execute(req, command, arg, this);
  }

}
