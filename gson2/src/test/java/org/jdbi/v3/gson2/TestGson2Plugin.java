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

import java.util.List;
import org.assertj.core.groups.Tuple;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.json.Json;
import org.jdbi.v3.postgres.PostgresDbRule;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.jdbi.v3.testing.JdbiRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TestGson2Plugin {
    @Rule
    public JdbiRule db = PostgresDbRule.rule().withPlugin(new Gson2Plugin());

    private Handle h;

    @Before
    public void setUp() {
        h = db.getHandle();
    }

    public static class Whozit {
        private final String food;
        private final int bitcoins;

        public Whozit(String food, int bitcoins) {
            this.food = food;
            this.bitcoins = bitcoins;
        }

        public String getFood() {
            return food;
        }

        public int getBitcoins() {
            return bitcoins;
        }
    }

    interface JsonDao {
        @SqlUpdate("insert into whozits (whozit) values(?)")
        int insert(@Json Whozit value);

        @SqlQuery("select whozit from whozits")
        @Json
        List<Whozit> select();
    }

    @Test
    public void testJsonMapping() {
        h.execute("create table whozits (id serial primary key, whozit json not null)");
        final JsonDao dao = h.attach(JsonDao.class);
        dao.insert(new Whozit("yams", 42));
        dao.insert(new Whozit("apples", 24));

        assertThat(dao.select())
            .extracting("food", "bitcoins")
            .containsExactlyInAnyOrder(
                    new Tuple("yams", 42),
                    new Tuple("apples", 24));
    }

    @Test
    public void testJsonNested() {
        h.execute("create table beany (id serial primary key, nested1 json, nested2 json)");
        assertThat(h.createUpdate("insert into beany(id, nested1, nested2) values (:id, :nested1, :nested2)")
            .bindBean(new Beany(42, 64, "quux"))
            .execute()).isEqualTo(1);

        Beany beany = h.createQuery("select * from beany")
            .mapToBean(Beany.class)
            .findOnly();

        assertThat(beany.getId()).isEqualTo(42);
        assertThat(beany.getNested1().getA()).isEqualTo(64);
        assertThat(beany.getNested2().getB()).isEqualTo("quux");
    }

    public static class Beany {
        private int id;
        private N1 nested1;
        private N2 nested2;

        public Beany() {}
        Beany(int id, int a, String b) {
            this.id = id;
            this.nested1 = new N1(a);
            this.nested2 = new N2(b);
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        @Json
        public N1 getNested1() {
            return nested1;
        }

        public void setNested1(N1 nested1) {
            this.nested1 = nested1;
        }

        @Json
        public N2 getNested2() {
            return nested2;
        }

        public void setNested2(N2 nested2) {
            this.nested2 = nested2;
        }
    }

    public static class N1 {
        private final int a;

        public N1(int a) {
            this.a = a;
        }

        public int getA() {
            return a;
        }
    }

    public static class N2 {
        private final String b;

        public N2(String b) {
            this.b = b;
        }

        public String getB() {
            return b;
        }
    }
}
