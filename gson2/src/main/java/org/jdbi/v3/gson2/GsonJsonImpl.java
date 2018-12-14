/*
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
package org.jdbi.v3.gson2;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;
import org.jdbi.v3.core.result.UnableToProduceResultException;
import org.jdbi.v3.core.statement.StatementContext;
import org.jdbi.v3.json.internal.JsonMapperImpl;

class GsonJsonImpl implements JsonMapperImpl {
    @Override
    public String toJson(Type type, Object value, StatementContext ctx) {
        return getMapper(ctx).toJson(value);
    }

    @Override
    public <T> T fromJson(Type type, String json, StatementContext ctx) {
        try {
            return getMapper(ctx).fromJson(json, type);
        } catch (JsonParseException e) {
            throw new UnableToProduceResultException(e, ctx);
        }
    }

    private Gson getMapper(StatementContext ctx) {
        return ctx.getConfig(Gson2Config.class).getGson();
    }
}
