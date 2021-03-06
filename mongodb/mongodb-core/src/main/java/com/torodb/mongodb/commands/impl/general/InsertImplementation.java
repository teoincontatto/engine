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

package com.torodb.mongodb.commands.impl.general;

import com.google.common.collect.ImmutableList;
import com.torodb.core.exceptions.user.UserException;
import com.torodb.core.language.AttributeReference;
import com.torodb.core.language.AttributeReference.Key;
import com.torodb.core.language.AttributeReference.ObjectKey;
import com.torodb.core.transaction.metainf.FieldIndexOrdering;
import com.torodb.kvdocument.conversion.mongowp.FromBsonValueTranslator;
import com.torodb.kvdocument.values.KvDocument;
import com.torodb.mongodb.commands.impl.WriteTorodbCommandImpl;
import com.torodb.mongodb.commands.signatures.general.InsertCommand.InsertArgument;
import com.torodb.mongodb.commands.signatures.general.InsertCommand.InsertResult;
import com.torodb.mongodb.core.MongodMetrics;
import com.torodb.mongodb.core.WriteMongodTransaction;
import com.torodb.mongodb.utils.DefaultIdUtils;
import com.torodb.mongowp.ErrorCode;
import com.torodb.mongowp.Status;
import com.torodb.mongowp.commands.Command;
import com.torodb.mongowp.commands.Request;
import com.torodb.torod.IndexFieldInfo;

import java.util.Arrays;
import java.util.stream.Stream;

import javax.inject.Singleton;

/**
 *
 */
@Singleton
public class InsertImplementation implements WriteTorodbCommandImpl<InsertArgument, InsertResult> {

  @Override
  public Status<InsertResult> apply(Request req,
      Command<? super InsertArgument, ? super InsertResult> command, InsertArgument arg,
      WriteMongodTransaction context) {
    MongodMetrics mongodMetrics = context.getConnection().getServer().getMetrics();

    mongodMetrics.getInserts().mark(arg.getDocuments().size());
    Stream<KvDocument> docsToInsert = arg.getDocuments().stream().map(FromBsonValueTranslator
        .getInstance())
        .map((v) -> (KvDocument) v);

    try {
      if (!context.getTorodTransaction().existsCollection(req.getDatabase(), arg.getCollection())) {
        context.getTorodTransaction().createIndex(req.getDatabase(), arg.getCollection(),
            DefaultIdUtils.ID_INDEX,
            ImmutableList.<IndexFieldInfo>of(new IndexFieldInfo(new AttributeReference(Arrays
                .asList(new Key[]{new ObjectKey(DefaultIdUtils.ID_KEY)})), FieldIndexOrdering.ASC
                .isAscending())), true);
      }

      context.getTorodTransaction().insert(req.getDatabase(), arg.getCollection(), docsToInsert);
    } catch (UserException ex) {
      //TODO: Improve error reporting
      return Status.from(ErrorCode.COMMAND_FAILED, ex.getLocalizedMessage());
    }

    return Status.ok(new InsertResult(arg.getDocuments().size()));
  }
}
